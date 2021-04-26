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
import org.citygml4j.ade.energy.model.supportingClasses.DailyPatternSchedule;
import org.citygml4j.ade.energy.model.supportingClasses.PeriodOfYear;
import org.citygml4j.ade.energy.model.supportingClasses.PeriodOfYearProperty;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DailyPatternScheduleImporter implements ADEImporter {
    private final CityGMLImportHelper helper;

    private PeriodOfYearImporter periodOfYearImporter;
    private PreparedStatement ps;
    private int batchCounter;

    public DailyPatternScheduleImporter(Connection connection, CityGMLImportHelper helper, ImportManager manager) throws CityGMLImportException, SQLException {
        this.helper = helper;

        ps = connection.prepareStatement("insert into " +
                helper.getTableNameWithSchema(manager.getSchemaMapper().getTableName(ADETable.SCHEDULE)) + " " +
                "(id) " +
                "values (?)");

        periodOfYearImporter = manager.getImporter(PeriodOfYearImporter.class);
    }

    public void doImport(DailyPatternSchedule dailyPatternSchedule, long objectId, AbstractObjectType<?> objectType, ForeignKeys foreignKeys) throws CityGMLImportException, SQLException {
        ps.setLong(1, objectId);

        ps.addBatch();
        if (++batchCounter == helper.getDatabaseAdapter().getMaxBatchSize())
            helper.executeBatch(objectType);

        if (dailyPatternSchedule.isSetPeriodOfYear()) {
            for (PeriodOfYearProperty property : dailyPatternSchedule.getPeriodOfYear()) {
                PeriodOfYear periodOfYear = property.getPeriodOfYear();
                if (periodOfYear != null) {
                    periodOfYearImporter.doImport(periodOfYear, objectId);
                    property.unsetPeriodOfYear();
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
