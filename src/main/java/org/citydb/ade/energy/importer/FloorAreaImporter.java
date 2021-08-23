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

import org.citydb.ade.energy.schema.ADESequence;
import org.citydb.ade.energy.schema.ADETable;
import org.citydb.ade.energy.schema.SchemaMapper;
import org.citydb.core.ade.importer.ADEImporter;
import org.citydb.core.ade.importer.CityGMLImportHelper;
import org.citydb.core.operation.importer.CityGMLImportException;
import org.citygml4j.ade.energy.model.core.AbstractThermalZone;
import org.citygml4j.ade.energy.model.core.AbstractUsageZone;
import org.citygml4j.ade.energy.model.core.FloorArea;
import org.citygml4j.model.citygml.building.AbstractBuilding;
import org.citygml4j.model.citygml.core.AbstractCityObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

public class FloorAreaImporter implements ADEImporter {
    private final CityGMLImportHelper helper;
    private final SchemaMapper schemaMapper;

    private PreparedStatement ps;
    private int batchCounter;

    public FloorAreaImporter(Connection connection, CityGMLImportHelper helper, ImportManager manager) throws SQLException {
        this.helper = helper;
        this.schemaMapper = manager.getSchemaMapper();

        ps = connection.prepareStatement("insert into " +
                helper.getTableNameWithSchema(manager.getSchemaMapper().getTableName(ADETable.FLOORAREA)) + " " +
                "(id, building_floorarea_id, thermalzone_floorarea_id, usagezone_floorarea_id, type, value, value_uom) " +
                "values (?, ?, ?, ?, ?, ?, ?)");
    }

    public void doImport(FloorArea floorArea, AbstractCityObject parent, long parentId) throws CityGMLImportException, SQLException {
        long objectId = helper.getNextSequenceValue(schemaMapper.getSequenceName(ADESequence.FLOORAREA_SEQ));
        ps.setLong(1, objectId);

        if (parent instanceof AbstractBuilding) {
            ps.setLong(2, parentId);
            ps.setNull(3, Types.NULL);
            ps.setNull(4, Types.NULL);
        } else if (parent instanceof AbstractThermalZone) {
            ps.setNull(2, Types.NULL);
            ps.setLong(3, parentId);
            ps.setNull(4, Types.NULL);
        } else if (parent instanceof AbstractUsageZone) {
            ps.setNull(2, Types.NULL);
            ps.setNull(3, Types.NULL);
            ps.setLong(4, parentId);
        } else {
            ps.setNull(2, Types.NULL);
            ps.setNull(3, Types.NULL);
            ps.setNull(4, Types.NULL);
        }

        if (floorArea.isSetType())
            ps.setString(5, floorArea.getType().value());
        else
            ps.setNull(5, Types.VARCHAR);

        if (floorArea.isSetValue() && floorArea.getValue().isSetValue()) {
            ps.setDouble(6, floorArea.getValue().getValue());
            ps.setString(7, floorArea.getValue().getUom());
        } else {
            ps.setNull(6, Types.DOUBLE);
            ps.setNull(7, Types.VARCHAR);
        }

        ps.addBatch();
        if (++batchCounter == helper.getDatabaseAdapter().getMaxBatchSize())
            helper.executeBatch(schemaMapper.getTableName(ADETable.FLOORAREA));
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
