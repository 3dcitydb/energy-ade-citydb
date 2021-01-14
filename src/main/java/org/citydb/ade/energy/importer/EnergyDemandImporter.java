package org.citydb.ade.energy.importer;

import org.citydb.ade.energy.schema.ADETable;
import org.citydb.ade.energy.schema.SchemaMapper;
import org.citydb.ade.importer.ADEImporter;
import org.citydb.ade.importer.CityGMLImportHelper;
import org.citydb.ade.importer.ForeignKeys;
import org.citydb.citygml.importer.CityGMLImportException;
import org.citydb.database.schema.mapping.AbstractObjectType;
import org.citygml4j.ade.energy.model.core.EnergyDemand;
import org.citygml4j.ade.energy.model.supportingClasses.AbstractTimeSeries;
import org.citygml4j.ade.energy.model.supportingClasses.AbstractTimeSeriesProperty;
import org.citygml4j.model.gml.base.Reference;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

public class EnergyDemandImporter implements ADEImporter {
    private final CityGMLImportHelper helper;
    private final SchemaMapper schemaMapper;

    private PreparedStatement ps;
    private int batchCounter;

    public EnergyDemandImporter(Connection connection, CityGMLImportHelper helper, ImportManager manager) throws CityGMLImportException, SQLException {
        this.helper = helper;
        schemaMapper = manager.getSchemaMapper();

        ps = connection.prepareStatement("insert into " +
                helper.getTableNameWithSchema(manager.getSchemaMapper().getTableName(ADETable.ENERGYDEMAND)) + " " +
                "(id, cityobject_demands_id, energyamount_id, enduse, maximumload, maximumload_uom, " +
                "energycarriertype, energycarriertype_codespace) " +
                "values (?, ?, ?, ?, ?, ?, ?, ?)");
    }

    public void doImport(EnergyDemand energyDemand, long objectId, AbstractObjectType<?> objectType, ForeignKeys foreignKeys) throws CityGMLImportException, SQLException {
        long parentId = foreignKeys.get("cityObjectId");

        ps.setLong(1, objectId);

        if (parentId != 0)
            ps.setLong(2, parentId);
        else
            ps.setNull(2, Types.NULL);

        long energyAmountId = 0;
        if (energyDemand.isSetEnergyAmount()) {
            AbstractTimeSeriesProperty property = energyDemand.getEnergyAmount();
            if (property.isSetAbstractTimeSeries()) {
                energyAmountId = helper.importObject(property.getAbstractTimeSeries());
                property.unsetAbstractTimeSeries();
            } else {
                String href = property.getHref();
                if (href != null && href.length() != 0)
                    helper.logOrThrowUnsupportedXLinkMessage(energyDemand, AbstractTimeSeries.class, href);
            }
        }

        if (energyAmountId != 0)
            ps.setLong(3, energyAmountId);
        else
            ps.setNull(3, Types.NULL);

        if (energyDemand.isSetEndUse())
            ps.setString(4, energyDemand.getEndUse().value());
        else
            ps.setNull(4, Types.VARCHAR);

        if (energyDemand.isSetMaximumLoad() && energyDemand.getMaximumLoad().isSetValue()) {
            ps.setDouble(5, energyDemand.getMaximumLoad().getValue());
            ps.setString(6, energyDemand.getMaximumLoad().getUom());
        } else {
            ps.setNull(5, Types.DOUBLE);
            ps.setNull(6, Types.VARCHAR);
        }

        if (energyDemand.isSetEnergyCarrierType() && energyDemand.getEnergyCarrierType().isSetValue()) {
            ps.setString(7, energyDemand.getEnergyCarrierType().getValue());
            ps.setString(8, energyDemand.getEnergyCarrierType().getCodeSpace());
        } else {
            ps.setNull(7, Types.VARCHAR);
            ps.setNull(8, Types.VARCHAR);
        }

        ps.addBatch();
        if (++batchCounter == helper.getDatabaseAdapter().getMaxBatchSize())
            helper.executeBatch(objectType);

        if (energyDemand.isSetDemandedBy()) {
            for (Reference reference : energyDemand.getDemandedBy()) {
                if (reference.isSetHref())
                    helper.propagateObjectXlink(
                            schemaMapper.getTableName(ADETable.ENERGYDEM_TO_CITYOBJEC),
                            objectId, "energydemand_id",
                            reference.getHref(), "cityobject_id");
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
