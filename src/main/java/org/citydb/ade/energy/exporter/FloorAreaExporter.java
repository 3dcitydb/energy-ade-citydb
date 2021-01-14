package org.citydb.ade.energy.exporter;

import org.citydb.ade.energy.schema.ADETable;
import org.citydb.ade.exporter.ADEExporter;
import org.citydb.ade.exporter.CityGMLExportHelper;
import org.citydb.citygml.exporter.CityGMLExportException;
import org.citygml4j.ade.energy.model.core.FloorArea;
import org.citygml4j.ade.energy.model.core.FloorAreaTypeValue;
import org.citygml4j.model.citygml.building.AbstractBuilding;
import org.citygml4j.model.gml.measures.Area;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

public class FloorAreaExporter implements ADEExporter {
    private PreparedStatement ps;

    public FloorAreaExporter(Connection connection, CityGMLExportHelper helper, ExportManager manager) throws CityGMLExportException, SQLException {
        String select = "select type, value, value_uom from " +
                helper.getTableNameWithSchema(manager.getSchemaMapper().getTableName(ADETable.FLOORAREA)) +
                " where ";

        ps = connection.prepareStatement(select + "building_floorarea_id = ?");
    }

    public Collection<FloorArea> doExport(AbstractBuilding parent, long parentId) throws CityGMLExportException, SQLException {
        ps.setLong(1, parentId);

        try (ResultSet rs = ps.executeQuery()) {
            Collection<FloorArea> floorAreas = new ArrayList<>();

            while (rs.next()) {
                FloorAreaTypeValue type = FloorAreaTypeValue.fromValue(rs.getString(1));
                if (type == null)
                    continue;

                double value = rs.getDouble(2);
                if (rs.wasNull())
                    continue;

                FloorArea floorArea = new FloorArea();
                floorArea.setType(type);

                Area area = new Area(value);
                area.setUom(rs.getString(3));
                floorArea.setValue(area);

                floorAreas.add(floorArea);
            }

            return floorAreas;
        }
    }

    @Override
    public void close() throws CityGMLExportException, SQLException {
        ps.close();
    }
}
