package org.citydb.ade.energy.importer;

import org.citydb.ade.energy.schema.ADETable;
import org.citydb.ade.importer.ADEImporter;
import org.citydb.ade.importer.CityGMLImportHelper;
import org.citydb.ade.importer.ForeignKeys;
import org.citydb.citygml.importer.CityGMLImportException;
import org.citydb.citygml.importer.util.AttributeValueJoiner;
import org.citydb.database.schema.mapping.AbstractObjectType;
import org.citygml4j.ade.energy.model.supportingClasses.AbstractTimeSeries;
import org.citygml4j.ade.energy.model.supportingClasses.RegularTimeSeries;
import org.citygml4j.ade.energy.model.supportingClasses.RegularTimeSeriesFile;
import org.citygml4j.ade.energy.model.supportingClasses.TimeIntervalLength;
import org.citygml4j.ade.energy.model.supportingClasses.TimePeriod;
import org.citygml4j.ade.energy.model.supportingClasses.TimePeriodProperty;
import org.citygml4j.ade.energy.model.supportingClasses.TimeValuesProperties;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class TimeSeriesImporter implements ADEImporter {
    private final Connection connection;
    private final CityGMLImportHelper helper;

    private AttributeValueJoiner valueJoiner;
    private PreparedStatement psTimeSeries;
    private PreparedStatement psRegularTimeSeries;
    private PreparedStatement psRegularTimeSeriesFile;
    private int batchCounter;

    public TimeSeriesImporter(Connection connection, CityGMLImportHelper helper, ImportManager manager) throws CityGMLImportException, SQLException {
        this.connection = connection;
        this.helper = helper;

        psTimeSeries = connection.prepareStatement("insert into " +
                helper.getTableNameWithSchema(manager.getSchemaMapper().getTableName(ADETable.TIMESERIES)) + " " +
                "(id, objectclass_id, timevaluesprop_acquisitionme, timevaluesprop_interpolation, timevaluesprop_qualitydescri, " +
                "timevaluespropertiest_source, timevaluesprop_thematicdescr) " +
                "values (?, ?, ?, ?, ?, ?, ?)");

        psRegularTimeSeries = connection.prepareStatement("insert into " +
                helper.getTableNameWithSchema(manager.getSchemaMapper().getTableName(ADETable.REGULARTIMESERIES)) + " " +
                "(id, values_, values_uom, timeperiodprop_beginposition, timeperiodproper_endposition, " +
                "timeinterval, timeinterval_unit, timeinterval_radix, timeinterval_factor) " +
                "values (?, ?, ?, ?, ?, ?, ?, ?, ?)");

        psRegularTimeSeriesFile = connection.prepareStatement("insert into " +
                helper.getTableNameWithSchema(manager.getSchemaMapper().getTableName(ADETable.REGULARTIMESERIESFILE)) + " " +
                "id, uom, file_, numberofheaderlines, fieldseparator, recordseparator, decimalsymbol, valuecolumnnumber, " +
                "timeperiodprop_beginposition, timeperiodproper_endposition, " +
                "timeinterval, timeinterval_unit, timeinterval_radix, timeinterval_factor) " +
                "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

        valueJoiner = helper.getAttributeValueJoiner();
    }

    public void doImport(AbstractTimeSeries timeSeries, long objectId, AbstractObjectType<?> objectType, ForeignKeys foreignKeys) throws CityGMLImportException, SQLException {
        psTimeSeries.setLong(1, objectId);
        psTimeSeries.setLong(2, objectType.getObjectClassId());

        String acquisitionMethod = null;
        String interpolationType = null;
        String qualityDescription = null;
        String source = null;
        String thematicDescription = null;

        if (timeSeries.isSetVariableProperties() && timeSeries.getVariableProperties().isSetTimeValuesProperties()) {
            TimeValuesProperties timeValuesProperties = timeSeries.getVariableProperties().getTimeValuesProperties();

            if (timeValuesProperties.isSetAcquisitionMethod())
                acquisitionMethod = timeValuesProperties.getAcquisitionMethod().value();
            if (timeValuesProperties.isSetInterpolationType())
                interpolationType = timeValuesProperties.getInterpolationType().value();
            if (timeValuesProperties.isSetQualityDescription())
                qualityDescription = timeValuesProperties.getQualityDescription();
            if (timeValuesProperties.isSetSource())
                source = timeValuesProperties.getSource();
            if (timeValuesProperties.isSetThematicDescription())
                thematicDescription = timeValuesProperties.getThematicDescription();
        }

        psTimeSeries.setString(3, acquisitionMethod);
        psTimeSeries.setString(4, interpolationType);
        psTimeSeries.setString(5, qualityDescription);
        psTimeSeries.setString(6, source);
        psTimeSeries.setString(7, thematicDescription);

        psTimeSeries.addBatch();

        if (timeSeries instanceof RegularTimeSeries) {
            RegularTimeSeries regularTimeSeries = (RegularTimeSeries) timeSeries;
            psRegularTimeSeries.setLong(1, objectId);

            if (regularTimeSeries.isSetValues() && regularTimeSeries.getValues().isSetValue()) {
                String values = valueJoiner.join(" ", regularTimeSeries.getValues().getValue());
                psRegularTimeSeries.setString(2, values);
                psRegularTimeSeries.setString(3, regularTimeSeries.getValues().getUom());
            } else {
                psRegularTimeSeries.setNull(2, Types.CLOB);
                psRegularTimeSeries.setNull(3, Types.VARCHAR);
            }

            importTimeValues(regularTimeSeries.getTemporalExtent(), regularTimeSeries.getTimeInterval(), psRegularTimeSeries, 4);

            psRegularTimeSeries.addBatch();
        }

        else if (timeSeries instanceof RegularTimeSeriesFile) {
            RegularTimeSeriesFile timeSeriesFile = (RegularTimeSeriesFile) timeSeries;
            psRegularTimeSeriesFile.setLong(1, objectId);

            psRegularTimeSeriesFile.setString(2, timeSeriesFile.getUom());
            psRegularTimeSeriesFile.setString(3, timeSeriesFile.getFile());

            if (timeSeriesFile.isSetNumberOfHeaderLines())
                psRegularTimeSeriesFile.setLong(4, timeSeriesFile.getNumberOfHeaderLines());
            else
                psRegularTimeSeriesFile.setNull(4, Types.NULL);

            psRegularTimeSeriesFile.setString(5, timeSeriesFile.getFieldSeparator());
            psRegularTimeSeriesFile.setString(6, timeSeriesFile.getRecordSeparator());
            psRegularTimeSeriesFile.setString(7, timeSeriesFile.getDecimalSymbol());

            if (timeSeriesFile.isSetValueColumnNumber())
                psRegularTimeSeriesFile.setLong(8, timeSeriesFile.getValueColumnNumber());
            else
                psRegularTimeSeriesFile.setNull(8, Types.NULL);

            importTimeValues(timeSeriesFile.getTemporalExtent(), timeSeriesFile.getTimeInterval(), psRegularTimeSeriesFile, 9);

            psRegularTimeSeriesFile.addBatch();
        }

        if (++batchCounter == helper.getDatabaseAdapter().getMaxBatchSize())
            helper.executeBatch(objectType);
    }

    @Override
    public void executeBatch() throws CityGMLImportException, SQLException {
        if (batchCounter > 0) {
            psTimeSeries.executeBatch();
            psRegularTimeSeries.executeBatch();
            psRegularTimeSeriesFile.executeBatch();
            batchCounter = 0;
        }
    }

    @Override
    public void close() throws CityGMLImportException, SQLException {
        psTimeSeries.close();
        psRegularTimeSeries.close();
        psRegularTimeSeriesFile.close();
    }

    private void importTimeValues(TimePeriodProperty timePeriodProperty, TimeIntervalLength timeIntervalLength, PreparedStatement ps, int index) throws SQLException {
        ZonedDateTime beginPosition = null;
        ZonedDateTime endPosition = null;
        if (timePeriodProperty != null && timePeriodProperty.isSetTimePeriod()) {
            TimePeriod timePeriod = timePeriodProperty.getTimePeriod();
            if (timePeriod.isSetBeginPosition())
                beginPosition = timePeriod.getBeginPosition();
            if (timePeriod.isSetEndPosition())
                endPosition = timePeriod.getEndPosition();
        }

        if (beginPosition != null)
            ps.setTimestamp(index, Timestamp.from(beginPosition.withZoneSameInstant(ZoneOffset.UTC).toInstant()));
        else
            ps.setNull(index, Types.TIMESTAMP);

        if (endPosition != null)
            ps.setTimestamp(index + 1, Timestamp.from(endPosition.withZoneSameInstant(ZoneOffset.UTC).toInstant()));
        else
            ps.setNull(index + 1, Types.TIMESTAMP);

        Double value = null;
        String unit = null;
        Integer radix = null;
        Integer factor = null;
        if (timeIntervalLength != null && timeIntervalLength.isSetValue()) {
            value = timeIntervalLength.getValue();
            unit = timeIntervalLength.getUnit();
            radix = timeIntervalLength.getRadix();
            factor = timeIntervalLength.getFactor();
        }

        if (value != null)
            ps.setDouble(index + 2, value);
        else
            ps.setNull(index + 2, Types.DOUBLE);

        ps.setString(index + 3, unit);

        if (radix != null)
            ps.setLong(index + 4, radix);
        else
            ps.setNull(index + 4, Types.NULL);

        if (factor != null)
            ps.setLong(index + 5, factor);
        else
            ps.setNull(index + 5, Types.NULL);
    }
}
