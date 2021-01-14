package org.citydb.ade.energy.importer;

import org.citydb.ade.importer.ADEImporter;
import org.citydb.ade.importer.CityGMLImportHelper;
import org.citydb.ade.importer.ForeignKeys;
import org.citydb.citygml.importer.CityGMLImportException;
import org.citydb.database.schema.mapping.AbstractObjectType;
import org.citygml4j.ade.energy.model.materialAndConstruction.AbstractMaterialProperty;
import org.citygml4j.ade.energy.model.materialAndConstruction.LayerComponent;
import org.citydb.ade.energy.schema.ADETable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

public class LayerComponentImporter implements ADEImporter {
    private final CityGMLImportHelper helper;

    private PreparedStatement ps;
    private int batchCounter;

    public LayerComponentImporter(Connection connection, CityGMLImportHelper helper, ImportManager manager) throws CityGMLImportException, SQLException {
        this.helper = helper;

        ps = connection.prepareStatement("insert into " +
                helper.getTableNameWithSchema(manager.getSchemaMapper().getTableName(ADETable.LAYERCOMPONENT)) + " " +
                "(id, layer_layercomponent_id, areafraction, areafraction_uom, thickness, thickness_uom, material_id) " +
                "values (?, ?, ?, ?, ?, ?, ?)");
    }

    public void doImport(LayerComponent layerComponent, long objectId, AbstractObjectType<?> objectType, ForeignKeys foreignKeys) throws CityGMLImportException, SQLException {
        long parentId = foreignKeys.get("parentId");

        ps.setLong(1, objectId);

        if (parentId != 0)
            ps.setLong(2, parentId);
        else
            ps.setNull(2, Types.NULL);

        if (layerComponent.isSetAreaFraction() && layerComponent.getAreaFraction().isSetValue()) {
            ps.setDouble(3, layerComponent.getAreaFraction().getValue());
            ps.setString(4, layerComponent.getAreaFraction().getUom());
        } else {
            ps.setNull(3, Types.DOUBLE);
            ps.setNull(4, Types.VARCHAR);
        }

        if (layerComponent.isSetThickness() && layerComponent.getThickness().isSetValue()) {
            ps.setDouble(5, layerComponent.getThickness().getValue());
            ps.setString(6, layerComponent.getThickness().getUom());
        } else {
            ps.setNull(5, Types.DOUBLE);
            ps.setNull(6, Types.VARCHAR);
        }

        long materialId = 0;
        if (layerComponent.isSetMaterial()) {
            AbstractMaterialProperty property = layerComponent.getMaterial();
            if (property.isSetAbstractMaterial()) {
                materialId = helper.importObject(property.getAbstractMaterial());
                property.unsetAbstractMaterial();
            } else
                helper.propagateObjectXlink(objectType.getTable(), objectId, property.getHref(), "material_id");
        }

        if (materialId != 0)
            ps.setLong(7, materialId);
        else
            ps.setNull(7, Types.NULL);

        ps.addBatch();
        if (++batchCounter == helper.getDatabaseAdapter().getMaxBatchSize())
            helper.executeBatch(objectType);
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
