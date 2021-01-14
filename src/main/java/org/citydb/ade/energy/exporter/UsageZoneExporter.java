package org.citydb.ade.energy.exporter;

import org.citydb.ade.energy.schema.ADETable;
import org.citydb.ade.exporter.ADEExporter;
import org.citydb.ade.exporter.CityGMLExportHelper;
import org.citydb.citygml.exporter.CityGMLExportException;
import org.citydb.database.schema.mapping.FeatureType;
import org.citydb.query.filter.projection.CombinedProjectionFilter;
import org.citydb.query.filter.projection.ProjectionFilter;
import org.citydb.sqlbuilder.expression.PlaceHolder;
import org.citydb.sqlbuilder.schema.Table;
import org.citydb.sqlbuilder.select.Select;
import org.citydb.sqlbuilder.select.join.JoinFactory;
import org.citydb.sqlbuilder.select.operator.comparison.ComparisonFactory;
import org.citydb.sqlbuilder.select.operator.comparison.ComparisonName;
import org.citygml4j.ade.energy.model.core.FloorArea;
import org.citygml4j.ade.energy.model.core.FloorAreaProperty;
import org.citygml4j.ade.energy.model.core.FloorAreaTypeValue;
import org.citygml4j.ade.energy.model.module.EnergyADEModule;
import org.citygml4j.ade.energy.model.occupantBehaviour.Facilities;
import org.citygml4j.ade.energy.model.occupantBehaviour.FacilitiesProperty;
import org.citygml4j.ade.energy.model.occupantBehaviour.Occupants;
import org.citygml4j.ade.energy.model.occupantBehaviour.OccupantsProperty;
import org.citygml4j.ade.energy.model.occupantBehaviour.UsageZone;
import org.citygml4j.ade.energy.model.supportingClasses.AbstractScheduleProperty;
import org.citygml4j.ade.energy.model.supportingClasses.DailyPatternSchedule;
import org.citygml4j.model.gml.basicTypes.Code;
import org.citygml4j.model.gml.measures.Area;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class UsageZoneExporter implements ADEExporter {
    private final CityGMLExportHelper helper;
    private final int objectClassId;

    private PreparedStatement ps;
    private DailyPatternScheduleExporter dailyPatternScheduleExporter;
    private FacilitiesExporter facilitiesExporter;
    private OccupantsExporter occupantsExporter;
    private String module;

    public UsageZoneExporter(Connection connection, CityGMLExportHelper helper, ExportManager manager) throws CityGMLExportException, SQLException {
        this.helper = helper;
        objectClassId = manager.getObjectMapper().getObjectClassId(UsageZone.class);

        String tableName = manager.getSchemaMapper().getTableName(ADETable.USAGEZONE);
        CombinedProjectionFilter projectionFilter = helper.getCombinedProjectionFilter(tableName);
        module = EnergyADEModule.v1_0.getNamespaceURI();

        Table table = new Table(helper.getTableNameWithSchema(tableName));
        Select select = new Select().addProjection(table.getColumn("id"), table.getColumn("usagezonetype"), table.getColumn("usagezonetype_codespace"));
        if (projectionFilter.containsProperty("coolingSchedule", module))
            select.addProjection(table.getColumn("coolingschedule_id"));
        if (projectionFilter.containsProperty("heatingSchedule", module))
            select.addProjection(table.getColumn("heatingschedule_id"));
        if (projectionFilter.containsProperty("ventilationSchedule", module))
            select.addProjection(table.getColumn("ventilationschedule_id"));
        if (projectionFilter.containsProperty("floorArea", module)) {
            Table floorArea = new Table(helper.getTableNameWithSchema(manager.getSchemaMapper().getTableName(ADETable.FLOORAREA)));
            select.addJoin(JoinFactory.left(floorArea, "usagezone_floorarea_id", ComparisonName.EQUAL_TO, table.getColumn("id")))
                    .addProjection(floorArea.getColumn("type"), floorArea.getColumn("value"), floorArea.getColumn("value_uom"));
        }
        select.addSelection(ComparisonFactory.equalTo(table.getColumn("building_usagezone_id"), new PlaceHolder<>()));
        ps = connection.prepareStatement(select.toString());

        dailyPatternScheduleExporter = manager.getExporter(DailyPatternScheduleExporter.class);
        facilitiesExporter = manager.getExporter(FacilitiesExporter.class);
        occupantsExporter = manager.getExporter(OccupantsExporter.class);
    }

    public Collection<UsageZone> doExport(long parentId) throws CityGMLExportException, SQLException {
        ps.setLong(1, parentId);

        try (ResultSet rs = ps.executeQuery()) {
            long currentUsageZoneId = 0;
            UsageZone usageZone = null;
            ProjectionFilter projectionFilter = null;
            Map<Long, UsageZone> usageZones = new HashMap<>();

            while (rs.next()) {
                long usageZoneId = rs.getLong("id");

                if (usageZoneId != currentUsageZoneId || usageZone == null) {
                    currentUsageZoneId = usageZoneId;
                    usageZone = usageZones.get(usageZoneId);

                    if (usageZone == null) {
                        usageZone = helper.createObject(usageZoneId, objectClassId, UsageZone.class);
                        if (usageZone == null) {
                            helper.logOrThrowErrorMessage("Failed to instantiate " + helper.getObjectSignature(objectClassId, usageZoneId) + " as usage zone object.");
                            continue;
                        }

                        FeatureType featureType = helper.getFeatureType(objectClassId);
                        projectionFilter = helper.getProjectionFilter(featureType);

                        String usageZoneType = rs.getString("usagezonetype");
                        if (usageZoneType != null) {
                            Code code = new Code(usageZoneType);
                            code.setCodeSpace(rs.getString("usagezonetype_codespace"));
                            usageZone.setUsageZoneType(code);
                        }

                        if (projectionFilter.containsProperty("coolingSchedule", module)) {
                            long scheduleId = rs.getLong("coolingschedule_id");
                            if (!rs.wasNull()) {
                                DailyPatternSchedule schedule = dailyPatternScheduleExporter.doExport(scheduleId);
                                if (schedule != null)
                                    usageZone.setCoolingSchedule(new AbstractScheduleProperty(schedule));
                            }
                        }

                        if (projectionFilter.containsProperty("heatingSchedule", module)) {
                            long scheduleId = rs.getLong("heatingschedule_id");
                            if (!rs.wasNull()) {
                                DailyPatternSchedule schedule = dailyPatternScheduleExporter.doExport(scheduleId);
                                if (schedule != null)
                                    usageZone.setHeatingSchedule(new AbstractScheduleProperty(schedule));
                            }
                        }

                        if (projectionFilter.containsProperty("ventilationSchedule", module)) {
                            long scheduleId = rs.getLong("ventilationschedule_id");
                            if (!rs.wasNull()) {
                                DailyPatternSchedule schedule = dailyPatternScheduleExporter.doExport(scheduleId);
                                if (schedule != null)
                                    usageZone.setVentilationSchedule(new AbstractScheduleProperty(schedule));
                            }
                        }

                        if (projectionFilter.containsProperty("equippedWith", module)) {
                            for (Facilities facilities : facilitiesExporter.doExport(usageZoneId))
                                usageZone.addEquippedWith(new FacilitiesProperty(facilities));
                        }

                        if (projectionFilter.containsProperty("occupiedBy", module)) {
                            for (Occupants occupants : occupantsExporter.doExport(usageZoneId))
                                usageZone.addOccupiedBy(new OccupantsProperty(occupants));
                        }

                        usageZone.setLocalProperty("projection", projectionFilter);
                        usageZones.put(usageZoneId, usageZone);
                    } else
                        projectionFilter = (ProjectionFilter) usageZone.getLocalProperty("projection");
                }

                if (projectionFilter.containsProperty("floorArea", module)) {
                    FloorAreaTypeValue type = FloorAreaTypeValue.fromValue(rs.getString("type"));
                    double value = rs.getDouble("value");

                    if (!rs.wasNull() && type != null) {
                        FloorArea floorArea = new FloorArea();
                        floorArea.setType(type);

                        Area area = new Area(value);
                        area.setUom(rs.getString("value_uom"));
                        floorArea.setValue(area);

                        usageZone.addFloorArea(new FloorAreaProperty(floorArea));
                    }
                }
            }

            return usageZones.values();
        }
    }

    @Override
    public void close() throws CityGMLExportException, SQLException {
        ps.close();
    }
}
