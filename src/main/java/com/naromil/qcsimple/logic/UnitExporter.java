package com.naromil.qcsimple.logic;

import com.naromil.qcsimple.data.BlockPalette;
import com.naromil.qcsimple.geom.Point2D;
import com.naromil.qcsimple.geom.Point3D;
import net.querz.nbt.tag.CompoundTag;

import java.util.HashMap;
import java.util.Map;

/**
 * Walks the layers and lets the components write their blocks, in the order the components were
 * originally applied: the framework first (corners, extensions, outline transitions and shell
 * voxels), then rows, columns, walls and floors voxel by voxel, then the inner column, the inner
 * wall, the gate and the outer wall once per unit.
 */
public final class UnitExporter {

    private UnitExporter() {
    }

    public static Map<Point3D, CompoundTag> export(Map<Integer, Map<Point2D, QCUnit>> layers, BlockPalette palette) {
        Map<Point3D, CompoundTag> blockMap = new HashMap<>();

        // Guard clause: Prevent crashes if exporting an empty canvas
        if (layers.isEmpty()) {
            System.out.println("Warning: Attempted to export empty layers.");
            return blockMap;
        }

        UnitExportContext ctx = new UnitExportContext(layers, blockMap, palette);

        for (Map.Entry<Integer, Map<Point2D, QCUnit>> layerEntry : layers.entrySet()) {
            int dy = layerEntry.getKey();
            Map<Point2D, QCUnit> currentLayerMap = layerEntry.getValue();

            for (Map.Entry<Point2D, QCUnit> pointEntry : currentLayerMap.entrySet()) {
                int dx = pointEntry.getKey().x();
                int dz = pointEntry.getKey().z();

                ctx.beginUnit(dx, dy, dz, currentLayerMap);

                // Process all single blocks
                for (int x = 0; x < 9; x++)
                    for (int y = 0; y < 9; y++)
                        for (int z = 0; z < 9; z++) {
                            int boundaryCount = SpatialUtils.getBoundaryCount(x, y, z);
                            palette.frameworkBlock().exportVoxel(ctx, x, y, z, boundaryCount);
                            palette.rowBlock().exportVoxel(ctx, x, y, z, boundaryCount);
                            palette.columnBlock().exportVoxel(ctx, x, y, z, boundaryCount);
                            palette.wallBlock().exportVoxel(ctx, x, y, z, boundaryCount);
                            palette.floorBlock().exportVoxel(ctx, x, y, z, boundaryCount);
                        }

                // Process the structures
                palette.innerColumn().exportUnit(ctx);
                palette.innerWall().exportUnit(ctx);
                palette.gate().exportUnit(ctx);
                palette.outerWall().exportUnit(ctx);
            }
        }

        return blockMap;
    }
}
