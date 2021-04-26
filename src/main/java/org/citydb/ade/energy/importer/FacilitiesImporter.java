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
import org.citydb.ade.importer.ADEImporter;
import org.citydb.ade.importer.CityGMLImportHelper;
import org.citydb.ade.importer.ForeignKeys;
import org.citydb.citygml.importer.CityGMLImportException;
import org.citydb.database.schema.mapping.AbstractObjectType;
import org.citygml4j.ade.energy.model.core.HeatExchangeTypeProperty;
import org.citygml4j.ade.energy.model.occupantBehaviour.Facilities;
import org.citygml4j.ade.energy.model.supportingClasses.AbstractSchedule;
import org.citygml4j.ade.energy.model.supportingClasses.AbstractScheduleProperty;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

public class FacilitiesImporter implements ADEImporter {
    private final CityGMLImportHelper helper;

    private HeatExchangeTypeImporter heatExchangeTypeImporter;
    private PreparedStatement ps;
    private int batchCounter;

    public FacilitiesImporter(Connection connection, CityGMLImportHelper helper, ImportManager manager) throws CityGMLImportException, SQLException {
        this.helper = helper;

        ps = connection.prepareStatement("insert into " +
                helper.getTableNameWithSchema(manager.getSchemaMapper().getTableName(ADETable.FACILITIES)) + " " +
                "(id, objectclass_id, usagezone_equippedwith_id, heatdissipation_id, operationschedule_id) " +
                "values (?, ?, ?, ?, ?)");

        heatExchangeTypeImporter = manager.getImporter(HeatExchangeTypeImporter.class);
    }

    public void doImport(Facilities facilities, long objectId, AbstractObjectType<?> objectType, ForeignKeys foreignKeys) throws CityGMLImportException, SQLException {
        long parentId = foreignKeys.get("parentId");

        ps.setLong(1, objectId);
        ps.setLong(2, objectType.getObjectClassId());

        if (parentId != 0)
            ps.setLong(3, parentId);
        else
            ps.setNull(3, Types.NULL);

        long heatDissipationId = 0;
        if (facilities.isSetHeatDissipation()) {
            HeatExchangeTypeProperty property = facilities.getHeatDissipation();
            if (property.isSetHeatExchangeType()) {
                heatDissipationId = heatExchangeTypeImporter.doImport(property.getHeatExchangeType());
                property.unsetHeatExchangeType();
            }
        }

        if (heatDissipationId != 0)
            ps.setLong(4, heatDissipationId);
        else
            ps.setNull(4, Types.NULL);

        long operationScheduleId = 0;
        if (facilities.isSetOperationSchedule()) {
            AbstractScheduleProperty property = facilities.getOperationSchedule();
            if (property.isSetAbstractSchedule()) {
                operationScheduleId = helper.importObject(property.getAbstractSchedule());
                property.unsetAbstractSchedule();
            } else {
                String href = property.getHref();
                if (href != null && href.length() != 0)
                    helper.logOrThrowUnsupportedXLinkMessage(facilities, AbstractSchedule.class, href);
            }
        }

        if (operationScheduleId != 0)
            ps.setLong(5, operationScheduleId);
        else
            ps.setNull(5, Types.NULL);

        ps.addBatch();
        if (++batchCounter == helper.getDatabaseAdapter().getMaxBatchSize())
            helper.executeBatch(objectType);
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
