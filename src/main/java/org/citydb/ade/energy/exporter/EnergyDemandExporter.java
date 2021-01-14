package org.citydb.ade.energy.exporter;

import org.citydb.ade.energy.schema.ADETable;
import org.citydb.ade.exporter.ADEExporter;
import org.citydb.ade.exporter.CityGMLExportHelper;
import org.citydb.citygml.exporter.CityGMLExportException;
import org.citydb.database.schema.mapping.FeatureType;
import org.citydb.database.schema.mapping.MappingConstants;
import org.citydb.query.filter.projection.CombinedProjectionFilter;
import org.citydb.query.filter.projection.ProjectionFilter;
import org.citydb.sqlbuilder.expression.PlaceHolder;
import org.citydb.sqlbuilder.schema.Table;
import org.citydb.sqlbuilder.select.Select;
import org.citydb.sqlbuilder.select.join.JoinFactory;
import org.citydb.sqlbuilder.select.operator.comparison.ComparisonFactory;
import org.citydb.sqlbuilder.select.operator.comparison.ComparisonName;
import org.citygml4j.ade.energy.model.core.EndUseTypeValue;
import org.citygml4j.ade.energy.model.core.EnergyDemand;
import org.citygml4j.ade.energy.model.module.EnergyADEModule;
import org.citygml4j.ade.energy.model.supportingClasses.AbstractTimeSeries;
import org.citygml4j.ade.energy.model.supportingClasses.AbstractTimeSeriesProperty;
import org.citygml4j.model.gml.base.Reference;
import org.citygml4j.model.gml.basicTypes.Code;
import org.citygml4j.model.gml.basicTypes.Measure;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class EnergyDemandExporter implements ADEExporter {
    private final CityGMLExportHelper helper;
    private final int objectClassId;

    private PreparedStatement ps;
    private TimeSeriesExporter timeSeriesExporter;
    private String module;

    public EnergyDemandExporter(Connection connection, CityGMLExportHelper helper, ExportManager manager) throws CityGMLExportException, SQLException {
        this.helper = helper;
        objectClassId = manager.getObjectMapper().getObjectClassId(EnergyDemand.class);

        String tableName = manager.getSchemaMapper().getTableName(ADETable.ENERGYDEMAND);
        CombinedProjectionFilter projectionFilter = helper.getCombinedProjectionFilter(tableName);
        module = EnergyADEModule.v1_0.getNamespaceURI();

        Table table = new Table(helper.getTableNameWithSchema(tableName));
        Select select = new Select().addProjection(table.getColumn("id"), table.getColumn("enduse"),
                table.getColumn("energyamount_id"));
        if (projectionFilter.containsProperty("maximumLoad", module))
            select.addProjection(table.getColumn("maximumload"), table.getColumn("maximumload_uom"));
        if (projectionFilter.containsProperty("energyCarrierType", module))
            select.addProjection(table.getColumn("energycarriertype"), table.getColumn("energycarriertype_codespace"));
        if (projectionFilter.containsProperty("demandedBy", module)) {
            Table demandedBy = new Table(helper.getTableNameWithSchema(manager.getSchemaMapper().getTableName(ADETable.ENERGYDEM_TO_CITYOBJEC)));
            Table cityObject = new Table(helper.getTableNameWithSchema(MappingConstants.CITYOBJECT));
            select.addJoin(JoinFactory.left(demandedBy, "energydemand_id", ComparisonName.EQUAL_TO, table.getColumn("id")))
                    .addJoin(JoinFactory.left(cityObject, "id", ComparisonName.EQUAL_TO, demandedBy.getColumn("cityobject_id")))
                    .addProjection(cityObject.getColumn("gmlid"));
        }
        select.addSelection(ComparisonFactory.equalTo(table.getColumn("cityobject_demands_id"), new PlaceHolder<>()));
        ps = connection.prepareStatement(select.toString());

        timeSeriesExporter = manager.getExporter(TimeSeriesExporter.class);
    }

    public Collection<EnergyDemand> doExport(long parentId) throws CityGMLExportException, SQLException {
        ps.setLong(1, parentId);

        try (ResultSet rs = ps.executeQuery()) {
            long currentEnergyDemandId = 0;
            EnergyDemand energyDemand = null;
            ProjectionFilter projectionFilter = null;
            Map<Long, EnergyDemand> energyDemands = new HashMap<>();

            while (rs.next()) {
                long energyDemandId = rs.getLong("id");

                if (energyDemandId != currentEnergyDemandId || energyDemand == null) {
                    currentEnergyDemandId = energyDemandId;
                    energyDemand = energyDemands.get(energyDemandId);

                    if (energyDemand == null) {
                        energyDemand = helper.createObject(energyDemandId, objectClassId, EnergyDemand.class);
                        if (energyDemand == null) {
                            helper.logOrThrowErrorMessage("Failed to instantiate " + helper.getObjectSignature(objectClassId, energyDemandId) + " as energy demand object.");
                            continue;
                        }

                        FeatureType featureType = helper.getFeatureType(objectClassId);
                        projectionFilter = helper.getProjectionFilter(featureType);

                        energyDemand.setEndUse(EndUseTypeValue.fromValue(rs.getString("enduse")));

                        long energyAmountId = rs.getLong("energyamount_id");
                        if (!rs.wasNull()) {
                            AbstractTimeSeries timeSeries = timeSeriesExporter.doExport(energyAmountId);
                            if (timeSeries != null)
                                energyDemand.setEnergyAmount(new AbstractTimeSeriesProperty(timeSeries));
                        }

                        if (projectionFilter.containsProperty("maximumLoad", module)) {
                            double maximumLoad = rs.getDouble("maximumload");
                            if (!rs.wasNull()) {
                                Measure measure = new Measure(maximumLoad);
                                measure.setUom(rs.getString("maximumload_uom"));
                                energyDemand.setMaximumLoad(measure);
                            }
                        }

                        if (projectionFilter.containsProperty("energyCarrierType", module)) {
                            String energyCarrierType = rs.getString("energycarriertype");
                            if (energyCarrierType != null) {
                                Code code = new Code(energyCarrierType);
                                code.setCodeSpace(rs.getString("energycarriertype_codespace"));
                                energyDemand.setEnergyCarrierType(code);
                            }
                        }

                        energyDemand.setLocalProperty("projection", projectionFilter);
                        energyDemands.put(energyDemandId, energyDemand);
                    } else
                        projectionFilter = (ProjectionFilter) energyDemand.getLocalProperty("projection");
                }

                if (projectionFilter.containsProperty("demandedBy", module)) {
                    String gmlId = rs.getString("gmlid");
                    if (gmlId != null)
                        energyDemand.addDemandedBy(new Reference("#" + gmlId));
                }

            }

            return energyDemands.values();
        }
    }

    @Override
    public void close() throws CityGMLExportException, SQLException {
        ps.close();
    }
}
