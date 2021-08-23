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

package org.citydb.ade.energy.importer;

import org.citydb.ade.energy.schema.ADETable;
import org.citydb.config.geometry.GeometryObject;
import org.citydb.core.ade.importer.ADEImporter;
import org.citydb.core.ade.importer.CityGMLImportHelper;
import org.citydb.core.ade.importer.ForeignKeys;
import org.citydb.core.database.schema.mapping.AbstractObjectType;
import org.citydb.core.operation.importer.CityGMLImportException;
import org.citydb.core.operation.importer.database.content.GeometryConverter;
import org.citygml4j.ade.energy.model.core.WeatherData;
import org.citygml4j.ade.energy.model.supportingClasses.AbstractTimeSeries;
import org.citygml4j.ade.energy.model.supportingClasses.AbstractTimeSeriesProperty;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

public class WeatherDataImporter implements ADEImporter {
    private final Connection connection;
    private final CityGMLImportHelper helper;

    private GeometryConverter geometryConverter;
    private PreparedStatement ps;
    private int batchCounter;

    public WeatherDataImporter(Connection connection, CityGMLImportHelper helper, ImportManager manager) throws CityGMLImportException, SQLException {
        this.connection = connection;
        this.helper = helper;

        ps = connection.prepareStatement("insert into " +
                helper.getTableNameWithSchema(manager.getSchemaMapper().getTableName(ADETable.WEATHERDATA)) + " " +
                "(id, cityobject_weatherdata_id, weatherstation_parameter_id, values_id, weatherdatatype, position) " +
                "values (?, ?, ?, ?, ?, ?)");

        geometryConverter = helper.getGeometryConverter();
    }

    public void doImport(WeatherData weatherData, long objectId, AbstractObjectType<?> objectType, ForeignKeys foreignKeys) throws CityGMLImportException, SQLException {
        long cityObjectId = foreignKeys.get("cityObjectId");
        long weatherStationId = foreignKeys.get("weatherStationId");

        ps.setLong(1, objectId);

        if (cityObjectId != 0) {
            ps.setLong(2, cityObjectId);
            ps.setNull(3, Types.NULL);
        } else if (weatherStationId != 0) {
            ps.setNull(2, Types.NULL);
            ps.setLong(3, weatherStationId);
        } else {
            ps.setNull(2, Types.NULL);
            ps.setNull(3, Types.NULL);
        }

        long valuesId = 0;
        if (weatherData.isSetValues()) {
            AbstractTimeSeriesProperty property = weatherData.getValues();
            if (property.isSetAbstractTimeSeries()) {
                valuesId = helper.importObject(property.getAbstractTimeSeries());
                property.unsetAbstractTimeSeries();
            } else {
                String href = property.getHref();
                if (href != null && href.length() != 0)
                    helper.logOrThrowUnsupportedXLinkMessage(weatherData, AbstractTimeSeries.class, href);
            }
        }

        if (valuesId != 0)
            ps.setLong(4, valuesId);
        else
            ps.setNull(4, Types.NULL);

        if (weatherData.isSetWeatherDataType())
            ps.setString(5, weatherData.getWeatherDataType().value());
        else
            ps.setNull(5, Types.VARCHAR);

        GeometryObject geometryObject = null;
        if (weatherData.isSetPosition())
            geometryObject = geometryConverter.getPoint(weatherData.getPosition());

        if (geometryObject != null)
            ps.setObject(6, helper.getDatabaseAdapter().getGeometryConverter().getDatabaseObject(geometryObject, connection));
        else
            ps.setNull(6, helper.getDatabaseAdapter().getGeometryConverter().getNullGeometryType(),
                    helper.getDatabaseAdapter().getGeometryConverter().getNullGeometryTypeName());

        ps.addBatch();
        if (++batchCounter == helper.getDatabaseAdapter().getMaxBatchSize())
            helper.executeBatch(objectType);
    }

    @Override
    public void executeBatch() throws CityGMLImportException, SQLException {
        if (batchCounter > 0) {
            ps.executeBatch();
            batchCounter = 0;
        }
    }

    @Override
    public void close() throws CityGMLImportException, SQLException {
        ps.close();
    }
}
