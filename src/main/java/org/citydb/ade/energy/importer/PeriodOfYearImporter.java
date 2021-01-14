package org.citydb.ade.energy.importer;

import org.citydb.ade.importer.ADEImporter;
import org.citydb.ade.importer.CityGMLImportHelper;
import org.citydb.citygml.importer.CityGMLImportException;
import org.citygml4j.ade.energy.model.supportingClasses.DailySchedule;
import org.citygml4j.ade.energy.model.supportingClasses.DailyScheduleProperty;
import org.citygml4j.ade.energy.model.supportingClasses.PeriodOfYear;
import org.citygml4j.ade.energy.model.supportingClasses.TimePeriod;
import org.citydb.ade.energy.schema.ADESequence;
import org.citydb.ade.energy.schema.ADETable;
import org.citydb.ade.energy.schema.SchemaMapper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class PeriodOfYearImporter implements ADEImporter {
    private final CityGMLImportHelper helper;
    private final SchemaMapper schemaMapper;

    private DailyScheduleImporter dailyScheduleImporter;
    private PreparedStatement ps;
    private int batchCounter;

    public PeriodOfYearImporter(Connection connection, CityGMLImportHelper helper, ImportManager manager) throws CityGMLImportException, SQLException {
        this.helper = helper;
        this.schemaMapper = manager.getSchemaMapper();

        ps = connection.prepareStatement("insert into " +
                helper.getTableNameWithSchema(manager.getSchemaMapper().getTableName(ADETable.PERIODOFYEAR)) + " " +
                "(id, schedule_periodofyear_id, timeperiodprop_beginposition, timeperiodproper_endposition) " +
                "values (?, ?, ?, ?)");

        dailyScheduleImporter = manager.getImporter(DailyScheduleImporter.class);
    }

    public void doImport(PeriodOfYear periodOfYear, long parentId) throws CityGMLImportException, SQLException {
        long objectId = helper.getNextSequenceValue(schemaMapper.getSequenceName(ADESequence.PERIODOFYEAR_SEQ));
        ps.setLong(1, objectId);

        ps.setLong(2, parentId);

        ZonedDateTime beginPosition = null;
        ZonedDateTime endPosition = null;
        if (periodOfYear.isSetPeriod() && periodOfYear.getPeriod().isSetTimePeriod()) {
            TimePeriod timePeriod = periodOfYear.getPeriod().getTimePeriod();
            if (timePeriod.isSetBeginPosition())
                beginPosition = timePeriod.getBeginPosition();
            if (timePeriod.isSetEndPosition())
                endPosition = timePeriod.getEndPosition();
        }

        if (beginPosition != null)
            ps.setTimestamp(3, Timestamp.from(beginPosition.withZoneSameInstant(ZoneOffset.UTC).toInstant()));
        else
            ps.setNull(3, Types.TIMESTAMP);

        if (endPosition != null)
            ps.setTimestamp(4, Timestamp.from(endPosition.withZoneSameInstant(ZoneOffset.UTC).toInstant()));
        else
            ps.setNull(4, Types.TIMESTAMP);

        ps.addBatch();
        if (++batchCounter == helper.getDatabaseAdapter().getMaxBatchSize())
            helper.executeBatch(schemaMapper.getTableName(ADETable.PERIODOFYEAR));

        if (periodOfYear.isSetDailySchedule()) {
            for (DailyScheduleProperty property : periodOfYear.getDailySchedule()) {
                DailySchedule dailySchedule = property.getDaySchedule();
                if (dailySchedule != null) {
                    dailyScheduleImporter.doImport(dailySchedule, objectId);
                    property.unsetDaySchedule();
                }
            }
        }
    }

    @Override
    public void executeBatch() throws CityGMLImportException, SQLException {
        if (batchCounter > 0) {
            ps.executeBatch();
            batchCounter = 0;
        }
    }

    @Override
    public void close() throws CityGMLImportException, SQLException {
        ps.close();
    }
}
