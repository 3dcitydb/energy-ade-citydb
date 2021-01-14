package org.citydb.ade.energy.importer;

import org.citydb.ade.energy.schema.ADETable;
import org.citydb.ade.importer.ADEImporter;
import org.citydb.ade.importer.CityGMLImportHelper;
import org.citydb.ade.importer.ForeignKeys;
import org.citydb.citygml.importer.CityGMLImportException;
import org.citydb.citygml.importer.database.content.GeometryConverter;
import org.citydb.config.geometry.GeometryObject;
import org.citydb.database.schema.mapping.AbstractObjectType;
import org.citygml4j.ade.energy.model.core.WeatherData;
import org.citygml4j.ade.energy.model.core.WeatherDataProperty;
import org.citygml4j.ade.energy.model.supportingClasses.WeatherStation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class WeatherStationImporter implements ADEImporter {
    private final Connection connection;
    private final CityGMLImportHelper helper;

    private GeometryConverter geometryConverter;
    private PreparedStatement ps;
    private int batchCounter;

    public WeatherStationImporter(Connection connection, CityGMLImportHelper helper, ImportManager manager) throws CityGMLImportException, SQLException {
        this.connection = connection;
        this.helper = helper;

        ps = connection.prepareStatement("insert into " +
                helper.getTableNameWithSchema(manager.getSchemaMapper().getTableName(ADETable.WEATHERSTATION)) + " " +
                "(id, stationname, position) " +
                "values (?, ?, ?)");

        geometryConverter = helper.getGeometryConverter();
    }

    public void doImport(WeatherStation weatherStation, long objectId, AbstractObjectType<?> objectType, ForeignKeys foreignKeys) throws CityGMLImportException, SQLException {
        ps.setLong(1, objectId);

        ps.setString(2, weatherStation.getStationName());

        GeometryObject geometryObject = null;
        if (weatherStation.isSetPosition())
            geometryObject = geometryConverter.getPoint(weatherStation.getPosition());

        if (geometryObject != null)
            ps.setObject(3, helper.getDatabaseAdapter().getGeometryConverter().getDatabaseObject(geometryObject, connection));
        else
            ps.setNull(3, helper.getDatabaseAdapter().getGeometryConverter().getNullGeometryType(),
                    helper.getDatabaseAdapter().getGeometryConverter().getNullGeometryTypeName());

        ps.addBatch();
        if (++batchCounter == helper.getDatabaseAdapter().getMaxBatchSize())
            helper.executeBatch(objectType);

        if (weatherStation.isSetParameter()) {
            for (WeatherDataProperty property : weatherStation.getParameter()) {
                WeatherData weatherData = property.getWeatherData();
                if (weatherData != null) {
                    helper.importObject(weatherData, ForeignKeys.create().with("weatherStationId", objectId));
                    property.unsetWeatherData();
                } else {
                    String href = property.getHref();
                    if (href != null && href.length() != 0)
                        helper.logOrThrowUnsupportedXLinkMessage(weatherStation, WeatherData.class, href);
                }
            }
        }
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
