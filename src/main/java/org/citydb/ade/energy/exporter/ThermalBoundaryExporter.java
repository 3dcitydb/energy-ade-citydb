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
import org.citydb.core.operation.exporter.database.content.SurfaceGeometryExporter;
import org.citydb.core.query.filter.projection.CombinedProjectionFilter;
import org.citydb.core.query.filter.projection.ProjectionFilter;
import org.citydb.sqlbuilder.expression.PlaceHolder;
import org.citydb.sqlbuilder.schema.Table;
import org.citydb.sqlbuilder.select.Select;
import org.citydb.sqlbuilder.select.join.JoinFactory;
import org.citydb.sqlbuilder.select.operator.comparison.ComparisonFactory;
import org.citydb.sqlbuilder.select.operator.comparison.ComparisonName;
import org.citygml4j.ade.energy.model.buildingPhysics.*;
import org.citygml4j.ade.energy.model.core.AbstractConstruction;
import org.citygml4j.ade.energy.model.core.AbstractConstructionProperty;
import org.citygml4j.ade.energy.model.module.EnergyADEModule;
import org.citygml4j.model.gml.measures.Angle;
import org.citygml4j.model.gml.measures.Area;
import org.citygml4j.util.gmlid.DefaultGMLIdManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class ThermalBoundaryExporter implements ADEExporter {
    private final CityGMLExportHelper helper;
    private final int objectClassId;
    private final int openingOjectClassId;

    private PreparedStatement ps;
    private ConstructionExporter constructionExporter;
    private SurfaceGeometryExporter surfaceGeometryExporter;
    private String module;

    public ThermalBoundaryExporter(Connection connection, CityGMLExportHelper helper, ExportManager manager) throws CityGMLExportException, SQLException {
        this.helper = helper;
        objectClassId = manager.getObjectMapper().getObjectClassId(ThermalBoundary.class);
        openingOjectClassId = manager.getObjectMapper().getObjectClassId(ThermalOpening.class);

        String tableName = manager.getSchemaMapper().getTableName(ADETable.THERMALBOUNDARY);
        CombinedProjectionFilter boundaryProjectionFilter = helper.getCombinedProjectionFilter(tableName);
        CombinedProjectionFilter openingProjectionFilter = helper.getCombinedProjectionFilter(manager.getSchemaMapper().getTableName(ADETable.THERMALOPENING));
        module = EnergyADEModule.v1_0.getNamespaceURI();

        Table table = new Table(helper.getTableNameWithSchema(tableName));
        Table delimits = new Table(helper.getTableNameWithSchema(manager.getSchemaMapper().getTableName(ADETable.THER_BOUN_TO_THER_DELI)));
        Table cityObject = new Table(helper.getTableNameWithSchema(MappingConstants.CITYOBJECT));

        Select select = new Select().addProjection(table.getColumn("id"), table.getColumn("thermalboundarytype"),
                        table.getColumn("surfacegeometry_id"), table.getColumn("construction_id"), cityObject.getColumn("gmlid"))
                .addJoin(JoinFactory.left(delimits, "thermalboundary_delimits_id", ComparisonName.EQUAL_TO, table.getColumn("id")))
                .addJoin(JoinFactory.left(cityObject, "id", ComparisonName.EQUAL_TO, delimits.getColumn("thermalzone_boundedby_id")));
        if (boundaryProjectionFilter.containsProperty("azimuth", module))
            select.addProjection(table.getColumn("azimuth"), table.getColumn("azimuth_uom"));
        if (boundaryProjectionFilter.containsProperty("inclination", module))
            select.addProjection(table.getColumn("inclination"), table.getColumn("inclination_uom"));
        if (boundaryProjectionFilter.containsProperty("area", module))
            select.addProjection(table.getColumn("area"), table.getColumn("area_uom"));
        if (boundaryProjectionFilter.containsProperty("contains", module)) {
            Table opening = new Table(helper.getTableNameWithSchema(manager.getSchemaMapper().getTableName(ADETable.THERMALOPENING)));
            select.addJoin(JoinFactory.left(opening, "thermalboundary_contains_id", ComparisonName.EQUAL_TO, table.getColumn("id")))
                    .addProjection(opening.getColumn("id", "opening_id"), opening.getColumn("surfacegeometry_id", "op_surfacegeometry_id"),
                            opening.getColumn("construction_id", "op_construction_id"));
            if (openingProjectionFilter.containsProperty("area", module))
                select.addProjection(opening.getColumn("area", "op_area"), opening.getColumn("area_uom", "op_area_uom"));
        }
        select.addSelection(ComparisonFactory.equalTo(table.getColumn("thermalzone_boundedby_id"), new PlaceHolder<>()));
        ps = connection.prepareStatement(select.toString());

        constructionExporter = manager.getExporter(ConstructionExporter.class);
        surfaceGeometryExporter = helper.getSurfaceGeometryExporter();
    }

    public Collection<ThermalBoundary> doExport(long parentId) throws CityGMLExportException, SQLException {
        ps.setLong(1, parentId);

        try (ResultSet rs = ps.executeQuery()) {
            long currentThermalBoundaryId = 0;
            ThermalBoundary thermalBoundary = null;
            ProjectionFilter boundaryProjectionFilter = null;
            Map<Long, ThermalBoundary> thermalBoundaries = new HashMap<>();
            Map<Long, Set<Long>> openingIds = new HashMap<>();
            Map<Long, Set<String>> thermalZones = new HashMap<>();

            while (rs.next()) {
                long thermalBoundaryId = rs.getLong("id");

                if (thermalBoundaryId != currentThermalBoundaryId || thermalBoundary == null) {
                    currentThermalBoundaryId = thermalBoundaryId;
                    thermalBoundary = thermalBoundaries.get(thermalBoundaryId);

                    if (thermalBoundary == null) {
                        thermalBoundary = helper.createObject(thermalBoundaryId, objectClassId, ThermalBoundary.class);
                        if (thermalBoundary == null) {
                            helper.logOrThrowErrorMessage("Failed to instantiate " + helper.getObjectSignature(objectClassId, thermalBoundaryId) + " as thermal boundary object.");
                            continue;
                        }

                        FeatureType featureType = helper.getFeatureType(objectClassId);
                        boundaryProjectionFilter = helper.getProjectionFilter(featureType);

                        thermalBoundary.setThermalBoundaryType(ThermalBoundaryTypeValue.fromValue(rs.getString("thermalboundarytype")));

                        long constructionId = rs.getLong("construction_id");
                        if (!rs.wasNull()) {
                            AbstractConstructionProperty property = constructionExporter.doExport(constructionId);
                            if (property != null) {
                                if (property.isSetAbstractConstruction()) {
                                    AbstractConstruction construction = property.getAbstractConstruction();
                                    String gmlId = construction.getId();
                                    if (gmlId == null) {
                                        gmlId = DefaultGMLIdManager.getInstance().generateUUID();
                                        construction.setId(gmlId);
                                    }

                                    if (helper.exportAsGlobalFeature(property.getAbstractConstruction())) {
                                        property.unsetAbstractConstruction();
                                        property.setHref("#" + gmlId);
                                    }
                                }

                                thermalBoundary.setConstruction(property);
                            }
                        }

                        long surfaceGeometryId = rs.getLong("surfacegeometry_id");
                        if (!rs.wasNull())
                            surfaceGeometryExporter.addBatch(surfaceGeometryId, thermalBoundary::setSurfaceGeometry);

                        if (boundaryProjectionFilter.containsProperty("azimuth", module)) {
                            double azimuth = rs.getDouble("azimuth");
                            if (!rs.wasNull()) {
                                Angle angle = new Angle(azimuth);
                                angle.setUom(rs.getString("azimuth_uom"));
                                thermalBoundary.setAzimuth(angle);
                            }
                        }

                        if (boundaryProjectionFilter.containsProperty("inclination", module)) {
                            double inclination = rs.getDouble("inclination");
                            if (!rs.wasNull()) {
                                Angle angle = new Angle(inclination);
                                angle.setUom(rs.getString("inclination_uom"));
                                thermalBoundary.setInclination(angle);
                            }
                        }

                        if (boundaryProjectionFilter.containsProperty("area", module)) {
                            double value = rs.getDouble("area");
                            if (!rs.wasNull()) {
                                Area area = new Area(value);
                                area.setUom(rs.getString("area_uom"));
                                thermalBoundary.setArea(area);
                            }
                        }

                        thermalBoundary.setLocalProperty("projection", boundaryProjectionFilter);
                        thermalBoundaries.put(thermalBoundaryId, thermalBoundary);
                    } else
                        boundaryProjectionFilter = (ProjectionFilter) thermalBoundary.getLocalProperty("projection");
                }

                if (boundaryProjectionFilter.containsProperty("contains", module)) {
                    long openingId = rs.getLong("opening_id");
                    if (!rs.wasNull() && openingIds.computeIfAbsent(thermalBoundaryId, v -> new HashSet<>()).add(openingId)) {
                        ThermalOpening thermalOpening = helper.createObject(openingId, openingOjectClassId, ThermalOpening.class);
                        if (thermalOpening == null) {
                            helper.logOrThrowErrorMessage("Failed to instantiate " + helper.getObjectSignature(openingOjectClassId, openingId) + " as thermal opening object.");
                            continue;
                        }

                        FeatureType openingType = helper.getFeatureType(objectClassId);
                        ProjectionFilter openingProjectionFilter = helper.getProjectionFilter(openingType);

                        long constructionId = rs.getLong("op_construction_id");
                        if (!rs.wasNull()) {
                            AbstractConstructionProperty property = constructionExporter.doExport(constructionId);
                            if (property != null) {
                                if (property.isSetAbstractConstruction()) {
                                    AbstractConstruction construction = property.getAbstractConstruction();
                                    String gmlId = construction.getId();
                                    if (gmlId == null) {
                                        gmlId = DefaultGMLIdManager.getInstance().generateUUID();
                                        construction.setId(gmlId);
                                    }

                                    if (helper.exportAsGlobalFeature(property.getAbstractConstruction())) {
                                        property.unsetAbstractConstruction();
                                        property.setHref("#" + gmlId);
                                    }
                                }

                                thermalOpening.setConstruction(property);
                            }
                        }

                        long surfaceGeometryId = rs.getLong("op_surfacegeometry_id");
                        if (!rs.wasNull())
                            surfaceGeometryExporter.addBatch(surfaceGeometryId, thermalOpening::setSurfaceGeometry);

                        if (openingProjectionFilter.containsProperty("area", module)) {
                            double value = rs.getDouble("op_area");
                            if (!rs.wasNull()) {
                                Area area = new Area(value);
                                area.setUom(rs.getString("op_area_uom"));
                                thermalOpening.setArea(area);
                            }
                        }

                        thermalBoundary.addContains(new ThermalOpeningProperty(thermalOpening));
                    }
                }

                if (boundaryProjectionFilter.containsProperty("delimits", module)) {
                    String delimits = rs.getString("gmlid");
                    if (delimits != null && thermalZones.computeIfAbsent(thermalBoundaryId, v -> new HashSet<>()).add(delimits))
                        thermalBoundary.addDelimits(new ThermalZoneProperty("#" + delimits));
                }
            }

            return thermalBoundaries.values();
        }
    }

    @Override
    public void close() throws CityGMLExportException, SQLException {
        ps.close();
    }
}
