package org.citydb.ade.energy.exporter;

import org.citydb.ade.exporter.ADEExporter;
import org.citydb.ade.exporter.CityGMLExportHelper;
import org.citydb.citygml.exporter.CityGMLExportException;
import org.citydb.citygml.exporter.database.content.SurfaceGeometry;
import org.citydb.database.schema.mapping.FeatureType;
import org.citydb.database.schema.mapping.MappingConstants;
import org.citydb.query.filter.projection.CombinedProjectionFilter;
import org.citydb.query.filter.projection.ProjectionFilter;
import org.citydb.sqlbuilder.expression.PlaceHolder;
import org.citydb.sqlbuilder.schema.Table;
import org.citydb.sqlbuilder.select.Select;
import org.citydb.sqlbuilder.select.join.JoinFactory;
import org.citydb.sqlbuilder.select.operator.comparison.ComparisonFactory;
import org.citydb.sqlbuilder.select.operator.comparison.ComparisonName;
import org.citygml4j.ade.energy.model.buildingPhysics.ThermalBoundary;
import org.citygml4j.ade.energy.model.buildingPhysics.ThermalBoundaryTypeValue;
import org.citygml4j.ade.energy.model.buildingPhysics.ThermalOpening;
import org.citygml4j.ade.energy.model.buildingPhysics.ThermalOpeningProperty;
import org.citygml4j.ade.energy.model.buildingPhysics.ThermalZoneProperty;
import org.citygml4j.ade.energy.model.core.AbstractConstruction;
import org.citygml4j.ade.energy.model.core.AbstractConstructionProperty;
import org.citygml4j.ade.energy.model.module.EnergyADEModule;
import org.citygml4j.model.gml.GMLClass;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurface;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurfaceProperty;
import org.citygml4j.model.gml.measures.Angle;
import org.citygml4j.model.gml.measures.Area;
import org.citygml4j.util.gmlid.DefaultGMLIdManager;
import org.citydb.ade.energy.schema.ADETable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ThermalBoundaryExporter implements ADEExporter {
    private final CityGMLExportHelper helper;
    private final int objectClassId;
    private final int openingOjectClassId;

    private PreparedStatement ps;
    private ConstructionExporter constructionExporter;
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
                        if (!rs.wasNull()) {
                            SurfaceGeometry geometry = helper.exportSurfaceGeometry(surfaceGeometryId);
                            if (geometry != null && geometry.getType() == GMLClass.MULTI_SURFACE) {
                                MultiSurfaceProperty multiSurfaceProperty = new MultiSurfaceProperty();
                                if (geometry.isSetGeometry())
                                    multiSurfaceProperty.setMultiSurface((MultiSurface) geometry.getGeometry());
                                else
                                    multiSurfaceProperty.setHref(geometry.getReference());

                                thermalBoundary.setSurfaceGeometry(multiSurfaceProperty);
                            }
                        }

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
                        if (!rs.wasNull()) {
                            SurfaceGeometry geometry = helper.exportSurfaceGeometry(surfaceGeometryId);
                            if (geometry != null && geometry.getType() == GMLClass.MULTI_SURFACE) {
                                MultiSurfaceProperty multiSurfaceProperty = new MultiSurfaceProperty();
                                if (geometry.isSetGeometry())
                                    multiSurfaceProperty.setMultiSurface((MultiSurface) geometry.getGeometry());
                                else
                                    multiSurfaceProperty.setHref(geometry.getReference());

                                thermalOpening.setSurfaceGeometry(multiSurfaceProperty);
                            }
                        }

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
