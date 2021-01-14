package org.citydb.ade.energy.importer;

import org.citydb.ade.energy.schema.ADESequence;
import org.citydb.ade.energy.schema.ADETable;
import org.citydb.ade.energy.schema.SchemaMapper;
import org.citydb.ade.importer.ADEImporter;
import org.citydb.ade.importer.CityGMLImportHelper;
import org.citydb.citygml.importer.CityGMLImportException;
import org.citygml4j.ade.energy.model.core.HeightAboveGround;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

public class HeightAboveGroundImporter implements ADEImporter {
    private final CityGMLImportHelper helper;
    private final SchemaMapper schemaMapper;

    private PreparedStatement ps;
    private int batchCounter;

    public HeightAboveGroundImporter(Connection connection, CityGMLImportHelper helper, ImportManager manager) throws SQLException {
        this.helper = helper;
        this.schemaMapper = manager.getSchemaMapper();

        ps = connection.prepareStatement("insert into " +
                helper.getTableNameWithSchema(manager.getSchemaMapper().getTableName(ADETable.HEIGHTABOVEGROUND)) + " " +
                "(id, building_heightabovegroun_id, heightreference, value, value_uom) " +
                "values (?, ?, ?, ?, ?)");
    }

    public void doImport(HeightAboveGround heightAboveGround, long parentId) throws CityGMLImportException, SQLException {
        long objectId = helper.getNextSequenceValue(schemaMapper.getSequenceName(ADESequence.HEIGHTABOVEGROUND_SEQ));
        ps.setLong(1, objectId);

        ps.setLong(2, parentId);

        if (heightAboveGround.isSetHeightReference())
            ps.setString(3, heightAboveGround.getHeightReference().value());
        else
            ps.setNull(3, Types.VARCHAR);

        if (heightAboveGround.isSetValue() && heightAboveGround.getValue().isSetValue()) {
            ps.setDouble(4, heightAboveGround.getValue().getValue());
            ps.setString(5, heightAboveGround.getValue().getUom());
        } else {
            ps.setNull(4, Types.DOUBLE);
            ps.setNull(5, Types.VARCHAR);
        }

        ps.addBatch();
        if (++batchCounter == helper.getDatabaseAdapter().getMaxBatchSize())
            helper.executeBatch(schemaMapper.getTableName(ADETable.HEIGHTABOVEGROUND));
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
