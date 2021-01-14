package org.citydb.ade.energy;

import org.citydb.ImpExp;
import org.citydb.ade.ADEExtension;
import org.citydb.ade.ADEExtensionException;
import org.citydb.ade.ADEObjectMapper;
import org.citydb.ade.energy.exporter.ExportManager;
import org.citydb.ade.energy.schema.ObjectMapper;
import org.citydb.ade.energy.schema.SchemaMapper;
import org.citydb.ade.exporter.ADEExportManager;
import org.citydb.ade.importer.ADEImportManager;
import org.citydb.database.schema.mapping.SchemaMapping;
import org.citygml4j.ade.energy.EnergyADEContext;
import org.citygml4j.model.citygml.ade.binding.ADEContext;
import org.citydb.ade.energy.importer.ImportManager;

import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

public class EnergyADEExtension extends ADEExtension {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SchemaMapper schemaMapper = new SchemaMapper();
    private final EnergyADEContext context = new EnergyADEContext();

    public static void main(String[] args) {
        EnergyADEExtension adeExtension = new EnergyADEExtension();
        adeExtension.setBasePath(Paths.get("resources/database").toAbsolutePath());
        new ImpExp().doMain(args, adeExtension);
    }

    @Override
    public void init(SchemaMapping schemaMapping) throws ADEExtensionException {
        objectMapper.populateObjectClassIds(schemaMapping);
        schemaMapper.populateSchemaNames(schemaMapping.getMetadata().getDBPrefix().toLowerCase());
    }

    @Override
    public List<ADEContext> getADEContexts() {
        return Collections.singletonList(context);
    }

    @Override
    public ADEObjectMapper getADEObjectMapper() {
        return objectMapper;
    }

    @Override
    public ADEImportManager createADEImportManager() {
        return new ImportManager(this, schemaMapper);
    }

    @Override
    public ADEExportManager createADEExportManager() {
        return new ExportManager(objectMapper, schemaMapper);
    }
}
