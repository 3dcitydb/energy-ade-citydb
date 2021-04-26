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
import org.citygml4j.ade.energy.model.supportingClasses.DailySchedule;
import org.citygml4j.ade.energy.model.supportingClasses.DailyScheduleProperty;
import org.citygml4j.ade.energy.model.supportingClasses.PeriodOfYear;
import org.citygml4j.ade.energy.model.supportingClasses.TimePeriod;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class PeriodOfYearImporter implements ADEImporter {
    private final CityGMLImportHelper helper;
    private final SchemaMapper schemaMapper;

    private DailyScheduleImporter dailyScheduleImporter;
    private PreparedStatement ps;
    private int batchCounter;

    public PeriodOfYearImporter(Connection connection, CityGMLImportHelper helper, ImportManager manager) throws CityGMLImportException, SQLException {
        this.helper = helper;
        this.schemaMapper = manager.getSchemaMapper();

        ps = connection.prepareStatement("insert into " +
                helper.getTableNameWithSchema(manager.getSchemaMapper().getTableName(ADETable.PERIODOFYEAR)) + " " +
                "(id, schedule_periodofyear_id, timeperiodprop_beginposition, timeperiodproper_endposition) " +
                "values (?, ?, ?, ?)");

        dailyScheduleImporter = manager.getImporter(DailyScheduleImporter.class);
    }

    public void doImport(PeriodOfYear periodOfYear, long parentId) throws CityGMLImportException, SQLException {
        long objectId = helper.getNextSequenceValue(schemaMapper.getSequenceName(ADESequence.PERIODOFYEAR_SEQ));
        ps.setLong(1, objectId);

        ps.setLong(2, parentId);

        ZonedDateTime beginPosition = null;
        ZonedDateTime endPosition = null;
        if (periodOfYear.isSetPeriod() && periodOfYear.getPeriod().isSetTimePeriod()) {
            TimePeriod timePeriod = periodOfYear.getPeriod().getTimePeriod();
            if (timePeriod.isSetBeginPosition())
                beginPosition = timePeriod.getBeginPosition();
            if (timePeriod.isSetEndPosition())
                endPosition = timePeriod.getEndPosition();
        }

        if (beginPosition != null)
            ps.setTimestamp(3, Timestamp.from(beginPosition.withZoneSameInstant(ZoneOffset.UTC).toInstant()));
        else
            ps.setNull(3, Types.TIMESTAMP);

        if (endPosition != null)
            ps.setTimestamp(4, Timestamp.from(endPosition.withZoneSameInstant(ZoneOffset.UTC).toInstant()));
        else
            ps.setNull(4, Types.TIMESTAMP);

        ps.addBatch();
        if (++batchCounter == helper.getDatabaseAdapter().getMaxBatchSize())
            helper.executeBatch(schemaMapper.getTableName(ADETable.PERIODOFYEAR));

        if (periodOfYear.isSetDailySchedule()) {
            for (DailyScheduleProperty property : periodOfYear.getDailySchedule()) {
                DailySchedule dailySchedule = property.getDaySchedule();
                if (dailySchedule != null) {
                    dailyScheduleImporter.doImport(dailySchedule, objectId);
                    property.unsetDaySchedule();
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
