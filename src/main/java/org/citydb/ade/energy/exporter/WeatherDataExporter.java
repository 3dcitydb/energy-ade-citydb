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
import org.citydb.core.database.schema.mapping.ObjectType;
import org.citydb.core.operation.exporter.CityGMLExportException;
import org.citydb.core.operation.exporter.database.content.GMLConverter;
import org.citydb.core.query.filter.projection.CombinedProjectionFilter;
import org.citydb.core.query.filter.projection.ProjectionFilter;
import org.citydb.sqlbuilder.expression.PlaceHolder;
import org.citydb.sqlbuilder.schema.Table;
import org.citydb.sqlbuilder.select.Select;
import org.citydb.sqlbuilder.select.operator.comparison.ComparisonFactory;
import org.citygml4j.ade.energy.model.core.WeatherData;
import org.citygml4j.ade.energy.model.core.WeatherDataTypeValue;
import org.citygml4j.ade.energy.model.module.EnergyADEModule;
import org.citygml4j.ade.energy.model.supportingClasses.AbstractTimeSeries;
import org.citygml4j.ade.energy.model.supportingClasses.AbstractTimeSeriesProperty;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

public class WeatherDataExporter implements ADEExporter {
    private final CityGMLExportHelper helper;
    private final int objectClassId;

    private PreparedStatement ps;
    private TimeSeriesExporter timeSeriesExporter;
    private GMLConverter gmlConverter;
    private String module;

    public WeatherDataExporter(Connection connection, CityGMLExportHelper helper, ExportManager manager) throws CityGMLExportException, SQLException {
        this.helper = helper;
        objectClassId = manager.getObjectMapper().getObjectClassId(WeatherData.class);

        String tableName = manager.getSchemaMapper().getTableName(ADETable.WEATHERDATA);
        CombinedProjectionFilter projectionFilter = helper.getCombinedProjectionFilter(tableName);
        module = EnergyADEModule.v1_0.getNamespaceURI();

        Table table = new Table(helper.getTableNameWithSchema(tableName));
        Select select = new Select().addProjection(table.getColumn("id"), table.getColumn("values_id"),
                table.getColumn("weatherdatatype"));
        if (projectionFilter.containsProperty("position", module))
            select.addProjection(table.getColumn("position"));
        select.addSelection(ComparisonFactory.equalTo(table.getColumn("cityobject_weatherdata_id"), new PlaceHolder<>()));
        ps = connection.prepareStatement(select.toString());

        timeSeriesExporter = manager.getExporter(TimeSeriesExporter.class);
        gmlConverter = helper.getGMLConverter();
    }

    public Collection<WeatherData> doExport(long parentId) throws CityGMLExportException, SQLException {
        ps.setLong(1, parentId);

        try (ResultSet rs = ps.executeQuery()) {
            Collection<WeatherData> result = new ArrayList<>();

            while (rs.next()) {
                long weatherDataId = rs.getLong("id");

                WeatherData weatherData = helper.createObject(weatherDataId, objectClassId, WeatherData.class);
                if (weatherData == null) {
                    helper.logOrThrowErrorMessage("Failed to instantiate " + helper.getObjectSignature(objectClassId, weatherDataId) + " as weather data object.");
                    continue;
                }

                ObjectType objectType = helper.getObjectType(objectClassId);
                ProjectionFilter projectionFilter = helper.getProjectionFilter(objectType);

                weatherData.setWeatherDataType(WeatherDataTypeValue.fromValue(rs.getString("weatherdatatype")));

                long valuesId = rs.getLong("values_id");
                if (!rs.wasNull()) {
                    AbstractTimeSeries timeSeries = timeSeriesExporter.doExport(valuesId);
                    if (timeSeries != null)
                        weatherData.setValues(new AbstractTimeSeriesProperty(timeSeries));
                }

                if (projectionFilter.containsProperty("position", module)) {
                    Object pointObj = rs.getObject("position");
                    if (pointObj != null) {
                        GeometryObject point = helper.getDatabaseAdapter().getGeometryConverter().getPoint(pointObj);
                        if (point != null)
                            weatherData.setPosition(gmlConverter.getPointProperty(point, false));
                    }
                }

                result.add(weatherData);
            }

            return result;
        }
    }

    @Override
    public void close() throws CityGMLExportException, SQLException {
        ps.close();
    }
}
