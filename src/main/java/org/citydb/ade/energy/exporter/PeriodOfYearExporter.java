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

package org.citydb.ade.energy.exporter;

import org.citydb.ade.energy.schema.ADETable;
import org.citydb.core.ade.exporter.ADEExporter;
import org.citydb.core.ade.exporter.CityGMLExportHelper;
import org.citydb.core.operation.exporter.CityGMLExportException;
import org.citydb.sqlbuilder.expression.PlaceHolder;
import org.citydb.sqlbuilder.schema.Table;
import org.citydb.sqlbuilder.select.Select;
import org.citydb.sqlbuilder.select.join.JoinFactory;
import org.citydb.sqlbuilder.select.operator.comparison.ComparisonFactory;
import org.citydb.sqlbuilder.select.operator.comparison.ComparisonName;
import org.citygml4j.ade.energy.model.supportingClasses.*;

import java.sql.*;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class PeriodOfYearExporter implements ADEExporter {
    private PreparedStatement ps;
    private TimeSeriesExporter timeSeriesExporter;

    public PeriodOfYearExporter(Connection connection, CityGMLExportHelper helper, ExportManager manager) throws CityGMLExportException, SQLException {
        Table table = new Table(helper.getTableNameWithSchema(manager.getSchemaMapper().getTableName(ADETable.PERIODOFYEAR)));
        Table dailySchedule = new Table(helper.getTableNameWithSchema(manager.getSchemaMapper().getTableName(ADETable.DAILYSCHEDULE)));

        Select select = new Select().addProjection(table.getColumn("id"),
                        table.getColumn("timeperiodprop_beginposition"), table.getColumn("timeperiodproper_endposition"),
                        dailySchedule.getColumn("daytype"), dailySchedule.getColumn("schedule_id"))
                .addJoin(JoinFactory.left(dailySchedule, "periodofyear_dailyschedul_id", ComparisonName.EQUAL_TO, table.getColumn("id")));
        select.addSelection(ComparisonFactory.equalTo(table.getColumn("schedule_periodofyear_id"), new PlaceHolder<>()));
        ps = connection.prepareStatement(select.toString());

        timeSeriesExporter = manager.getExporter(TimeSeriesExporter.class);
    }

    public Collection<PeriodOfYear> doExport(long parentId) throws CityGMLExportException, SQLException {
        ps.setLong(1, parentId);

        try (ResultSet rs = ps.executeQuery()) {
            long currentPeriodOfYearId = 0;
            PeriodOfYear periodOfYear = null;
            Map<Long, PeriodOfYear> periodOfYears = new HashMap<>();

            while (rs.next()) {
                long periodOfYearId = rs.getLong("id");

                if (periodOfYearId != currentPeriodOfYearId || periodOfYear == null) {
                    currentPeriodOfYearId = periodOfYearId;
                    periodOfYear = periodOfYears.get(periodOfYearId);

                    if (periodOfYear == null) {
                        periodOfYear = new PeriodOfYear();

                        Timestamp beginPosition = rs.getTimestamp("timeperiodprop_beginposition");
                        Timestamp endPosition = rs.getTimestamp("timeperiodproper_endposition");
                        if (beginPosition != null && endPosition != null) {
                            TimePeriod timePeriod = new TimePeriod();
                            timePeriod.setBeginPosition(beginPosition.toLocalDateTime().atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneOffset.UTC));
                            timePeriod.setEndPosition(endPosition.toLocalDateTime().atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneOffset.UTC));

                            periodOfYear.setPeriod(new TimePeriodProperty(timePeriod));
                        }

                        periodOfYears.put(periodOfYearId, periodOfYear);
                    }
                }

                DayTypeValue dayType = DayTypeValue.fromValue(rs.getString("daytype"));
                if (dayType != null) {
                    DailySchedule dailySchedule = new DailySchedule();
                    dailySchedule.setDayType(dayType);

                    long scheduleId = rs.getLong("schedule_id");
                    if (!rs.wasNull()) {
                        AbstractTimeSeries timeSeries = timeSeriesExporter.doExport(scheduleId);
                        if (timeSeries != null)
                            dailySchedule.setSchedule(new AbstractTimeSeriesProperty(timeSeries));
                    }

                    periodOfYear.addDailySchedule(new DailyScheduleProperty(dailySchedule));
                }
            }

            return periodOfYears.values();
        }
    }

    @Override
    public void close() throws CityGMLExportException, SQLException {
        ps.close();
    }
}
