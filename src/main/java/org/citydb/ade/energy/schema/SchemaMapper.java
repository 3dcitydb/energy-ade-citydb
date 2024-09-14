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

package org.citydb.ade.energy.schema;

import java.util.EnumMap;
import java.util.Map;

public class SchemaMapper {
    private EnumMap<ADETable, String> tableNames = new EnumMap<>(ADETable.class);
    private EnumMap<ADESequence, String> sequenceNames = new EnumMap<>(ADESequence.class);

    public void populateSchemaNames(String prefix) {
        for (ADETable table : ADETable.values())
            tableNames.put(table, prefix + "_" + table.toString().toLowerCase());

        for (ADESequence sequence : ADESequence.values())
            sequenceNames.put(sequence, prefix + "_" + sequence.toString().toLowerCase());
    }

    public String getTableName(ADETable table) {
        return tableNames.get(table);
    }

    public ADETable fromTableName(String tableName) {
        tableName = tableName.toLowerCase();

        for (Map.Entry<ADETable, String> entry : tableNames.entrySet()) {
            if (entry.getValue().equals(tableName))
                return entry.getKey();
        }

        return null;
    }

    public String getSequenceName(ADESequence sequence) {
        return sequenceNames.get(sequence);
    }
}
