package org.citydb.ade.energy.importer;

import org.citydb.ade.importer.ADEImporter;
import org.citydb.ade.importer.CityGMLImportHelper;
import org.citydb.citygml.importer.CityGMLImportException;
import org.citygml4j.ade.energy.model.materialAndConstruction.Reflectance;
import org.citydb.ade.energy.schema.ADESequence;
import org.citydb.ade.energy.schema.ADETable;
import org.citydb.ade.energy.schema.SchemaMapper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

public class ReflectanceImporter implements ADEImporter {
    private final CityGMLImportHelper helper;
    private final SchemaMapper schemaMapper;

    private PreparedStatement ps;
    private int batchCounter;

    public ReflectanceImporter(Connection connection, CityGMLImportHelper helper, ImportManager manager) throws SQLException {
        this.helper = helper;
        this.schemaMapper = manager.getSchemaMapper();

        ps = connection.prepareStatement("insert into " +
                helper.getTableNameWithSchema(manager.getSchemaMapper().getTableName(ADETable.REFLECTANCE)) + " " +
                "(id, opticalproper_reflectance_id, surface, wavelengthrange, fraction, fraction_uom) " +
                "values (?, ?, ?, ?, ?, ?)");
    }

    public void doImport(Reflectance reflectance, long parentId) throws CityGMLImportException, SQLException {
        long objectId = helper.getNextSequenceValue(schemaMapper.getSequenceName(ADESequence.REFLECTANCE_SEQ));
        ps.setLong(1, objectId);

        ps.setLong(2, parentId);

        if (reflectance.isSetSurface())
            ps.setString(3, reflectance.getSurface().value());
        else
            ps.setNull(3, Types.VARCHAR);

        if (reflectance.isSetWavelengthRangeType())
            ps.setString(4, reflectance.getWavelengthRangeType().value());
        else
            ps.setNull(4, Types.VARCHAR);

        if (reflectance.isSetFraction() && reflectance.getFraction().isSetValue()) {
            ps.setDouble(5, reflectance.getFraction().getValue());
            ps.setString(6, reflectance.getFraction().getUom());
        } else {
            ps.setNull(5, Types.DOUBLE);
            ps.setNull(6, Types.VARCHAR);
        }

        ps.addBatch();
        if (++batchCounter == helper.getDatabaseAdapter().getMaxBatchSize())
            helper.executeBatch(schemaMapper.getTableName(ADETable.REFLECTANCE));
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
