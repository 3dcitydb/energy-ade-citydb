package org.citydb.ade.energy.importer;

import org.citydb.ade.importer.ADEImporter;
import org.citydb.ade.importer.CityGMLImportHelper;
import org.citydb.citygml.importer.CityGMLImportException;
import org.citygml4j.ade.energy.model.core.Transmittance;
import org.citygml4j.ade.energy.model.core.TransmittanceProperty;
import org.citygml4j.ade.energy.model.materialAndConstruction.OpticalProperties;
import org.citygml4j.ade.energy.model.materialAndConstruction.Reflectance;
import org.citygml4j.ade.energy.model.materialAndConstruction.ReflectanceProperty;
import org.citydb.ade.energy.schema.ADESequence;
import org.citydb.ade.energy.schema.ADETable;
import org.citydb.ade.energy.schema.SchemaMapper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

public class OpticalPropertiesImporter implements ADEImporter {
    private final CityGMLImportHelper helper;
    private final SchemaMapper schemaMapper;

    private ReflectanceImporter reflectanceImporter;
    private TransmittanceImporter transmittanceImporter;
    private PreparedStatement ps;
    private int batchCounter;

    public OpticalPropertiesImporter(Connection connection, CityGMLImportHelper helper, ImportManager manager) throws CityGMLImportException, SQLException {
        this.helper = helper;
        this.schemaMapper = manager.getSchemaMapper();

        ps = connection.prepareStatement("insert into " +
                helper.getTableNameWithSchema(manager.getSchemaMapper().getTableName(ADETable.OPTICALPROPERTIES)) + " " +
                "(id, glazingratio, glazingratio_uom) " +
                "values (?, ?, ?)");

        reflectanceImporter = manager.getImporter(ReflectanceImporter.class);
        transmittanceImporter = manager.getImporter(TransmittanceImporter.class);
    }

    public long doImport(OpticalProperties opticalProperties) throws CityGMLImportException, SQLException {
        long objectId = helper.getNextSequenceValue(schemaMapper.getSequenceName(ADESequence.OPTICALPROPERTIES_SEQ));
        ps.setLong(1, objectId);

        if (opticalProperties.isSetGlazingRatio() && opticalProperties.getGlazingRatio().isSetValue()) {
            ps.setDouble(2, opticalProperties.getGlazingRatio().getValue());
            ps.setString(3, opticalProperties.getGlazingRatio().getUom());
        } else {
            ps.setNull(2, Types.DOUBLE);
            ps.setNull(3, Types.VARCHAR);
        }

        ps.addBatch();
        if (++batchCounter == helper.getDatabaseAdapter().getMaxBatchSize())
            helper.executeBatch(schemaMapper.getTableName(ADETable.OPTICALPROPERTIES));

        if (opticalProperties.isSetReflectance()) {
            for (ReflectanceProperty property : opticalProperties.getReflectance()) {
                Reflectance reflectance = property.getReflectance();
                if (reflectance != null) {
                    reflectanceImporter.doImport(reflectance, objectId);
                    property.unsetReflectance();
                }
            }
        }

        if (opticalProperties.isSetTransmittance()) {
            for (TransmittanceProperty property : opticalProperties.getTransmittance()) {
                Transmittance transmittance = property.getTransmittance();
                if (transmittance != null) {
                    transmittanceImporter.doImport(transmittance, objectId);
                    property.unsetTransmittance();
                }
            }
        }

        return objectId;
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
