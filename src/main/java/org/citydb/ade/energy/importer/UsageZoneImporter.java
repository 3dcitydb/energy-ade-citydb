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

package org.citydb.ade.energy.importer;

import org.citydb.ade.energy.schema.ADETable;
import org.citydb.core.ade.importer.ADEImporter;
import org.citydb.core.ade.importer.CityGMLImportHelper;
import org.citydb.core.ade.importer.ForeignKeys;
import org.citydb.core.database.schema.mapping.AbstractObjectType;
import org.citydb.core.operation.importer.CityGMLImportException;
import org.citygml4j.ade.energy.model.core.FloorArea;
import org.citygml4j.ade.energy.model.core.FloorAreaProperty;
import org.citygml4j.ade.energy.model.occupantBehaviour.*;
import org.citygml4j.ade.energy.model.supportingClasses.AbstractSchedule;
import org.citygml4j.ade.energy.model.supportingClasses.AbstractScheduleProperty;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

public class UsageZoneImporter implements ADEImporter {
    private final CityGMLImportHelper helper;

    private FloorAreaImporter floorAreaImporter;
    private PreparedStatement ps;
    private int batchCounter;

    public UsageZoneImporter(Connection connection, CityGMLImportHelper helper, ImportManager manager) throws CityGMLImportException, SQLException {
        this.helper = helper;

        ps = connection.prepareStatement("insert into " +
                helper.getTableNameWithSchema(manager.getSchemaMapper().getTableName(ADETable.USAGEZONE)) + " " +
                "(id, building_usagezone_id, thermalzone_contains_id, coolingschedule_id, heatingschedule_id, " +
                "ventilationschedule_id, usagezonetype, usagezonetype_codespace) " +
                "values (?, ?, ?, ?, ?, ?, ?, ?)");

        floorAreaImporter = manager.getImporter(FloorAreaImporter.class);
    }

    public void doImport(UsageZone usageZone, long objectId, AbstractObjectType<?> objectType, ForeignKeys foreignKeys) throws CityGMLImportException, SQLException {
        long buildingId = foreignKeys.get("buildingId");
        long thermalZoneId = foreignKeys.get("thermalZoneId");

        ps.setLong(1, objectId);

        if (buildingId != 0) {
            ps.setLong(2, buildingId);
            ps.setNull(3, Types.NULL);
        } else if (thermalZoneId != 0) {
            ps.setNull(2, Types.NULL);
            ps.setLong(3, thermalZoneId);
        } else {
            ps.setNull(2, Types.NULL);
            ps.setNull(3, Types.NULL);
        }

        long coolingScheduleId = 0;
        if (usageZone.isSetCoolingSchedule()) {
            AbstractScheduleProperty property = usageZone.getCoolingSchedule();
            if (property.isSetAbstractSchedule()) {
                coolingScheduleId = helper.importObject(property.getAbstractSchedule());
                property.unsetAbstractSchedule();
            } else {
                String href = property.getHref();
                if (href != null && href.length() != 0)
                    helper.logOrThrowUnsupportedXLinkMessage(usageZone, AbstractSchedule.class, href);
            }
        }

        if (coolingScheduleId != 0)
            ps.setLong(4, coolingScheduleId);
        else
            ps.setNull(4, Types.NULL);

        long heatingScheduleId = 0;
        if (usageZone.isSetHeatingSchedule()) {
            AbstractScheduleProperty property = usageZone.getHeatingSchedule();
            if (property.isSetAbstractSchedule()) {
                heatingScheduleId = helper.importObject(property.getAbstractSchedule());
                property.unsetAbstractSchedule();
            } else {
                String href = property.getHref();
                if (href != null && href.length() != 0)
                    helper.logOrThrowUnsupportedXLinkMessage(usageZone, AbstractSchedule.class, href);
            }
        }

        if (heatingScheduleId != 0)
            ps.setLong(5, heatingScheduleId);
        else
            ps.setNull(5, Types.NULL);

        long ventilationScheduleId = 0;
        if (usageZone.isSetVentilationSchedule()) {
            AbstractScheduleProperty property = usageZone.getVentilationSchedule();
            if (property.isSetAbstractSchedule()) {
                ventilationScheduleId = helper.importObject(property.getAbstractSchedule());
                property.unsetAbstractSchedule();
            } else {
                String href = property.getHref();
                if (href != null && href.length() != 0)
                    helper.logOrThrowUnsupportedXLinkMessage(usageZone, AbstractSchedule.class, href);
            }
        }

        if (ventilationScheduleId != 0)
            ps.setLong(6, ventilationScheduleId);
        else
            ps.setNull(6, Types.NULL);

        if (usageZone.isSetUsageZoneType() && usageZone.getUsageZoneType().isSetValue()) {
            ps.setString(7, usageZone.getUsageZoneType().getValue());
            ps.setString(8, usageZone.getUsageZoneType().getCodeSpace());
        } else {
            ps.setNull(7, Types.VARCHAR);
            ps.setNull(8, Types.VARCHAR);
        }

        ps.addBatch();
        if (++batchCounter == helper.getDatabaseAdapter().getMaxBatchSize())
            helper.executeBatch(objectType);

        if (usageZone.isSetFloorArea()) {
            for (FloorAreaProperty property : usageZone.getFloorArea()) {
                FloorArea floorArea = property.getFloorArea();
                if (floorArea != null) {
                    floorAreaImporter.doImport(floorArea, usageZone, objectId);
                    property.unsetFloorArea();
                }
            }
        }

        if (usageZone.isSetEquippedWith()) {
            for (FacilitiesProperty property : usageZone.getEquippedWith()) {
                Facilities facilities = property.getFacilities();
                if (facilities != null) {
                    helper.importObject(facilities, ForeignKeys.create().with("parentId", objectId));
                    property.unsetFacilities();
                } else {
                    String href = property.getHref();
                    if (href != null && href.length() != 0)
                        helper.logOrThrowUnsupportedXLinkMessage(usageZone, Facilities.class, href);
                }
            }
        }

        if (usageZone.isSetOccupiedBy()) {
            for (OccupantsProperty property : usageZone.getOccupiedBy()) {
                Occupants occupants = property.getOccupants();
                if (occupants != null) {
                    helper.importObject(occupants, ForeignKeys.create().with("parentId", objectId));
                    property.unsetOccupants();
                } else {
                    String href = property.getHref();
                    if (href != null && href.length() != 0)
                        helper.logOrThrowUnsupportedXLinkMessage(usageZone, Occupants.class, href);
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
