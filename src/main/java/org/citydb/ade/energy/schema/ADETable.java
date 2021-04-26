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

package org.citydb.ade.energy.schema;

import org.citydb.ade.energy.importer.BuildingPropertiesImporter;
import org.citydb.ade.energy.importer.CityObjectPropertiesImporter;
import org.citydb.ade.energy.importer.ConstructionImporter;
import org.citydb.ade.energy.importer.DailyPatternScheduleImporter;
import org.citydb.ade.energy.importer.DailyScheduleImporter;
import org.citydb.ade.energy.importer.EnergyDemandImporter;
import org.citydb.ade.energy.importer.FacilitiesImporter;
import org.citydb.ade.energy.importer.FloorAreaImporter;
import org.citydb.ade.energy.importer.HeatExchangeTypeImporter;
import org.citydb.ade.energy.importer.HeightAboveGroundImporter;
import org.citydb.ade.energy.importer.LayerComponentImporter;
import org.citydb.ade.energy.importer.LayerImporter;
import org.citydb.ade.energy.importer.MaterialImporter;
import org.citydb.ade.energy.importer.OccupantsImporter;
import org.citydb.ade.energy.importer.OpticalPropertiesImporter;
import org.citydb.ade.energy.importer.PeriodOfYearImporter;
import org.citydb.ade.energy.importer.ReflectanceImporter;
import org.citydb.ade.energy.importer.ThermalBoundaryImporter;
import org.citydb.ade.energy.importer.ThermalBoundaryToThermalZoneImporter;
import org.citydb.ade.energy.importer.ThermalOpeningImporter;
import org.citydb.ade.energy.importer.ThermalZoneImporter;
import org.citydb.ade.energy.importer.TimeSeriesImporter;
import org.citydb.ade.energy.importer.TransmittanceImporter;
import org.citydb.ade.energy.importer.UsageZoneImporter;
import org.citydb.ade.energy.importer.VolumeTypeImporter;
import org.citydb.ade.energy.importer.WeatherDataImporter;
import org.citydb.ade.energy.importer.WeatherStationImporter;
import org.citydb.ade.importer.ADEImporter;

public enum ADETable {
    BUILDING(BuildingPropertiesImporter.class),
    CITYOBJECT(CityObjectPropertiesImporter.class),
    CONSTRUCTION(ConstructionImporter.class),
    DAILYSCHEDULE(DailyScheduleImporter.class),
    ENERGYDEM_TO_CITYOBJEC(null),
    ENERGYDEMAND(EnergyDemandImporter.class),
    FACILITIES(FacilitiesImporter.class),
    FLOORAREA(FloorAreaImporter.class),
    GAS(MaterialImporter.class),
    HEATEXCHANGETYPE(HeatExchangeTypeImporter.class),
    HEIGHTABOVEGROUND(HeightAboveGroundImporter.class),
    LAYER(LayerImporter.class),
    LAYERCOMPONENT(LayerComponentImporter.class),
    MATERIAL(MaterialImporter.class),
    OCCUPANTS(OccupantsImporter.class),
    OPTICALPROPERTIES(OpticalPropertiesImporter.class),
    PERIODOFYEAR(PeriodOfYearImporter.class),
    REFLECTANCE(ReflectanceImporter.class),
    REGULARTIMESERIES(TimeSeriesImporter.class),
    REGULARTIMESERIESFILE(TimeSeriesImporter.class),
    SCHEDULE(DailyPatternScheduleImporter.class),
    SOLIDMATERIAL(MaterialImporter.class),
    THER_BOUN_TO_THER_DELI(ThermalBoundaryToThermalZoneImporter.class),
    THERMALBOUNDARY(ThermalBoundaryImporter.class),
    THERMALOPENING(ThermalOpeningImporter.class),
    THERMALZONE(ThermalZoneImporter.class),
    TIMESERIES(TimeSeriesImporter.class),
    TIMEVALUESPROPERTIES(null),
    TRANSMITTANCE(TransmittanceImporter.class),
    USAGEZONE(UsageZoneImporter.class),
    VOLUMETYPE(VolumeTypeImporter.class),
    WEATHERDATA(WeatherDataImporter.class),
    WEATHERSTATION(WeatherStationImporter.class);

    private Class<? extends ADEImporter> importerClass;

    ADETable(Class<? extends ADEImporter> importerClass) {
        this.importerClass = importerClass;
    }

    public Class<? extends ADEImporter> getImporterClass() {
        return importerClass;
    }
}
