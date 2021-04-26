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
import org.citydb.ade.exporter.ADEExporter;
import org.citydb.ade.exporter.CityGMLExportHelper;
import org.citydb.citygml.exporter.CityGMLExportException;
import org.citydb.sqlbuilder.expression.PlaceHolder;
import org.citydb.sqlbuilder.schema.Table;
import org.citydb.sqlbuilder.select.Select;
import org.citydb.sqlbuilder.select.join.JoinFactory;
import org.citydb.sqlbuilder.select.operator.comparison.ComparisonFactory;
import org.citydb.sqlbuilder.select.operator.comparison.ComparisonName;
import org.citygml4j.ade.energy.model.core.HeatExchangeType;
import org.citygml4j.ade.energy.model.core.HeatExchangeTypeProperty;
import org.citygml4j.ade.energy.model.occupantBehaviour.Occupants;
import org.citygml4j.ade.energy.model.supportingClasses.AbstractScheduleProperty;
import org.citygml4j.ade.energy.model.supportingClasses.DailyPatternSchedule;
import org.citygml4j.model.gml.basicTypes.Measure;
import org.citygml4j.model.gml.measures.Scale;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class OccupantsExporter implements ADEExporter {
    private final CityGMLExportHelper helper;
    private final int objectClassId;

    private PreparedStatement ps;
    private DailyPatternScheduleExporter dailyPatternScheduleExporter;

    public OccupantsExporter(Connection connection, CityGMLExportHelper helper, ExportManager manager) throws CityGMLExportException, SQLException {
        this.helper = helper;
        objectClassId = manager.getObjectMapper().getObjectClassId(Occupants.class);

        Table table = new Table(helper.getTableNameWithSchema(manager.getSchemaMapper().getTableName(ADETable.OCCUPANTS)));
        Table heatExchangeType = new Table(helper.getTableNameWithSchema(manager.getSchemaMapper().getTableName(ADETable.HEATEXCHANGETYPE)));

        Select select = new Select().addProjection(table.getColumn("id"), table.getColumn("numberofoccupants"),
                table.getColumn("occupancyrate_id"),
                heatExchangeType.getColumn("convectivefraction"), heatExchangeType.getColumn("convectivefraction_uom"),
                heatExchangeType.getColumn("latentfraction"), heatExchangeType.getColumn("latentfraction_uom"),
                heatExchangeType.getColumn("radiantfraction"), heatExchangeType.getColumn("radiantfraction_uom"),
                heatExchangeType.getColumn("totalvalue"), heatExchangeType.getColumn("totalvalue_uom"))
                .addJoin(JoinFactory.left(heatExchangeType, "id", ComparisonName.EQUAL_TO, table.getColumn("heatdissipation_id")))
                .addSelection(ComparisonFactory.equalTo(table.getColumn("usagezone_occupiedby_id"), new PlaceHolder<>()));
        ps = connection.prepareStatement(select.toString());

        dailyPatternScheduleExporter = manager.getExporter(DailyPatternScheduleExporter.class);
    }

    public Collection<Occupants> doExport(long parentId) throws CityGMLExportException, SQLException {
        ps.setLong(1, parentId);

        try (ResultSet rs = ps.executeQuery()) {
            List<Occupants> result = new ArrayList<>();

            while (rs.next()) {
                long objectId = rs.getLong("id");

                Occupants occupants = helper.createObject(objectId, objectClassId, Occupants.class);
                if (occupants == null) {
                    helper.logOrThrowErrorMessage("Failed to instantiate " + helper.getObjectSignature(objectClassId, objectId) + " as occupants object.");
                    continue;
                }

                int numberOfOccupants = rs.getInt("numberofoccupants");
                if (!rs.wasNull())
                    occupants.setNumberOfOccupants(numberOfOccupants);

                long occupancyrateId = rs.getLong("occupancyrate_id");
                if (!rs.wasNull()) {
                    DailyPatternSchedule schedule = dailyPatternScheduleExporter.doExport(occupancyrateId);
                    if (schedule != null)
                        occupants.setOccupancyRate(new AbstractScheduleProperty(schedule));
                }

                double totalValue = rs.getDouble("totalvalue");
                if (!rs.wasNull()) {
                    HeatExchangeType heatExchangeType = new HeatExchangeType();

                    Measure measure = new Measure(totalValue);
                    measure.setUom(rs.getString("totalvalue_uom"));
                    heatExchangeType.setTotalValue(measure);

                    double convectiveFraction = rs.getDouble("convectivefraction");
                    if (!rs.wasNull()) {
                        Scale scale = new Scale(convectiveFraction);
                        scale.setUom(rs.getString("convectivefraction_uom"));
                        heatExchangeType.setConvectiveFraction(scale);
                    }

                    double latentFraction = rs.getDouble("latentfraction");
                    if (!rs.wasNull()) {
                        Scale scale = new Scale(latentFraction);
                        scale.setUom(rs.getString("latentfraction_uom"));
                        heatExchangeType.setLatentFraction(scale);
                    }

                    double radiantFraction = rs.getDouble("radiantfraction");
                    if (!rs.wasNull()) {
                        Scale scale = new Scale(radiantFraction);
                        scale.setUom(rs.getString("radiantfraction_uom"));
                        heatExchangeType.setRadiantFraction(scale);
                    }

                    occupants.setHeatDissipation(new HeatExchangeTypeProperty(heatExchangeType));
                }

                result.add(occupants);
            }

            return result;
        }
    }

    @Override
    public void close() throws CityGMLExportException, SQLException {
        ps.close();
    }
}
