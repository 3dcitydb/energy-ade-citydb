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
import org.citydb.ade.importer.ADEImporter;
import org.citydb.ade.importer.CityGMLImportHelper;
import org.citydb.citygml.importer.CityGMLImportException;
import org.citygml4j.ade.energy.model.core.Transmittance;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

public class TransmittanceImporter implements ADEImporter {
    private final CityGMLImportHelper helper;
    private final SchemaMapper schemaMapper;

    private PreparedStatement ps;
    private int batchCounter;

    public TransmittanceImporter(Connection connection, CityGMLImportHelper helper, ImportManager manager) throws SQLException {
        this.helper = helper;
        this.schemaMapper = manager.getSchemaMapper();

        ps = connection.prepareStatement("insert into " +
                helper.getTableNameWithSchema(manager.getSchemaMapper().getTableName(ADETable.TRANSMITTANCE)) + " " +
                "(id, opticalprope_transmittanc_id, wavelengthrange, fraction, fraction_uom) " +
                "values (?, ?, ?, ?, ?)");
    }

    public void doImport(Transmittance transmittance, long parentId) throws CityGMLImportException, SQLException {
        long objectId = helper.getNextSequenceValue(schemaMapper.getSequenceName(ADESequence.TRANSMITTANCE_SEQ));
        ps.setLong(1, objectId);

        ps.setLong(2, parentId);

        if (transmittance.isSetWavelengthRangeType())
            ps.setString(3, transmittance.getWavelengthRangeType().value());
        else
            ps.setNull(3, Types.VARCHAR);

        if (transmittance.isSetFraction() && transmittance.getFraction().isSetValue()) {
            ps.setDouble(4, transmittance.getFraction().getValue());
            ps.setString(5, transmittance.getFraction().getUom());
        } else {
            ps.setNull(4, Types.DOUBLE);
            ps.setNull(5, Types.VARCHAR);
        }

        ps.addBatch();
        if (++batchCounter == helper.getDatabaseAdapter().getMaxBatchSize())
            helper.executeBatch(schemaMapper.getTableName(ADETable.TRANSMITTANCE));
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
