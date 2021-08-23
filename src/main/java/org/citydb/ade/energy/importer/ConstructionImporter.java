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
import org.citygml4j.ade.energy.model.materialAndConstruction.Construction;
import org.citygml4j.ade.energy.model.materialAndConstruction.Layer;
import org.citygml4j.ade.energy.model.materialAndConstruction.LayerProperty;
import org.citygml4j.ade.energy.model.materialAndConstruction.OpticalPropertiesProperty;

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
