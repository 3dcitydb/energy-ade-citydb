package org.citydb.ade.energy.importer;

import org.citydb.ade.energy.schema.ADETable;
import org.citydb.ade.energy.schema.SchemaMapper;
import org.citydb.ade.importer.ADEImporter;
import org.citydb.ade.importer.ADEPropertyCollection;
import org.citydb.ade.importer.CityGMLImportHelper;
import org.citydb.ade.importer.ForeignKeys;
import org.citydb.citygml.importer.CityGMLImportException;
import org.citydb.database.schema.mapping.FeatureType;
import org.citygml4j.ade.energy.model.core.DemandsProperty;
import org.citygml4j.ade.energy.model.core.EnergyDemand;
import org.citygml4j.ade.energy.model.core.WeatherData;
import org.citygml4j.ade.energy.model.core.WeatherDataPropertyElement;
import org.citygml4j.model.citygml.core.AbstractCityObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class CityObjectPropertiesImporter implements ADEImporter {
	private final CityGMLImportHelper helper;
	private final SchemaMapper schemaMapper;

	private PreparedStatement ps;
	private int batchCounter;

	public CityObjectPropertiesImporter(Connection connection, CityGMLImportHelper helper, ImportManager manager) throws CityGMLImportException, SQLException {
		this.helper = helper;
		this.schemaMapper = manager.getSchemaMapper();

		ps = connection.prepareStatement("insert into " +
				helper.getTableNameWithSchema(schemaMapper.getTableName(ADETable.CITYOBJECT)) + " " +
				"(id) " +
				"values (?)");
	}

	public void doImport(ADEPropertyCollection properties, AbstractCityObject parent, long parentId, FeatureType parentType) throws CityGMLImportException, SQLException {
		ps.setLong(1, parentId);

		ps.addBatch();
		if (++batchCounter == helper.getDatabaseAdapter().getMaxBatchSize())
			helper.executeBatch(schemaMapper.getTableName(ADETable.CITYOBJECT));

		if (properties.contains(DemandsProperty.class)) {
			for (DemandsProperty propertyElement : properties.getAll(DemandsProperty.class)) {
				EnergyDemand energyDemand = propertyElement.getValue().getEnergyDemand();
				if (energyDemand != null) {
					helper.importObject(energyDemand, ForeignKeys.create().with("cityObjectId", parentId));
					propertyElement.getValue().unsetEnergyDemand();
				} else {
					String href = propertyElement.getValue().getHref();
					if (href != null && href.length() != 0)
						helper.logOrThrowUnsupportedXLinkMessage(parent, EnergyDemand.class, href);
				}
			}
		}

		if (properties.contains(WeatherDataPropertyElement.class)) {
			for (WeatherDataPropertyElement propertyElement : properties.getAll(WeatherDataPropertyElement.class)) {
				WeatherData weatherData = propertyElement.getValue().getWeatherData();
				if (weatherData != null) {
					helper.importObject(weatherData, ForeignKeys.create().with("cityObjectId", parentId));
					propertyElement.getValue().unsetWeatherData();
				} else {
					String href = propertyElement.getValue().getHref();
					if (href != null && href.length() != 0)
						helper.logOrThrowUnsupportedXLinkMessage(parent, WeatherData.class, href);
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
