package com.naromil.qcsimple.logic;

import com.naromil.qcsimple.data.BlockPalette;
import com.naromil.qcsimple.data.Gate;
import com.naromil.qcsimple.data.InnerColumn;
import com.naromil.qcsimple.data.InnerStructure;
import com.naromil.qcsimple.data.InnerWall;
import com.naromil.qcsimple.geom.Point2D;
import com.naromil.qcsimple.geom.Point3D;
import net.querz.nbt.tag.CompoundTag;

import java.util.HashMap;
import java.util.Map;

/**
 * Turns an opened structure back into the editor's layer model: it detects the unit grid, asks
 * every component for its own blocks and finally centres the layout on the editor grid.
 */
public final class UnitImporter {

    private UnitImporter() {
    }

    public static Map<Integer, Map<Point2D, QCUnit>> importLayers(Map<Point3D, CompoundTag> blockMap, BlockPalette palette,
                                                                 int centerX, int centerZ) {
        Map<Integer, Map<Point2D, QCUnit>> layers = new HashMap<>();

        // 1. Guessing the framework block and the grid origin
        GridOrigin origin = palette.frameworkBlock().detectGrid(blockMap);

        UnitImportContext ctx = new UnitImportContext(blockMap, layers, palette, origin.x(), origin.y(), origin.z());

        // 2. Converting to layers itself
        scanUnits(ctx);

        // 3. Extract the rest of single blocks and structures
        palette.columnBlock().extract(ctx);
        palette.rowBlock().extract(ctx);
        palette.floorBlock().extract(ctx);
        palette.wallBlock().extract(ctx);
        palette.outerWall().extract(ctx);

        // 4. Detect inner walls, gates and inner columns
        InnerWall innerWall = palette.innerWall();
        Gate gate = palette.gate();
        InnerStructure.scanSides(ctx, (scanCtx, absX, absY, absZ, side, isGate) -> {
            if (isGate) {
                gate.onDetectedSide(scanCtx, absX, absY, absZ, side);
            } else {
                innerWall.onDetectedSide(scanCtx, absX, absY, absZ, side);
            }
        });
        if (!ctx.innerWallExtracted()) {
            innerWall.setExtracted(null);
        }
        if (!ctx.gateExtracted()) {
            gate.setExtracted(null);
        }

        InnerColumn innerColumn = palette.innerColumn();
        innerColumn.extract(ctx);
        if (!ctx.innerColumnExtracted()) {
            innerColumn.setExtracted(null);
        }

        // 5. Center the layers to the center of the canvas
        centerLayers(ctx, centerX, centerZ);

        return layers;
    }

    /** Builds the unit layers from the framework voxels and records the grid statistics. */
    private static void scanUnits(UnitImportContext ctx) {
        Map<Point3D, CompoundTag> blockMap = ctx.blockMap();
        Map<Integer, Map<Point2D, QCUnit>> layers = ctx.layers();
        String frameworkId = ctx.frameworkId();

        int maxX = -1, maxY = -1, maxZ = -1; // The southeast corner unit coordinate in the highest layer

        int sumDx = 0, sumDz = 0;
        int count = 0;

        for (Map.Entry<Point3D, CompoundTag> entry : blockMap.entrySet()) {
            Point3D pos = entry.getKey();
            CompoundTag blockTag = entry.getValue();

            // Ensure block is the corner of a framework with the lowest (x, y, z)
            if (!blockTag.getString("Name").equals(frameworkId)) continue;
            if ((pos.x() - ctx.originX()) % 8 != 0 || (pos.y() - ctx.originY()) % 8 != 0 || (pos.z() - ctx.originZ()) % 8 != 0) continue;

            boolean flag = true;

            for (int i = 0; i < 9; i++)
                for (int j = 0; j < 9; j++)
                    for (int k = 0; k < 9; k++) {

                        CompoundTag targetBlockTag = blockMap.get(new Point3D(pos.x() + i, pos.y() + j, pos.z() + k));

                        if (SpatialUtils.getBoundaryCount(i, j, k) >= 2) {
                            if ((j == 0 || j == 8) && (targetBlockTag == null || !targetBlockTag.getString("Name").equals(frameworkId))) {
                                flag = false;
                                break;

                            } else if (j > 0 && j < 8 && targetBlockTag == null) {
                                flag = false;
                                break;
                            }
                        }
                    }

            if (!flag) continue;

            // Calculate unit coordinates by dividing absolute position by 8
            int dx = Math.floorDiv(pos.x() - ctx.originX(), 8);
            int dy = Math.floorDiv(pos.y() - ctx.originY(), 8);
            int dz = Math.floorDiv(pos.z() - ctx.originZ(), 8);

            sumDx += dx;
            sumDz += dz;
            count++;

            // Track maximum coordinates for block config extraction
            if (dy > maxY || (dy == maxY && (dz > maxZ || (dz == maxZ && dx > maxX)))) {
                maxX = dx;
                maxY = dy;
                maxZ = dz;
            }

            // Initialize layer map if needed
            if (!layers.containsKey(dy + 1)) {
                layers.put(dy + 1, new HashMap<>());
            }

            Map<Point2D, QCUnit> currentLayer = layers.get(dy + 1);
            Point2D unitPos = new Point2D(dx, dz);

            // Create unit if it doesn't exist
            if (!currentLayer.containsKey(unitPos)) {
                currentLayer.put(unitPos, new QCUnit());
            }
        }

        int avgDx = count > 0 ? sumDx / count : 0;
        int avgDz = count > 0 ? sumDz / count : 0;

        ctx.setStats(new UnitGridStats(maxX, maxY, maxZ, avgDx, avgDz, count));
    }

    /** Moves every unit so the imported layout ends up around the middle of the editor canvas. */
    private static void centerLayers(UnitImportContext ctx, int centerX, int centerZ) {
        UnitGridStats stats = ctx.stats();

        for (Map<Point2D, QCUnit> layer : ctx.layers().values()) {
            Map<Point2D, QCUnit> newLayer = new HashMap<>();
            for (Map.Entry<Point2D, QCUnit> entry : layer.entrySet()) {
                Point2D unitPos = entry.getKey();
                newLayer.put(new Point2D(centerX - stats.avgDx() + unitPos.x(), centerZ - stats.avgDz() + unitPos.z()), entry.getValue());
            }
            layer.clear();
            layer.putAll(newLayer);
        }
    }
}
