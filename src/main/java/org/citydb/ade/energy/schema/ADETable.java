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

import org.citydb.ade.energy.importer.*;
import org.citydb.core.ade.importer.ADEImporter;

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
