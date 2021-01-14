package org.citydb.ade.energy.importer;

import org.citydb.ade.energy.schema.ADETable;
import org.citydb.ade.energy.schema.SchemaMapper;
import org.citydb.ade.importer.ADEImporter;
import org.citydb.ade.importer.CityGMLImportHelper;
import org.citydb.citygml.importer.CityGMLImportException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ThermalBoundaryToThermalZoneImporter implements ADEImporter {
    private final CityGMLImportHelper helper;
    private final SchemaMapper schemaMapper;

    private PreparedStatement ps;
    private int batchCounter;

    public ThermalBoundaryToThermalZoneImporter(Connection connection, CityGMLImportHelper helper, ImportManager manager) throws SQLException {
        this.helper = helper;
        this.schemaMapper = manager.getSchemaMapper();

        ps = connection.prepareStatement("insert into " +
                helper.getTableNameWithSchema(manager.getSchemaMapper().getTableName(ADETable.THER_BOUN_TO_THER_DELI)) + " " +
                "(thermalzone_boundedby_id, thermalboundary_delimits_id) " +
                "values (?, ?)");
    }

    public void doImport(long thermalZoneId, long thermalBoundaryId) throws CityGMLImportException, SQLException {
        ps.setLong(1, thermalZoneId);
        ps.setLong(2, thermalBoundaryId);

        ps.addBatch();
        if (++batchCounter == helper.getDatabaseAdapter().getMaxBatchSize())
            helper.executeBatch(schemaMapper.getTableName(ADETable.THER_BOUN_TO_THER_DELI));
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
