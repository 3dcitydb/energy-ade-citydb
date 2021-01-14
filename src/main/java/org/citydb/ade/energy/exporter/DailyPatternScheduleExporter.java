package org.citydb.ade.energy.exporter;

import org.citydb.ade.exporter.ADEExporter;
import org.citydb.ade.exporter.CityGMLExportHelper;
import org.citydb.citygml.exporter.CityGMLExportException;
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
