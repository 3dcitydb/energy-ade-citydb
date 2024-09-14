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
import org.citydb.sqlbuilder.select.OrderByToken;
import org.citydb.sqlbuilder.select.Select;
import org.citydb.sqlbuilder.select.join.JoinFactory;
import org.citydb.sqlbuilder.select.operator.comparison.ComparisonFactory;
import org.citydb.sqlbuilder.select.operator.comparison.ComparisonName;
import org.citygml4j.ade.energy.model.materialAndConstruction.*;
import org.citygml4j.ade.energy.model.module.EnergyADEModule;
import org.citygml4j.model.gml.basicTypes.Measure;
import org.citygml4j.model.gml.measures.Length;
import org.citygml4j.model.gml.measures.Scale;
import org.citygml4j.util.gmlid.DefaultGMLIdManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class LayerExporter implements ADEExporter {
    private final CityGMLExportHelper helper;
    private final int objectClassId;
    private final int componentObjectClassId;

    private PreparedStatement ps;
    private String module;

    public LayerExporter(Connection connection, CityGMLExportHelper helper, ExportManager manager) throws CityGMLExportException, SQLException {
        this.helper = helper;
        objectClassId = manager.getObjectMapper().getObjectClassId(Layer.class);
        componentObjectClassId = manager.getObjectMapper().getObjectClassId(LayerComponent.class);

        String tableName = manager.getSchemaMapper().getTableName(ADETable.LAYER);
        CombinedProjectionFilter componentProjectionFilter = helper.getCombinedProjectionFilter(manager.getSchemaMapper().getTableName(ADETable.LAYERCOMPONENT));
        CombinedProjectionFilter gasProjectionFilter = helper.getCombinedProjectionFilter(manager.getSchemaMapper().getTableName(ADETable.GAS));
        CombinedProjectionFilter solidProjectionMaterialFilter = helper.getCombinedProjectionFilter(manager.getSchemaMapper().getTableName(ADETable.SOLIDMATERIAL));
        module = EnergyADEModule.v1_0.getNamespaceURI();

        Table table = new Table(helper.getTableNameWithSchema(tableName));
        Table component = new Table(helper.getTableNameWithSchema(manager.getSchemaMapper().getTableName(ADETable.LAYERCOMPONENT)));
        Table material = new Table(helper.getTableNameWithSchema(manager.getSchemaMapper().getTableName(ADETable.MATERIAL)));
        Table gas = new Table(helper.getTableNameWithSchema(manager.getSchemaMapper().getTableName(ADETable.GAS)));
        Table solidMaterial = new Table(helper.getTableNameWithSchema(manager.getSchemaMapper().getTableName(ADETable.SOLIDMATERIAL)));
        Table cityObject = new Table(helper.getTableNameWithSchema(MappingConstants.CITYOBJECT));

        Select select = new Select().addProjection(table.getColumn("id"), component.getColumn("id", "cid"),
                        component.getColumn("thickness"), component.getColumn("thickness_uom"),
                        material.getColumn("id", "mid"), material.getColumn("objectclass_id"), cityObject.getColumn("gmlid"),
                        gas.getColumn("rvalue"), gas.getColumn("rvalue_uom"),
                        solidMaterial.getColumn("conductivity"), solidMaterial.getColumn("conductivity_uom"),
                        solidMaterial.getColumn("density"), solidMaterial.getColumn("density_uom"),
                        solidMaterial.getColumn("specificheat"), solidMaterial.getColumn("specificheat_uom"))
                .addJoin(JoinFactory.left(component, "layer_layercomponent_id", ComparisonName.EQUAL_TO, table.getColumn("id")))
                .addJoin(JoinFactory.left(material, "id", ComparisonName.EQUAL_TO, component.getColumn("material_id")))
                .addJoin(JoinFactory.left(gas, "id", ComparisonName.EQUAL_TO, material.getColumn("id")))
                .addJoin(JoinFactory.left(solidMaterial, "id", ComparisonName.EQUAL_TO, material.getColumn("id")))
                .addJoin(JoinFactory.left(cityObject, "id", ComparisonName.EQUAL_TO, material.getColumn("id")))
                .addOrderBy(new OrderByToken(table.getColumn("id")));
        if (componentProjectionFilter.containsProperty("areaFraction", module))
            select.addProjection(component.getColumn("areafraction"), component.getColumn("areafraction_uom"));
        if (gasProjectionFilter.containsProperty("isVentilated", module))
            select.addProjection(gas.getColumn("isventilated"));
        if (solidProjectionMaterialFilter.containsProperty("permeance", module))
            select.addProjection(solidMaterial.getColumn("permeance"), solidMaterial.getColumn("permeance_uom"));
        select.addSelection(ComparisonFactory.equalTo(table.getColumn("construction_layer_id"), new PlaceHolder<>()));
        ps = connection.prepareStatement(select.toString());
    }

    public Collection<Layer> doExport(long parentId) throws CityGMLExportException, SQLException {
        ps.setLong(1, parentId);

        try (ResultSet rs = ps.executeQuery()) {
            long currentLayerId = 0;
            Layer layer = null;
            Map<Long, Layer> layers = new HashMap<>();
            Map<Long, Set<Long>> componentIds = new HashMap<>();

            while (rs.next()) {
                long layerId = rs.getLong("id");

                if (layerId != currentLayerId || layer == null) {
                    currentLayerId = layerId;
                    layer = layers.get(layerId);

                    if (layer == null) {
                        layer = helper.createObject(layerId, objectClassId, Layer.class);
                        if (layer == null) {
                            helper.logOrThrowErrorMessage("Failed to instantiate " + helper.getObjectSignature(objectClassId, layerId) + " as layer object.");
                            continue;
                        }

                        layers.put(layerId, layer);
                    }
                }

                long componentId = rs.getLong("cid");
                if (!rs.wasNull() && componentIds.computeIfAbsent(layerId, v -> new HashSet<>()).add(componentId)) {
                    LayerComponent component = helper.createObject(componentId, componentObjectClassId, LayerComponent.class);
                    if (component == null) {
                        helper.logOrThrowErrorMessage("Failed to instantiate " + helper.getObjectSignature(componentObjectClassId, componentId) + " as layer component object.");
                        continue;
                    }

                    FeatureType featureType = helper.getFeatureType(objectClassId);
                    ProjectionFilter componentProjectionFilter = helper.getProjectionFilter(featureType);

                    double thickness = rs.getDouble("thickness");
                    if (!rs.wasNull()) {
                        Length length = new Length(thickness);
                        length.setUom(rs.getString("thickness_uom"));
                        component.setThickness(length);
                    }

                    if (componentProjectionFilter.containsProperty("areaFraction", module)) {
                        double areaFraction = rs.getDouble("areafraction");
                        if (!rs.wasNull()) {
                            Scale scale = new Scale(areaFraction);
                            scale.setUom(rs.getString("areafraction_uom"));
                            component.setAreaFraction(scale);
                        }
                    }

                    long materialId = rs.getLong("mid");
                    if (!rs.wasNull()) {
                        AbstractMaterialProperty property = new AbstractMaterialProperty();
                        int materialObjectClassId = rs.getInt("objectclass_id");
                        String gmlId = rs.getString("gmlid");

                        if (gmlId == null || !helper.lookupAndPutObjectId(gmlId, materialId, materialObjectClassId)) {
                            AbstractMaterial material = helper.createObject(materialId, materialObjectClassId, AbstractMaterial.class);
                            if (material == null) {
                                helper.logOrThrowErrorMessage("Failed to instantiate " + helper.getObjectSignature(materialObjectClassId, materialId) + " as material object.");
                                continue;
                            }

                            featureType = helper.getFeatureType(objectClassId);
                            ProjectionFilter materialProjectionFilter = helper.getProjectionFilter(featureType);

                            if (material instanceof Gas) {
                                Gas gas = (Gas) material;

                                double rValue = rs.getDouble("rvalue");
                                if (!rs.wasNull()) {
                                    Measure measure = new Measure(rValue);
                                    measure.setUom(rs.getString("rvalue_uom"));
                                    gas.setRValue(measure);
                                }

                                if (materialProjectionFilter.containsProperty("isVentilated", module)) {
                                    boolean isVentilated = rs.getBoolean("isventilated");
                                    if (!rs.wasNull())
                                        gas.setIsVentilated(isVentilated);
                                }

                            } else if (material instanceof SolidMaterial) {
                                SolidMaterial solidMaterial = (SolidMaterial) material;

                                double conductivity = rs.getDouble("conductivity");
                                if (!rs.wasNull()) {
                                    Measure measure = new Measure(conductivity);
                                    measure.setUom(rs.getString("conductivity_uom"));
                                    solidMaterial.setConductivity(measure);
                                }

                                double density = rs.getDouble("density");
                                if (!rs.wasNull()) {
                                    Measure measure = new Measure(density);
                                    measure.setUom(rs.getString("density_uom"));
                                    solidMaterial.setDensity(measure);
                                }

                                double specificHeat = rs.getDouble("specificheat");
                                if (!rs.wasNull()) {
                                    Measure measure = new Measure(specificHeat);
                                    measure.setUom(rs.getString("specificheat_uom"));
                                    solidMaterial.setSpecificHeat(measure);
                                }

                                if (materialProjectionFilter.containsProperty("permeance", module)) {
                                    double permeance = rs.getDouble("permeance");
                                    if (!rs.wasNull()) {
                                        Measure measure = new Measure(permeance);
                                        measure.setUom(rs.getString("permeance_uom"));
                                        solidMaterial.setPermeance(measure);
                                    }
                                }
                            }

                            if (gmlId == null) {
                                gmlId = DefaultGMLIdManager.getInstance().generateUUID();
                                material.setId(gmlId);
                            }

                            if (!helper.exportAsGlobalFeature(material))
                                property.setAbstractMaterial(material);
                        }

                        if (!property.isSetAbstractMaterial())
                            property.setHref("#" + gmlId);

                        component.setMaterial(property);
                    }

                    layer.addLayerComponent(new LayerComponentProperty(component));
                }
            }

            return layers.values();
        }
    }

    @Override
    public void close() throws CityGMLExportException, SQLException {
        ps.close();
    }
}
