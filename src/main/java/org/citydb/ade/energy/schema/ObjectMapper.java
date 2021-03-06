package org.citydb.ade.energy.schema;

import org.citydb.ade.ADEExtensionException;
import org.citydb.ade.ADEObjectMapper;
import org.citydb.database.schema.mapping.AbstractObjectType;
import org.citydb.database.schema.mapping.SchemaMapping;
import org.citygml4j.ade.energy.model.buildingPhysics.ThermalBoundary;
import org.citygml4j.ade.energy.model.buildingPhysics.ThermalOpening;
import org.citygml4j.ade.energy.model.buildingPhysics.ThermalZone;
import org.citygml4j.ade.energy.model.core.AbstractConstruction;
import org.citygml4j.ade.energy.model.core.AbstractThermalZone;
import org.citygml4j.ade.energy.model.core.AbstractUsageZone;
import org.citygml4j.ade.energy.model.core.EnergyDemand;
import org.citygml4j.ade.energy.model.core.WeatherData;
import org.citygml4j.ade.energy.model.materialAndConstruction.AbstractMaterial;
import org.citygml4j.ade.energy.model.materialAndConstruction.Construction;
import org.citygml4j.ade.energy.model.materialAndConstruction.Gas;
import org.citygml4j.ade.energy.model.materialAndConstruction.Layer;
import org.citygml4j.ade.energy.model.materialAndConstruction.LayerComponent;
import org.citygml4j.ade.energy.model.materialAndConstruction.SolidMaterial;
import org.citygml4j.ade.energy.model.occupantBehaviour.DHWFacilities;
import org.citygml4j.ade.energy.model.occupantBehaviour.ElectricalAppliances;
import org.citygml4j.ade.energy.model.occupantBehaviour.Facilities;
import org.citygml4j.ade.energy.model.occupantBehaviour.LightingFacilities;
import org.citygml4j.ade.energy.model.occupantBehaviour.Occupants;
import org.citygml4j.ade.energy.model.occupantBehaviour.UsageZone;
import org.citygml4j.ade.energy.model.supportingClasses.AbstractSchedule;
import org.citygml4j.ade.energy.model.supportingClasses.AbstractTimeSeries;
import org.citygml4j.ade.energy.model.supportingClasses.DailyPatternSchedule;
import org.citygml4j.ade.energy.model.supportingClasses.RegularTimeSeries;
import org.citygml4j.ade.energy.model.supportingClasses.RegularTimeSeriesFile;
import org.citygml4j.ade.energy.model.supportingClasses.WeatherStation;
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
                    } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
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
