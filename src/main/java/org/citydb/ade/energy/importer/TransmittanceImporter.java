package org.citydb.ade.energy.importer;

import org.citydb.ade.importer.ADEImporter;
import org.citydb.ade.importer.CityGMLImportHelper;
import org.citydb.citygml.importer.CityGMLImportException;
import org.citygml4j.ade.energy.model.core.Transmittance;
import org.citydb.ade.energy.schema.ADESequence;
import org.citydb.ade.energy.schema.ADETable;
import org.citydb.ade.energy.schema.SchemaMapper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

public class TransmittanceImporter implements ADEImporter {
    private final CityGMLImportHelper helper;
    private final SchemaMapper schemaMapper;

    private PreparedStatement ps;
    private int batchCounter;

    public TransmittanceImporter(Connection connection, CityGMLImportHelper helper, ImportManager manager) throws SQLException {
        this.helper = helper;
        this.schemaMapper = manager.getSchemaMapper();

        ps = connection.prepareStatement("insert into " +
                helper.getTableNameWithSchema(manager.getSchemaMapper().getTableName(ADETable.TRANSMITTANCE)) + " " +
                "(id, opticalprope_transmittanc_id, wavelengthrange, fraction, fraction_uom) " +
                "values (?, ?, ?, ?, ?)");
    }

    public void doImport(Transmittance transmittance, long parentId) throws CityGMLImportException, SQLException {
        long objectId = helper.getNextSequenceValue(schemaMapper.getSequenceName(ADESequence.TRANSMITTANCE_SEQ));
        ps.setLong(1, objectId);

        ps.setLong(2, parentId);

        if (transmittance.isSetWavelengthRangeType())
            ps.setString(3, transmittance.getWavelengthRangeType().value());
        else
            ps.setNull(3, Types.VARCHAR);

        if (transmittance.isSetFraction() && transmittance.getFraction().isSetValue()) {
            ps.setDouble(4, transmittance.getFraction().getValue());
            ps.setString(5, transmittance.getFraction().getUom());
        } else {
            ps.setNull(4, Types.DOUBLE);
            ps.setNull(5, Types.VARCHAR);
        }

        ps.addBatch();
        if (++batchCounter == helper.getDatabaseAdapter().getMaxBatchSize())
            helper.executeBatch(schemaMapper.getTableName(ADETable.TRANSMITTANCE));
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
