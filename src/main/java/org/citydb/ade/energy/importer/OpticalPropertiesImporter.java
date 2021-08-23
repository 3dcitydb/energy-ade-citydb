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

package org.citydb.ade.energy.importer;

import org.citydb.ade.energy.schema.ADESequence;
import org.citydb.ade.energy.schema.ADETable;
import org.citydb.ade.energy.schema.SchemaMapper;
import org.citydb.core.ade.importer.ADEImporter;
import org.citydb.core.ade.importer.CityGMLImportHelper;
import org.citydb.core.operation.importer.CityGMLImportException;
import org.citygml4j.ade.energy.model.core.Transmittance;
import org.citygml4j.ade.energy.model.core.TransmittanceProperty;
import org.citygml4j.ade.energy.model.materialAndConstruction.OpticalProperties;
import org.citygml4j.ade.energy.model.materialAndConstruction.Reflectance;
import org.citygml4j.ade.energy.model.materialAndConstruction.ReflectanceProperty;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

public class OpticalPropertiesImporter implements ADEImporter {
    private final CityGMLImportHelper helper;
    private final SchemaMapper schemaMapper;

    private ReflectanceImporter reflectanceImporter;
    private TransmittanceImporter transmittanceImporter;
    private PreparedStatement ps;
    private int batchCounter;

    public OpticalPropertiesImporter(Connection connection, CityGMLImportHelper helper, ImportManager manager) throws CityGMLImportException, SQLException {
        this.helper = helper;
        this.schemaMapper = manager.getSchemaMapper();

        ps = connection.prepareStatement("insert into " +
                helper.getTableNameWithSchema(manager.getSchemaMapper().getTableName(ADETable.OPTICALPROPERTIES)) + " " +
                "(id, glazingratio, glazingratio_uom) " +
                "values (?, ?, ?)");

        reflectanceImporter = manager.getImporter(ReflectanceImporter.class);
        transmittanceImporter = manager.getImporter(TransmittanceImporter.class);
    }

    public long doImport(OpticalProperties opticalProperties) throws CityGMLImportException, SQLException {
        long objectId = helper.getNextSequenceValue(schemaMapper.getSequenceName(ADESequence.OPTICALPROPERTIES_SEQ));
        ps.setLong(1, objectId);

        if (opticalProperties.isSetGlazingRatio() && opticalProperties.getGlazingRatio().isSetValue()) {
            ps.setDouble(2, opticalProperties.getGlazingRatio().getValue());
            ps.setString(3, opticalProperties.getGlazingRatio().getUom());
        } else {
            ps.setNull(2, Types.DOUBLE);
            ps.setNull(3, Types.VARCHAR);
        }

        ps.addBatch();
        if (++batchCounter == helper.getDatabaseAdapter().getMaxBatchSize())
            helper.executeBatch(schemaMapper.getTableName(ADETable.OPTICALPROPERTIES));

        if (opticalProperties.isSetReflectance()) {
            for (ReflectanceProperty property : opticalProperties.getReflectance()) {
                Reflectance reflectance = property.getReflectance();
                if (reflectance != null) {
                    reflectanceImporter.doImport(reflectance, objectId);
                    property.unsetReflectance();
                }
            }
        }

        if (opticalProperties.isSetTransmittance()) {
            for (TransmittanceProperty property : opticalProperties.getTransmittance()) {
                Transmittance transmittance = property.getTransmittance();
                if (transmittance != null) {
                    transmittanceImporter.doImport(transmittance, objectId);
                    property.unsetTransmittance();
                }
            }
        }

        return objectId;
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
