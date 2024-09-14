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

package org.citydb.ade.energy.importer;

import org.citydb.ade.energy.schema.ADETable;
import org.citydb.ade.energy.schema.SchemaMapper;
import org.citydb.core.ade.ADEExtension;
import org.citydb.core.ade.importer.*;
import org.citydb.core.database.schema.mapping.AbstractObjectType;
import org.citydb.core.database.schema.mapping.FeatureType;
import org.citydb.core.operation.importer.CityGMLImportException;
import org.citygml4j.ade.energy.model.buildingPhysics.ThermalBoundary;
import org.citygml4j.ade.energy.model.buildingPhysics.ThermalOpening;
import org.citygml4j.ade.energy.model.buildingPhysics.ThermalZone;
import org.citygml4j.ade.energy.model.core.*;
import org.citygml4j.ade.energy.model.materialAndConstruction.AbstractMaterial;
import org.citygml4j.ade.energy.model.materialAndConstruction.Construction;
import org.citygml4j.ade.energy.model.materialAndConstruction.Layer;
import org.citygml4j.ade.energy.model.materialAndConstruction.LayerComponent;
import org.citygml4j.ade.energy.model.occupantBehaviour.Facilities;
import org.citygml4j.ade.energy.model.occupantBehaviour.Occupants;
import org.citygml4j.ade.energy.model.occupantBehaviour.UsageZone;
import org.citygml4j.ade.energy.model.supportingClasses.AbstractTimeSeries;
import org.citygml4j.ade.energy.model.supportingClasses.DailyPatternSchedule;
import org.citygml4j.ade.energy.model.supportingClasses.WeatherStation;
import org.citygml4j.model.citygml.ade.binding.ADEModelObject;
import org.citygml4j.model.citygml.building.AbstractBuilding;
import org.citygml4j.model.citygml.core.AbstractCityObject;
import org.citygml4j.model.gml.feature.AbstractFeature;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class ImportManager implements ADEImportManager {
    private final ADEExtension adeExtension;
    private final Map<Class<? extends ADEImporter>, ADEImporter> importers;
    private final SchemaMapper schemaMapper;

    private Connection connection;
    private CityGMLImportHelper helper;

    public ImportManager(ADEExtension adeExtension, SchemaMapper schemaMapper) {
        this.adeExtension = adeExtension;
        this.schemaMapper = schemaMapper;
        importers = new HashMap<>();
    }

    @Override
    public void init(Connection connection, CityGMLImportHelper helper) throws CityGMLImportException, SQLException {
        this.connection = connection;
        this.helper = helper;
    }

    @Override
    public void importObject(ADEModelObject object, long objectId, AbstractObjectType<?> objectType, ForeignKeys foreignKeys) throws CityGMLImportException, SQLException {
        if (object instanceof AbstractMaterial)
            getImporter(MaterialImporter.class).doImport((AbstractMaterial) object, objectId, objectType, foreignKeys);
        else if (object instanceof AbstractTimeSeries)
            getImporter(TimeSeriesImporter.class).doImport((AbstractTimeSeries) object, objectId, objectType, foreignKeys);
        else if (object instanceof Construction)
            getImporter(ConstructionImporter.class).doImport((Construction) object, objectId, objectType, foreignKeys);
        else if (object instanceof DailyPatternSchedule)
            getImporter(DailyPatternScheduleImporter.class).doImport((DailyPatternSchedule) object, objectId, objectType, foreignKeys);
        else if (object instanceof EnergyDemand)
            getImporter(EnergyDemandImporter.class).doImport((EnergyDemand) object, objectId, objectType, foreignKeys);
        else if (object instanceof Facilities)
            getImporter(FacilitiesImporter.class).doImport((Facilities) object, objectId, objectType, foreignKeys);
        else if (object instanceof LayerComponent)
            getImporter(LayerComponentImporter.class).doImport((LayerComponent) object, objectId, objectType, foreignKeys);
        else if (object instanceof Layer)
            getImporter(LayerImporter.class).doImport((Layer) object, objectId, objectType, foreignKeys);
        else if (object instanceof Occupants)
            getImporter(OccupantsImporter.class).doImport((Occupants) object, objectId, objectType, foreignKeys);
        else if (object instanceof ThermalBoundary)
            getImporter(ThermalBoundaryImporter.class).doImport((ThermalBoundary) object, objectId, objectType, foreignKeys);
        else if (object instanceof ThermalOpening)
            getImporter(ThermalOpeningImporter.class).doImport((ThermalOpening) object, objectId, objectType, foreignKeys);
        else if (object instanceof ThermalZone)
            getImporter(ThermalZoneImporter.class).doImport((ThermalZone) object, objectId, objectType, foreignKeys);
        else if (object instanceof UsageZone)
            getImporter(UsageZoneImporter.class).doImport((UsageZone) object, objectId, objectType, foreignKeys);
        else if (object instanceof WeatherData)
            getImporter(WeatherDataImporter.class).doImport((WeatherData) object, objectId, objectType, foreignKeys);
        else if (object instanceof WeatherStation)
            getImporter(WeatherStationImporter.class).doImport((WeatherStation) object, objectId, objectType, foreignKeys);
    }

    @Override
    public void importGenericApplicationProperties(ADEPropertyCollection properties, AbstractFeature parent, long parentId, FeatureType parentType) throws CityGMLImportException, SQLException {
        if (parent instanceof AbstractCityObject
                && properties.containsOneOf(DemandsProperty.class, WeatherDataPropertyElement.class))
            getImporter(CityObjectPropertiesImporter.class).doImport(properties, (AbstractCityObject) parent, parentId, parentType);
        if (parent instanceof AbstractBuilding
                && properties.containsOneOf(ConstructionWeightProperty.class, BuildingTypeProperty.class,
                ReferencePointProperty.class, FloorAreaPropertyElement.class, VolumeTypePropertyElement.class,
                HeightAboveGroundPropertyElement.class, UsageZoneProperty.class, ThermalZonePropertyElement.class))
            getImporter(BuildingPropertiesImporter.class).doImport(properties, (AbstractBuilding) parent, parentId, parentType);
    }

    @Override
    public void executeBatch(String tableName) throws CityGMLImportException, SQLException {
        ADETable adeTable = schemaMapper.fromTableName(tableName);
        if (adeTable != null) {
            ADEImporter importer = importers.get(adeTable.getImporterClass());
            if (importer != null)
                importer.executeBatch();
        } else
            throw new CityGMLImportException("The table " + tableName + " not managed by the ADE extension for '" + adeExtension.getMetadata().getIdentifier() + "'.");
    }

    @Override
    public void close() throws CityGMLImportException, SQLException {
        for (ADEImporter importer : importers.values())
            importer.close();
    }

    protected SchemaMapper getSchemaMapper() {
        return schemaMapper;
    }

    protected <T extends ADEImporter> T getImporter(Class<T> type) throws CityGMLImportException, SQLException {
        ADEImporter importer = importers.get(type);

        if (importer == null) {
            if (type == BuildingPropertiesImporter.class)
                importer = new BuildingPropertiesImporter(connection, helper, this);
            else if (type == CityObjectPropertiesImporter.class)
                importer = new CityObjectPropertiesImporter(connection, helper, this);
            else if (type == ConstructionImporter.class)
                importer = new ConstructionImporter(connection, helper, this);
            else if (type == DailyPatternScheduleImporter.class)
                importer = new DailyPatternScheduleImporter(connection, helper, this);
            else if (type == DailyScheduleImporter.class)
                importer = new DailyScheduleImporter(connection, helper, this);
            else if (type == EnergyDemandImporter.class)
                importer = new EnergyDemandImporter(connection, helper, this);
            else if (type == FacilitiesImporter.class)
                importer = new FacilitiesImporter(connection, helper, this);
            else if (type == FloorAreaImporter.class)
                importer = new FloorAreaImporter(connection, helper, this);
            else if (type == HeatExchangeTypeImporter.class)
                importer = new HeatExchangeTypeImporter(connection, helper, this);
            else if (type == HeightAboveGroundImporter.class)
                importer = new HeightAboveGroundImporter(connection, helper, this);
            else if (type == LayerImporter.class)
                importer = new LayerImporter(connection, helper, this);
            else if (type == LayerComponentImporter.class)
                importer = new LayerComponentImporter(connection, helper, this);
            else if (type == MaterialImporter.class)
                importer = new MaterialImporter(connection, helper, this);
            else if (type == OccupantsImporter.class)
                importer = new OccupantsImporter(connection, helper, this);
            else if (type == OpticalPropertiesImporter.class)
                importer = new OpticalPropertiesImporter(connection, helper, this);
            else if (type == PeriodOfYearImporter.class)
                importer = new PeriodOfYearImporter(connection, helper, this);
            else if (type == ReflectanceImporter.class)
                importer = new ReflectanceImporter(connection, helper, this);
            else if (type == ThermalBoundaryImporter.class)
                importer = new ThermalBoundaryImporter(connection, helper, this);
            else if (type == ThermalBoundaryToThermalZoneImporter.class)
                importer = new ThermalBoundaryToThermalZoneImporter(connection, helper, this);
            else if (type == ThermalOpeningImporter.class)
                importer = new ThermalOpeningImporter(connection, helper, this);
            else if (type == ThermalZoneImporter.class)
                importer = new ThermalZoneImporter(connection, helper, this);
            else if (type == TimeSeriesImporter.class)
                importer = new TimeSeriesImporter(connection, helper, this);
            else if (type == TransmittanceImporter.class)
                importer = new TransmittanceImporter(connection, helper, this);
            else if (type == UsageZoneImporter.class)
                importer = new UsageZoneImporter(connection, helper, this);
            else if (type == VolumeTypeImporter.class)
                importer = new VolumeTypeImporter(connection, helper, this);
            else if (type == WeatherDataImporter.class)
                importer = new WeatherDataImporter(connection, helper, this);
            else if (type == WeatherStationImporter.class)
                importer = new WeatherStationImporter(connection, helper, this);

            if (importer == null)
                throw new SQLException("Failed to build ADE importer of type " + type.getName() + ".");

            importers.put(type, importer);
        }

        return type.cast(importer);
    }
}
