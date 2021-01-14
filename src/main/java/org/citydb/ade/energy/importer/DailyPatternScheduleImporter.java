package org.citydb.ade.energy.importer;

import org.citydb.ade.energy.schema.ADETable;
import org.citydb.ade.importer.ADEImporter;
import org.citydb.ade.importer.CityGMLImportHelper;
import org.citydb.ade.importer.ForeignKeys;
import org.citydb.citygml.importer.CityGMLImportException;
import org.citydb.database.schema.mapping.AbstractObjectType;
import org.citygml4j.ade.energy.model.supportingClasses.DailyPatternSchedule;
import org.citygml4j.ade.energy.model.supportingClasses.PeriodOfYear;
import org.citygml4j.ade.energy.model.supportingClasses.PeriodOfYearProperty;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DailyPatternScheduleImporter implements ADEImporter {
    private final CityGMLImportHelper helper;

    private PeriodOfYearImporter periodOfYearImporter;
    private PreparedStatement ps;
    private int batchCounter;

    public DailyPatternScheduleImporter(Connection connection, CityGMLImportHelper helper, ImportManager manager) throws CityGMLImportException, SQLException {
        this.helper = helper;

        ps = connection.prepareStatement("insert into " +
                helper.getTableNameWithSchema(manager.getSchemaMapper().getTableName(ADETable.SCHEDULE)) + " " +
                "(id) " +
                "values (?)");

        periodOfYearImporter = manager.getImporter(PeriodOfYearImporter.class);
    }

    public void doImport(DailyPatternSchedule dailyPatternSchedule, long objectId, AbstractObjectType<?> objectType, ForeignKeys foreignKeys) throws CityGMLImportException, SQLException {
        ps.setLong(1, objectId);

        ps.addBatch();
        if (++batchCounter == helper.getDatabaseAdapter().getMaxBatchSize())
            helper.executeBatch(objectType);

        if (dailyPatternSchedule.isSetPeriodOfYear()) {
            for (PeriodOfYearProperty property : dailyPatternSchedule.getPeriodOfYear()) {
                PeriodOfYear periodOfYear = property.getPeriodOfYear();
                if (periodOfYear != null) {
                    periodOfYearImporter.doImport(periodOfYear, objectId);
                    property.unsetPeriodOfYear();
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
