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

import org.citydb.core.ade.exporter.ADEExporter;
import org.citydb.core.ade.exporter.CityGMLExportHelper;
import org.citydb.core.operation.exporter.CityGMLExportException;
import org.citygml4j.ade.energy.model.supportingClasses.DailyPatternSchedule;
import org.citygml4j.ade.energy.model.supportingClasses.PeriodOfYear;
import org.citygml4j.ade.energy.model.supportingClasses.PeriodOfYearProperty;

import java.sql.SQLException;

public class DailyPatternScheduleExporter implements ADEExporter {
    private final CityGMLExportHelper helper;
    private final int objectClassId;

    private PeriodOfYearExporter periodOfYearExporter;

    public DailyPatternScheduleExporter(CityGMLExportHelper helper, ExportManager manager) throws CityGMLExportException, SQLException {
        this.helper = helper;
        objectClassId = manager.getObjectMapper().getObjectClassId(DailyPatternSchedule.class);

        periodOfYearExporter = manager.getExporter(PeriodOfYearExporter.class);
    }

    public DailyPatternSchedule doExport(long objectId) throws CityGMLExportException, SQLException {
        DailyPatternSchedule schedule = helper.createObject(objectId, objectClassId, DailyPatternSchedule.class);
        if (schedule == null) {
            helper.logOrThrowErrorMessage("Failed to instantiate " + helper.getObjectSignature(objectClassId, objectId) + " as daily pattern schedule object.");
            return null;
        }

        for (PeriodOfYear periodOfYear : periodOfYearExporter.doExport(objectId))
            schedule.addPeriodOfYear(new PeriodOfYearProperty(periodOfYear));

        return schedule;
    }

    @Override
    public void close() throws CityGMLExportException, SQLException {
        //
    }
}
