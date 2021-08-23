/*
 * 3D City Database - The Open Source CityGML Database
 * https://www.3dcitydb.org/
 *
 * Copyright 2013 - 2021
 * Chair of Geoinformatics
 * Technical University of Munich, Germany
 * https://www.lrg.tum.de/gis/
 *
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 *
 * Virtual City Systems, Berlin <https://vc.systems/>
 * M.O.S.S. Computer Grafik Systeme GmbH, Taufkirchen <http://www.moss.de/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citydb.ade.energy.importer;

import org.citydb.ade.energy.schema.ADETable;
import org.citydb.core.ade.importer.ADEImporter;
import org.citydb.core.ade.importer.CityGMLImportHelper;
import org.citydb.core.ade.importer.ForeignKeys;
import org.citydb.core.database.schema.mapping.AbstractObjectType;
import org.citydb.core.operation.importer.CityGMLImportException;
import org.citygml4j.ade.energy.model.materialAndConstruction.AbstractMaterialProperty;
import org.citygml4j.ade.energy.model.materialAndConstruction.LayerComponent;

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
