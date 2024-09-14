-- This document was automatically created by the ADE-Manager tool of 3DCityDB (https://www.3dcitydb.org) on 2024-09-14 16:04:33 
-- ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ 
-- *********************************** Disable Versioning ********************************* 
-- ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ 

exec DBMS_WM.DisableVersioning('ng_building,ng_cityobject,ng_construction,ng_dailyschedule,ng_energydem_to_cityobjec,ng_energydemand,ng_facilities,ng_floorarea,ng_gas,ng_heatexchangetype,ng_heightaboveground,ng_layer,ng_layercomponent,ng_material,ng_occupants,ng_opticalproperties,ng_periodofyear,ng_reflectance,ng_regulartimeseries,ng_regulartimeseriesfile,ng_schedule,ng_solidmaterial,ng_ther_boun_to_ther_deli,ng_thermalboundary,ng_thermalopening,ng_thermalzone,ng_timeseries,ng_timevaluesproperties,ng_transmittance,ng_usagezone,ng_volumetype,ng_weatherdata,ng_weatherstation,',true, true);
