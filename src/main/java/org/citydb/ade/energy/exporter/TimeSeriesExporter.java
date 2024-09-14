/*
 * 3D City Database - The Open Source CityGML Database
 * https://www.3dcitydb.org/
 *
 * Copyright 2013 - 2024
 * Chair of Geoinformatics
 * Technical University of Munich, Germany
 * https://www.lrg.tum.de/gis/
 *
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 *
 * Virtual City Systems, Berlin <https://vc.systems/>
 * M.O.S.S. Computer Grafik Systeme GmbH, Taufkirchen <http://www.moss.de/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citydb.ade.energy.exporter;

import org.citydb.ade.energy.schema.ADETable;
import org.citydb.core.ade.exporter.ADEExporter;
import org.citydb.core.ade.exporter.CityGMLExportHelper;
import org.citydb.core.database.schema.mapping.ObjectType;
import org.citydb.core.operation.exporter.CityGMLExportException;
import org.citydb.core.operation.exporter.util.AttributeValueSplitter;
import org.citydb.core.query.filter.projection.CombinedProjectionFilter;
import org.citydb.core.query.filter.projection.ProjectionFilter;
import org.citydb.sqlbuilder.expression.PlaceHolder;
import org.citydb.sqlbuilder.schema.Table;
import org.citydb.sqlbuilder.select.Select;
import org.citydb.sqlbuilder.select.join.JoinFactory;
import org.citydb.sqlbuilder.select.operator.comparison.ComparisonFactory;
import org.citydb.sqlbuilder.select.operator.comparison.ComparisonName;
import org.citygml4j.ade.energy.model.module.EnergyADEModule;
import org.citygml4j.ade.energy.model.supportingClasses.*;
import org.citygml4j.model.gml.basicTypes.MeasureList;

import java.sql.*;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;

public class TimeSeriesExporter implements ADEExporter {
    private final CityGMLExportHelper helper;

    private PreparedStatement ps;
    private AttributeValueSplitter attributeSplitter;
    private String module;

    public TimeSeriesExporter(Connection connection, CityGMLExportHelper helper, ExportManager manager) throws CityGMLExportException, SQLException {
        this.helper = helper;

        String tableName = manager.getSchemaMapper().getTableName(ADETable.TIMESERIES);
        CombinedProjectionFilter projectionFilter = helper.getCombinedProjectionFilter(tableName);
        module = EnergyADEModule.v1_0.getNamespaceURI();

        Table table = new Table(helper.getTableNameWithSchema(tableName));
        Table regularTimeSeries = new Table(helper.getTableNameWithSchema(manager.getSchemaMapper().getTableName(ADETable.REGULARTIMESERIES)));
        Table regularTimeSeriesFile = new Table(helper.getTableNameWithSchema(manager.getSchemaMapper().getTableName(ADETable.REGULARTIMESERIESFILE)));

        Select select = new Select().addProjection(table.getColumn("objectclass_id"),
                        table.getColumn("timevaluesprop_acquisitionme"), table.getColumn("timevaluesprop_interpolation"),
                        table.getColumn("timevaluesprop_qualitydescri"), table.getColumn("timevaluespropertiest_source"),
                        table.getColumn("timevaluesprop_thematicdescr"),
                        regularTimeSeries.getColumn("values_"), regularTimeSeries.getColumn("values_uom"),
                        regularTimeSeries.getColumn("timeperiodprop_beginposition"), regularTimeSeries.getColumn("timeperiodproper_endposition"),
                        regularTimeSeries.getColumn("timeinterval_unit"), regularTimeSeries.getColumn("timeinterval_radix"),
                        regularTimeSeries.getColumn("timeinterval_factor"), regularTimeSeries.getColumn("timeinterval"),
                        regularTimeSeriesFile.getColumn("file_"), regularTimeSeriesFile.getColumn("uom"),
                        regularTimeSeriesFile.getColumn("fieldseparator"), regularTimeSeriesFile.getColumn("timeperiodprop_beginposition", "file_beginposition"),
                        regularTimeSeriesFile.getColumn("timeperiodproper_endposition", "file_endposition"), regularTimeSeriesFile.getColumn("timeinterval_unit", "file_unit"),
                        regularTimeSeriesFile.getColumn("timeinterval_radix", "file_radix"), regularTimeSeriesFile.getColumn("timeinterval_factor", "file_factor"),
                        regularTimeSeriesFile.getColumn("timeinterval", "file_timeinterval"))
                .addJoin(JoinFactory.left(regularTimeSeries, "id", ComparisonName.EQUAL_TO, table.getColumn("id")))
                .addJoin(JoinFactory.left(regularTimeSeriesFile, "id", ComparisonName.EQUAL_TO, table.getColumn("id")));
        if (projectionFilter.containsProperty("numberOfHeaderLines", module))
            select.addProjection(regularTimeSeriesFile.getColumn("numberofheaderlines"));
        if (projectionFilter.containsProperty("recordSeparator", module))
            select.addProjection(regularTimeSeriesFile.getColumn("recordseparator"));
        if (projectionFilter.containsProperty("decimalSymbol", module))
            select.addProjection(regularTimeSeriesFile.getColumn("decimalsymbol"));
        if (projectionFilter.containsProperty("valueColumnNumber", module))
            select.addProjection(regularTimeSeriesFile.getColumn("valuecolumnnumber"));
        select.addSelection(ComparisonFactory.equalTo(table.getColumn("id"), new PlaceHolder<>()));
        ps = connection.prepareStatement(select.toString());

        attributeSplitter = helper.getAttributeValueSplitter();
    }

    public AbstractTimeSeries doExport(long objectId) throws CityGMLExportException, SQLException {
        ps.setLong(1, objectId);

        try (ResultSet rs = ps.executeQuery()) {
            AbstractTimeSeries timeSeries = null;

            if (rs.next()) {
                int objectClassId = rs.getInt("objectclass_id");

                timeSeries = helper.createObject(objectId, objectClassId, AbstractTimeSeries.class);
                if (timeSeries == null) {
                    helper.logOrThrowErrorMessage("Failed to instantiate " + helper.getObjectSignature(objectClassId, objectId) + " as time series object.");
                    return null;
                }

                ObjectType objectType = helper.getObjectType(objectClassId);
                ProjectionFilter projectionFilter = helper.getProjectionFilter(objectType);

                AcquisitionMethodValue acquisitionMethod = AcquisitionMethodValue.fromValue(rs.getString("timevaluesprop_acquisitionme"));
                InterpolationTypeValue interpolationType = InterpolationTypeValue.fromValue(rs.getString("timevaluesprop_interpolation"));
                if (acquisitionMethod != null && interpolationType != null) {
                    TimeValuesProperties timeValuesProperties = new TimeValuesProperties();
                    timeValuesProperties.setAcquisitionMethod(acquisitionMethod);
                    timeValuesProperties.setInterpolationType(interpolationType);

                    timeValuesProperties.setQualityDescription(rs.getString("timevaluesprop_qualitydescri"));
                    timeValuesProperties.setSource(rs.getString("timevaluespropertiest_source"));
                    timeValuesProperties.setThematicDescription(rs.getString("timevaluesprop_thematicdescr"));

                    timeSeries.setVariableProperties(new TimeValuesPropertiesProperty(timeValuesProperties));
                }

                if (timeSeries instanceof RegularTimeSeries) {
                    RegularTimeSeries regularTimeSeries = (RegularTimeSeries) timeSeries;

                    List<Double> values = attributeSplitter.splitDoubleList(rs.getString("values_"));
                    if (values != null) {
                        MeasureList measureList = new MeasureList();
                        measureList.setValue(values);
                        measureList.setUom(rs.getString("values_uom"));
                        regularTimeSeries.setValues(measureList);
                    }

                    TimePeriod timePeriod = exportTimePeriod(rs, "timeperiodprop_beginposition", "timeperiodproper_endposition");
                    if (timePeriod != null)
                        regularTimeSeries.setTemporalExtent(new TimePeriodProperty(timePeriod));

                    TimeIntervalLength timeIntervalLength = exportTimeIntervalLength(rs, "timeinterval", "timeinterval_unit", "timeinterval_radix", "timeinterval_factor");
                    if (timeIntervalLength != null)
                        regularTimeSeries.setTimeInterval(timeIntervalLength);

                } else if (timeSeries instanceof RegularTimeSeriesFile) {
                    RegularTimeSeriesFile regularTimeSeriesFile = (RegularTimeSeriesFile) timeSeries;

                    regularTimeSeriesFile.setFile(rs.getString("file_"));
                    regularTimeSeriesFile.setUom(rs.getString("uom"));
                    regularTimeSeriesFile.setFieldSeparator(rs.getString("fieldseparator"));

                    TimePeriod timePeriod = exportTimePeriod(rs, "file_beginposition", "file_endposition");
                    if (timePeriod != null)
                        regularTimeSeriesFile.setTemporalExtent(new TimePeriodProperty(timePeriod));

                    TimeIntervalLength timeIntervalLength = exportTimeIntervalLength(rs, "file_timeinterval", "file_unit", "file_radix", "file_factor");
                    if (timeIntervalLength != null)
                        regularTimeSeriesFile.setTimeInterval(timeIntervalLength);

                    if (projectionFilter.containsProperty("numberOfHeaderLines", module)) {
                        int numberOfHeaderLines = rs.getInt("numberofheaderlines");
                        if (!rs.wasNull())
                            regularTimeSeriesFile.setNumberOfHeaderLines(numberOfHeaderLines);
                    }

                    if (projectionFilter.containsProperty("recordSeparator", module))
                        regularTimeSeriesFile.setRecordSeparator(rs.getString("recordseparator"));

                    if (projectionFilter.containsProperty("decimalSymbol", module))
                        regularTimeSeriesFile.setDecimalSymbol(rs.getString("decimalsymbol"));

                    if (projectionFilter.containsProperty("valueColumnNumber", module)) {
                        int valueColumnNumber = rs.getInt("valuecolumnnumber");
                        if (!rs.wasNull())
                            regularTimeSeriesFile.setValueColumnNumber(valueColumnNumber);
                    }
                }
            }

            return timeSeries;
        }
    }

    private TimePeriod exportTimePeriod(ResultSet rs, String beginPositionColumn, String endPositionColumn) throws SQLException {
        TimePeriod timePeriod = null;

        Timestamp beginPosition = rs.getTimestamp(beginPositionColumn);
        Timestamp endPosition = rs.getTimestamp(endPositionColumn);
        if (beginPosition != null && endPosition != null) {
            timePeriod = new TimePeriod();
            timePeriod.setBeginPosition(beginPosition.toLocalDateTime().atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneOffset.UTC));
            timePeriod.setEndPosition(endPosition.toLocalDateTime().atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneOffset.UTC));
        }

        return timePeriod;
    }

    private TimeIntervalLength exportTimeIntervalLength(ResultSet rs, String valueColumn, String unitColumn, String radixColumn, String factorColumn) throws SQLException {
        TimeIntervalLength timeIntervalLength = null;

        double value = rs.getDouble(valueColumn);
        if (!rs.wasNull()) {
            timeIntervalLength = new TimeIntervalLength();

            timeIntervalLength.setValue(value);
            timeIntervalLength.setUnit(rs.getString(unitColumn));

            int radix = rs.getInt(radixColumn);
            if (!rs.wasNull())
                timeIntervalLength.setRadix(radix);

            int factor = rs.getInt(factorColumn);
            if (!rs.wasNull())
                timeIntervalLength.setFactor(factor);
        }

        return timeIntervalLength;
    }

    @Override
    public void close() throws CityGMLExportException, SQLException {
        ps.close();
    }
}
