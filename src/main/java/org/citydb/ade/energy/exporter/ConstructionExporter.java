/*
 * 3D City Database - The Open Source CityGML Database
 * https://www.3dcitydb.org/
 *
 * Copyright 2013 - 2024
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

package org.citydb.ade.energy.exporter;

import org.citydb.ade.energy.schema.ADETable;
import org.citydb.core.ade.exporter.ADEExporter;
import org.citydb.core.ade.exporter.CityGMLExportHelper;
import org.citydb.core.database.schema.mapping.FeatureType;
import org.citydb.core.database.schema.mapping.MappingConstants;
import org.citydb.core.operation.exporter.CityGMLExportException;
import org.citydb.core.query.filter.projection.CombinedProjectionFilter;
import org.citydb.core.query.filter.projection.ProjectionFilter;
import org.citydb.sqlbuilder.expression.PlaceHolder;
import org.citydb.sqlbuilder.schema.Table;
import org.citydb.sqlbuilder.select.Select;
import org.citydb.sqlbuilder.select.join.JoinFactory;
import org.citydb.sqlbuilder.select.operator.comparison.ComparisonFactory;
import org.citydb.sqlbuilder.select.operator.comparison.ComparisonName;
import org.citygml4j.ade.energy.model.core.AbstractConstructionProperty;
import org.citygml4j.ade.energy.model.materialAndConstruction.*;
import org.citygml4j.ade.energy.model.module.EnergyADEModule;
import org.citygml4j.model.gml.basicTypes.Measure;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ConstructionExporter implements ADEExporter {
    private final CityGMLExportHelper helper;
    private final int objectClassId;

    private PreparedStatement ps;
    private OpticalPropertiesExporter opticalPropertiesExporter;
    private LayerExporter layerExporter;
    private String module;

    public ConstructionExporter(Connection connection, CityGMLExportHelper helper, ExportManager manager) throws CityGMLExportException, SQLException {
        this.helper = helper;
        objectClassId = manager.getObjectMapper().getObjectClassId(Construction.class);

        String tableName = manager.getSchemaMapper().getTableName(ADETable.CONSTRUCTION);
        CombinedProjectionFilter projectionFilter = helper.getCombinedProjectionFilter(tableName);
        module = EnergyADEModule.v1_0.getNamespaceURI();

        Table table = new Table(helper.getTableNameWithSchema(tableName));
        Table cityObject = new Table(helper.getTableNameWithSchema(MappingConstants.CITYOBJECT));

        Select select = new Select().addProjection(table.getColumn("id"), cityObject.getColumn("gmlid"))
                .addJoin(JoinFactory.inner(cityObject, "id", ComparisonName.EQUAL_TO, table.getColumn("id")));
        if (projectionFilter.containsProperty("uValue", module))
            select.addProjection(table.getColumn("uvalue"), table.getColumn("uvalue_uom"));
        if (projectionFilter.containsProperty("opticalProperties", module))
            select.addProjection(table.getColumn("opticalproperties_id"));
        select.addSelection(ComparisonFactory.equalTo(table.getColumn("id"), new PlaceHolder<>()));
        ps = connection.prepareStatement(select.toString());

        opticalPropertiesExporter = manager.getExporter(OpticalPropertiesExporter.class);
        layerExporter = manager.getExporter(LayerExporter.class);
    }

    public AbstractConstructionProperty doExport(long objectId) throws CityGMLExportException, SQLException {
        ps.setLong(1, objectId);

        try (ResultSet rs = ps.executeQuery()) {
            Construction construction = null;

            if (rs.next()) {
                String gmlId = rs.getString("gmlid");
                if (gmlId != null && helper.lookupAndPutObjectId(gmlId, objectId, objectClassId))
                    return new AbstractConstructionProperty("#" + gmlId);

                construction = helper.createObject(objectId, objectClassId, Construction.class);
                if (construction == null) {
                    helper.logOrThrowErrorMessage("Failed to instantiate " + helper.getObjectSignature(objectClassId, objectId) + " as construction object.");
                    return null;
                }

                construction.setId(gmlId);
                FeatureType featureType = helper.getFeatureType(objectClassId);
                ProjectionFilter projectionFilter = helper.getProjectionFilter(featureType);

                if (projectionFilter.containsProperty("uValue", module)) {
                    double uValue = rs.getDouble("uvalue");
                    if (!rs.wasNull()) {
                        Measure measure = new Measure(uValue);
                        measure.setUom(rs.getString("uvalue_uom"));
                        construction.setUValue(measure);
                    }
                }

                if (projectionFilter.containsProperty("opticalProperties", module)) {
                    long propertiesId = rs.getLong("opticalproperties_id");
                    if (!rs.wasNull()) {
                        OpticalProperties opticalProperties = opticalPropertiesExporter.doExport(propertiesId);
                        if (opticalProperties != null)
                            construction.setOpticalProperties(new OpticalPropertiesProperty(opticalProperties));
                    }
                }

                if (projectionFilter.containsProperty("layer", module)) {
                    for (Layer layer : layerExporter.doExport(objectId))
                        construction.addLayer(new LayerProperty(layer));
                }
            }

            return new AbstractConstructionProperty(construction);
        }
    }

    @Override
    public void close() throws CityGMLExportException, SQLException {
        ps.close();
    }
}
