package org.citydb.ade.energy.importer;

import org.citydb.ade.energy.schema.ADETable;
import org.citydb.ade.energy.schema.SchemaMapper;
import org.citydb.ade.importer.ADEImporter;
import org.citydb.ade.importer.ADEPropertyCollection;
import org.citydb.ade.importer.CityGMLImportHelper;
import org.citydb.ade.importer.ForeignKeys;
import org.citydb.citygml.importer.CityGMLImportException;
import org.citydb.citygml.importer.database.content.GeometryConverter;
import org.citydb.config.geometry.GeometryObject;
import org.citydb.database.schema.mapping.FeatureType;
import org.citygml4j.ade.energy.model.core.AbstractThermalZone;
import org.citygml4j.ade.energy.model.core.AbstractUsageZone;
import org.citygml4j.ade.energy.model.core.BuildingTypeProperty;
import org.citygml4j.ade.energy.model.core.ConstructionWeightProperty;
import org.citygml4j.ade.energy.model.core.FloorArea;
import org.citygml4j.ade.energy.model.core.FloorAreaPropertyElement;
import org.citygml4j.ade.energy.model.core.HeightAboveGround;
import org.citygml4j.ade.energy.model.core.HeightAboveGroundPropertyElement;
import org.citygml4j.ade.energy.model.core.ReferencePointProperty;
import org.citygml4j.ade.energy.model.core.ThermalZonePropertyElement;
import org.citygml4j.ade.energy.model.core.UsageZoneProperty;
import org.citygml4j.ade.energy.model.core.VolumeType;
import org.citygml4j.ade.energy.model.core.VolumeTypePropertyElement;
import org.citygml4j.model.citygml.building.AbstractBuilding;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

public class BuildingPropertiesImporter implements ADEImporter {
	private final Connection connection;
	private final CityGMLImportHelper helper;
	private final SchemaMapper schemaMapper;

	private FloorAreaImporter floorAreaImporter;
	private VolumeTypeImporter volumeTypeImporter;
	private HeightAboveGroundImporter heightAboveGroundImporter;
	private GeometryConverter geometryConverter;
	private PreparedStatement ps;
	private int batchCounter;

	public BuildingPropertiesImporter(Connection connection, CityGMLImportHelper helper, ImportManager manager) throws CityGMLImportException, SQLException {
		this.connection = connection;
		this.helper = helper;
		this.schemaMapper = manager.getSchemaMapper();

		ps = connection.prepareStatement("insert into " +
				helper.getTableNameWithSchema(schemaMapper.getTableName(ADETable.BUILDING)) + " " +
				"(id, constructionweight, buildingtype, buildingtype_codespace, referencepoint) " +
				"values (?, ?, ?, ?, ?)");

		floorAreaImporter = manager.getImporter(FloorAreaImporter.class);
		volumeTypeImporter = manager.getImporter(VolumeTypeImporter.class);
		heightAboveGroundImporter = manager.getImporter(HeightAboveGroundImporter.class);
		geometryConverter = helper.getGeometryConverter();
	}

	public void doImport(ADEPropertyCollection properties, AbstractBuilding parent, long parentId, FeatureType parentType) throws CityGMLImportException, SQLException {
		ps.setLong(1, parentId);

		ConstructionWeightProperty constructionWeight = properties.getFirst(ConstructionWeightProperty.class);
		if (constructionWeight != null && constructionWeight.isSetValue())
			ps.setString(2, constructionWeight.getValue().value());
		else
			ps.setNull(2, Types.VARCHAR);

		BuildingTypeProperty buildingType = properties.getFirst(BuildingTypeProperty.class);
		if (buildingType != null && buildingType.isSetValue()) {
			ps.setString(3, buildingType.getValue().getValue());
			ps.setString(4, buildingType.getValue().getCodeSpace());
		} else {
			ps.setNull(3, Types.VARCHAR);
			ps.setNull(4, Types.VARCHAR);
		}

		GeometryObject geometryObject = null;
		ReferencePointProperty referencePoint = properties.getFirst(ReferencePointProperty.class);
		if (referencePoint != null && referencePoint.isSetValue())
			geometryObject = geometryConverter.getPoint(referencePoint.getValue());

		if (geometryObject != null)
			ps.setObject(5, helper.getDatabaseAdapter().getGeometryConverter().getDatabaseObject(geometryObject, connection));
		else
			ps.setNull(5, helper.getDatabaseAdapter().getGeometryConverter().getNullGeometryType(),
					helper.getDatabaseAdapter().getGeometryConverter().getNullGeometryTypeName());

		ps.addBatch();
		if (++batchCounter == helper.getDatabaseAdapter().getMaxBatchSize())
			helper.executeBatch(schemaMapper.getTableName(ADETable.BUILDING));

		if (properties.contains(FloorAreaPropertyElement.class)) {
			for (FloorAreaPropertyElement propertyElement : properties.getAll(FloorAreaPropertyElement.class)) {
				FloorArea floorArea = propertyElement.getValue().getFloorArea();
				if (floorArea != null) {
					floorAreaImporter.doImport(floorArea, parent, parentId);
					propertyElement.getValue().unsetFloorArea();
				}
			}
		}

		if (properties.contains(VolumeTypePropertyElement.class)) {
			for (VolumeTypePropertyElement propertyElement : properties.getAll(VolumeTypePropertyElement.class)) {
				VolumeType volumeType = propertyElement.getValue().getVolumeType();
				if (volumeType != null) {
					volumeTypeImporter.doImport(volumeType, parent, parentId);
					propertyElement.getValue().unsetVolumeType();
				}
			}
		}

		if (properties.contains(HeightAboveGroundPropertyElement.class)) {
			for (HeightAboveGroundPropertyElement propertyElement : properties.getAll(HeightAboveGroundPropertyElement.class)) {
				HeightAboveGround heightAboveGround = propertyElement.getValue().getHeightAboveGround();
				if (heightAboveGround != null) {
					heightAboveGroundImporter.doImport(heightAboveGround, parentId);
					propertyElement.getValue().unsetHeightAboveGround();
				}
			}
		}

		if (properties.contains(UsageZoneProperty.class)) {
			for (UsageZoneProperty propertyElement : properties.getAll(UsageZoneProperty.class)) {
				AbstractUsageZone usageZone = propertyElement.getValue().getAbstractUsageZone();
				if (usageZone != null) {
					helper.importObject(usageZone, ForeignKeys.create().with("buildingId", parentId));
					propertyElement.getValue().unsetAbstractUsageZone();
				} else {
					String href = propertyElement.getValue().getHref();
					if (href != null && href.length() != 0)
						helper.logOrThrowUnsupportedXLinkMessage(parent, AbstractUsageZone.class, href);
				}
			}
		}

		if (properties.contains(ThermalZonePropertyElement.class)) {
			for (ThermalZonePropertyElement propertyElement : properties.getAll(ThermalZonePropertyElement.class)) {
				AbstractThermalZone thermalZone = propertyElement.getValue().getAbstractThermalZone();
				if (thermalZone != null) {
					helper.importObject(thermalZone, ForeignKeys.create().with("buildingId", parentId));
					propertyElement.getValue().unsetAbstractThermalZone();
				} else {
					String href = propertyElement.getValue().getHref();
					if (href != null && href.length() != 0)
						helper.logOrThrowUnsupportedXLinkMessage(parent, AbstractThermalZone.class, href);
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
