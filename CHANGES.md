Change Log
==========

### 1.4.0 - 2021-10-08

##### Changes
* Updated PostgreSQL database schema to use 64-bit `bigint` as data type instead of 32-bit `integer` for all 
  primary key and foreign key columns. Hence, this release can only be used with version 4.2.x of the 3DCityDB. 
  It is not compatible with previous versions of the 3DCityDB anymore.
* Updated impexp-client-gui to 5.0.0
* Updated energy-ade-citygml4j to 1.0.4.

### 1.3.0 - 2021-04-30

##### Fixes
* Fixed import of properties inherited from `AbstractCityObject` for the `WeatherStation` feature. [#2](https://github.com/3dcitydb/energy-ade-citydb/issues/2)
* Fixed import of nested `WeatherData` features of `WeatherStation`. [#2](https://github.com/3dcitydb/energy-ade-citydb/issues/2)
* Fixed export of ADE properties for `WeatherStation`. [#2](https://github.com/3dcitydb/energy-ade-citydb/issues/2), [importer-exporter #184](https://github.com/3dcitydb/importer-exporter/pull/184)
* Fixed import of properties inherited from `AbstractGML` for the `WeatherData` object. [#2](https://github.com/3dcitydb/energy-ade-citydb/issues/2)
* Fixed import of properties inherited from `AbstractFeature` for the `LayerComponent` feature. [#4](https://github.com/3dcitydb/energy-ade-citydb/issues/4)

##### Changes
* Updated impexp-client to 4.3.0
* Updated energy-ade-citygml4j to 1.0.3.