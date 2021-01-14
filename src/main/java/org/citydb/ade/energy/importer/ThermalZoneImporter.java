package org.citydb.ade.energy.importer;

import org.citydb.ade.energy.schema.ADETable;
import org.citydb.ade.energy.schema.SchemaMapper;
import org.citydb.ade.importer.ADEImporter;
import org.citydb.ade.importer.CityGMLImportHelper;
import org.citydb.ade.importer.ForeignKeys;
import org.citydb.citygml.importer.CityGMLImportException;
import org.citydb.database.schema.mapping.AbstractObjectType;
import org.citygml4j.ade.energy.model.buildingPhysics.ThermalBoundary;
import org.citygml4j.ade.energy.model.buildingPhysics.ThermalBoundaryProperty;
import org.citygml4j.ade.energy.model.buildingPhysics.ThermalZone;
import org.citygml4j.ade.energy.model.core.AbstractUsageZone;
import org.citygml4j.ade.energy.model.core.AbstractUsageZoneProperty;
import org.citygml4j.ade.energy.model.core.FloorArea;
import org.citygml4j.ade.energy.model.core.FloorAreaProperty;
import org.citygml4j.ade.energy.model.core.VolumeType;
import org.citygml4j.ade.energy.model.core.VolumeTypeProperty;
import org.citygml4j.model.gml.geometry.primitives.SolidProperty;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

public class ThermalZoneImporter implements ADEImporter {
    private final CityGMLImportHelper helper;
    private final SchemaMapper schemaMapper;

    private FloorAreaImporter floorAreaImporter;
    private VolumeTypeImporter volumeTypeImporter;
    private PreparedStatement ps;
    private int batchCounter;

    public ThermalZoneImporter(Connection connection, CityGMLImportHelper helper, ImportManager manager) throws CityGMLImportException, SQLException {
        this.helper = helper;
        this.schemaMapper = manager.getSchemaMapper();

        ps = connection.prepareStatement("insert into " +
                helper.getTableNameWithSchema(manager.getSchemaMapper().getTableName(ADETable.THERMALZONE)) + " " +
                "(id, building_thermalzone_id, iscooled, isheated, volumegeometry_id, infiltrationrate, infiltrationrate_uom) " +
                "values (?, ?, ?, ?, ?, ?, ?)");

        floorAreaImporter = manager.getImporter(FloorAreaImporter.class);
        volumeTypeImporter = manager.getImporter(VolumeTypeImporter.class);
    }

    public void doImport(ThermalZone thermalZone, long objectId, AbstractObjectType<?> objectType, ForeignKeys foreignKeys) throws CityGMLImportException, SQLException {
        long parentId = foreignKeys.get("buildingId");

        ps.setLong(1, objectId);

        if (parentId != 0)
            ps.setLong(2, parentId);
        else
            ps.setNull(2, Types.NULL);

        ps.setInt(3, thermalZone.isCooled() ? 1 : 0);
        ps.setInt(4, thermalZone.isHeated() ? 1 : 0);

        long volumeGeometryId = 0;
        if (thermalZone.isSetVolumeGeometry()) {
            SolidProperty solidProperty = thermalZone.getVolumeGeometry();

            if (solidProperty != null) {
                if (solidProperty.isSetSolid()) {
                    volumeGeometryId = helper.importSurfaceGeometry(solidProperty.getSolid(), objectId);
                    solidProperty.unsetSolid();
                } else {
                    String href = solidProperty.getHref();
                    if (href != null && href.length() != 0)
                        helper.propagateSurfaceGeometryXlink(href, objectType.getTable(), objectId, "volumegeometry_id");
                }
            }
        }

        if (volumeGeometryId != 0)
            ps.setLong(5, volumeGeometryId);
        else
            ps.setNull(5, Types.NULL);

        if (thermalZone.isSetInfiltrationRate() && thermalZone.getInfiltrationRate().isSetValue()) {
            ps.setDouble(6, thermalZone.getInfiltrationRate().getValue());
            ps.setString(7, thermalZone.getInfiltrationRate().getUom());
        } else {
            ps.setNull(6, Types.DOUBLE);
            ps.setNull(7, Types.VARCHAR);
        }

        ps.addBatch();
        if (++batchCounter == helper.getDatabaseAdapter().getMaxBatchSize())
            helper.executeBatch(objectType);

        if (thermalZone.isSetFloorArea()) {
            for (FloorAreaProperty property : thermalZone.getFloorArea()) {
                FloorArea floorArea = property.getFloorArea();
                if (floorArea != null) {
                    floorAreaImporter.doImport(floorArea, thermalZone, objectId);
                    property.unsetFloorArea();
                }
            }
        }

        if (thermalZone.isSetVolume()) {
            for (VolumeTypeProperty property : thermalZone.getVolume()) {
                VolumeType volumeType = property.getVolumeType();
                if (volumeType != null) {
                    volumeTypeImporter.doImport(volumeType, thermalZone, objectId);
                    property.unsetVolumeType();
                }
            }
        }

        if (thermalZone.isSetThermalBoundary()) {
            for (ThermalBoundaryProperty property : thermalZone.getThermalBoundary()) {
                ThermalBoundary thermalBoundary = property.getThermalBoundary();
                if (thermalBoundary != null) {
                    helper.importObject(thermalBoundary, ForeignKeys.create().with("parentId", objectId));
                    property.unsetThermalBoundary();
                } else {
                    String href = property.getHref();
                    if (href != null && href.length() != 0)
                        helper.logOrThrowUnsupportedXLinkMessage(thermalZone, ThermalBoundary.class, href);
                }
            }
        }

        if (thermalZone.isSetContains()) {
            for (AbstractUsageZoneProperty property : thermalZone.getContains()) {
                AbstractUsageZone usageZone = property.getAbstractUsageZone();
                if (usageZone != null) {
                    helper.importObject(usageZone, ForeignKeys.create().with("thermalZoneId", objectId));
                    property.unsetAbstractUsageZone();
                } else
                    helper.propagateReverseObjectXlink(schemaMapper.getTableName(ADETable.USAGEZONE),
                            property.getHref(), objectId, "thermalzone_contains_id");
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
