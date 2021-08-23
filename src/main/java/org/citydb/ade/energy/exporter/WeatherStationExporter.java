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

package org.citydb.ade.energy.exporter;

import org.citydb.ade.energy.schema.ADETable;
import org.citydb.config.geometry.GeometryObject;
import org.citydb.core.ade.exporter.ADEExporter;
import org.citydb.core.ade.exporter.CityGMLExportHelper;
import org.citydb.core.database.schema.mapping.AbstractType;
import org.citydb.core.database.schema.mapping.ObjectType;
import org.citydb.core.operation.exporter.CityGMLExportException;
import org.citydb.core.operation.exporter.database.content.GMLConverter;
import org.citydb.core.query.filter.projection.CombinedProjectionFilter;
import org.citydb.core.query.filter.projection.ProjectionFilter;
import org.citydb.sqlbuilder.expression.PlaceHolder;
import org.citydb.sqlbuilder.schema.Table;
import org.citydb.sqlbuilder.select.Select;
import org.citydb.sqlbuilder.select.join.JoinFactory;
import org.citydb.sqlbuilder.select.operator.comparison.ComparisonFactory;
import org.citydb.sqlbuilder.select.operator.comparison.ComparisonName;
import org.citygml4j.ade.energy.model.core.WeatherData;
import org.citygml4j.ade.energy.model.core.WeatherDataProperty;
import org.citygml4j.ade.energy.model.core.WeatherDataTypeValue;
import org.citygml4j.ade.energy.model.module.EnergyADEModule;
import org.citygml4j.ade.energy.model.supportingClasses.AbstractTimeSeries;
import org.citygml4j.ade.energy.model.supportingClasses.AbstractTimeSeriesProperty;
import org.citygml4j.ade.energy.model.supportingClasses.WeatherStation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class WeatherStationExporter implements ADEExporter {
    private final CityGMLExportHelper helper;
    private final int weatherDataObjectClassId;

    private PreparedStatement ps;
    private TimeSeriesExporter timeSeriesExporter;
    private GMLConverter gmlConverter;
    private String module;

    public WeatherStationExporter(Connection connection, CityGMLExportHelper helper, ExportManager manager) throws CityGMLExportException, SQLException {
        this.helper = helper;
        weatherDataObjectClassId = manager.getObjectMapper().getObjectClassId(WeatherData.class);

        String tableName = manager.getSchemaMapper().getTableName(ADETable.WEATHERSTATION);
        CombinedProjectionFilter projectionFilter = helper.getCombinedProjectionFilter(tableName);
        module = EnergyADEModule.v1_0.getNamespaceURI();

        Table table = new Table(helper.getTableNameWithSchema(tableName));
        Select select = new Select().addProjection(table.getColumn("id"));
        if (projectionFilter.containsProperty("position", module))
            select.addProjection(table.getColumn("position"));
        if (projectionFilter.containsProperty("stationName", module))
            select.addProjection(table.getColumn("stationname"));
        if (projectionFilter.containsProperty("parameter", module)) {
            Table weatherData = new Table(helper.getTableNameWithSchema(manager.getSchemaMapper().getTableName(ADETable.WEATHERDATA)));
            CombinedProjectionFilter weatherDataProjectionFilter = helper.getCombinedProjectionFilter(tableName);
            select.addJoin(JoinFactory.left(weatherData, "weatherstation_parameter_id", ComparisonName.EQUAL_TO, table.getColumn("id")))
                    .addProjection(weatherData.getColumn("id", "w_id"), weatherData.getColumn("values_id"),
                            weatherData.getColumn("weatherdatatype"));
            if (weatherDataProjectionFilter.containsProperty("position", module))
                select.addProjection(weatherData.getColumn("position", "w_position"));
        }
        select.addSelection(ComparisonFactory.equalTo(table.getColumn("id"), new PlaceHolder<>()));
        ps = connection.prepareStatement(select.toString());

        timeSeriesExporter = manager.getExporter(TimeSeriesExporter.class);
        gmlConverter = helper.getGMLConverter();
    }

    public void doExport(WeatherStation weatherStation, long objectId, AbstractType<?> objectType, ProjectionFilter projectionFilter) throws CityGMLExportException, SQLException {
        ps.setLong(1, objectId);

        try (ResultSet rs = ps.executeQuery()) {
            boolean isInitialized = false;

            while (rs.next()) {
                if (!isInitialized) {
                    if (projectionFilter.containsProperty("stationName", module))
                        weatherStation.setStationName(rs.getString("stationname"));

                    if (projectionFilter.containsProperty("position", module)) {
                        Object pointObj = rs.getObject("position");
                        if (pointObj != null) {
                            GeometryObject point = helper.getDatabaseAdapter().getGeometryConverter().getPoint(pointObj);
                            if (point != null)
                                weatherStation.setPosition(gmlConverter.getPointProperty(point, false));
                        }
                    }

                    isInitialized = true;
                }

                long weatherDataId = rs.getLong("w_id");
                if (!rs.wasNull()) {
                    WeatherData weatherData = helper.createObject(weatherDataId, weatherDataObjectClassId, WeatherData.class);
                    if (weatherData == null) {
                        helper.logOrThrowErrorMessage("Failed to instantiate " + helper.getObjectSignature(weatherDataObjectClassId, weatherDataId) + " as weather data object.");
                        continue;
                    }

                    ObjectType weatherDataObjectType = helper.getObjectType(weatherDataObjectClassId);
                    ProjectionFilter weatherDataProjectionFilter = helper.getProjectionFilter(weatherDataObjectType);

                    weatherData.setWeatherDataType(WeatherDataTypeValue.fromValue(rs.getString("weatherdatatype")));

                    long valuesId = rs.getLong("values_id");
                    if (!rs.wasNull()) {
                        AbstractTimeSeries timeSeries = timeSeriesExporter.doExport(valuesId);
                        if (timeSeries != null)
                            weatherData.setValues(new AbstractTimeSeriesProperty(timeSeries));
                    }

                    if (weatherDataProjectionFilter.containsProperty("position", module)) {
                        Object pointObj = rs.getObject("w_position");
                        if (pointObj != null) {
                            GeometryObject point = helper.getDatabaseAdapter().getGeometryConverter().getPoint(pointObj);
                            if (point != null)
                                weatherData.setPosition(gmlConverter.getPointProperty(point, false));
                        }
                    }

                    weatherStation.addParameter(new WeatherDataProperty(weatherData));
                }
            }
        }
    }

    @Override
    public void close() throws CityGMLExportException, SQLException {
        ps.close();
    }
}
