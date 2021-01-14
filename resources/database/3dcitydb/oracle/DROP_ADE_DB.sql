-- This document was automatically created by the ADE-Manager tool of 3DCityDB (https://www.3dcitydb.org) on 2019-04-17 16:53:37 
-- ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ 
-- *********************************** Drop foreign keys ********************************** 
-- ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ 
-- -------------------------------------------------------------------- 
-- ng_building 
-- -------------------------------------------------------------------- 
ALTER TABLE ng_building
    DROP CONSTRAINT ng_building_fk;

-- -------------------------------------------------------------------- 
-- ng_cityobject 
-- -------------------------------------------------------------------- 
ALTER TABLE ng_cityobject
    DROP CONSTRAINT ng_cityobject_fk;

-- -------------------------------------------------------------------- 
-- ng_construction 
-- -------------------------------------------------------------------- 
ALTER TABLE ng_construction
    DROP CONSTRAINT ng_construction_fk;

ALTER TABLE ng_construction
    DROP CONSTRAINT ng_constructi_opticalpr_fk;

-- -------------------------------------------------------------------- 
-- ng_dailyschedule 
-- -------------------------------------------------------------------- 
ALTER TABLE ng_dailyschedule
    DROP CONSTRAINT ng_dailyschedu_schedule_fk;

ALTER TABLE ng_dailyschedule
    DROP CONSTRAINT ng_dailys_period_dailys_fk;

-- -------------------------------------------------------------------- 
-- ng_energydem_to_cityobjec 
-- -------------------------------------------------------------------- 
ALTER TABLE ng_energydem_to_cityobjec
    DROP CONSTRAINT ng_energyde_to_cityobj_fk1;

ALTER TABLE ng_energydem_to_cityobjec
    DROP CONSTRAINT ng_energyde_to_cityobj_fk2;

-- -------------------------------------------------------------------- 
-- ng_energydemand 
-- -------------------------------------------------------------------- 
ALTER TABLE ng_energydemand
    DROP CONSTRAINT ng_energydemand_fk;

ALTER TABLE ng_energydemand
    DROP CONSTRAINT ng_energydema_energyamo_fk;

ALTER TABLE ng_energydemand
    DROP CONSTRAINT ng_energy_cityob_demand_fk;

-- -------------------------------------------------------------------- 
-- ng_facilities 
-- -------------------------------------------------------------------- 
ALTER TABLE ng_facilities
    DROP CONSTRAINT ng_facilities_objectcla_fk;

ALTER TABLE ng_facilities
    DROP CONSTRAINT ng_facilities_fk;

ALTER TABLE ng_facilities
    DROP CONSTRAINT ng_facilities_heatdissi_fk;

ALTER TABLE ng_facilities
    DROP CONSTRAINT ng_facilities_operation_fk;

ALTER TABLE ng_facilities
    DROP CONSTRAINT ng_facili_usagez_equipp_fk;

-- -------------------------------------------------------------------- 
-- ng_floorarea 
-- -------------------------------------------------------------------- 
ALTER TABLE ng_floorarea
    DROP CONSTRAINT ng_floora_buildi_floora_fk;

ALTER TABLE ng_floorarea
    DROP CONSTRAINT ng_floora_therma_floora_fk;

ALTER TABLE ng_floorarea
    DROP CONSTRAINT ng_floora_usagez_floora_fk;

-- -------------------------------------------------------------------- 
-- ng_gas 
-- -------------------------------------------------------------------- 
ALTER TABLE ng_gas
    DROP CONSTRAINT ng_gas_fk;

-- -------------------------------------------------------------------- 
-- ng_heightaboveground 
-- -------------------------------------------------------------------- 
ALTER TABLE ng_heightaboveground
    DROP CONSTRAINT ng_height_buildi_height_fk;

-- -------------------------------------------------------------------- 
-- ng_layer 
-- -------------------------------------------------------------------- 
ALTER TABLE ng_layer
    DROP CONSTRAINT ng_layer_fk;

ALTER TABLE ng_layer
    DROP CONSTRAINT ng_layer_construc_layer_fk;

-- -------------------------------------------------------------------- 
-- ng_layercomponent 
-- -------------------------------------------------------------------- 
ALTER TABLE ng_layercomponent
    DROP CONSTRAINT ng_layercomponent_fk;

ALTER TABLE ng_layercomponent
    DROP CONSTRAINT ng_layercompon_material_fk;

ALTER TABLE ng_layercomponent
    DROP CONSTRAINT ng_layerco_layer_layerc_fk;

-- -------------------------------------------------------------------- 
-- ng_material 
-- -------------------------------------------------------------------- 
ALTER TABLE ng_material
    DROP CONSTRAINT ng_material_fk;

ALTER TABLE ng_material
    DROP CONSTRAINT ng_material_objectclass_fk;

-- -------------------------------------------------------------------- 
-- ng_occupants 
-- -------------------------------------------------------------------- 
ALTER TABLE ng_occupants
    DROP CONSTRAINT ng_occupants_fk;

ALTER TABLE ng_occupants
    DROP CONSTRAINT ng_occupants_heatdissip_fk;

ALTER TABLE ng_occupants
    DROP CONSTRAINT ng_occupants_occupancyr_fk;

ALTER TABLE ng_occupants
    DROP CONSTRAINT ng_occupa_usagez_occupi_fk;

-- -------------------------------------------------------------------- 
-- ng_periodofyear 
-- -------------------------------------------------------------------- 
ALTER TABLE ng_periodofyear
    DROP CONSTRAINT ng_period_schedu_period_fk;

-- -------------------------------------------------------------------- 
-- ng_reflectance 
-- -------------------------------------------------------------------- 
ALTER TABLE ng_reflectance
    DROP CONSTRAINT ng_reflec_optica_reflec_fk;

-- -------------------------------------------------------------------- 
-- ng_regulartimeseries 
-- -------------------------------------------------------------------- 
ALTER TABLE ng_regulartimeseries
    DROP CONSTRAINT ng_regulartimeseries_fk;

-- -------------------------------------------------------------------- 
-- ng_regulartimeseriesfile 
-- -------------------------------------------------------------------- 
ALTER TABLE ng_regulartimeseriesfile
    DROP CONSTRAINT ng_regulartimeseriesfil_fk;

-- -------------------------------------------------------------------- 
-- ng_schedule 
-- -------------------------------------------------------------------- 
ALTER TABLE ng_schedule
    DROP CONSTRAINT ng_schedule_fk;

-- -------------------------------------------------------------------- 
-- ng_solidmaterial 
-- -------------------------------------------------------------------- 
ALTER TABLE ng_solidmaterial
    DROP CONSTRAINT ng_solidmaterial_fk;

-- -------------------------------------------------------------------- 
-- ng_ther_boun_to_ther_deli 
-- -------------------------------------------------------------------- 
ALTER TABLE ng_ther_boun_to_ther_deli
    DROP CONSTRAINT ng_ther_bou_to_the_del_fk1;

ALTER TABLE ng_ther_boun_to_ther_deli
    DROP CONSTRAINT ng_ther_bou_to_the_del_fk2;

-- -------------------------------------------------------------------- 
-- ng_thermalboundary 
-- -------------------------------------------------------------------- 
ALTER TABLE ng_thermalboundary
    DROP CONSTRAINT ng_thermalboundary_fk;

ALTER TABLE ng_thermalboundary
    DROP CONSTRAINT ng_thermalbou_construct_fk;

ALTER TABLE ng_thermalboundary
    DROP CONSTRAINT ng_therma_therma_bounde_fk;

ALTER TABLE ng_thermalboundary
    DROP CONSTRAINT ng_thermalbou_surfacege_fk;

-- -------------------------------------------------------------------- 
-- ng_thermalopening 
-- -------------------------------------------------------------------- 
ALTER TABLE ng_thermalopening
    DROP CONSTRAINT ng_thermalopening_fk;

ALTER TABLE ng_thermalopening
    DROP CONSTRAINT ng_thermalope_construct_fk;

ALTER TABLE ng_thermalopening
    DROP CONSTRAINT ng_therma_therma_contai_fk;

ALTER TABLE ng_thermalopening
    DROP CONSTRAINT ng_thermalope_surfacege_fk;

-- -------------------------------------------------------------------- 
-- ng_thermalzone 
-- -------------------------------------------------------------------- 
ALTER TABLE ng_thermalzone
    DROP CONSTRAINT ng_thermalzone_fk;

ALTER TABLE ng_thermalzone
    DROP CONSTRAINT ng_therma_buildi_therma_fk;

ALTER TABLE ng_thermalzone
    DROP CONSTRAINT ng_thermalzon_volumegeo_fk;

-- -------------------------------------------------------------------- 
-- ng_timeseries 
-- -------------------------------------------------------------------- 
ALTER TABLE ng_timeseries
    DROP CONSTRAINT ng_timeseries_fk;

ALTER TABLE ng_timeseries
    DROP CONSTRAINT ng_timeseries_objectcla_fk;

-- -------------------------------------------------------------------- 
-- ng_transmittance 
-- -------------------------------------------------------------------- 
ALTER TABLE ng_transmittance
    DROP CONSTRAINT ng_transm_optica_transm_fk;

-- -------------------------------------------------------------------- 
-- ng_usagezone 
-- -------------------------------------------------------------------- 
ALTER TABLE ng_usagezone
    DROP CONSTRAINT ng_usagezone_fk;

ALTER TABLE ng_usagezone
    DROP CONSTRAINT ng_usagez_buildi_usagez_fk;

ALTER TABLE ng_usagezone
    DROP CONSTRAINT ng_usagez_therma_contai_fk;

ALTER TABLE ng_usagezone
    DROP CONSTRAINT ng_usagezone_coolingsch_fk;

ALTER TABLE ng_usagezone
    DROP CONSTRAINT ng_usagezone_heatingsch_fk;

ALTER TABLE ng_usagezone
    DROP CONSTRAINT ng_usagezone_ventilatio_fk;

-- -------------------------------------------------------------------- 
-- ng_volumetype 
-- -------------------------------------------------------------------- 
ALTER TABLE ng_volumetype
    DROP CONSTRAINT ng_volume_buildi_volume_fk;

ALTER TABLE ng_volumetype
    DROP CONSTRAINT ng_volume_therma_volume_fk;

-- -------------------------------------------------------------------- 
-- ng_weatherdata 
-- -------------------------------------------------------------------- 
ALTER TABLE ng_weatherdata
    DROP CONSTRAINT ng_weatherdata_fk;

ALTER TABLE ng_weatherdata
    DROP CONSTRAINT ng_weatherdata_values_fk;

ALTER TABLE ng_weatherdata
    DROP CONSTRAINT ng_weathe_cityob_weathe_fk;

ALTER TABLE ng_weatherdata
    DROP CONSTRAINT ng_weathe_weathe_parame_fk;

-- -------------------------------------------------------------------- 
-- ng_weatherstation 
-- -------------------------------------------------------------------- 
ALTER TABLE ng_weatherstation
    DROP CONSTRAINT ng_weatherstation_fk;

-- ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ 
-- *********************************** Drop tables *************************************** 
-- ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ 
-- -------------------------------------------------------------------- 
-- ng_building 
-- -------------------------------------------------------------------- 
DROP TABLE ng_building;

-- -------------------------------------------------------------------- 
-- ng_cityobject 
-- -------------------------------------------------------------------- 
DROP TABLE ng_cityobject;

-- -------------------------------------------------------------------- 
-- ng_construction 
-- -------------------------------------------------------------------- 
DROP TABLE ng_construction;

-- -------------------------------------------------------------------- 
-- ng_dailyschedule 
-- -------------------------------------------------------------------- 
DROP TABLE ng_dailyschedule;

-- -------------------------------------------------------------------- 
-- ng_energydem_to_cityobjec 
-- -------------------------------------------------------------------- 
DROP TABLE ng_energydem_to_cityobjec;

-- -------------------------------------------------------------------- 
-- ng_energydemand 
-- -------------------------------------------------------------------- 
DROP TABLE ng_energydemand;

-- -------------------------------------------------------------------- 
-- ng_facilities 
-- -------------------------------------------------------------------- 
DROP TABLE ng_facilities;

-- -------------------------------------------------------------------- 
-- ng_floorarea 
-- -------------------------------------------------------------------- 
DROP TABLE ng_floorarea;

-- -------------------------------------------------------------------- 
-- ng_gas 
-- -------------------------------------------------------------------- 
DROP TABLE ng_gas;

-- -------------------------------------------------------------------- 
-- ng_heatexchangetype 
-- -------------------------------------------------------------------- 
DROP TABLE ng_heatexchangetype;

-- -------------------------------------------------------------------- 
-- ng_heightaboveground 
-- -------------------------------------------------------------------- 
DROP TABLE ng_heightaboveground;

-- -------------------------------------------------------------------- 
-- ng_layer 
-- -------------------------------------------------------------------- 
DROP TABLE ng_layer;

-- -------------------------------------------------------------------- 
-- ng_layercomponent 
-- -------------------------------------------------------------------- 
DROP TABLE ng_layercomponent;

-- -------------------------------------------------------------------- 
-- ng_material 
-- -------------------------------------------------------------------- 
DROP TABLE ng_material;

-- -------------------------------------------------------------------- 
-- ng_occupants 
-- -------------------------------------------------------------------- 
DROP TABLE ng_occupants;

-- -------------------------------------------------------------------- 
-- ng_opticalproperties 
-- -------------------------------------------------------------------- 
DROP TABLE ng_opticalproperties;

-- -------------------------------------------------------------------- 
-- ng_periodofyear 
-- -------------------------------------------------------------------- 
DROP TABLE ng_periodofyear;

-- -------------------------------------------------------------------- 
-- ng_reflectance 
-- -------------------------------------------------------------------- 
DROP TABLE ng_reflectance;

-- -------------------------------------------------------------------- 
-- ng_regulartimeseries 
-- -------------------------------------------------------------------- 
DROP TABLE ng_regulartimeseries;

-- -------------------------------------------------------------------- 
-- ng_regulartimeseriesfile 
-- -------------------------------------------------------------------- 
DROP TABLE ng_regulartimeseriesfile;

-- -------------------------------------------------------------------- 
-- ng_schedule 
-- -------------------------------------------------------------------- 
DROP TABLE ng_schedule;

-- -------------------------------------------------------------------- 
-- ng_solidmaterial 
-- -------------------------------------------------------------------- 
DROP TABLE ng_solidmaterial;

-- -------------------------------------------------------------------- 
-- ng_ther_boun_to_ther_deli 
-- -------------------------------------------------------------------- 
DROP TABLE ng_ther_boun_to_ther_deli;

-- -------------------------------------------------------------------- 
-- ng_thermalboundary 
-- -------------------------------------------------------------------- 
DROP TABLE ng_thermalboundary;

-- -------------------------------------------------------------------- 
-- ng_thermalopening 
-- -------------------------------------------------------------------- 
DROP TABLE ng_thermalopening;

-- -------------------------------------------------------------------- 
-- ng_thermalzone 
-- -------------------------------------------------------------------- 
DROP TABLE ng_thermalzone;

-- -------------------------------------------------------------------- 
-- ng_timeseries 
-- -------------------------------------------------------------------- 
DROP TABLE ng_timeseries;

-- -------------------------------------------------------------------- 
-- ng_timevaluesproperties 
-- -------------------------------------------------------------------- 
DROP TABLE ng_timevaluesproperties;

-- -------------------------------------------------------------------- 
-- ng_transmittance 
-- -------------------------------------------------------------------- 
DROP TABLE ng_transmittance;

-- -------------------------------------------------------------------- 
-- ng_usagezone 
-- -------------------------------------------------------------------- 
DROP TABLE ng_usagezone;

-- -------------------------------------------------------------------- 
-- ng_volumetype 
-- -------------------------------------------------------------------- 
DROP TABLE ng_volumetype;

-- -------------------------------------------------------------------- 
-- ng_weatherdata 
-- -------------------------------------------------------------------- 
DROP TABLE ng_weatherdata;

-- -------------------------------------------------------------------- 
-- ng_weatherstation 
-- -------------------------------------------------------------------- 
DROP TABLE ng_weatherstation;

-- ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ 
-- *********************************** Drop Sequences ************************************* 
-- ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ 

DROP SEQUENCE ng_volumetype_seq;

DROP SEQUENCE ng_floorarea_seq;

DROP SEQUENCE ng_heightaboveground_seq;

DROP SEQUENCE ng_heatexchangetype_seq;

DROP SEQUENCE ng_transmittance_seq;

DROP SEQUENCE ng_opticalproperties_seq;

DROP SEQUENCE ng_reflectance_seq;

DROP SEQUENCE ng_timevaluesproperti_seq;

DROP SEQUENCE ng_periodofyear_seq;

DROP SEQUENCE ng_dailyschedule_seq;

PURGE RECYCLEBIN;
