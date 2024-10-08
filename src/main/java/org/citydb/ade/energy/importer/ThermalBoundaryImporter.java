/*
 * 3D City Database - The Open Source CityGML Database
 * https://www.3dcitydb.org/
 *
 * Copyright 2013 - 2024
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

import org.citydb.ade.energy.schema.ADETable;
import org.citydb.ade.energy.schema.SchemaMapper;
import org.citydb.core.ade.importer.ADEImporter;
import org.citydb.core.ade.importer.CityGMLImportHelper;
import org.citydb.core.ade.importer.ForeignKeys;
import org.citydb.core.database.schema.mapping.AbstractObjectType;
import org.citydb.core.operation.importer.CityGMLImportException;
import org.citygml4j.ade.energy.model.buildingPhysics.*;
import org.citygml4j.ade.energy.model.core.AbstractConstructionProperty;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurfaceProperty;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

public class ThermalBoundaryImporter implements ADEImporter {
    private final CityGMLImportHelper helper;
    private final SchemaMapper schemaMapper;

    private ThermalBoundaryToThermalZoneImporter boundaryToZoneImporter;
    private PreparedStatement ps;
    private int batchCounter;

    public ThermalBoundaryImporter(Connection connection, CityGMLImportHelper helper, ImportManager manager) throws CityGMLImportException, SQLException {
        this.helper = helper;
        schemaMapper = manager.getSchemaMapper();

        ps = connection.prepareStatement("insert into " +
                helper.getTableNameWithSchema(manager.getSchemaMapper().getTableName(ADETable.THERMALBOUNDARY)) + " " +
                "(id, thermalzone_boundedby_id, thermalboundarytype, surfacegeometry_id, azimuth, azimuth_uom, " +
                "inclination, inclination_uom, area, area_uom, construction_id) " +
                "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

        boundaryToZoneImporter = manager.getImporter(ThermalBoundaryToThermalZoneImporter.class);
    }

    public void doImport(ThermalBoundary thermalBoundary, long objectId, AbstractObjectType<?> objectType, ForeignKeys foreignKeys) throws CityGMLImportException, SQLException {
        long parentId = foreignKeys.get("parentId");

        ps.setLong(1, objectId);

        if (parentId != 0)
            ps.setLong(2, parentId);
        else
            ps.setNull(2, Types.NULL);

        if (thermalBoundary.isSetThermalBoundaryType())
            ps.setString(3, thermalBoundary.getThermalBoundaryType().value());
        else
            ps.setNull(3, Types.VARCHAR);

        long surfaceGeometryId = 0;
        if (thermalBoundary.isSetSurfaceGeometry()) {
            MultiSurfaceProperty multiSurfaceProperty = thermalBoundary.getSurfaceGeometry();

            if (multiSurfaceProperty != null) {
                if (multiSurfaceProperty.isSetMultiSurface()) {
                    surfaceGeometryId = helper.importSurfaceGeometry(multiSurfaceProperty.getMultiSurface(), objectId);
                    multiSurfaceProperty.unsetMultiSurface();
                } else {
                    String href = multiSurfaceProperty.getHref();
                    if (href != null && href.length() != 0)
                        helper.propagateSurfaceGeometryXlink(href, objectType.getTable(), objectId, "surfacegeometry_id");
                }
            }
        }

        if (surfaceGeometryId != 0)
            ps.setLong(4, surfaceGeometryId);
        else
            ps.setNull(4, Types.NULL);

        if (thermalBoundary.isSetAzimuth() && thermalBoundary.getAzimuth().isSetValue()) {
            ps.setDouble(5, thermalBoundary.getAzimuth().getValue());
            ps.setString(6, thermalBoundary.getAzimuth().getUom());
        } else {
            ps.setNull(5, Types.DOUBLE);
            ps.setNull(6, Types.VARCHAR);
        }

        if (thermalBoundary.isSetInclination() && thermalBoundary.getInclination().isSetValue()) {
            ps.setDouble(7, thermalBoundary.getInclination().getValue());
            ps.setString(8, thermalBoundary.getInclination().getUom());
        } else {
            ps.setNull(7, Types.DOUBLE);
            ps.setNull(8, Types.VARCHAR);
        }

        if (thermalBoundary.isSetArea() && thermalBoundary.getArea().isSetValue()) {
            ps.setDouble(9, thermalBoundary.getArea().getValue());
            ps.setString(10, thermalBoundary.getArea().getUom());
        } else {
            ps.setNull(9, Types.DOUBLE);
            ps.setNull(10, Types.VARCHAR);
        }

        long constructionId = 0;
        if (thermalBoundary.isSetConstruction()) {
            AbstractConstructionProperty property = thermalBoundary.getConstruction();
            if (property.isSetAbstractConstruction()) {
                constructionId = helper.importObject(property.getAbstractConstruction());
                property.unsetAbstractConstruction();
            } else
                helper.propagateObjectXlink(objectType.getTable(), objectId, property.getHref(), "construction_id");
        }

        if (constructionId != 0)
            ps.setLong(11, constructionId);
        else
            ps.setNull(11, Types.NULL);

        ps.addBatch();
        if (++batchCounter == helper.getDatabaseAdapter().getMaxBatchSize())
            helper.executeBatch(objectType);

        if (thermalBoundary.isSetDelimits()) {
            for (ThermalZoneProperty property : thermalBoundary.getDelimits()) {
                ThermalZone thermalZone = property.getThermalZone();
                if (thermalZone != null) {
                    long thermalZoneId = helper.importObject(thermalZone, ForeignKeys.EMPTY_SET);
                    property.unsetThermalZone();
                    boundaryToZoneImporter.doImport(thermalZoneId, objectId);
                } else {
                    String href = property.getHref();
                    if (href != null && href.length() != 0)
                        helper.propagateObjectXlink(
                                schemaMapper.getTableName(ADETable.THER_BOUN_TO_THER_DELI),
                                objectId, "thermalboundary_delimits_id",
                                href, "thermalzone_boundedby_id");
                }
            }
        }

        if (thermalBoundary.isSetContains()) {
            for (ThermalOpeningProperty property : thermalBoundary.getContains()) {
                ThermalOpening thermalOpening = property.getThermalOpening();
                if (thermalOpening != null) {
                    helper.importObject(thermalOpening, ForeignKeys.create().with("parentId", objectId));
                    property.unsetThermalOpening();
                } else {
                    String href = property.getHref();
                    if (href != null && href.length() != 0)
                        helper.logOrThrowUnsupportedXLinkMessage(thermalBoundary, ThermalOpening.class, href);
                }
            }
        }
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
