package org.citydb.ade.energy.exporter;

import org.citydb.ade.energy.schema.ADETable;
import org.citydb.ade.exporter.ADEExporter;
import org.citydb.ade.exporter.CityGMLExportHelper;
import org.citydb.citygml.exporter.CityGMLExportException;
import org.citydb.database.schema.mapping.FeatureType;
import org.citydb.query.filter.projection.CombinedProjectionFilter;
import org.citydb.query.filter.projection.ProjectionFilter;
import org.citydb.sqlbuilder.expression.PlaceHolder;
import org.citydb.sqlbuilder.schema.Table;
import org.citydb.sqlbuilder.select.Select;
import org.citydb.sqlbuilder.select.operator.comparison.ComparisonFactory;
import org.citygml4j.ade.energy.model.core.DemandsProperty;
import org.citygml4j.ade.energy.model.core.EnergyDemand;
import org.citygml4j.ade.energy.model.core.EnergyDemandProperty;
import org.citygml4j.ade.energy.model.core.WeatherData;
import org.citygml4j.ade.energy.model.core.WeatherDataProperty;
import org.citygml4j.ade.energy.model.core.WeatherDataPropertyElement;
import org.citygml4j.ade.energy.model.module.EnergyADEModule;
import org.citygml4j.model.citygml.core.AbstractCityObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CityObjectPropertiesExporter implements ADEExporter {
    private final CityGMLExportHelper helper;

    private PreparedStatement ps;
    private WeatherDataExporter weatherDataExporter;
    private EnergyDemandExporter energyDemandExporter;
    private String module;

    public CityObjectPropertiesExporter(Connection connection, CityGMLExportHelper helper, ExportManager manager) throws CityGMLExportException, SQLException {
        this.helper = helper;

        String tableName = manager.getSchemaMapper().getTableName(ADETable.CITYOBJECT);
        CombinedProjectionFilter projectionFilter = helper.getCombinedProjectionFilter(tableName);
        module = EnergyADEModule.v1_0.getNamespaceURI();

        Table table = new Table(helper.getTableNameWithSchema(tableName));
        Select select = new Select().addProjection(table.getColumn("id"))
                .addSelection(ComparisonFactory.equalTo(table.getColumn("id"), new PlaceHolder<>()));
        ps = connection.prepareStatement(select.toString());

        weatherDataExporter = manager.getExporter(WeatherDataExporter.class);
        energyDemandExporter = manager.getExporter(EnergyDemandExporter.class);
    }

    public void doExport(AbstractCityObject parent, long parentId, FeatureType parentType, ProjectionFilter projectionFilter) throws CityGMLExportException, SQLException {
        ps.setLong(1, parentId);

        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                if (projectionFilter.containsProperty("weatherData", module)) {
                    for (WeatherData weatherData : weatherDataExporter.doExport(parentId)) {
                        WeatherDataPropertyElement property = new WeatherDataPropertyElement(new WeatherDataProperty(weatherData));
                        parent.addGenericApplicationPropertyOfCityObject(property);
                    }
                }

                if (projectionFilter.containsProperty("demands", module)) {
                    for (EnergyDemand energyDemand : energyDemandExporter.doExport(parentId)) {
                        DemandsProperty property = new DemandsProperty(new EnergyDemandProperty(energyDemand));
                        parent.addGenericApplicationPropertyOfCityObject(property);
                    }
                }
            }
        }
    }

    @Override
    public void close() throws CityGMLExportException, SQLException {
        ps.close();
    }
}
