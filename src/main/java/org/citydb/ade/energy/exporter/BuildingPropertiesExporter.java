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
import org.citydb.config.geometry.GeometryObject;
import org.citydb.core.ade.exporter.ADEExporter;
import org.citydb.core.ade.exporter.CityGMLExportHelper;
import org.citydb.core.database.schema.mapping.FeatureType;
import org.citydb.core.operation.exporter.CityGMLExportException;
import org.citydb.core.operation.exporter.database.content.GMLConverter;
import org.citydb.core.query.filter.projection.CombinedProjectionFilter;
import org.citydb.core.query.filter.projection.ProjectionFilter;
import org.citydb.sqlbuilder.expression.PlaceHolder;
import org.citydb.sqlbuilder.schema.Table;
import org.citydb.sqlbuilder.select.Select;
import org.citydb.sqlbuilder.select.operator.comparison.ComparisonFactory;
import org.citygml4j.ade.energy.model.buildingPhysics.ThermalZone;
import org.citygml4j.ade.energy.model.core.*;
import org.citygml4j.ade.energy.model.module.EnergyADEModule;
import org.citygml4j.ade.energy.model.occupantBehaviour.UsageZone;
import org.citygml4j.model.citygml.building.AbstractBuilding;
import org.citygml4j.model.gml.basicTypes.Code;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BuildingPropertiesExporter implements ADEExporter {
    private final CityGMLExportHelper helper;

    private PreparedStatement ps;
    private HeightAboveGroundExporter heightAboveGroundExporter;
    private FloorAreaExporter floorAreaExporter;
    private VolumeTypeExporter volumeTypeExporter;
    private UsageZoneExporter usageZoneExporter;
    private ThermalZoneExporter thermalZoneExporter;
    private GMLConverter gmlConverter;
    private String module;

    public BuildingPropertiesExporter(Connection connection, CityGMLExportHelper helper, ExportManager manager) throws CityGMLExportException, SQLException {
        this.helper = helper;

        String tableName = manager.getSchemaMapper().getTableName(ADETable.BUILDING);
        CombinedProjectionFilter projectionFilter = helper.getCombinedProjectionFilter(tableName);
        module = EnergyADEModule.v1_0.getNamespaceURI();

        Table table = new Table(helper.getTableNameWithSchema(tableName));
        Select select = new Select().addProjection(table.getColumn("id"));
        if (projectionFilter.containsProperty("constructionWeight", module))
            select.addProjection(table.getColumn("constructionweight"));
        if (projectionFilter.containsProperty("buildingType", module))
            select.addProjection(table.getColumn("buildingtype"), table.getColumn("buildingtype_codespace"));
        if (projectionFilter.containsProperty("referencePoint", module))
            select.addProjection(table.getColumn("referencepoint"));
        select.addSelection(ComparisonFactory.equalTo(table.getColumn("id"), new PlaceHolder<>()));
        ps = connection.prepareStatement(select.toString());

        heightAboveGroundExporter = manager.getExporter(HeightAboveGroundExporter.class);
        floorAreaExporter = manager.getExporter(FloorAreaExporter.class);
        volumeTypeExporter = manager.getExporter(VolumeTypeExporter.class);
        usageZoneExporter = manager.getExporter(UsageZoneExporter.class);
        thermalZoneExporter = manager.getExporter(ThermalZoneExporter.class);
        gmlConverter = helper.getGMLConverter();
    }

    public void doExport(AbstractBuilding parent, long parentId, FeatureType parentType, ProjectionFilter projectionFilter) throws CityGMLExportException, SQLException {
        ps.setLong(1, parentId);

        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                if (projectionFilter.containsProperty("constructionWeight", module)) {
                    String value = rs.getString("constructionweight");
                    if (!rs.wasNull()) {
                        ConstructionWeightValue constructionWeight = ConstructionWeightValue.fromValue(value);
                        if (constructionWeight != null) {
                            ConstructionWeightProperty property = new ConstructionWeightProperty(constructionWeight);
                            parent.addGenericApplicationPropertyOfAbstractBuilding(property);
                        }
                    }
                }

                if (projectionFilter.containsProperty("buildingType", module)) {
                    String value = rs.getString("buildingtype");
                    if (!rs.wasNull()) {
                        Code buildingType = new Code(value);
                        buildingType.setCodeSpace(rs.getString("buildingtype_codespace"));
                        BuildingTypeProperty property = new BuildingTypeProperty(buildingType);
                        parent.addGenericApplicationPropertyOfAbstractBuilding(property);
                    }
                }

                if (projectionFilter.containsProperty("referencePoint", module)) {
                    Object pointObj = rs.getObject("referencepoint");
                    if (pointObj != null) {
                        GeometryObject point = helper.getDatabaseAdapter().getGeometryConverter().getPoint(pointObj);
                        if (point != null) {
                            ReferencePointProperty property = new ReferencePointProperty(gmlConverter.getPointProperty(point, false));
                            parent.addGenericApplicationPropertyOfAbstractBuilding(property);
                        }
                    }
                }

                if (projectionFilter.containsProperty("heightAboveGround", module)) {
                    for (HeightAboveGround heightAboveGround : heightAboveGroundExporter.doExport(parentId)) {
                        HeightAboveGroundPropertyElement property = new HeightAboveGroundPropertyElement(new HeightAboveGroundProperty(heightAboveGround));
                        parent.addGenericApplicationPropertyOfAbstractBuilding(property);
                    }
                }

                if (projectionFilter.containsProperty("floorArea", module)) {
                    for (FloorArea floorArea : floorAreaExporter.doExport(parent, parentId)) {
                        FloorAreaPropertyElement property = new FloorAreaPropertyElement(new FloorAreaProperty(floorArea));
                        parent.addGenericApplicationPropertyOfAbstractBuilding(property);
                    }
                }

                if (projectionFilter.containsProperty("volume", module)) {
                    for (VolumeType volumeType : volumeTypeExporter.doExport(parent, parentId)) {
                        VolumeTypePropertyElement property = new VolumeTypePropertyElement(new VolumeTypeProperty(volumeType));
                        parent.addGenericApplicationPropertyOfAbstractBuilding(property);
                    }
                }

                if (projectionFilter.containsProperty("usageZone", module)) {
                    for (UsageZone usageZone : usageZoneExporter.doExport(parentId)) {
                        UsageZoneProperty property = new UsageZoneProperty(new AbstractUsageZoneProperty(usageZone));
                        parent.addGenericApplicationPropertyOfAbstractBuilding(property);
                    }
                }

                if (projectionFilter.containsProperty("thermalZone", module)) {
                    for (ThermalZone thermalZone : thermalZoneExporter.doExport(parentId)) {
                        ThermalZonePropertyElement property = new ThermalZonePropertyElement(new AbstractThermalZoneProperty(thermalZone));
                        parent.addGenericApplicationPropertyOfAbstractBuilding(property);
                    }
                }
            }
        }
    }

    @Override
    public void close() throws CityGMLExportException, SQLException {
        ps.close();
    }
}
