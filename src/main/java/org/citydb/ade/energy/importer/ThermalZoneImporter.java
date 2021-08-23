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
import org.citydb.ade.energy.schema.SchemaMapper;
import org.citydb.core.ade.importer.ADEImporter;
import org.citydb.core.ade.importer.CityGMLImportHelper;
import org.citydb.core.ade.importer.ForeignKeys;
import org.citydb.core.database.schema.mapping.AbstractObjectType;
import org.citydb.core.operation.importer.CityGMLImportException;
import org.citygml4j.ade.energy.model.buildingPhysics.ThermalBoundary;
import org.citygml4j.ade.energy.model.buildingPhysics.ThermalBoundaryProperty;
import org.citygml4j.ade.energy.model.buildingPhysics.ThermalZone;
import org.citygml4j.ade.energy.model.core.*;
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
