/*
 * 3D City Database - The Open Source CityGML Database
 * https://www.3dcitydb.org/
 *
 * Copyright 2013 - 2021
 * Chair of Geoinformatics
 * Technical University of Munich, Germany
 * https://www.lrg.tum.de/gis/
 *
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 *
 * Virtual City Systems, Berlin <https://vc.systems/>
 * M.O.S.S. Computer Grafik Systeme GmbH, Taufkirchen <http://www.moss.de/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citydb.ade.energy;

import org.citydb.ImpExpLauncher;
import org.citydb.ade.ADEExtension;
import org.citydb.ade.ADEExtensionException;
import org.citydb.ade.ADEObjectMapper;
import org.citydb.ade.energy.exporter.ExportManager;
import org.citydb.ade.energy.importer.ImportManager;
import org.citydb.ade.energy.schema.ObjectMapper;
import org.citydb.ade.energy.schema.SchemaMapper;
import org.citydb.ade.exporter.ADEExportManager;
import org.citydb.ade.importer.ADEImportManager;
import org.citydb.database.schema.mapping.SchemaMapping;
import org.citygml4j.ade.energy.EnergyADEContext;
import org.citygml4j.model.citygml.ade.binding.ADEContext;

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
        new ImpExpLauncher().withArgs(args)
                .withADEExtension(adeExtension)
                .start();
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
