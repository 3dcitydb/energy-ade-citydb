package org.citydb.ade.energy.exporter;

import org.citydb.ade.exporter.ADEExporter;
import org.citydb.ade.exporter.CityGMLExportHelper;
import org.citydb.citygml.exporter.CityGMLExportException;
import org.citygml4j.ade.energy.model.core.AbstractThermalZone;
import org.citygml4j.ade.energy.model.core.VolumeType;
import org.citygml4j.ade.energy.model.core.VolumeTypeValue;
import org.citygml4j.model.citygml.building.AbstractBuilding;
import org.citygml4j.model.gml.measures.Volume;
import org.citydb.ade.energy.schema.ADETable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

public class VolumeTypeExporter implements ADEExporter {
    private PreparedStatement psBuilding;
    private PreparedStatement psThermalZone;

    public VolumeTypeExporter(Connection connection, CityGMLExportHelper helper, ExportManager manager) throws CityGMLExportException, SQLException {
        String select = "select type, value, value_uom from " +
                helper.getTableNameWithSchema(manager.getSchemaMapper().getTableName(ADETable.VOLUMETYPE)) +
                " where ";

        psBuilding = connection.prepareStatement(select + "building_volume_id = ?");
        psThermalZone = connection.prepareStatement(select + "thermalzone_volume_id = ?");
    }

    public Collection<VolumeType> doExport(AbstractBuilding parent, long parentId) throws CityGMLExportException, SQLException {
        return doExport(parentId, psBuilding);
    }

    public Collection<VolumeType> doExport(AbstractThermalZone parent, long parentId) throws CityGMLExportException, SQLException {
        return doExport(parentId, psThermalZone);
    }

    private Collection<VolumeType> doExport(long parentId, PreparedStatement ps) throws CityGMLExportException, SQLException {
        ps.setLong(1, parentId);

        try (ResultSet rs = ps.executeQuery()) {
            Collection<VolumeType> result = new ArrayList<>();

            while (rs.next()) {
                VolumeTypeValue type = VolumeTypeValue.fromValue(rs.getString(1));
                if (type == null)
                    continue;

                double value = rs.getDouble(2);
                if (rs.wasNull())
                    continue;

                VolumeType volumeType = new VolumeType();
                volumeType.setType(type);

                Volume volume = new Volume(value);
                volume.setUom(rs.getString(3));
                volumeType.setValue(volume);

                result.add(volumeType);
            }

            return result;
        }
    }

    @Override
    public void close() throws CityGMLExportException, SQLException {
        psBuilding.close();
        psThermalZone.close();
    }
}
