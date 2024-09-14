-- This document was automatically created by the ADE-Manager tool of 3DCityDB (https://www.3dcitydb.org) on 2024-09-14 17:01:14 
-- ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ 
-- *********************************** Create Sequences *********************************** 
-- ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ 

CREATE SEQUENCE ng_heatexchangetype_seq INCREMENT BY 1 START WITH 1 MINVALUE 1 CACHE 10000;

CREATE SEQUENCE ng_volumetype_seq INCREMENT BY 1 START WITH 1 MINVALUE 1 CACHE 10000;

CREATE SEQUENCE ng_dailyschedule_seq INCREMENT BY 1 START WITH 1 MINVALUE 1 CACHE 10000;

CREATE SEQUENCE ng_timevaluesproperti_seq INCREMENT BY 1 START WITH 1 MINVALUE 1 CACHE 10000;

CREATE SEQUENCE ng_periodofyear_seq INCREMENT BY 1 START WITH 1 MINVALUE 1 CACHE 10000;

CREATE SEQUENCE ng_heightaboveground_seq INCREMENT BY 1 START WITH 1 MINVALUE 1 CACHE 10000;

CREATE SEQUENCE ng_floorarea_seq INCREMENT BY 1 START WITH 1 MINVALUE 1 CACHE 10000;

CREATE SEQUENCE ng_reflectance_seq INCREMENT BY 1 START WITH 1 MINVALUE 1 CACHE 10000;

CREATE SEQUENCE ng_transmittance_seq INCREMENT BY 1 START WITH 1 MINVALUE 1 CACHE 10000;

CREATE SEQUENCE ng_opticalproperties_seq INCREMENT BY 1 START WITH 1 MINVALUE 1 CACHE 10000;

-- ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ 
-- *********************************** Create tables ************************************** 
-- ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ 
-- -------------------------------------------------------------------- 
-- ng_building 
-- -------------------------------------------------------------------- 
CREATE TABLE ng_building
(
    id NUMBER(38) NOT NULL,
    buildingtype VARCHAR2(1000),
    buildingtype_codespace VARCHAR2(1000),
    constructionweight VARCHAR2(1000),
    referencepoint MDSYS.SDO_GEOMETRY,
    PRIMARY KEY (id)
);

-- -------------------------------------------------------------------- 
-- ng_cityobject 
-- -------------------------------------------------------------------- 
CREATE TABLE ng_cityobject
(
    id NUMBER(38) NOT NULL,
    PRIMARY KEY (id)
);

-- -------------------------------------------------------------------- 
-- ng_construction 
-- -------------------------------------------------------------------- 
CREATE TABLE ng_construction
(
    id NUMBER(38) NOT NULL,
    opticalproperties_id NUMBER(38),
    uvalue NUMBER,
    uvalue_uom VARCHAR2(1000),
    PRIMARY KEY (id)
);

-- -------------------------------------------------------------------- 
-- ng_dailyschedule 
-- -------------------------------------------------------------------- 
CREATE TABLE ng_dailyschedule
(
    id NUMBER(38) NOT NULL,
    daytype VARCHAR2(1000),
    periodofyear_dailyschedul_id NUMBER(38),
    schedule_id NUMBER(38),
    PRIMARY KEY (id)
);

-- -------------------------------------------------------------------- 
-- ng_energydem_to_cityobjec 
-- -------------------------------------------------------------------- 
CREATE TABLE ng_energydem_to_cityobjec
(
    cityobject_id NUMBER(38) NOT NULL,
    energydemand_id NUMBER(38) NOT NULL,
    PRIMARY KEY (cityobject_id, energydemand_id)
);

-- -------------------------------------------------------------------- 
-- ng_energydemand 
-- -------------------------------------------------------------------- 
CREATE TABLE ng_energydemand
(
    id NUMBER(38) NOT NULL,
    cityobject_demands_id NUMBER(38),
    enduse VARCHAR2(1000),
    energyamount_id NUMBER(38),
    energycarriertype VARCHAR2(1000),
    energycarriertype_codespace VARCHAR2(1000),
    maximumload NUMBER,
    maximumload_uom VARCHAR2(1000),
    PRIMARY KEY (id)
);

-- -------------------------------------------------------------------- 
-- ng_facilities 
-- -------------------------------------------------------------------- 
CREATE TABLE ng_facilities
(
    id NUMBER(38) NOT NULL,
    heatdissipation_id NUMBER(38),
    objectclass_id INTEGER,
    operationschedule_id NUMBER(38),
    usagezone_equippedwith_id NUMBER(38),
    PRIMARY KEY (id)
);

-- -------------------------------------------------------------------- 
-- ng_floorarea 
-- -------------------------------------------------------------------- 
CREATE TABLE ng_floorarea
(
    id NUMBER(38) NOT NULL,
    building_floorarea_id NUMBER(38),
    thermalzone_floorarea_id NUMBER(38),
    type VARCHAR2(1000),
    usagezone_floorarea_id NUMBER(38),
    value NUMBER,
    value_uom VARCHAR2(1000),
    PRIMARY KEY (id)
);

-- -------------------------------------------------------------------- 
-- ng_gas 
-- -------------------------------------------------------------------- 
CREATE TABLE ng_gas
(
    id NUMBER(38) NOT NULL,
    isventilated NUMBER,
    rvalue NUMBER,
    rvalue_uom VARCHAR2(1000),
    PRIMARY KEY (id)
);

-- -------------------------------------------------------------------- 
-- ng_heatexchangetype 
-- -------------------------------------------------------------------- 
CREATE TABLE ng_heatexchangetype
(
    id NUMBER(38) NOT NULL,
    convectivefraction NUMBER,
    convectivefraction_uom VARCHAR2(1000),
    latentfraction NUMBER,
    latentfraction_uom VARCHAR2(1000),
    radiantfraction NUMBER,
    radiantfraction_uom VARCHAR2(1000),
    totalvalue NUMBER,
    totalvalue_uom VARCHAR2(1000),
    PRIMARY KEY (id)
);

-- -------------------------------------------------------------------- 
-- ng_heightaboveground 
-- -------------------------------------------------------------------- 
CREATE TABLE ng_heightaboveground
(
    id NUMBER(38) NOT NULL,
    building_heightabovegroun_id NUMBER(38),
    heightreference VARCHAR2(1000),
    value NUMBER,
    value_uom VARCHAR2(1000),
    PRIMARY KEY (id)
);

-- -------------------------------------------------------------------- 
-- ng_layer 
-- -------------------------------------------------------------------- 
CREATE TABLE ng_layer
(
    id NUMBER(38) NOT NULL,
    construction_layer_id NUMBER(38),
    PRIMARY KEY (id)
);

-- -------------------------------------------------------------------- 
-- ng_layercomponent 
-- -------------------------------------------------------------------- 
CREATE TABLE ng_layercomponent
(
    id NUMBER(38) NOT NULL,
    areafraction NUMBER,
    areafraction_uom VARCHAR2(1000),
    layer_layercomponent_id NUMBER(38),
    material_id NUMBER(38),
    thickness NUMBER,
    thickness_uom VARCHAR2(1000),
    PRIMARY KEY (id)
);

-- -------------------------------------------------------------------- 
-- ng_material 
-- -------------------------------------------------------------------- 
CREATE TABLE ng_material
(
    id NUMBER(38) NOT NULL,
    objectclass_id INTEGER,
    PRIMARY KEY (id)
);

-- -------------------------------------------------------------------- 
-- ng_occupants 
-- -------------------------------------------------------------------- 
CREATE TABLE ng_occupants
(
    id NUMBER(38) NOT NULL,
    heatdissipation_id NUMBER(38),
    numberofoccupants INTEGER,
    occupancyrate_id NUMBER(38),
    usagezone_occupiedby_id NUMBER(38),
    PRIMARY KEY (id)
);

-- -------------------------------------------------------------------- 
-- ng_opticalproperties 
-- -------------------------------------------------------------------- 
CREATE TABLE ng_opticalproperties
(
    id NUMBER(38) NOT NULL,
    glazingratio NUMBER,
    glazingratio_uom VARCHAR2(1000),
    PRIMARY KEY (id)
);

-- -------------------------------------------------------------------- 
-- ng_periodofyear 
-- -------------------------------------------------------------------- 
CREATE TABLE ng_periodofyear
(
    id NUMBER(38) NOT NULL,
    objectclass_id INTEGER,
    schedule_periodofyear_id NUMBER(38),
    timeperiodprop_beginposition TIMESTAMP,
    timeperiodproper_endposition TIMESTAMP,
    PRIMARY KEY (id)
);

-- -------------------------------------------------------------------- 
-- ng_reflectance 
-- -------------------------------------------------------------------- 
CREATE TABLE ng_reflectance
(
    id NUMBER(38) NOT NULL,
    fraction NUMBER,
    fraction_uom VARCHAR2(1000),
    opticalproper_reflectance_id NUMBER(38),
    surface VARCHAR2(1000),
    wavelengthrange VARCHAR2(1000),
    PRIMARY KEY (id)
);

-- -------------------------------------------------------------------- 
-- ng_regulartimeseries 
-- -------------------------------------------------------------------- 
CREATE TABLE ng_regulartimeseries
(
    id NUMBER(38) NOT NULL,
    timeinterval NUMBER,
    timeinterval_factor INTEGER,
    timeinterval_radix INTEGER,
    timeinterval_unit VARCHAR2(1000),
    timeperiodprop_beginposition TIMESTAMP,
    timeperiodproper_endposition TIMESTAMP,
    values_ CLOB,
    values_uom VARCHAR2(1000),
    PRIMARY KEY (id)
);

-- -------------------------------------------------------------------- 
-- ng_regulartimeseriesfile 
-- -------------------------------------------------------------------- 
CREATE TABLE ng_regulartimeseriesfile
(
    id NUMBER(38) NOT NULL,
    decimalsymbol VARCHAR2(1000),
    fieldseparator VARCHAR2(1000),
    file_ VARCHAR2(1000),
    numberofheaderlines INTEGER,
    recordseparator VARCHAR2(1000),
    timeinterval NUMBER,
    timeinterval_factor INTEGER,
    timeinterval_radix INTEGER,
    timeinterval_unit VARCHAR2(1000),
    timeperiodprop_beginposition TIMESTAMP,
    timeperiodproper_endposition TIMESTAMP,
    uom VARCHAR2(1000),
    valuecolumnnumber INTEGER,
    PRIMARY KEY (id)
);

-- -------------------------------------------------------------------- 
-- ng_schedule 
-- -------------------------------------------------------------------- 
CREATE TABLE ng_schedule
(
    id NUMBER(38) NOT NULL,
    PRIMARY KEY (id)
);

-- -------------------------------------------------------------------- 
-- ng_solidmaterial 
-- -------------------------------------------------------------------- 
CREATE TABLE ng_solidmaterial
(
    id NUMBER(38) NOT NULL,
    conductivity NUMBER,
    conductivity_uom VARCHAR2(1000),
    density NUMBER,
    density_uom VARCHAR2(1000),
    permeance NUMBER,
    permeance_uom VARCHAR2(1000),
    specificheat NUMBER,
    specificheat_uom VARCHAR2(1000),
    PRIMARY KEY (id)
);

-- -------------------------------------------------------------------- 
-- ng_ther_boun_to_ther_deli 
-- -------------------------------------------------------------------- 
CREATE TABLE ng_ther_boun_to_ther_deli
(
    thermalboundary_delimits_id NUMBER(38) NOT NULL,
    thermalzone_boundedby_id NUMBER(38) NOT NULL,
    PRIMARY KEY (thermalboundary_delimits_id, thermalzone_boundedby_id)
);

-- -------------------------------------------------------------------- 
-- ng_thermalboundary 
-- -------------------------------------------------------------------- 
CREATE TABLE ng_thermalboundary
(
    id NUMBER(38) NOT NULL,
    area NUMBER,
    area_uom VARCHAR2(1000),
    azimuth NUMBER,
    azimuth_uom VARCHAR2(1000),
    construction_id NUMBER(38),
    inclination NUMBER,
    inclination_uom VARCHAR2(1000),
    surfacegeometry_id NUMBER(38),
    thermalboundarytype VARCHAR2(1000),
    thermalzone_boundedby_id NUMBER(38),
    PRIMARY KEY (id)
);

-- -------------------------------------------------------------------- 
-- ng_thermalopening 
-- -------------------------------------------------------------------- 
CREATE TABLE ng_thermalopening
(
    id NUMBER(38) NOT NULL,
    area NUMBER,
    area_uom VARCHAR2(1000),
    construction_id NUMBER(38),
    surfacegeometry_id NUMBER(38),
    thermalboundary_contains_id NUMBER(38),
    PRIMARY KEY (id)
);

-- -------------------------------------------------------------------- 
-- ng_thermalzone 
-- -------------------------------------------------------------------- 
CREATE TABLE ng_thermalzone
(
    id NUMBER(38) NOT NULL,
    building_thermalzone_id NUMBER(38),
    infiltrationrate NUMBER,
    infiltrationrate_uom VARCHAR2(1000),
    iscooled NUMBER,
    isheated NUMBER,
    volumegeometry_id NUMBER(38),
    PRIMARY KEY (id)
);

-- -------------------------------------------------------------------- 
-- ng_timeseries 
-- -------------------------------------------------------------------- 
CREATE TABLE ng_timeseries
(
    id NUMBER(38) NOT NULL,
    objectclass_id INTEGER,
    timevaluesprop_acquisitionme VARCHAR2(1000),
    timevaluesprop_interpolation VARCHAR2(1000),
    timevaluesprop_qualitydescri VARCHAR2(1000),
    timevaluesprop_thematicdescr VARCHAR2(1000),
    timevaluespropertiest_source VARCHAR2(1000),
    PRIMARY KEY (id)
);

-- -------------------------------------------------------------------- 
-- ng_timevaluesproperties 
-- -------------------------------------------------------------------- 
CREATE TABLE ng_timevaluesproperties
(
    id NUMBER(38) NOT NULL,
    acquisitionmethod VARCHAR2(1000),
    interpolationtype VARCHAR2(1000),
    qualitydescription VARCHAR2(1000),
    source VARCHAR2(1000),
    thematicdescription VARCHAR2(1000),
    PRIMARY KEY (id)
);

-- -------------------------------------------------------------------- 
-- ng_transmittance 
-- -------------------------------------------------------------------- 
CREATE TABLE ng_transmittance
(
    id NUMBER(38) NOT NULL,
    fraction NUMBER,
    fraction_uom VARCHAR2(1000),
    opticalprope_transmittanc_id NUMBER(38),
    wavelengthrange VARCHAR2(1000),
    PRIMARY KEY (id)
);

-- -------------------------------------------------------------------- 
-- ng_usagezone 
-- -------------------------------------------------------------------- 
CREATE TABLE ng_usagezone
(
    id NUMBER(38) NOT NULL,
    building_usagezone_id NUMBER(38),
    coolingschedule_id NUMBER(38),
    heatingschedule_id NUMBER(38),
    thermalzone_contains_id NUMBER(38),
    usagezonetype VARCHAR2(1000),
    usagezonetype_codespace VARCHAR2(1000),
    ventilationschedule_id NUMBER(38),
    PRIMARY KEY (id)
);

-- -------------------------------------------------------------------- 
-- ng_volumetype 
-- -------------------------------------------------------------------- 
CREATE TABLE ng_volumetype
(
    id NUMBER(38) NOT NULL,
    building_volume_id NUMBER(38),
    thermalzone_volume_id NUMBER(38),
    type VARCHAR2(1000),
    value NUMBER,
    value_uom VARCHAR2(1000),
    PRIMARY KEY (id)
);

-- -------------------------------------------------------------------- 
-- ng_weatherdata 
-- -------------------------------------------------------------------- 
CREATE TABLE ng_weatherdata
(
    id NUMBER(38) NOT NULL,
    cityobject_weatherdata_id NUMBER(38),
    position MDSYS.SDO_GEOMETRY,
    values_id NUMBER(38),
    weatherdatatype VARCHAR2(1000),
    weatherstation_parameter_id NUMBER(38),
    PRIMARY KEY (id)
);

-- -------------------------------------------------------------------- 
-- ng_weatherstation 
-- -------------------------------------------------------------------- 
CREATE TABLE ng_weatherstation
(
    id NUMBER(38) NOT NULL,
    genericapplicationpropertyof CLOB,
    position MDSYS.SDO_GEOMETRY,
    stationname VARCHAR2(1000),
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

ALTER TABLE ng_facilities ADD CONSTRAINT ng_facilities_operation_fk FOREIGN KEY (operationschedule_id)
REFERENCES ng_schedule (id)
ON DELETE SET NULL;

ALTER TABLE ng_facilities ADD CONSTRAINT ng_facilities_heatdissi_fk FOREIGN KEY (heatdissipation_id)
REFERENCES ng_heatexchangetype (id)
ON DELETE SET NULL;

ALTER TABLE ng_facilities ADD CONSTRAINT ng_facili_usagez_equipp_fk FOREIGN KEY (usagezone_equippedwith_id)
REFERENCES ng_usagezone (id);

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

ALTER TABLE ng_usagezone ADD CONSTRAINT ng_usagezone_ventilatio_fk FOREIGN KEY (ventilationschedule_id)
REFERENCES ng_schedule (id)
ON DELETE SET NULL;

ALTER TABLE ng_usagezone ADD CONSTRAINT ng_usagez_buildi_usagez_fk FOREIGN KEY (building_usagezone_id)
REFERENCES ng_building (id);

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

SET SERVEROUTPUT ON
SET FEEDBACK ON
SET VER OFF
VARIABLE SRID NUMBER;
BEGIN
  SELECT SRID INTO :SRID FROM DATABASE_SRS;
END;
/

column mc new_value SRSNO print
select :SRID mc from dual;

prompt Used SRID for spatial indexes: &SRSNO; 

-- -------------------------------------------------------------------- 
-- ng_building 
-- -------------------------------------------------------------------- 
DELETE FROM USER_SDO_GEOM_METADATA WHERE TABLE_NAME='NG_BUILDING' AND COLUMN_NAME='REFERENCEPOINT';
INSERT INTO USER_SDO_GEOM_METADATA (TABLE_NAME, COLUMN_NAME, DIMINFO, SRID)
VALUES ('NG_BUILDING','REFERENCEPOINT',
MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('X', 0.000, 10000000.000, 0.0005), MDSYS.SDO_DIM_ELEMENT('Y', 0.000, 10000000.000, 0.0005),MDSYS.SDO_DIM_ELEMENT('Z', -1000, 10000, 0.0005)), &SRSNO);
CREATE INDEX ng_building_referencep_spx ON ng_building (referencepoint) INDEXTYPE IS MDSYS.SPATIAL_INDEX;

-- -------------------------------------------------------------------- 
-- ng_construction 
-- -------------------------------------------------------------------- 
CREATE INDEX ng_construct_opticalpr_fkx ON ng_construction (opticalproperties_id);

-- -------------------------------------------------------------------- 
-- ng_dailyschedule 
-- -------------------------------------------------------------------- 
CREATE INDEX ng_dailys_period_daily_fkx ON ng_dailyschedule (periodofyear_dailyschedul_id);

CREATE INDEX ng_dailysched_schedule_fkx ON ng_dailyschedule (schedule_id);

-- -------------------------------------------------------------------- 
-- ng_energydem_to_cityobjec 
-- -------------------------------------------------------------------- 
CREATE INDEX ng_energyd_to_cityobj_fk2x ON ng_energydem_to_cityobjec (cityobject_id);

CREATE INDEX ng_energyd_to_cityobj_fk1x ON ng_energydem_to_cityobjec (energydemand_id);

-- -------------------------------------------------------------------- 
-- ng_energydemand 
-- -------------------------------------------------------------------- 
CREATE INDEX ng_energy_cityob_deman_fkx ON ng_energydemand (cityobject_demands_id);

CREATE INDEX ng_energydem_energyamo_fkx ON ng_energydemand (energyamount_id);

-- -------------------------------------------------------------------- 
-- ng_facilities 
-- -------------------------------------------------------------------- 
CREATE INDEX ng_facilitie_heatdissi_fkx ON ng_facilities (heatdissipation_id);

CREATE INDEX ng_facilitie_objectcla_fkx ON ng_facilities (objectclass_id);

CREATE INDEX ng_facilitie_operation_fkx ON ng_facilities (operationschedule_id);

CREATE INDEX ng_facili_usagez_equip_fkx ON ng_facilities (usagezone_equippedwith_id);

-- -------------------------------------------------------------------- 
-- ng_floorarea 
-- -------------------------------------------------------------------- 
CREATE INDEX ng_floora_buildi_floor_fkx ON ng_floorarea (building_floorarea_id);

CREATE INDEX ng_floora_therma_floor_fkx ON ng_floorarea (thermalzone_floorarea_id);

CREATE INDEX ng_floora_usagez_floor_fkx ON ng_floorarea (usagezone_floorarea_id);

-- -------------------------------------------------------------------- 
-- ng_heightaboveground 
-- -------------------------------------------------------------------- 
CREATE INDEX ng_height_buildi_heigh_fkx ON ng_heightaboveground (building_heightabovegroun_id);

-- -------------------------------------------------------------------- 
-- ng_layer 
-- -------------------------------------------------------------------- 
CREATE INDEX ng_layer_constru_layer_fkx ON ng_layer (construction_layer_id);

-- -------------------------------------------------------------------- 
-- ng_layercomponent 
-- -------------------------------------------------------------------- 
CREATE INDEX ng_layerc_layer_layerc_fkx ON ng_layercomponent (layer_layercomponent_id);

CREATE INDEX ng_layercompo_material_fkx ON ng_layercomponent (material_id);

-- -------------------------------------------------------------------- 
-- ng_material 
-- -------------------------------------------------------------------- 
CREATE INDEX ng_material_objectclas_fkx ON ng_material (objectclass_id);

-- -------------------------------------------------------------------- 
-- ng_occupants 
-- -------------------------------------------------------------------- 
CREATE INDEX ng_occupants_heatdissi_fkx ON ng_occupants (heatdissipation_id);

CREATE INDEX ng_occupants_occupancy_fkx ON ng_occupants (occupancyrate_id);

CREATE INDEX ng_occupa_usagez_occup_fkx ON ng_occupants (usagezone_occupiedby_id);

-- -------------------------------------------------------------------- 
-- ng_periodofyear 
-- -------------------------------------------------------------------- 
CREATE INDEX ng_periodofy_objectcla_fkx ON ng_periodofyear (objectclass_id);

CREATE INDEX ng_period_schedu_perio_fkx ON ng_periodofyear (schedule_periodofyear_id);

-- -------------------------------------------------------------------- 
-- ng_reflectance 
-- -------------------------------------------------------------------- 
CREATE INDEX ng_reflec_optica_refle_fkx ON ng_reflectance (opticalproper_reflectance_id);

-- -------------------------------------------------------------------- 
-- ng_ther_boun_to_ther_deli 
-- -------------------------------------------------------------------- 
CREATE INDEX ng_ther_bou_to_the_del_fk2 ON ng_ther_boun_to_ther_deli (thermalboundary_delimits_id);

CREATE INDEX ng_ther_bou_to_the_del_fk1 ON ng_ther_boun_to_ther_deli (thermalzone_boundedby_id);

-- -------------------------------------------------------------------- 
-- ng_thermalboundary 
-- -------------------------------------------------------------------- 
CREATE INDEX ng_thermalbo_construct_fkx ON ng_thermalboundary (construction_id);

CREATE INDEX ng_thermalbo_surfacege_fkx ON ng_thermalboundary (surfacegeometry_id);

CREATE INDEX ng_therma_therma_bound_fkx ON ng_thermalboundary (thermalzone_boundedby_id);

-- -------------------------------------------------------------------- 
-- ng_thermalopening 
-- -------------------------------------------------------------------- 
CREATE INDEX ng_thermalop_construct_fkx ON ng_thermalopening (construction_id);

CREATE INDEX ng_thermalop_surfacege_fkx ON ng_thermalopening (surfacegeometry_id);

CREATE INDEX ng_therma_therma_conta_fkx ON ng_thermalopening (thermalboundary_contains_id);

-- -------------------------------------------------------------------- 
-- ng_thermalzone 
-- -------------------------------------------------------------------- 
CREATE INDEX ng_therma_buildi_therm_fkx ON ng_thermalzone (building_thermalzone_id);

CREATE INDEX ng_thermalzo_volumegeo_fkx ON ng_thermalzone (volumegeometry_id);

-- -------------------------------------------------------------------- 
-- ng_timeseries 
-- -------------------------------------------------------------------- 
CREATE INDEX ng_timeserie_objectcla_fkx ON ng_timeseries (objectclass_id);

-- -------------------------------------------------------------------- 
-- ng_transmittance 
-- -------------------------------------------------------------------- 
CREATE INDEX ng_transm_optica_trans_fkx ON ng_transmittance (opticalprope_transmittanc_id);

-- -------------------------------------------------------------------- 
-- ng_usagezone 
-- -------------------------------------------------------------------- 
CREATE INDEX ng_usagez_buildi_usage_fkx ON ng_usagezone (building_usagezone_id);

CREATE INDEX ng_usagezone_coolingsc_fkx ON ng_usagezone (coolingschedule_id);

CREATE INDEX ng_usagezone_heatingsc_fkx ON ng_usagezone (heatingschedule_id);

CREATE INDEX ng_usagez_therma_conta_fkx ON ng_usagezone (thermalzone_contains_id);

CREATE INDEX ng_usagezone_ventilati_fkx ON ng_usagezone (ventilationschedule_id);

-- -------------------------------------------------------------------- 
-- ng_volumetype 
-- -------------------------------------------------------------------- 
CREATE INDEX ng_volume_buildi_volum_fkx ON ng_volumetype (building_volume_id);

CREATE INDEX ng_volume_therma_volum_fkx ON ng_volumetype (thermalzone_volume_id);

-- -------------------------------------------------------------------- 
-- ng_weatherdata 
-- -------------------------------------------------------------------- 
CREATE INDEX ng_weathe_cityob_weath_fkx ON ng_weatherdata (cityobject_weatherdata_id);

DELETE FROM USER_SDO_GEOM_METADATA WHERE TABLE_NAME='NG_WEATHERDATA' AND COLUMN_NAME='POSITION';
INSERT INTO USER_SDO_GEOM_METADATA (TABLE_NAME, COLUMN_NAME, DIMINFO, SRID)
VALUES ('NG_WEATHERDATA','POSITION',
MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('X', 0.000, 10000000.000, 0.0005), MDSYS.SDO_DIM_ELEMENT('Y', 0.000, 10000000.000, 0.0005),MDSYS.SDO_DIM_ELEMENT('Z', -1000, 10000, 0.0005)), &SRSNO);
CREATE INDEX ng_weatherdat_position_spx ON ng_weatherdata (position) INDEXTYPE IS MDSYS.SPATIAL_INDEX;

CREATE INDEX ng_weatherdata_values_fkx ON ng_weatherdata (values_id);

CREATE INDEX ng_weathe_weathe_param_fkx ON ng_weatherdata (weatherstation_parameter_id);

-- -------------------------------------------------------------------- 
-- ng_weatherstation 
-- -------------------------------------------------------------------- 
DELETE FROM USER_SDO_GEOM_METADATA WHERE TABLE_NAME='NG_WEATHERSTATION' AND COLUMN_NAME='POSITION';
INSERT INTO USER_SDO_GEOM_METADATA (TABLE_NAME, COLUMN_NAME, DIMINFO, SRID)
VALUES ('NG_WEATHERSTATION','POSITION',
MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('X', 0.000, 10000000.000, 0.0005), MDSYS.SDO_DIM_ELEMENT('Y', 0.000, 10000000.000, 0.0005),MDSYS.SDO_DIM_ELEMENT('Z', -1000, 10000, 0.0005)), &SRSNO);
CREATE INDEX ng_weathersta_position_spx ON ng_weatherstation (position) INDEXTYPE IS MDSYS.SPATIAL_INDEX;

