package org.citydb.ade.energy.importer;

import org.citydb.ade.energy.schema.ADESequence;
import org.citydb.ade.energy.schema.ADETable;
import org.citydb.ade.energy.schema.SchemaMapper;
import org.citydb.ade.importer.ADEImporter;
import org.citydb.ade.importer.CityGMLImportHelper;
import org.citydb.citygml.importer.CityGMLImportException;
import org.citygml4j.ade.energy.model.core.AbstractThermalZone;
import org.citygml4j.ade.energy.model.core.AbstractUsageZone;
import org.citygml4j.ade.energy.model.core.FloorArea;
import org.citygml4j.model.citygml.building.AbstractBuilding;
import org.citygml4j.model.citygml.core.AbstractCityObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

public class FloorAreaImporter implements ADEImporter {
    private final CityGMLImportHelper helper;
    private final SchemaMapper schemaMapper;

    private PreparedStatement ps;
    private int batchCounter;

    public FloorAreaImporter(Connection connection, CityGMLImportHelper helper, ImportManager manager) throws SQLException {
        this.helper = helper;
        this.schemaMapper = manager.getSchemaMapper();

        ps = connection.prepareStatement("insert into " +
                helper.getTableNameWithSchema(manager.getSchemaMapper().getTableName(ADETable.FLOORAREA)) + " " +
                "(id, building_floorarea_id, thermalzone_floorarea_id, usagezone_floorarea_id, type, value, value_uom) " +
                "values (?, ?, ?, ?, ?, ?, ?)");
    }

    public void doImport(FloorArea floorArea, AbstractCityObject parent, long parentId) throws CityGMLImportException, SQLException {
        long objectId = helper.getNextSequenceValue(schemaMapper.getSequenceName(ADESequence.FLOORAREA_SEQ));
        ps.setLong(1, objectId);

        if (parent instanceof AbstractBuilding) {
            ps.setLong(2, parentId);
            ps.setNull(3, Types.NULL);
            ps.setNull(4, Types.NULL);
        } else if (parent instanceof AbstractThermalZone) {
            ps.setNull(2, Types.NULL);
            ps.setLong(3, parentId);
            ps.setNull(4, Types.NULL);
        } else if (parent instanceof AbstractUsageZone) {
            ps.setNull(2, Types.NULL);
            ps.setNull(3, Types.NULL);
            ps.setLong(4, parentId);
        } else {
            ps.setNull(2, Types.NULL);
            ps.setNull(3, Types.NULL);
            ps.setNull(4, Types.NULL);
        }

        if (floorArea.isSetType())
            ps.setString(5, floorArea.getType().value());
        else
            ps.setNull(5, Types.VARCHAR);

        if (floorArea.isSetValue() && floorArea.getValue().isSetValue()) {
            ps.setDouble(6, floorArea.getValue().getValue());
            ps.setString(7, floorArea.getValue().getUom());
        } else {
            ps.setNull(6, Types.DOUBLE);
            ps.setNull(7, Types.VARCHAR);
        }

        ps.addBatch();
        if (++batchCounter == helper.getDatabaseAdapter().getMaxBatchSize())
            helper.executeBatch(schemaMapper.getTableName(ADETable.FLOORAREA));
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
