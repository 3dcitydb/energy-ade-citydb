package org.citydb.ade.energy.importer;

import org.citydb.ade.importer.ADEImporter;
import org.citydb.ade.importer.CityGMLImportHelper;
import org.citydb.ade.importer.ForeignKeys;
import org.citydb.citygml.importer.CityGMLImportException;
import org.citydb.database.schema.mapping.AbstractObjectType;
import org.citygml4j.ade.energy.model.materialAndConstruction.Layer;
import org.citygml4j.ade.energy.model.materialAndConstruction.LayerComponent;
import org.citygml4j.ade.energy.model.materialAndConstruction.LayerComponentProperty;
import org.citydb.ade.energy.schema.ADETable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

public class LayerImporter implements ADEImporter {
    private final CityGMLImportHelper helper;

    private PreparedStatement ps;
    private int batchCounter;

    public LayerImporter(Connection connection, CityGMLImportHelper helper, ImportManager manager) throws CityGMLImportException, SQLException {
        this.helper = helper;

        ps = connection.prepareStatement("insert into " +
                helper.getTableNameWithSchema(manager.getSchemaMapper().getTableName(ADETable.LAYER)) + " " +
                "(id, construction_layer_id) " +
                "values (?, ?)");
    }

    public void doImport(Layer layer, long objectId, AbstractObjectType<?> objectType, ForeignKeys foreignKeys) throws CityGMLImportException, SQLException {
        long parentId = foreignKeys.get("parentId");

        ps.setLong(1, objectId);

        if (parentId != 0)
            ps.setLong(2, parentId);
        else
            ps.setNull(2, Types.NULL);

        ps.addBatch();
        if (++batchCounter == helper.getDatabaseAdapter().getMaxBatchSize())
            helper.executeBatch(objectType);

        if (layer.isSetLayerComponent()) {
            for (LayerComponentProperty property : layer.getLayerComponent()) {
                LayerComponent layerComponent = property.getLayerComponent();
                if (layerComponent != null) {
                    helper.importObject(layerComponent, ForeignKeys.create().with("parentId", objectId));
                    property.unsetLayerComponent();
                } else {
                    String href = property.getHref();
                    if (href != null && href.length() != 0)
                        helper.logOrThrowUnsupportedXLinkMessage(layer, LayerComponent.class, href);
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
