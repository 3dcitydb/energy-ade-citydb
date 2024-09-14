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

import org.citydb.core.ade.ADEExtensionException;
import org.citydb.core.ade.ADEObjectMapper;
import org.citydb.core.database.schema.mapping.AbstractObjectType;
import org.citydb.core.database.schema.mapping.SchemaMapping;
import org.citygml4j.ade.energy.model.buildingPhysics.ThermalBoundary;
import org.citygml4j.ade.energy.model.buildingPhysics.ThermalOpening;
import org.citygml4j.ade.energy.model.buildingPhysics.ThermalZone;
import org.citygml4j.ade.energy.model.core.*;
import org.citygml4j.ade.energy.model.materialAndConstruction.*;
import org.citygml4j.ade.energy.model.occupantBehaviour.*;
import org.citygml4j.ade.energy.model.supportingClasses.*;
import org.citygml4j.model.gml.base.AbstractGML;
import org.citygml4j.model.module.citygml.CityGMLVersion;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class ObjectMapper implements ADEObjectMapper {
    private Map<Class<? extends AbstractGML>, Integer> objectClassIds = new HashMap<>();

    public void populateObjectClassIds(SchemaMapping schemaMapping) throws ADEExtensionException {
        for (AbstractObjectType<?> type : schemaMapping.getAbstractObjectTypes()) {
            int objectClassId = type.getObjectClassId();

            switch (type.getPath()) {
                case "AbstractConstruction":
                    objectClassIds.put(AbstractConstruction.class, objectClassId);
                    break;
                case "AbstractMaterial":
                    objectClassIds.put(AbstractMaterial.class, objectClassId);
                    break;
                case "AbstractSchedule":
                    objectClassIds.put(AbstractSchedule.class, objectClassId);
                    break;
                case "AbstractThermalZone":
                    objectClassIds.put(AbstractThermalZone.class, objectClassId);
                    break;
                case "AbstractTimeSeries":
                    objectClassIds.put(AbstractTimeSeries.class, objectClassId);
                    break;
                case "AbstractUsageZone":
                    objectClassIds.put(AbstractUsageZone.class, objectClassId);
                    break;
                case "Construction":
                    objectClassIds.put(Construction.class, objectClassId);
                    break;
                case "DHWFacilities":
                    objectClassIds.put(DHWFacilities.class, objectClassId);
                    break;
                case "DailyPatternSchedule":
                    objectClassIds.put(DailyPatternSchedule.class, objectClassId);
                    break;
                case "ElectricalAppliances":
                    objectClassIds.put(ElectricalAppliances.class, objectClassId);
                    break;
                case "EnergyDemand":
                    objectClassIds.put(EnergyDemand.class, objectClassId);
                    break;
                case "Facilities":
                    objectClassIds.put(Facilities.class, objectClassId);
                    break;
                case "Gas":
                    objectClassIds.put(Gas.class, objectClassId);
                    break;
                case "Layer":
                    objectClassIds.put(Layer.class, objectClassId);
                    break;
                case "LayerComponent":
                    objectClassIds.put(LayerComponent.class, objectClassId);
                    break;
                case "LightingFacilities":
                    objectClassIds.put(LightingFacilities.class, objectClassId);
                    break;
                case "Occupants":
                    objectClassIds.put(Occupants.class, objectClassId);
                    break;
                case "RegularTimeSeries":
                    objectClassIds.put(RegularTimeSeries.class, objectClassId);
                    break;
                case "RegularTimeSeriesFile":
                    objectClassIds.put(RegularTimeSeriesFile.class, objectClassId);
                    break;
                case "SolidMaterial":
                    objectClassIds.put(SolidMaterial.class, objectClassId);
                    break;
                case "ThermalBoundary":
                    objectClassIds.put(ThermalBoundary.class, objectClassId);
                    break;
                case "ThermalOpening":
                    objectClassIds.put(ThermalOpening.class, objectClassId);
                    break;
                case "ThermalZone":
                    objectClassIds.put(ThermalZone.class, objectClassId);
                    break;
                case "UsageZone":
                    objectClassIds.put(UsageZone.class, objectClassId);
                    break;
                case "WeatherData":
                    objectClassIds.put(WeatherData.class, objectClassId);
                    break;
                case "WeatherStation":
                    objectClassIds.put(WeatherStation.class, objectClassId);
                    break;
            }
        }
    }

    @Override
    public AbstractGML createObject(int objectClassId, CityGMLVersion version) {
        if (version == CityGMLVersion.v2_0_0) {
            for (Entry<Class<? extends AbstractGML>, Integer> entry : objectClassIds.entrySet()) {
                if (entry.getValue() == objectClassId) {
                    try {
                        return entry.getKey().getDeclaredConstructor().newInstance();
                    } catch (InstantiationException | IllegalAccessException | NoSuchMethodException |
                             InvocationTargetException e) {
                        //
                    }
                }
            }
        }

        return null;
    }

    @Override
    public int getObjectClassId(Class<? extends AbstractGML> adeObjectClass) {
        Integer objectClassId = objectClassIds.get(adeObjectClass);
        return objectClassId != null ? objectClassId : 0;
    }
}
