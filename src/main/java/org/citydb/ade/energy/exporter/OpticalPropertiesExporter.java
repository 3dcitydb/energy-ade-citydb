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

package org.citydb.ade.energy.exporter;

import org.citydb.ade.energy.schema.ADETable;
import org.citydb.core.ade.exporter.ADEExporter;
import org.citydb.core.ade.exporter.CityGMLExportHelper;
import org.citydb.core.operation.exporter.CityGMLExportException;
import org.citydb.sqlbuilder.expression.PlaceHolder;
import org.citydb.sqlbuilder.schema.Table;
import org.citydb.sqlbuilder.select.Select;
import org.citydb.sqlbuilder.select.join.JoinFactory;
import org.citydb.sqlbuilder.select.operator.comparison.ComparisonFactory;
import org.citydb.sqlbuilder.select.operator.comparison.ComparisonName;
import org.citygml4j.ade.energy.model.core.Transmittance;
import org.citygml4j.ade.energy.model.core.TransmittanceProperty;
import org.citygml4j.ade.energy.model.core.WavelengthRangeType;
import org.citygml4j.ade.energy.model.materialAndConstruction.OpticalProperties;
import org.citygml4j.ade.energy.model.materialAndConstruction.Reflectance;
import org.citygml4j.ade.energy.model.materialAndConstruction.ReflectanceProperty;
import org.citygml4j.ade.energy.model.materialAndConstruction.SurfaceSide;
import org.citygml4j.model.gml.measures.Scale;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class OpticalPropertiesExporter implements ADEExporter {
    private PreparedStatement ps;

    public OpticalPropertiesExporter(Connection connection, CityGMLExportHelper helper, ExportManager manager) throws CityGMLExportException, SQLException {
        Table table = new Table(helper.getTableNameWithSchema(manager.getSchemaMapper().getTableName(ADETable.OPTICALPROPERTIES)));
        Table reflectance = new Table(helper.getTableNameWithSchema(manager.getSchemaMapper().getTableName(ADETable.REFLECTANCE)));
        Table transmittance = new Table(helper.getTableNameWithSchema(manager.getSchemaMapper().getTableName(ADETable.TRANSMITTANCE)));

        Select select = new Select().addProjection(table.getColumn("id"), table.getColumn("glazingratio"),
                table.getColumn("glazingratio_uom"), reflectance.getColumn("id", "r_id"), reflectance.getColumn("surface"),
                reflectance.getColumn("wavelengthrange"), reflectance.getColumn("fraction"), reflectance.getColumn("fraction_uom"),
                transmittance.getColumn("id", "t_id"), transmittance.getColumn("wavelengthrange", "t_wavelengthrange"),
                transmittance.getColumn("fraction", "t_fraction"), transmittance.getColumn("fraction_uom", "t_fraction_uom"))
                .addJoin(JoinFactory.left(reflectance, "opticalproper_reflectance_id", ComparisonName.EQUAL_TO, table.getColumn("id")))
                .addJoin(JoinFactory.left(transmittance, "opticalprope_transmittanc_id", ComparisonName.EQUAL_TO, table.getColumn("id")));
        select.addSelection(ComparisonFactory.equalTo(table.getColumn("id"), new PlaceHolder<>()));
        ps = connection.prepareStatement(select.toString());
    }

    public OpticalProperties doExport(long objectId) throws CityGMLExportException, SQLException {
        ps.setLong(1, objectId);

        try (ResultSet rs = ps.executeQuery()) {
            OpticalProperties properties = null;
            Map<Long, Set<Long>> reflectanceIds = new HashMap<>();
            Map<Long, Set<Long>> transmittanceIds = new HashMap<>();

            while (rs.next()) {
                long propertiesId = rs.getLong("id");

                if (properties == null) {
                    properties = new OpticalProperties();

                    double glazingratio = rs.getDouble("glazingratio");
                    if (!rs.wasNull()) {
                        Scale scale = new Scale(glazingratio);
                        scale.setUom(rs.getString("glazingratio_uom"));
                        properties.setGlazingRatio(scale);
                    }
                }

                long reflectanceId = rs.getLong("r_id");
                if (!rs.wasNull() && reflectanceIds.computeIfAbsent(propertiesId, v -> new HashSet<>()).add(reflectanceId)) {
                    SurfaceSide surface = SurfaceSide.fromValue(rs.getString("surface"));
                    WavelengthRangeType wavelengthRange = WavelengthRangeType.fromValue(rs.getString("wavelengthrange"));
                    double fraction = rs.getDouble("fraction");

                    if (!rs.wasNull() && surface != null && wavelengthRange != null) {
                        Reflectance reflectance = new Reflectance();

                        reflectance.setSurface(surface);
                        reflectance.setWavelengthRangeType(wavelengthRange);

                        Scale scale = new Scale(fraction);
                        scale.setUom(rs.getString("fraction_uom"));
                        reflectance.setFraction(scale);

                        properties.addReflectance(new ReflectanceProperty(reflectance));
                    }
                }

                long transmittanceId = rs.getLong("t_id");
                if (!rs.wasNull() && transmittanceIds.computeIfAbsent(propertiesId, v -> new HashSet<>()).add(transmittanceId)) {
                    WavelengthRangeType wavelengthRange = WavelengthRangeType.fromValue(rs.getString("t_wavelengthrange"));
                    double fraction = rs.getDouble("t_fraction");

                    if (!rs.wasNull() && wavelengthRange != null) {
                        Transmittance transmittance = new Transmittance();

                        transmittance.setWavelengthRangeType(wavelengthRange);

                        Scale scale = new Scale(fraction);
                        scale.setUom(rs.getString("t_fraction_uom"));
                        transmittance.setFraction(scale);

                        properties.addTransmittance(new TransmittanceProperty(transmittance));
                    }
                }
            }

            return properties;
        }
    }

    @Override
    public void close() throws CityGMLExportException, SQLException {
        ps.close();
    }
}
