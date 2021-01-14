package org.citydb.ade.energy.importer;

import org.citydb.ade.importer.ADEImporter;
import org.citydb.ade.importer.CityGMLImportHelper;
import org.citydb.ade.importer.ForeignKeys;
import org.citydb.citygml.importer.CityGMLImportException;
import org.citydb.database.schema.mapping.AbstractObjectType;
import org.citygml4j.ade.energy.model.core.HeatExchangeTypeProperty;
import org.citygml4j.ade.energy.model.occupantBehaviour.Occupants;
import org.citygml4j.ade.energy.model.supportingClasses.AbstractSchedule;
import org.citygml4j.ade.energy.model.supportingClasses.AbstractScheduleProperty;
import org.citydb.ade.energy.schema.ADETable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

public class OccupantsImporter implements ADEImporter {
    private final CityGMLImportHelper helper;

    private HeatExchangeTypeImporter heatExchangeTypeImporter;
    private PreparedStatement ps;
    private int batchCounter;

    public OccupantsImporter(Connection connection, CityGMLImportHelper helper, ImportManager manager) throws CityGMLImportException, SQLException {
        this.helper = helper;

        ps = connection.prepareStatement("insert into " +
                helper.getTableNameWithSchema(manager.getSchemaMapper().getTableName(ADETable.OCCUPANTS)) + " " +
                "(id, usagezone_occupiedby_id, numberofoccupants, heatdissipation_id, occupancyrate_id) " +
                "values (?, ?, ?, ?, ?)");

        heatExchangeTypeImporter = manager.getImporter(HeatExchangeTypeImporter.class);
    }

    public void doImport(Occupants occupants, long objectId, AbstractObjectType<?> objectType, ForeignKeys foreignKeys) throws CityGMLImportException, SQLException {
        long parentId = foreignKeys.get("parentId");

        ps.setLong(1, objectId);

        if (parentId != 0)
            ps.setLong(2, parentId);
        else
            ps.setNull(2, Types.NULL);

        if (occupants.isSetNumberOfOccupants())
            ps.setLong(3, occupants.getNumberOfOccupants());
        else
            ps.setNull(3, Types.NULL);

        long heatDissipationId = 0;
        if (occupants.isSetHeatDissipation()) {
            HeatExchangeTypeProperty property = occupants.getHeatDissipation();
            if (property.isSetHeatExchangeType()) {
                heatDissipationId = heatExchangeTypeImporter.doImport(property.getHeatExchangeType());
                property.unsetHeatExchangeType();
            }
        }

        if (heatDissipationId != 0)
            ps.setLong(4, heatDissipationId);
        else
            ps.setNull(4, Types.NULL);

        long occupancyRateId = 0;
        if (occupants.isSetOccupancyRate()) {
            AbstractScheduleProperty property = occupants.getOccupancyRate();
            if (property.isSetAbstractSchedule()) {
                occupancyRateId = helper.importObject(property.getAbstractSchedule());
                property.unsetAbstractSchedule();
            } else {
                String href = property.getHref();
                if (href != null && href.length() != 0)
                    helper.logOrThrowUnsupportedXLinkMessage(occupants, AbstractSchedule.class, href);
            }
        }

        if (occupancyRateId != 0)
            ps.setLong(5, occupancyRateId);
        else
            ps.setNull(5, Types.NULL);

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
