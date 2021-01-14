package org.citydb.ade.energy.exporter;

import org.citydb.ade.energy.schema.ADETable;
import org.citydb.ade.exporter.ADEExporter;
import org.citydb.ade.exporter.CityGMLExportHelper;
import org.citydb.citygml.exporter.CityGMLExportException;
import org.citygml4j.ade.energy.model.core.ElevationReferenceValue;
import org.citygml4j.ade.energy.model.core.HeightAboveGround;
import org.citygml4j.model.gml.measures.Length;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class HeightAboveGroundExporter implements ADEExporter {
    private PreparedStatement ps;

    public HeightAboveGroundExporter(Connection connection, CityGMLExportHelper helper, ExportManager manager) throws CityGMLExportException, SQLException {
        ps = connection.prepareStatement("select heightreference, value, value_uom from " +
                helper.getTableNameWithSchema(manager.getSchemaMapper().getTableName(ADETable.HEIGHTABOVEGROUND)) + " " +
                "where building_heightabovegroun_id = ?");
    }

    public Collection<HeightAboveGround> doExport(long parentId) throws CityGMLExportException, SQLException {
        ps.setLong(1, parentId);

        try (ResultSet rs = ps.executeQuery()) {
            List<HeightAboveGround> heightAboveGrounds = new ArrayList<>();

            while (rs.next()) {
                ElevationReferenceValue heightReference = ElevationReferenceValue.fromValue(rs.getString(1));
                if (heightReference == null)
                    continue;

                double value = rs.getDouble(2);
                if (rs.wasNull())
                    continue;

                HeightAboveGround heightAboveGround = new HeightAboveGround();
                heightAboveGround.setHeightReference(heightReference);

                Length length = new Length(value);
                length.setUom(rs.getString(3));
                heightAboveGround.setValue(length);

                heightAboveGrounds.add(heightAboveGround);
            }

            return heightAboveGrounds;
        }
    }

    @Override
    public void close() throws CityGMLExportException, SQLException {
        ps.close();
    }
}
