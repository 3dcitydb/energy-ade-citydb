package org.citydb.ade.energy.importer;

import org.citydb.ade.importer.ADEImporter;
import org.citydb.ade.importer.CityGMLImportHelper;
import org.citydb.ade.importer.ForeignKeys;
import org.citydb.citygml.importer.CityGMLImportException;
import org.citydb.database.schema.mapping.AbstractObjectType;
import org.citygml4j.ade.energy.model.materialAndConstruction.Construction;
import org.citygml4j.ade.energy.model.materialAndConstruction.Layer;
import org.citygml4j.ade.energy.model.materialAndConstruction.LayerProperty;
import org.citygml4j.ade.energy.model.materialAndConstruction.OpticalPropertiesProperty;
import org.citydb.ade.energy.schema.ADETable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

public class ConstructionImporter implements ADEImporter {
    private final CityGMLImportHelper helper;

    private OpticalPropertiesImporter opticalPropertiesImporter;
    private PreparedStatement ps;
    private int batchCounter;

    public ConstructionImporter(Connection connection, CityGMLImportHelper helper, ImportManager manager) throws CityGMLImportException, SQLException {
        this.helper = helper;

        ps = connection.prepareStatement("insert into " +
                helper.getTableNameWithSchema(manager.getSchemaMapper().getTableName(ADETable.CONSTRUCTION)) + " " +
                "(id, uvalue, uvalue_uom, opticalproperties_id) " +
                "values (?, ?, ?, ?)");

        opticalPropertiesImporter = manager.getImporter(OpticalPropertiesImporter.class);
    }

    public void doImport(Construction construction, long objectId, AbstractObjectType<?> objectType, ForeignKeys foreignKeys) throws CityGMLImportException, SQLException {
        ps.setLong(1, objectId);

        if (construction.isSetUValue() && construction.getUValue().isSetValue()) {
            ps.setDouble(2, construction.getUValue().getValue());
            ps.setString(3, construction.getUValue().getUom());
        } else {
            ps.setNull(2, Types.DOUBLE);
            ps.setNull(3, Types.VARCHAR);
        }

        long opticalPropertiesId = 0;
        if (construction.isSetOpticalProperties()) {
            OpticalPropertiesProperty property = construction.getOpticalProperties();
            if (property.isSetObject()) {
                opticalPropertiesId = opticalPropertiesImporter.doImport(property.getObject());
                property.unsetObject();
            }
        }

        if (opticalPropertiesId != 0)
            ps.setLong(4, opticalPropertiesId);
        else
            ps.setNull(4, Types.NULL);

        ps.addBatch();
        if (++batchCounter == helper.getDatabaseAdapter().getMaxBatchSize())
            helper.executeBatch(objectType);

        if (construction.isSetLayer()) {
            for (LayerProperty property : construction.getLayer()) {
                Layer layer = property.getLayer();
                if (layer != null) {
                    helper.importObject(layer, ForeignKeys.create().with("parentId", objectId));
                    property.unsetLayer();
                } else {
                    String href = property.getHref();
                    if (href != null && href.length() != 0)
                        helper.logOrThrowUnsupportedXLinkMessage(construction, Layer.class, href);
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
