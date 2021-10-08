-- This document was automatically created by the ADE-Manager tool of 3DCityDB (https://www.3dcitydb.org) on 2021-10-08 13:50:33 
-- ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ 
-- *********************************** Create tables ************************************** 
-- ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ 
-- -------------------------------------------------------------------- 
-- ng_building 
-- -------------------------------------------------------------------- 
CREATE TABLE ng_building
(
    id BIGINT NOT NULL,
    buildingtype VARCHAR(1000),
    buildingtype_codespace VARCHAR(1000),
    constructionweight VARCHAR(1000),
    referencepoint geometry(GEOMETRYZ),
    PRIMARY KEY (id)
);

-- -------------------------------------------------------------------- 
-- ng_cityobject 
-- -------------------------------------------------------------------- 
CREATE TABLE ng_cityobject
(
    id BIGINT NOT NULL,
    PRIMARY KEY (id)
);

-- -------------------------------------------------------------------- 
-- ng_construction 
-- -------------------------------------------------------------------- 
CREATE TABLE ng_construction
(
    id BIGINT NOT NULL,
    opticalproperties_id BIGINT,
    uvalue NUMERIC,
    uvalue_uom VARCHAR(1000),
    PRIMARY KEY (id)
);

-- -------------------------------------------------------------------- 
-- ng_dailyschedule 
-- -------------------------------------------------------------------- 
CREATE TABLE ng_dailyschedule
(
    id BIGINT NOT NULL,
    daytype VARCHAR(1000),
    periodofyear_dailyschedul_id BIGINT,
    schedule_id BIGINT,
    PRIMARY KEY (id)
);

-- -------------------------------------------------------------------- 
-- ng_energydem_to_cityobjec 
-- -------------------------------------------------------------------- 
CREATE TABLE ng_energydem_to_cityobjec
(
    cityobject_id BIGINT NOT NULL,
    energydemand_id BIGINT NOT NULL,
    PRIMARY KEY (cityobject_id, energydemand_id)
);

-- -------------------------------------------------------------------- 
-- ng_energydemand 
-- -------------------------------------------------------------------- 
CREATE TABLE ng_energydemand
(
    id BIGINT NOT NULL,
    cityobject_demands_id BIGINT,
    enduse VARCHAR(1000),
    energyamount_id BIGINT,
    energycarriertype VARCHAR(1000),
    energycarriertype_codespace VARCHAR(1000),
    maximumload NUMERIC,
    maximumload_uom VARCHAR(1000),
    PRIMARY KEY (id)
);

-- -------------------------------------------------------------------- 
-- ng_facilities 
-- -------------------------------------------------------------------- 
CREATE TABLE ng_facilities
(
    id BIGINT NOT NULL,
    heatdissipation_id BIGINT,
    objectclass_id INTEGER,
    operationschedule_id BIGINT,
    usagezone_equippedwith_id BIGINT,
    PRIMARY KEY (id)
);

-- -------------------------------------------------------------------- 
-- ng_floorarea 
-- -------------------------------------------------------------------- 
CREATE TABLE ng_floorarea
(
    id BIGINT NOT NULL,
    building_floorarea_id BIGINT,
    thermalzone_floorarea_id BIGINT,
    type VARCHAR(1000),
    usagezone_floorarea_id BIGINT,
    value NUMERIC,
    value_uom VARCHAR(1000),
    PRIMARY KEY (id)
);

-- -------------------------------------------------------------------- 
-- ng_gas 
-- -------------------------------------------------------------------- 
CREATE TABLE ng_gas
(
    id BIGINT NOT NULL,
    isventilated NUMERIC,
    rvalue NUMERIC,
    rvalue_uom VARCHAR(1000),
    PRIMARY KEY (id)
);

-- -------------------------------------------------------------------- 
-- ng_heatexchangetype 
-- -------------------------------------------------------------------- 
CREATE TABLE ng_heatexchangetype
(
    id BIGINT NOT NULL,
    convectivefraction NUMERIC,
    convectivefraction_uom VARCHAR(1000),
    latentfraction NUMERIC,
    latentfraction_uom VARCHAR(1000),
    radiantfraction NUMERIC,
    radiantfraction_uom VARCHAR(1000),
    totalvalue NUMERIC,
    totalvalue_uom VARCHAR(1000),
    PRIMARY KEY (id)
);

-- -------------------------------------------------------------------- 
-- ng_heightaboveground 
-- -------------------------------------------------------------------- 
CREATE TABLE ng_heightaboveground
(
    id BIGINT NOT NULL,
    building_heightabovegroun_id BIGINT,
    heightreference VARCHAR(1000),
    value NUMERIC,
    value_uom VARCHAR(1000),
    PRIMARY KEY (id)
);

-- -------------------------------------------------------------------- 
-- ng_layer 
-- -------------------------------------------------------------------- 
CREATE TABLE ng_layer
(
    id BIGINT NOT NULL,
    construction_layer_id BIGINT,
    PRIMARY KEY (id)
);

-- -------------------------------------------------------------------- 
-- ng_layercomponent 
-- -------------------------------------------------------------------- 
CREATE TABLE ng_layercomponent
(
    id BIGINT NOT NULL,
    areafraction NUMERIC,
    areafraction_uom VARCHAR(1000),
    layer_layercomponent_id BIGINT,
    material_id BIGINT,
    thickness NUMERIC,
    thickness_uom VARCHAR(1000),
    PRIMARY KEY (id)
);

-- -------------------------------------------------------------------- 
-- ng_material 
-- -------------------------------------------------------------------- 
CREATE TABLE ng_material
(
    id BIGINT NOT NULL,
    objectclass_id INTEGER,
    PRIMARY KEY (id)
);

-- -------------------------------------------------------------------- 
-- ng_occupants 
-- -------------------------------------------------------------------- 
CREATE TABLE ng_occupants
(
    id BIGINT NOT NULL,
    heatdissipation_id BIGINT,
    numberofoccupants INTEGER,
    occupancyrate_id BIGINT,
    usagezone_occupiedby_id BIGINT,
    PRIMARY KEY (id)
);

-- -------------------------------------------------------------------- 
-- ng_opticalproperties 
-- -------------------------------------------------------------------- 
CREATE TABLE ng_opticalproperties
(
    id BIGINT NOT NULL,
    glazingratio NUMERIC,
    glazingratio_uom VARCHAR(1000),
    PRIMARY KEY (id)
);

-- -------------------------------------------------------------------- 
-- ng_periodofyear 
-- -------------------------------------------------------------------- 
CREATE TABLE ng_periodofyear
(
    id BIGINT NOT NULL,
    objectclass_id INTEGER,
    schedule_periodofyear_id BIGINT,
    timeperiodprop_beginposition TIMESTAMP WITH TIME ZONE,
    timeperiodproper_endposition TIMESTAMP WITH TIME ZONE,
    PRIMARY KEY (id)
);

-- -------------------------------------------------------------------- 
-- ng_reflectance 
-- -------------------------------------------------------------------- 
CREATE TABLE ng_reflectance
(
    id BIGINT NOT NULL,
    fraction NUMERIC,
    fraction_uom VARCHAR(1000),
    opticalproper_reflectance_id BIGINT,
    surface VARCHAR(1000),
    wavelengthrange VARCHAR(1000),
    PRIMARY KEY (id)
);

-- -------------------------------------------------------------------- 
-- ng_regulartimeseries 
-- -------------------------------------------------------------------- 
CREATE TABLE ng_regulartimeseries
(
    id BIGINT NOT NULL,
    timeinterval NUMERIC,
    timeinterval_factor INTEGER,
    timeinterval_radix INTEGER,
    timeinterval_unit VARCHAR(1000),
    timeperiodprop_beginposition TIMESTAMP WITH TIME ZONE,
    timeperiodproper_endposition TIMESTAMP WITH TIME ZONE,
    values_ TEXT,
    values_uom VARCHAR(1000),
    PRIMARY KEY (id)
);

-- -------------------------------------------------------------------- 
-- ng_regulartimeseriesfile 
-- -------------------------------------------------------------------- 
CREATE TABLE ng_regulartimeseriesfile
(
    id BIGINT NOT NULL,
    decimalsymbol VARCHAR(1000),
    fieldseparator VARCHAR(1000),
    file_ VARCHAR(1000),
    numberofheaderlines INTEGER,
    recordseparator VARCHAR(1000),
    timeinterval NUMERIC,
    timeinterval_factor INTEGER,
    timeinterval_radix INTEGER,
    timeinterval_unit VARCHAR(1000),
    timeperiodprop_beginposition TIMESTAMP WITH TIME ZONE,
    timeperiodproper_endposition TIMESTAMP WITH TIME ZONE,
    uom VARCHAR(1000),
    valuecolumnnumber INTEGER,
    PRIMARY KEY (id)
);

-- -------------------------------------------------------------------- 
-- ng_schedule 
-- -------------------------------------------------------------------- 
CREATE TABLE ng_schedule
(
    id BIGINT NOT NULL,
    PRIMARY KEY (id)
);

-- -------------------------------------------------------------------- 
-- ng_solidmaterial 
-- -------------------------------------------------------------------- 
CREATE TABLE ng_solidmaterial
(
    id BIGINT NOT NULL,
    conductivity NUMERIC,
    conductivity_uom VARCHAR(1000),
    density NUMERIC,
    density_uom VARCHAR(1000),
    permeance NUMERIC,
    permeance_uom VARCHAR(1000),
    specificheat NUMERIC,
    specificheat_uom VARCHAR(1000),
    PRIMARY KEY (id)
);

-- -------------------------------------------------------------------- 
-- ng_ther_boun_to_ther_deli 
-- -------------------------------------------------------------------- 
CREATE TABLE ng_ther_boun_to_ther_deli
(
    thermalboundary_delimits_id BIGINT NOT NULL,
    thermalzone_boundedby_id BIGINT NOT NULL,
    PRIMARY KEY (thermalboundary_delimits_id, thermalzone_boundedby_id)
);

-- -------------------------------------------------------------------- 
-- ng_thermalboundary 
-- -------------------------------------------------------------------- 
CREATE TABLE ng_thermalboundary
(
    id BIGINT NOT NULL,
    area NUMERIC,
    area_uom VARCHAR(1000),
    azimuth NUMERIC,
    azimuth_uom VARCHAR(1000),
    construction_id BIGINT,
    inclination NUMERIC,
    inclination_uom VARCHAR(1000),
    surfacegeometry_id BIGINT,
    thermalboundarytype VARCHAR(1000),
    thermalzone_boundedby_id BIGINT,
    PRIMARY KEY (id)
);

-- -------------------------------------------------------------------- 
-- ng_thermalopening 
-- -------------------------------------------------------------------- 
CREATE TABLE ng_thermalopening
(
    id BIGINT NOT NULL,
    area NUMERIC,
    area_uom VARCHAR(1000),
    construction_id BIGINT,
    surfacegeometry_id BIGINT,
    thermalboundary_contains_id BIGINT,
    PRIMARY KEY (id)
);

-- -------------------------------------------------------------------- 
-- ng_thermalzone 
-- -------------------------------------------------------------------- 
CREATE TABLE ng_thermalzone
(
    id BIGINT NOT NULL,
    building_thermalzone_id BIGINT,
    infiltrationrate NUMERIC,
    infiltrationrate_uom VARCHAR(1000),
    iscooled NUMERIC,
    isheated NUMERIC,
    volumegeometry_id BIGINT,
    PRIMARY KEY (id)
);

-- -------------------------------------------------------------------- 
-- ng_timeseries 
-- -------------------------------------------------------------------- 
CREATE TABLE ng_timeseries
(
    id BIGINT NOT NULL,
    objectclass_id INTEGER,
    timevaluesprop_acquisitionme VARCHAR(1000),
    timevaluesprop_interpolation VARCHAR(1000),
    timevaluesprop_qualitydescri VARCHAR(1000),
    timevaluesprop_thematicdescr VARCHAR(1000),
    timevaluespropertiest_source VARCHAR(1000),
    PRIMARY KEY (id)
);

-- -------------------------------------------------------------------- 
-- ng_timevaluesproperties 
-- -------------------------------------------------------------------- 
CREATE TABLE ng_timevaluesproperties
(
    id BIGINT NOT NULL,
    acquisitionmethod VARCHAR(1000),
    interpolationtype VARCHAR(1000),
    qualitydescription VARCHAR(1000),
    source VARCHAR(1000),
    thematicdescription VARCHAR(1000),
    PRIMARY KEY (id)
);

-- -------------------------------------------------------------------- 
-- ng_transmittance 
-- -------------------------------------------------------------------- 
CREATE TABLE ng_transmittance
(
    id BIGINT NOT NULL,
    fraction NUMERIC,
    fraction_uom VARCHAR(1000),
    opticalprope_transmittanc_id BIGINT,
    wavelengthrange VARCHAR(1000),
    PRIMARY KEY (id)
);

-- -------------------------------------------------------------------- 
-- ng_usagezone 
-- -------------------------------------------------------------------- 
CREATE TABLE ng_usagezone
(
    id BIGINT NOT NULL,
    building_usagezone_id BIGINT,
    coolingschedule_id BIGINT,
    heatingschedule_id BIGINT,
    thermalzone_contains_id BIGINT,
    usagezonetype VARCHAR(1000),
    usagezonetype_codespace VARCHAR(1000),
    ventilationschedule_id BIGINT,
    PRIMARY KEY (id)
);

-- -------------------------------------------------------------------- 
-- ng_volumetype 
-- -------------------------------------------------------------------- 
CREATE TABLE ng_volumetype
(
    id BIGINT NOT NULL,
    building_volume_id BIGINT,
    thermalzone_volume_id BIGINT,
    type VARCHAR(1000),
    value NUMERIC,
    value_uom VARCHAR(1000),
    PRIMARY KEY (id)
);

-- -------------------------------------------------------------------- 
-- ng_weatherdata 
-- -------------------------------------------------------------------- 
CREATE TABLE ng_weatherdata
(
    id BIGINT NOT NULL,
    cityobject_weatherdata_id BIGINT,
    position geometry(GEOMETRYZ),
    values_id BIGINT,
    weatherdatatype VARCHAR(1000),
    weatherstation_parameter_id BIGINT,
    PRIMARY KEY (id)
);

-- -------------------------------------------------------------------- 
-- ng_weatherstation 
-- -------------------------------------------------------------------- 
CREATE TABLE ng_weatherstation
(
    id BIGINT NOT NULL,
    genericapplicationpropertyof TEXT,
    position geometry(GEOMETRYZ),
    stationname VARCHAR(1000),
    PRIMARY KEY (id)
);

-- ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ 
-- *********************************** Create foreign keys ******************************** 
-- ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ 
-- -------------------------------------------------------------------- 
-- ng_building 
-- -------------------------------------------------------------------- 
ALTER TABLE ng_building ADD CONSTRAINT ng_building_fk FOREIGN KEY (id)
REFERENCES building (id);

-- -------------------------------------------------------------------- 
-- ng_cityobject 
-- -------------------------------------------------------------------- 
ALTER TABLE ng_cityobject ADD CONSTRAINT ng_cityobject_fk FOREIGN KEY (id)
REFERENCES cityobject (id);

-- -------------------------------------------------------------------- 
-- ng_construction 
-- -------------------------------------------------------------------- 
ALTER TABLE ng_construction ADD CONSTRAINT ng_construction_fk FOREIGN KEY (id)
REFERENCES cityobject (id);

ALTER TABLE ng_construction ADD CONSTRAINT ng_constructi_opticalpr_fk FOREIGN KEY (opticalproperties_id)
REFERENCES ng_opticalproperties (id)
ON DELETE SET NULL;

-- -------------------------------------------------------------------- 
-- ng_dailyschedule 
-- -------------------------------------------------------------------- 
ALTER TABLE ng_dailyschedule ADD CONSTRAINT ng_dailyschedu_schedule_fk FOREIGN KEY (schedule_id)
REFERENCES ng_timeseries (id)
ON DELETE SET NULL;

ALTER TABLE ng_dailyschedule ADD CONSTRAINT ng_dailys_period_dailys_fk FOREIGN KEY (periodofyear_dailyschedul_id)
REFERENCES ng_periodofyear (id);

-- -------------------------------------------------------------------- 
-- ng_energydem_to_cityobjec 
-- -------------------------------------------------------------------- 
ALTER TABLE ng_energydem_to_cityobjec ADD CONSTRAINT ng_energyde_to_cityobj_fk1 FOREIGN KEY (energydemand_id)
REFERENCES ng_energydemand (id)
ON DELETE CASCADE;

ALTER TABLE ng_energydem_to_cityobjec ADD CONSTRAINT ng_energyde_to_cityobj_fk2 FOREIGN KEY (cityobject_id)
REFERENCES cityobject (id)
ON DELETE CASCADE;

-- -------------------------------------------------------------------- 
-- ng_energydemand 
-- -------------------------------------------------------------------- 
ALTER TABLE ng_energydemand ADD CONSTRAINT ng_energydemand_fk FOREIGN KEY (id)
REFERENCES cityobject (id);

ALTER TABLE ng_energydemand ADD CONSTRAINT ng_energydema_energyamo_fk FOREIGN KEY (energyamount_id)
REFERENCES ng_timeseries (id)
ON DELETE SET NULL;

ALTER TABLE ng_energydemand ADD CONSTRAINT ng_energy_cityob_demand_fk FOREIGN KEY (cityobject_demands_id)
REFERENCES ng_cityobject (id)
ON DELETE SET NULL;

-- -------------------------------------------------------------------- 
-- ng_facilities 
-- -------------------------------------------------------------------- 
ALTER TABLE ng_facilities ADD CONSTRAINT ng_facilities_objectcla_fk FOREIGN KEY (objectclass_id)
REFERENCES objectclass (id);

ALTER TABLE ng_facilities ADD CONSTRAINT ng_facilities_fk FOREIGN KEY (id)
REFERENCES cityobject (id);

ALTER TABLE ng_facilities ADD CONSTRAINT ng_facili_usagez_equipp_fk FOREIGN KEY (usagezone_equippedwith_id)
REFERENCES ng_usagezone (id);

ALTER TABLE ng_facilities ADD CONSTRAINT ng_facilities_operation_fk FOREIGN KEY (operationschedule_id)
REFERENCES ng_schedule (id)
ON DELETE SET NULL;

ALTER TABLE ng_facilities ADD CONSTRAINT ng_facilities_heatdissi_fk FOREIGN KEY (heatdissipation_id)
REFERENCES ng_heatexchangetype (id)
ON DELETE SET NULL;

-- -------------------------------------------------------------------- 
-- ng_floorarea 
-- -------------------------------------------------------------------- 
ALTER TABLE ng_floorarea ADD CONSTRAINT ng_floora_buildi_floora_fk FOREIGN KEY (building_floorarea_id)
REFERENCES ng_building (id);

ALTER TABLE ng_floorarea ADD CONSTRAINT ng_floora_therma_floora_fk FOREIGN KEY (thermalzone_floorarea_id)
REFERENCES ng_thermalzone (id);

ALTER TABLE ng_floorarea ADD CONSTRAINT ng_floora_usagez_floora_fk FOREIGN KEY (usagezone_floorarea_id)
REFERENCES ng_usagezone (id);

-- -------------------------------------------------------------------- 
-- ng_gas 
-- -------------------------------------------------------------------- 
ALTER TABLE ng_gas ADD CONSTRAINT ng_gas_fk FOREIGN KEY (id)
REFERENCES ng_material (id);

-- -------------------------------------------------------------------- 
-- ng_heightaboveground 
-- -------------------------------------------------------------------- 
ALTER TABLE ng_heightaboveground ADD CONSTRAINT ng_height_buildi_height_fk FOREIGN KEY (building_heightabovegroun_id)
REFERENCES ng_building (id);

-- -------------------------------------------------------------------- 
-- ng_layer 
-- -------------------------------------------------------------------- 
ALTER TABLE ng_layer ADD CONSTRAINT ng_layer_fk FOREIGN KEY (id)
REFERENCES cityobject (id);

ALTER TABLE ng_layer ADD CONSTRAINT ng_layer_construc_layer_fk FOREIGN KEY (construction_layer_id)
REFERENCES ng_construction (id);

-- -------------------------------------------------------------------- 
-- ng_layercomponent 
-- -------------------------------------------------------------------- 
ALTER TABLE ng_layercomponent ADD CONSTRAINT ng_layercomponent_fk FOREIGN KEY (id)
REFERENCES cityobject (id);

ALTER TABLE ng_layercomponent ADD CONSTRAINT ng_layercompon_material_fk FOREIGN KEY (material_id)
REFERENCES ng_material (id)
ON DELETE SET NULL;

ALTER TABLE ng_layercomponent ADD CONSTRAINT ng_layerco_layer_layerc_fk FOREIGN KEY (layer_layercomponent_id)
REFERENCES ng_layer (id);

-- -------------------------------------------------------------------- 
-- ng_material 
-- -------------------------------------------------------------------- 
ALTER TABLE ng_material ADD CONSTRAINT ng_material_fk FOREIGN KEY (id)
REFERENCES cityobject (id);

ALTER TABLE ng_material ADD CONSTRAINT ng_material_objectclass_fk FOREIGN KEY (objectclass_id)
REFERENCES objectclass (id);

-- -------------------------------------------------------------------- 
-- ng_occupants 
-- -------------------------------------------------------------------- 
ALTER TABLE ng_occupants ADD CONSTRAINT ng_occupants_fk FOREIGN KEY (id)
REFERENCES cityobject (id);

ALTER TABLE ng_occupants ADD CONSTRAINT ng_occupa_usagez_occupi_fk FOREIGN KEY (usagezone_occupiedby_id)
REFERENCES ng_usagezone (id);

ALTER TABLE ng_occupants ADD CONSTRAINT ng_occupants_heatdissip_fk FOREIGN KEY (heatdissipation_id)
REFERENCES ng_heatexchangetype (id)
ON DELETE SET NULL;

ALTER TABLE ng_occupants ADD CONSTRAINT ng_occupants_occupancyr_fk FOREIGN KEY (occupancyrate_id)
REFERENCES ng_schedule (id)
ON DELETE SET NULL;

-- -------------------------------------------------------------------- 
-- ng_periodofyear 
-- -------------------------------------------------------------------- 
ALTER TABLE ng_periodofyear ADD CONSTRAINT ng_periodofye_objectcla_fk FOREIGN KEY (objectclass_id)
REFERENCES objectclass (id);

ALTER TABLE ng_periodofyear ADD CONSTRAINT ng_period_schedu_period_fk FOREIGN KEY (schedule_periodofyear_id)
REFERENCES ng_schedule (id);

-- -------------------------------------------------------------------- 
-- ng_reflectance 
-- -------------------------------------------------------------------- 
ALTER TABLE ng_reflectance ADD CONSTRAINT ng_reflec_optica_reflec_fk FOREIGN KEY (opticalproper_reflectance_id)
REFERENCES ng_opticalproperties (id);

-- -------------------------------------------------------------------- 
-- ng_regulartimeseries 
-- -------------------------------------------------------------------- 
ALTER TABLE ng_regulartimeseries ADD CONSTRAINT ng_regulartimeseries_fk FOREIGN KEY (id)
REFERENCES ng_timeseries (id);

-- -------------------------------------------------------------------- 
-- ng_regulartimeseriesfile 
-- -------------------------------------------------------------------- 
ALTER TABLE ng_regulartimeseriesfile ADD CONSTRAINT ng_regulartimeseriesfil_fk FOREIGN KEY (id)
REFERENCES ng_timeseries (id);

-- -------------------------------------------------------------------- 
-- ng_schedule 
-- -------------------------------------------------------------------- 
ALTER TABLE ng_schedule ADD CONSTRAINT ng_schedule_fk FOREIGN KEY (id)
REFERENCES cityobject (id);

-- -------------------------------------------------------------------- 
-- ng_solidmaterial 
-- -------------------------------------------------------------------- 
ALTER TABLE ng_solidmaterial ADD CONSTRAINT ng_solidmaterial_fk FOREIGN KEY (id)
REFERENCES ng_material (id);

-- -------------------------------------------------------------------- 
-- ng_ther_boun_to_ther_deli 
-- -------------------------------------------------------------------- 
ALTER TABLE ng_ther_boun_to_ther_deli ADD CONSTRAINT ng_ther_bou_to_the_del_fk1 FOREIGN KEY (thermalzone_boundedby_id)
REFERENCES ng_thermalzone (id);

ALTER TABLE ng_ther_boun_to_ther_deli ADD CONSTRAINT ng_ther_bou_to_the_del_fk2 FOREIGN KEY (thermalboundary_delimits_id)
REFERENCES ng_thermalboundary (id)
ON DELETE CASCADE;

-- -------------------------------------------------------------------- 
-- ng_thermalboundary 
-- -------------------------------------------------------------------- 
ALTER TABLE ng_thermalboundary ADD CONSTRAINT ng_thermalboundary_fk FOREIGN KEY (id)
REFERENCES cityobject (id);

ALTER TABLE ng_thermalboundary ADD CONSTRAINT ng_therma_therma_bounde_fk FOREIGN KEY (thermalzone_boundedby_id)
REFERENCES ng_thermalzone (id);

ALTER TABLE ng_thermalboundary ADD CONSTRAINT ng_thermalbou_construct_fk FOREIGN KEY (construction_id)
REFERENCES ng_construction (id)
ON DELETE SET NULL;

ALTER TABLE ng_thermalboundary ADD CONSTRAINT ng_thermalbou_surfacege_fk FOREIGN KEY (surfacegeometry_id)
REFERENCES surface_geometry (id);

-- -------------------------------------------------------------------- 
-- ng_thermalopening 
-- -------------------------------------------------------------------- 
ALTER TABLE ng_thermalopening ADD CONSTRAINT ng_thermalopening_fk FOREIGN KEY (id)
REFERENCES cityobject (id);

ALTER TABLE ng_thermalopening ADD CONSTRAINT ng_thermalope_construct_fk FOREIGN KEY (construction_id)
REFERENCES ng_construction (id)
ON DELETE SET NULL;

ALTER TABLE ng_thermalopening ADD CONSTRAINT ng_therma_therma_contai_fk FOREIGN KEY (thermalboundary_contains_id)
REFERENCES ng_thermalboundary (id);

ALTER TABLE ng_thermalopening ADD CONSTRAINT ng_thermalope_surfacege_fk FOREIGN KEY (surfacegeometry_id)
REFERENCES surface_geometry (id);

-- -------------------------------------------------------------------- 
-- ng_thermalzone 
-- -------------------------------------------------------------------- 
ALTER TABLE ng_thermalzone ADD CONSTRAINT ng_thermalzone_fk FOREIGN KEY (id)
REFERENCES cityobject (id);

ALTER TABLE ng_thermalzone ADD CONSTRAINT ng_therma_buildi_therma_fk FOREIGN KEY (building_thermalzone_id)
REFERENCES ng_building (id);

ALTER TABLE ng_thermalzone ADD CONSTRAINT ng_thermalzon_volumegeo_fk FOREIGN KEY (volumegeometry_id)
REFERENCES surface_geometry (id);

-- -------------------------------------------------------------------- 
-- ng_timeseries 
-- -------------------------------------------------------------------- 
ALTER TABLE ng_timeseries ADD CONSTRAINT ng_timeseries_fk FOREIGN KEY (id)
REFERENCES cityobject (id);

ALTER TABLE ng_timeseries ADD CONSTRAINT ng_timeseries_objectcla_fk FOREIGN KEY (objectclass_id)
REFERENCES objectclass (id);

-- -------------------------------------------------------------------- 
-- ng_transmittance 
-- -------------------------------------------------------------------- 
ALTER TABLE ng_transmittance ADD CONSTRAINT ng_transm_optica_transm_fk FOREIGN KEY (opticalprope_transmittanc_id)
REFERENCES ng_opticalproperties (id);

-- -------------------------------------------------------------------- 
-- ng_usagezone 
-- -------------------------------------------------------------------- 
ALTER TABLE ng_usagezone ADD CONSTRAINT ng_usagezone_fk FOREIGN KEY (id)
REFERENCES cityobject (id);

ALTER TABLE ng_usagezone ADD CONSTRAINT ng_usagezone_coolingsch_fk FOREIGN KEY (coolingschedule_id)
REFERENCES ng_schedule (id)
ON DELETE SET NULL;

ALTER TABLE ng_usagezone ADD CONSTRAINT ng_usagezone_heatingsch_fk FOREIGN KEY (heatingschedule_id)
REFERENCES ng_schedule (id)
ON DELETE SET NULL;

ALTER TABLE ng_usagezone ADD CONSTRAINT ng_usagez_buildi_usagez_fk FOREIGN KEY (building_usagezone_id)
REFERENCES ng_building (id);

ALTER TABLE ng_usagezone ADD CONSTRAINT ng_usagezone_ventilatio_fk FOREIGN KEY (ventilationschedule_id)
REFERENCES ng_schedule (id)
ON DELETE SET NULL;

ALTER TABLE ng_usagezone ADD CONSTRAINT ng_usagez_therma_contai_fk FOREIGN KEY (thermalzone_contains_id)
REFERENCES ng_thermalzone (id)
ON DELETE SET NULL;

-- -------------------------------------------------------------------- 
-- ng_volumetype 
-- -------------------------------------------------------------------- 
ALTER TABLE ng_volumetype ADD CONSTRAINT ng_volume_buildi_volume_fk FOREIGN KEY (building_volume_id)
REFERENCES ng_building (id);

ALTER TABLE ng_volumetype ADD CONSTRAINT ng_volume_therma_volume_fk FOREIGN KEY (thermalzone_volume_id)
REFERENCES ng_thermalzone (id);

-- -------------------------------------------------------------------- 
-- ng_weatherdata 
-- -------------------------------------------------------------------- 
ALTER TABLE ng_weatherdata ADD CONSTRAINT ng_weatherdata_fk FOREIGN KEY (id)
REFERENCES cityobject (id);

ALTER TABLE ng_weatherdata ADD CONSTRAINT ng_weatherdata_values_fk FOREIGN KEY (values_id)
REFERENCES ng_timeseries (id)
ON DELETE SET NULL;

ALTER TABLE ng_weatherdata ADD CONSTRAINT ng_weathe_cityob_weathe_fk FOREIGN KEY (cityobject_weatherdata_id)
REFERENCES ng_cityobject (id)
ON DELETE SET NULL;

ALTER TABLE ng_weatherdata ADD CONSTRAINT ng_weathe_weathe_parame_fk FOREIGN KEY (weatherstation_parameter_id)
REFERENCES ng_weatherstation (id)
ON DELETE SET NULL;

-- -------------------------------------------------------------------- 
-- ng_weatherstation 
-- -------------------------------------------------------------------- 
ALTER TABLE ng_weatherstation ADD CONSTRAINT ng_weatherstation_fk FOREIGN KEY (id)
REFERENCES cityobject (id);

-- ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ 
-- *********************************** Create Indexes ************************************* 
-- ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ 
-- -------------------------------------------------------------------- 
-- ng_building 
-- -------------------------------------------------------------------- 
CREATE INDEX ng_building_referencep_spx ON ng_building
    USING gist
    (
      referencepoint
    );

-- -------------------------------------------------------------------- 
-- ng_construction 
-- -------------------------------------------------------------------- 
CREATE INDEX ng_construct_opticalpr_fkx ON ng_construction
    USING btree
    (
      opticalproperties_id ASC NULLS LAST
    )   WITH (FILLFACTOR = 90);

-- -------------------------------------------------------------------- 
-- ng_dailyschedule 
-- -------------------------------------------------------------------- 
CREATE INDEX ng_dailys_period_daily_fkx ON ng_dailyschedule
    USING btree
    (
      periodofyear_dailyschedul_id ASC NULLS LAST
    )   WITH (FILLFACTOR = 90);

CREATE INDEX ng_dailysched_schedule_fkx ON ng_dailyschedule
    USING btree
    (
      schedule_id ASC NULLS LAST
    )   WITH (FILLFACTOR = 90);

-- -------------------------------------------------------------------- 
-- ng_energydem_to_cityobjec 
-- -------------------------------------------------------------------- 
CREATE INDEX ng_energyd_to_cityobj_fk2x ON ng_energydem_to_cityobjec
    USING btree
    (
      cityobject_id ASC NULLS LAST
    )   WITH (FILLFACTOR = 90);

CREATE INDEX ng_energyd_to_cityobj_fk1x ON ng_energydem_to_cityobjec
    USING btree
    (
      energydemand_id ASC NULLS LAST
    )   WITH (FILLFACTOR = 90);

-- -------------------------------------------------------------------- 
-- ng_energydemand 
-- -------------------------------------------------------------------- 
CREATE INDEX ng_energy_cityob_deman_fkx ON ng_energydemand
    USING btree
    (
      cityobject_demands_id ASC NULLS LAST
    )   WITH (FILLFACTOR = 90);

CREATE INDEX ng_energydem_energyamo_fkx ON ng_energydemand
    USING btree
    (
      energyamount_id ASC NULLS LAST
    )   WITH (FILLFACTOR = 90);

-- -------------------------------------------------------------------- 
-- ng_facilities 
-- -------------------------------------------------------------------- 
CREATE INDEX ng_facilitie_heatdissi_fkx ON ng_facilities
    USING btree
    (
      heatdissipation_id ASC NULLS LAST
    )   WITH (FILLFACTOR = 90);

CREATE INDEX ng_facilitie_objectcla_fkx ON ng_facilities
    USING btree
    (
      objectclass_id ASC NULLS LAST
    )   WITH (FILLFACTOR = 90);

CREATE INDEX ng_facilitie_operation_fkx ON ng_facilities
    USING btree
    (
      operationschedule_id ASC NULLS LAST
    )   WITH (FILLFACTOR = 90);

CREATE INDEX ng_facili_usagez_equip_fkx ON ng_facilities
    USING btree
    (
      usagezone_equippedwith_id ASC NULLS LAST
    )   WITH (FILLFACTOR = 90);

-- -------------------------------------------------------------------- 
-- ng_floorarea 
-- -------------------------------------------------------------------- 
CREATE INDEX ng_floora_buildi_floor_fkx ON ng_floorarea
    USING btree
    (
      building_floorarea_id ASC NULLS LAST
    )   WITH (FILLFACTOR = 90);

CREATE INDEX ng_floora_therma_floor_fkx ON ng_floorarea
    USING btree
    (
      thermalzone_floorarea_id ASC NULLS LAST
    )   WITH (FILLFACTOR = 90);

CREATE INDEX ng_floora_usagez_floor_fkx ON ng_floorarea
    USING btree
    (
      usagezone_floorarea_id ASC NULLS LAST
    )   WITH (FILLFACTOR = 90);

-- -------------------------------------------------------------------- 
-- ng_heightaboveground 
-- -------------------------------------------------------------------- 
CREATE INDEX ng_height_buildi_heigh_fkx ON ng_heightaboveground
    USING btree
    (
      building_heightabovegroun_id ASC NULLS LAST
    )   WITH (FILLFACTOR = 90);

-- -------------------------------------------------------------------- 
-- ng_layer 
-- -------------------------------------------------------------------- 
CREATE INDEX ng_layer_constru_layer_fkx ON ng_layer
    USING btree
    (
      construction_layer_id ASC NULLS LAST
    )   WITH (FILLFACTOR = 90);

-- -------------------------------------------------------------------- 
-- ng_layercomponent 
-- -------------------------------------------------------------------- 
CREATE INDEX ng_layerc_layer_layerc_fkx ON ng_layercomponent
    USING btree
    (
      layer_layercomponent_id ASC NULLS LAST
    )   WITH (FILLFACTOR = 90);

CREATE INDEX ng_layercompo_material_fkx ON ng_layercomponent
    USING btree
    (
      material_id ASC NULLS LAST
    )   WITH (FILLFACTOR = 90);

-- -------------------------------------------------------------------- 
-- ng_material 
-- -------------------------------------------------------------------- 
CREATE INDEX ng_material_objectclas_fkx ON ng_material
    USING btree
    (
      objectclass_id ASC NULLS LAST
    )   WITH (FILLFACTOR = 90);

-- -------------------------------------------------------------------- 
-- ng_occupants 
-- -------------------------------------------------------------------- 
CREATE INDEX ng_occupants_heatdissi_fkx ON ng_occupants
    USING btree
    (
      heatdissipation_id ASC NULLS LAST
    )   WITH (FILLFACTOR = 90);

CREATE INDEX ng_occupants_occupancy_fkx ON ng_occupants
    USING btree
    (
      occupancyrate_id ASC NULLS LAST
    )   WITH (FILLFACTOR = 90);

CREATE INDEX ng_occupa_usagez_occup_fkx ON ng_occupants
    USING btree
    (
      usagezone_occupiedby_id ASC NULLS LAST
    )   WITH (FILLFACTOR = 90);

-- -------------------------------------------------------------------- 
-- ng_periodofyear 
-- -------------------------------------------------------------------- 
CREATE INDEX ng_periodofy_objectcla_fkx ON ng_periodofyear
    USING btree
    (
      objectclass_id ASC NULLS LAST
    )   WITH (FILLFACTOR = 90);

CREATE INDEX ng_period_schedu_perio_fkx ON ng_periodofyear
    USING btree
    (
      schedule_periodofyear_id ASC NULLS LAST
    )   WITH (FILLFACTOR = 90);

-- -------------------------------------------------------------------- 
-- ng_reflectance 
-- -------------------------------------------------------------------- 
CREATE INDEX ng_reflec_optica_refle_fkx ON ng_reflectance
    USING btree
    (
      opticalproper_reflectance_id ASC NULLS LAST
    )   WITH (FILLFACTOR = 90);

-- -------------------------------------------------------------------- 
-- ng_ther_boun_to_ther_deli 
-- -------------------------------------------------------------------- 
CREATE INDEX ng_ther_bou_to_the_del_fk2 ON ng_ther_boun_to_ther_deli
    USING btree
    (
      thermalboundary_delimits_id ASC NULLS LAST
    )   WITH (FILLFACTOR = 90);

CREATE INDEX ng_ther_bou_to_the_del_fk1 ON ng_ther_boun_to_ther_deli
    USING btree
    (
      thermalzone_boundedby_id ASC NULLS LAST
    )   WITH (FILLFACTOR = 90);

-- -------------------------------------------------------------------- 
-- ng_thermalboundary 
-- -------------------------------------------------------------------- 
CREATE INDEX ng_thermalbo_construct_fkx ON ng_thermalboundary
    USING btree
    (
      construction_id ASC NULLS LAST
    )   WITH (FILLFACTOR = 90);

CREATE INDEX ng_thermalbo_surfacege_fkx ON ng_thermalboundary
    USING btree
    (
      surfacegeometry_id ASC NULLS LAST
    )   WITH (FILLFACTOR = 90);

CREATE INDEX ng_therma_therma_bound_fkx ON ng_thermalboundary
    USING btree
    (
      thermalzone_boundedby_id ASC NULLS LAST
    )   WITH (FILLFACTOR = 90);

-- -------------------------------------------------------------------- 
-- ng_thermalopening 
-- -------------------------------------------------------------------- 
CREATE INDEX ng_thermalop_construct_fkx ON ng_thermalopening
    USING btree
    (
      construction_id ASC NULLS LAST
    )   WITH (FILLFACTOR = 90);

CREATE INDEX ng_thermalop_surfacege_fkx ON ng_thermalopening
    USING btree
    (
      surfacegeometry_id ASC NULLS LAST
    )   WITH (FILLFACTOR = 90);

CREATE INDEX ng_therma_therma_conta_fkx ON ng_thermalopening
    USING btree
    (
      thermalboundary_contains_id ASC NULLS LAST
    )   WITH (FILLFACTOR = 90);

-- -------------------------------------------------------------------- 
-- ng_thermalzone 
-- -------------------------------------------------------------------- 
CREATE INDEX ng_therma_buildi_therm_fkx ON ng_thermalzone
    USING btree
    (
      building_thermalzone_id ASC NULLS LAST
    )   WITH (FILLFACTOR = 90);

CREATE INDEX ng_thermalzo_volumegeo_fkx ON ng_thermalzone
    USING btree
    (
      volumegeometry_id ASC NULLS LAST
    )   WITH (FILLFACTOR = 90);

-- -------------------------------------------------------------------- 
-- ng_timeseries 
-- -------------------------------------------------------------------- 
CREATE INDEX ng_timeserie_objectcla_fkx ON ng_timeseries
    USING btree
    (
      objectclass_id ASC NULLS LAST
    )   WITH (FILLFACTOR = 90);

-- -------------------------------------------------------------------- 
-- ng_transmittance 
-- -------------------------------------------------------------------- 
CREATE INDEX ng_transm_optica_trans_fkx ON ng_transmittance
    USING btree
    (
      opticalprope_transmittanc_id ASC NULLS LAST
    )   WITH (FILLFACTOR = 90);

-- -------------------------------------------------------------------- 
-- ng_usagezone 
-- -------------------------------------------------------------------- 
CREATE INDEX ng_usagez_buildi_usage_fkx ON ng_usagezone
    USING btree
    (
      building_usagezone_id ASC NULLS LAST
    )   WITH (FILLFACTOR = 90);

CREATE INDEX ng_usagezone_coolingsc_fkx ON ng_usagezone
    USING btree
    (
      coolingschedule_id ASC NULLS LAST
    )   WITH (FILLFACTOR = 90);

CREATE INDEX ng_usagezone_heatingsc_fkx ON ng_usagezone
    USING btree
    (
      heatingschedule_id ASC NULLS LAST
    )   WITH (FILLFACTOR = 90);

CREATE INDEX ng_usagez_therma_conta_fkx ON ng_usagezone
    USING btree
    (
      thermalzone_contains_id ASC NULLS LAST
    )   WITH (FILLFACTOR = 90);

CREATE INDEX ng_usagezone_ventilati_fkx ON ng_usagezone
    USING btree
    (
      ventilationschedule_id ASC NULLS LAST
    )   WITH (FILLFACTOR = 90);

-- -------------------------------------------------------------------- 
-- ng_volumetype 
-- -------------------------------------------------------------------- 
CREATE INDEX ng_volume_buildi_volum_fkx ON ng_volumetype
    USING btree
    (
      building_volume_id ASC NULLS LAST
    )   WITH (FILLFACTOR = 90);

CREATE INDEX ng_volume_therma_volum_fkx ON ng_volumetype
    USING btree
    (
      thermalzone_volume_id ASC NULLS LAST
    )   WITH (FILLFACTOR = 90);

-- -------------------------------------------------------------------- 
-- ng_weatherdata 
-- -------------------------------------------------------------------- 
CREATE INDEX ng_weathe_cityob_weath_fkx ON ng_weatherdata
    USING btree
    (
      cityobject_weatherdata_id ASC NULLS LAST
    )   WITH (FILLFACTOR = 90);

CREATE INDEX ng_weatherdat_position_spx ON ng_weatherdata
    USING gist
    (
      position
    );

CREATE INDEX ng_weatherdata_values_fkx ON ng_weatherdata
    USING btree
    (
      values_id ASC NULLS LAST
    )   WITH (FILLFACTOR = 90);

CREATE INDEX ng_weathe_weathe_param_fkx ON ng_weatherdata
    USING btree
    (
      weatherstation_parameter_id ASC NULLS LAST
    )   WITH (FILLFACTOR = 90);

-- -------------------------------------------------------------------- 
-- ng_weatherstation 
-- -------------------------------------------------------------------- 
CREATE INDEX ng_weathersta_position_spx ON ng_weatherstation
    USING gist
    (
      position
    );

-- ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ 
-- *********************************** Create Sequences *********************************** 
-- ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ 

CREATE SEQUENCE ng_volumetype_seq
INCREMENT BY 1
MINVALUE 0
MAXVALUE 9223372036854775807
START WITH 1
CACHE 1
NO CYCLE
OWNED BY NONE;


CREATE SEQUENCE ng_floorarea_seq
INCREMENT BY 1
MINVALUE 0
MAXVALUE 9223372036854775807
START WITH 1
CACHE 1
NO CYCLE
OWNED BY NONE;


CREATE SEQUENCE ng_heightaboveground_seq
INCREMENT BY 1
MINVALUE 0
MAXVALUE 9223372036854775807
START WITH 1
CACHE 1
NO CYCLE
OWNED BY NONE;


CREATE SEQUENCE ng_heatexchangetype_seq
INCREMENT BY 1
MINVALUE 0
MAXVALUE 9223372036854775807
START WITH 1
CACHE 1
NO CYCLE
OWNED BY NONE;


CREATE SEQUENCE ng_transmittance_seq
INCREMENT BY 1
MINVALUE 0
MAXVALUE 9223372036854775807
START WITH 1
CACHE 1
NO CYCLE
OWNED BY NONE;


CREATE SEQUENCE ng_opticalproperties_seq
INCREMENT BY 1
MINVALUE 0
MAXVALUE 9223372036854775807
START WITH 1
CACHE 1
NO CYCLE
OWNED BY NONE;


CREATE SEQUENCE ng_reflectance_seq
INCREMENT BY 1
MINVALUE 0
MAXVALUE 9223372036854775807
START WITH 1
CACHE 1
NO CYCLE
OWNED BY NONE;


CREATE SEQUENCE ng_timevaluesproperti_seq
INCREMENT BY 1
MINVALUE 0
MAXVALUE 9223372036854775807
START WITH 1
CACHE 1
NO CYCLE
OWNED BY NONE;


CREATE SEQUENCE ng_periodofyear_seq
INCREMENT BY 1
MINVALUE 0
MAXVALUE 9223372036854775807
START WITH 1
CACHE 1
NO CYCLE
OWNED BY NONE;


CREATE SEQUENCE ng_dailyschedule_seq
INCREMENT BY 1
MINVALUE 0
MAXVALUE 9223372036854775807
START WITH 1
CACHE 1
NO CYCLE
OWNED BY NONE;


