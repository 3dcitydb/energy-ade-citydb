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

package org.citydb.ade.energy.exporter;

import org.citydb.ade.energy.schema.ADETable;
import org.citydb.core.ade.exporter.ADEExporter;
import org.citydb.core.ade.exporter.CityGMLExportHelper;
import org.citydb.core.operation.exporter.CityGMLExportException;
import org.citygml4j.ade.energy.model.core.ElevationReferenceValue;
import org.citygml4j.ade.energy.model.core.HeightAboveGround;
import org.citygml4j.model.gml.measures.Length;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class HeightAboveGroundExporter implements ADEExporter {
    private PreparedStatement ps;

    public HeightAboveGroundExporter(Connection connection, CityGMLExportHelper helper, ExportManager manager) throws CityGMLExportException, SQLException {
        ps = connection.prepareStatement("select heightreference, value, value_uom from " +
                helper.getTableNameWithSchema(manager.getSchemaMapper().getTableName(ADETable.HEIGHTABOVEGROUND)) + " " +
                "where building_heightabovegroun_id = ?");
    }

    public Collection<HeightAboveGround> doExport(long parentId) throws CityGMLExportException, SQLException {
        ps.setLong(1, parentId);

        try (ResultSet rs = ps.executeQuery()) {
            List<HeightAboveGround> heightAboveGrounds = new ArrayList<>();

            while (rs.next()) {
                ElevationReferenceValue heightReference = ElevationReferenceValue.fromValue(rs.getString(1));
                if (heightReference == null)
                    continue;

                double value = rs.getDouble(2);
                if (rs.wasNull())
                    continue;

                HeightAboveGround heightAboveGround = new HeightAboveGround();
                heightAboveGround.setHeightReference(heightReference);

                Length length = new Length(value);
                length.setUom(rs.getString(3));
                heightAboveGround.setValue(length);

                heightAboveGrounds.add(heightAboveGround);
            }

            return heightAboveGrounds;
        }
    }

    @Override
    public void close() throws CityGMLExportException, SQLException {
        ps.close();
    }
}
