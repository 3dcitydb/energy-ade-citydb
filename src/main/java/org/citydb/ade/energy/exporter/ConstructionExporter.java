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
import org.citygml4j.ade.energy.model.core.AbstractConstructionProperty;
import org.citygml4j.ade.energy.model.materialAndConstruction.Construction;
import org.citygml4j.ade.energy.model.materialAndConstruction.Layer;
import org.citygml4j.ade.energy.model.materialAndConstruction.LayerProperty;
import org.citygml4j.ade.energy.model.materialAndConstruction.OpticalProperties;
import org.citygml4j.ade.energy.model.materialAndConstruction.OpticalPropertiesProperty;
import org.citygml4j.ade.energy.model.module.EnergyADEModule;
import org.citygml4j.model.gml.basicTypes.Measure;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ConstructionExporter implements ADEExporter {
    private final CityGMLExportHelper helper;
    private final int objectClassId;

    private PreparedStatement ps;
    private OpticalPropertiesExporter opticalPropertiesExporter;
    private LayerExporter layerExporter;
    private String module;

    public ConstructionExporter(Connection connection, CityGMLExportHelper helper, ExportManager manager) throws CityGMLExportException, SQLException {
        this.helper = helper;
        objectClassId = manager.getObjectMapper().getObjectClassId(Construction.class);

        String tableName = manager.getSchemaMapper().getTableName(ADETable.CONSTRUCTION);
        CombinedProjectionFilter projectionFilter = helper.getCombinedProjectionFilter(tableName);
        module = EnergyADEModule.v1_0.getNamespaceURI();

        Table table = new Table(helper.getTableNameWithSchema(tableName));

        Select select = new Select().addProjection(table.getColumn("id"));
        if (projectionFilter.containsProperty("uValue", module))
            select.addProjection(table.getColumn("uvalue"), table.getColumn("uvalue_uom"));
        if (projectionFilter.containsProperty("opticalProperties", module))
            select.addProjection(table.getColumn("opticalproperties_id"));
        select.addSelection(ComparisonFactory.equalTo(table.getColumn("id"), new PlaceHolder<>()));
        ps = connection.prepareStatement(select.toString());

        opticalPropertiesExporter = manager.getExporter(OpticalPropertiesExporter.class);
        layerExporter = manager.getExporter(LayerExporter.class);
    }

    public AbstractConstructionProperty doExport(long objectId) throws CityGMLExportException, SQLException {
        ps.setLong(1, objectId);

        try (ResultSet rs = ps.executeQuery()) {
            Construction construction = null;

            if (rs.next()) {
                construction = helper.createObject(objectId, objectClassId, Construction.class);
                if (construction == null) {
                    helper.logOrThrowErrorMessage("Failed to instantiate " + helper.getObjectSignature(objectClassId, objectId) + " as construction object.");
                    return null;
                }

                if (construction.isSetId() && helper.lookupAndPutObjectUID(construction.getId(), objectId, objectClassId))
                    return new AbstractConstructionProperty("#" + construction.getId());

                FeatureType featureType = helper.getFeatureType(objectClassId);
                ProjectionFilter projectionFilter = helper.getProjectionFilter(featureType);

                if (projectionFilter.containsProperty("uValue", module)) {
                    double uValue = rs.getDouble("uvalue");
                    if (!rs.wasNull()) {
                        Measure measure = new Measure(uValue);
                        measure.setUom(rs.getString("uvalue_uom"));
                        construction.setUValue(measure);
                    }
                }

                if (projectionFilter.containsProperty("opticalProperties", module)) {
                    long propertiesId = rs.getLong("opticalproperties_id");
                    if (!rs.wasNull()) {
                        OpticalProperties opticalProperties = opticalPropertiesExporter.doExport(propertiesId);
                        if (opticalProperties != null)
                            construction.setOpticalProperties(new OpticalPropertiesProperty(opticalProperties));
                    }
                }

                if (projectionFilter.containsProperty("layer", module)) {
                    for (Layer layer : layerExporter.doExport(objectId))
                        construction.addLayer(new LayerProperty(layer));
                }
            }

            return new AbstractConstructionProperty(construction);
        }
    }

    @Override
    public void close() throws CityGMLExportException, SQLException {
        ps.close();
    }
}
