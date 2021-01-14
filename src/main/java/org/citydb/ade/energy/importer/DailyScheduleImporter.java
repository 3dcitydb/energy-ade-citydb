package org.citydb.ade.energy.importer;

import org.citydb.ade.energy.schema.ADESequence;
import org.citydb.ade.energy.schema.ADETable;
import org.citydb.ade.energy.schema.SchemaMapper;
import org.citydb.ade.importer.ADEImporter;
import org.citydb.ade.importer.CityGMLImportHelper;
import org.citydb.citygml.importer.CityGMLImportException;
import org.citygml4j.ade.energy.model.supportingClasses.AbstractTimeSeriesProperty;
import org.citygml4j.ade.energy.model.supportingClasses.DailySchedule;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

public class DailyScheduleImporter implements ADEImporter {
    private final CityGMLImportHelper helper;
    private final SchemaMapper schemaMapper;

    private PreparedStatement ps;
    private int batchCounter;

    public DailyScheduleImporter(Connection connection, CityGMLImportHelper helper, ImportManager manager) throws SQLException {
        this.helper = helper;
        this.schemaMapper = manager.getSchemaMapper();

        ps = connection.prepareStatement("insert into " +
                helper.getTableNameWithSchema(manager.getSchemaMapper().getTableName(ADETable.DAILYSCHEDULE)) + " " +
                "(id, periodofyear_dailyschedul_id, daytype, schedule_id) " +
                "values (?, ?, ?, ?)");
    }

    public void doImport(DailySchedule dailySchedule, long parentId) throws CityGMLImportException, SQLException {
        long objectId = helper.getNextSequenceValue(schemaMapper.getSequenceName(ADESequence.DAILYSCHEDULE_SEQ));
        ps.setLong(1, objectId);

        ps.setLong(2, parentId);

        if (dailySchedule.isSetDayType())
            ps.setString(3, dailySchedule.getDayType().value());
        else
            ps.setNull(3, Types.VARCHAR);

        long scheduleId = 0;
        if (dailySchedule.isSetSchedule()) {
            AbstractTimeSeriesProperty property = dailySchedule.getSchedule();
            if (property.isSetAbstractTimeSeries()) {
                scheduleId = helper.importObject(property.getAbstractTimeSeries());
                property.unsetAbstractTimeSeries();
            } else {
                String href = property.getHref();
                if (href != null && href.length() != 0)
                    helper.logOrThrowErrorMessage("DailySchedule: Unsupported XLink reference '" + href + "' to time series object.");
            }
        }

        if (scheduleId != 0)
            ps.setLong(4, scheduleId);
        else
            ps.setNull(4, Types.NULL);

        ps.addBatch();
        if (++batchCounter == helper.getDatabaseAdapter().getMaxBatchSize())
            helper.executeBatch(schemaMapper.getTableName(ADETable.DAILYSCHEDULE));
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
