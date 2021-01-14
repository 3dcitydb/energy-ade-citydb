package org.citydb.ade.energy.importer;

import org.citydb.ade.importer.ADEImporter;
import org.citydb.ade.importer.CityGMLImportHelper;
import org.citydb.ade.importer.ForeignKeys;
import org.citydb.citygml.importer.CityGMLImportException;
import org.citydb.database.schema.mapping.AbstractObjectType;
import org.citygml4j.ade.energy.model.materialAndConstruction.AbstractMaterial;
import org.citygml4j.ade.energy.model.materialAndConstruction.Gas;
import org.citygml4j.ade.energy.model.materialAndConstruction.SolidMaterial;
import org.citydb.ade.energy.schema.ADETable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

public class MaterialImporter implements ADEImporter {
    private final CityGMLImportHelper helper;

    private PreparedStatement psMaterial;
    private PreparedStatement psSolidMaterial;
    private PreparedStatement psGas;
    private int batchCounter;

    public MaterialImporter(Connection connection, CityGMLImportHelper helper, ImportManager manager) throws SQLException {
        this.helper = helper;

        psMaterial = connection.prepareStatement("insert into " +
                helper.getTableNameWithSchema(manager.getSchemaMapper().getTableName(ADETable.MATERIAL)) + " " +
                "(id, objectclass_id) " +
                "values (?, ?)");

        psSolidMaterial = connection.prepareStatement("insert into " +
                helper.getTableNameWithSchema(manager.getSchemaMapper().getTableName(ADETable.SOLIDMATERIAL)) + " " +
                "(id, conductivity, conductivity_uom, density, density_uom, permeance, permeance_uom, specificheat, specificheat_uom) " +
                "values (?, ?, ?, ?, ?, ?, ?, ?, ?)");

        psGas = connection.prepareStatement("insert into " +
                helper.getTableNameWithSchema(manager.getSchemaMapper().getTableName(ADETable.GAS)) + " " +
                "(id, isventilated, rvalue, rvalue_uom) " +
                "values (?, ?, ?, ?)");
    }

    public void doImport(AbstractMaterial material, long objectId, AbstractObjectType<?> objectType, ForeignKeys foreignKeys) throws CityGMLImportException, SQLException {
        psMaterial.setLong(1, objectId);
        psMaterial.setInt(2, objectType.getObjectClassId());
        psMaterial.addBatch();

        if (material instanceof SolidMaterial) {
            SolidMaterial solidMaterial = (SolidMaterial)material;

            psSolidMaterial.setLong(1, objectId);

            if (solidMaterial.isSetConductivity() && solidMaterial.getConductivity().isSetValue()) {
                psSolidMaterial.setDouble(2, solidMaterial.getConductivity().getValue());
                psSolidMaterial.setString(3, solidMaterial.getConductivity().getUom());
            } else {
                psSolidMaterial.setNull(2, Types.DOUBLE);
                psSolidMaterial.setNull(3, Types.VARCHAR);
            }

            if (solidMaterial.isSetDensity() && solidMaterial.getDensity().isSetValue()) {
                psSolidMaterial.setDouble(4, solidMaterial.getDensity().getValue());
                psSolidMaterial.setString(5, solidMaterial.getDensity().getUom());
            } else {
                psSolidMaterial.setNull(4, Types.DOUBLE);
                psSolidMaterial.setNull(5, Types.VARCHAR);
            }

            if (solidMaterial.isSetPermeance() && solidMaterial.getPermeance().isSetValue()) {
                psSolidMaterial.setDouble(6, solidMaterial.getPermeance().getValue());
                psSolidMaterial.setString(7, solidMaterial.getPermeance().getUom());
            } else {
                psSolidMaterial.setNull(6, Types.DOUBLE);
                psSolidMaterial.setNull(7, Types.VARCHAR);
            }

            if (solidMaterial.isSetSpecificHeat() && solidMaterial.getSpecificHeat().isSetValue()) {
                psSolidMaterial.setDouble(8, solidMaterial.getSpecificHeat().getValue());
                psSolidMaterial.setString(9, solidMaterial.getSpecificHeat().getUom());
            } else {
                psSolidMaterial.setNull(8, Types.DOUBLE);
                psSolidMaterial.setNull(9, Types.VARCHAR);
            }

            psSolidMaterial.addBatch();
        } else if (material instanceof Gas) {
            Gas gas = (Gas)material;

            psGas.setLong(1, objectId);
            psGas.setInt(2, gas.isVentilated() ? 1 : 0);

            if (gas.isSetRValue() && gas.getRValue().isSetValue()) {
                psGas.setDouble(3, gas.getRValue().getValue());
                psGas.setString(4, gas.getRValue().getUom());
            } else {
                psGas.setNull(3, Types.DOUBLE);
                psGas.setNull(4, Types.VARCHAR);
            }

            psGas.addBatch();
        }

        if (++batchCounter == helper.getDatabaseAdapter().getMaxBatchSize())
            helper.executeBatch(objectType);
    }

    @Override
    public void executeBatch() throws CityGMLImportException, SQLException {
        if (batchCounter > 0) {
            psMaterial.executeBatch();
            psSolidMaterial.executeBatch();
            psGas.executeBatch();
            batchCounter = 0;
        }
    }

    @Override
    public void close() throws CityGMLImportException, SQLException {
        psMaterial.close();
        psSolidMaterial.close();
        psGas.close();
    }
}
