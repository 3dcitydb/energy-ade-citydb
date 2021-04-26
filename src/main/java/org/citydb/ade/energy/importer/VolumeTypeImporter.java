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
import org.citydb.ade.importer.ADEImporter;
import org.citydb.ade.importer.CityGMLImportHelper;
import org.citydb.citygml.importer.CityGMLImportException;
import org.citygml4j.ade.energy.model.core.AbstractThermalZone;
import org.citygml4j.ade.energy.model.core.VolumeType;
import org.citygml4j.model.citygml.building.AbstractBuilding;
import org.citygml4j.model.citygml.core.AbstractCityObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

public class VolumeTypeImporter implements ADEImporter {
    private final CityGMLImportHelper helper;
    private final SchemaMapper schemaMapper;

    private PreparedStatement ps;
    private int batchCounter;

    public VolumeTypeImporter(Connection connection, CityGMLImportHelper helper, ImportManager manager) throws SQLException {
        this.helper = helper;
        this.schemaMapper = manager.getSchemaMapper();

        ps = connection.prepareStatement("insert into " +
                helper.getTableNameWithSchema(manager.getSchemaMapper().getTableName(ADETable.VOLUMETYPE)) + " " +
                "(id, building_volume_id, thermalzone_volume_id, type, value, value_uom) " +
                "values (?, ?, ?, ?, ?, ?)");
    }

    public void doImport(VolumeType volumeType, AbstractCityObject parent, long parentId) throws CityGMLImportException, SQLException {
        long objectId = helper.getNextSequenceValue(schemaMapper.getSequenceName(ADESequence.VOLUMETYPE_SEQ));
        ps.setLong(1, objectId);

        if (parent instanceof AbstractBuilding) {
            ps.setLong(2, parentId);
            ps.setNull(3, Types.NULL);
        } else if (parent instanceof AbstractThermalZone) {
            ps.setNull(2, Types.NULL);
            ps.setLong(3, parentId);
        } else {
            ps.setNull(2, Types.NULL);
            ps.setNull(3, Types.NULL);
        }

        if (volumeType.isSetType())
            ps.setString(4, volumeType.getType().value());
        else
            ps.setNull(4, Types.VARCHAR);

        if (volumeType.isSetValue() && volumeType.getValue().isSetValue()) {
            ps.setDouble(5, volumeType.getValue().getValue());
            ps.setString(6, volumeType.getValue().getUom());
        } else {
            ps.setNull(5, Types.DOUBLE);
            ps.setNull(6, Types.VARCHAR);
        }

        ps.addBatch();
        if (++batchCounter == helper.getDatabaseAdapter().getMaxBatchSize())
            helper.executeBatch(schemaMapper.getTableName(ADETable.VOLUMETYPE));
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
