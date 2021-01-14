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
import org.citygml4j.ade.energy.model.buildingPhysics.ThermalBoundaryProperty;
import org.citygml4j.ade.energy.model.buildingPhysics.ThermalZone;
import org.citygml4j.ade.energy.model.core.AbstractUsageZoneProperty;
import org.citygml4j.ade.energy.model.core.FloorArea;
import org.citygml4j.ade.energy.model.core.FloorAreaProperty;
import org.citygml4j.ade.energy.model.core.FloorAreaTypeValue;
import org.citygml4j.ade.energy.model.core.VolumeType;
import org.citygml4j.ade.energy.model.core.VolumeTypeProperty;
import org.citygml4j.ade.energy.model.module.EnergyADEModule;
import org.citygml4j.model.gml.GMLClass;
import org.citygml4j.model.gml.basicTypes.Measure;
import org.citygml4j.model.gml.geometry.primitives.AbstractSolid;
import org.citygml4j.model.gml.geometry.primitives.SolidProperty;
import org.citygml4j.model.gml.measures.Area;
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

public class ThermalZoneExporter implements ADEExporter {
    private final CityGMLExportHelper helper;
    private final int objectClassId;

    private PreparedStatement ps;
    private VolumeTypeExporter volumeTypeExporter;
    private ThermalBoundaryExporter thermalBoundaryExporter;
    private String module;

    public ThermalZoneExporter(Connection connection, CityGMLExportHelper helper, ExportManager manager) throws CityGMLExportException, SQLException {
        this.helper = helper;
        objectClassId = manager.getObjectMapper().getObjectClassId(ThermalZone.class);

        String tableName = manager.getSchemaMapper().getTableName(ADETable.THERMALZONE);
        CombinedProjectionFilter projectionFilter = helper.getCombinedProjectionFilter(tableName);
        module = EnergyADEModule.v1_0.getNamespaceURI();

        Table table = new Table(helper.getTableNameWithSchema(tableName));
        Table floorArea = new Table(helper.getTableNameWithSchema(manager.getSchemaMapper().getTableName(ADETable.FLOORAREA)));

        Select select = new Select().addProjection(table.getColumn("id"), table.getColumn("iscooled"), table.getColumn("isheated"),
                floorArea.getColumn("id", "floorarea_id"), floorArea.getColumn("type"), floorArea.getColumn("value"), floorArea.getColumn("value_uom"))
                .addJoin(JoinFactory.left(floorArea, "thermalzone_floorarea_id", ComparisonName.EQUAL_TO, table.getColumn("id")));
        if (projectionFilter.containsProperty("infiltrationRate", module))
            select.addProjection(table.getColumn("infiltrationrate"), table.getColumn("infiltrationrate_uom"));
        if (projectionFilter.containsProperty("volumeGeometry", module))
            select.addProjection(table.getColumn("volumegeometry_id"));
        if (projectionFilter.containsProperty("contains", module)) {
            Table usageZone = new Table(helper.getTableNameWithSchema(manager.getSchemaMapper().getTableName(ADETable.USAGEZONE)));
            Table cityObject = new Table(helper.getTableNameWithSchema(MappingConstants.CITYOBJECT));
            select.addJoin(JoinFactory.left(usageZone, "thermalzone_contains_id", ComparisonName.EQUAL_TO, table.getColumn("id")))
                    .addJoin(JoinFactory.left(cityObject, "id", ComparisonName.EQUAL_TO, usageZone.getColumn("id")))
                    .addProjection(cityObject.getColumn("gmlid"));
        }
        select.addSelection(ComparisonFactory.equalTo(table.getColumn("building_thermalzone_id"), new PlaceHolder<>()));
        ps = connection.prepareStatement(select.toString());

        volumeTypeExporter = manager.getExporter(VolumeTypeExporter.class);
        thermalBoundaryExporter = manager.getExporter(ThermalBoundaryExporter.class);
    }

    public Collection<ThermalZone> doExport(long parentId) throws CityGMLExportException, SQLException {
        ps.setLong(1, parentId);

        try (ResultSet rs = ps.executeQuery()) {
            long currentThermalZoneId = 0;
            ThermalZone thermalZone = null;
            ProjectionFilter projectionFilter = null;
            Map<Long, ThermalZone> thermalZones = new HashMap<>();
            Map<Long, Set<Long>> floorAreaIds = new HashMap<>();
            Map<Long, Set<String>> usageZones = new HashMap<>();

            while (rs.next()) {
                long thermalZoneId = rs.getLong("id");

                if (thermalZoneId != currentThermalZoneId || thermalZone == null) {
                    currentThermalZoneId = thermalZoneId;
                    thermalZone = thermalZones.get(thermalZoneId);

                    if (thermalZone == null) {
                        thermalZone = helper.createObject(thermalZoneId, objectClassId, ThermalZone.class);
                        if (thermalZone == null) {
                            helper.logOrThrowErrorMessage("Failed to instantiate " + helper.getObjectSignature(objectClassId, thermalZoneId) + " as thermal zone object.");
                            continue;
                        }

                        FeatureType featureType = helper.getFeatureType(objectClassId);
                        projectionFilter = helper.getProjectionFilter(featureType);

                        thermalZone.setIsCooled(rs.getBoolean("iscooled"));
                        thermalZone.setIsHeated(rs.getBoolean("isheated"));

                        for (VolumeType volumeType : volumeTypeExporter.doExport(thermalZone, thermalZoneId))
                            thermalZone.addVolume(new VolumeTypeProperty(volumeType));

                        if (projectionFilter.containsProperty("infiltrationRate", module)) {
                            double infiltRationrate = rs.getDouble("infiltrationrate");
                            if (!rs.wasNull()) {
                                Measure measure = new Measure(infiltRationrate);
                                measure.setUom(rs.getString("infiltrationrate_uom"));
                                thermalZone.setInfiltrationRate(measure);
                            }
                        }

                        if (projectionFilter.containsProperty("volumeGeometry", module)) {
                            for (ThermalBoundary thermalBoundary : thermalBoundaryExporter.doExport(thermalZoneId))
                                thermalZone.addThermalBoundary(new ThermalBoundaryProperty(thermalBoundary));
                        }

                        if (projectionFilter.containsProperty("volumeGeometry", module)) {
                            long volumeGeometryId = rs.getLong("volumegeometry_id");
                            if (!rs.wasNull()) {
                                SurfaceGeometry geometry = helper.exportSurfaceGeometry(volumeGeometryId);
                                if (geometry != null && (geometry.getType() == GMLClass.SOLID || geometry.getType() == GMLClass.COMPOSITE_SOLID)) {
                                    SolidProperty solidProperty = new SolidProperty();
                                    if (geometry.isSetGeometry())
                                        solidProperty.setSolid((AbstractSolid) geometry.getGeometry());
                                    else
                                        solidProperty.setHref(geometry.getReference());

                                    thermalZone.setVolumeGeometry(solidProperty);
                                }
                            }
                        }

                        thermalZone.setLocalProperty("projection", projectionFilter);
                        thermalZones.put(thermalZoneId, thermalZone);
                    } else
                        projectionFilter = (ProjectionFilter) thermalZone.getLocalProperty("projection");
                }

                long floorAreaId = rs.getLong("floorarea_id");
                if (!rs.wasNull() && floorAreaIds.computeIfAbsent(thermalZoneId, v -> new HashSet<>()).add(floorAreaId)) {
                    FloorAreaTypeValue type = FloorAreaTypeValue.fromValue(rs.getString("type"));
                    double value = rs.getDouble("value");
                    if (!rs.wasNull() && type != null) {
                        FloorArea floorArea = new FloorArea();
                        floorArea.setType(type);

                        Area area = new Area(value);
                        area.setUom(rs.getString("value_uom"));
                        floorArea.setValue(area);

                        thermalZone.addFloorArea(new FloorAreaProperty(floorArea));
                    }
                }

                if (projectionFilter.containsProperty("contains", module)) {
                    String contains = rs.getString("gmlid");
                    if (contains != null && usageZones.computeIfAbsent(thermalZoneId, v -> new HashSet<>()).add(contains))
                        thermalZone.addContains(new AbstractUsageZoneProperty("#" + contains));
                }
            }

            return thermalZones.values();
        }
    }

    @Override
    public void close() throws CityGMLExportException, SQLException {
        ps.close();
    }
}
