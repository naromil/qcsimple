package io.github.naromil.qcsimple.data;

import io.github.naromil.qcsimple.editor.Point2D;
import io.github.naromil.qcsimple.main.MainApp;
import net.querz.nbt.tag.CompoundTag;
import net.querz.nbt.tag.IntTag;
import net.querz.nbt.tag.ListTag;

import java.util.HashMap;
import java.util.Map;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class UnitBlockConverter {

    // Main logic that converts a map of units into a map of blocks
    public static Map<Point3D, CompoundTag> convertLayersToBlockMap(Map<Integer, Map<Point2D, QCUnit>> layers) {
        // Guard clause: Prevent crashes if exporting an empty canvas
        if (layers.isEmpty()) {
            System.out.println("Warning: Attempted to export empty layers.");
            return new HashMap<Point3D, CompoundTag>();
        }

        Map<Point3D, CompoundTag> blockMap = new HashMap<Point3D, CompoundTag>();

        for (Map.Entry<Integer, Map<Point2D, QCUnit>> layerEntry : layers.entrySet()) {
            int dy = layerEntry.getKey();
            Map<Point2D, QCUnit> currentLayerMap = layerEntry.getValue();

            for (Map.Entry<Point2D, QCUnit> pointEntry : currentLayerMap.entrySet()) {
                int dx = pointEntry.getKey().x();
                int dz = pointEntry.getKey().z();
                QCUnit currentUnit = pointEntry.getValue();

                // 1. Process all single blocks
                for (int x = 0; x < 9; x++)
                    for (int y = 0; y < 9; y++)
                        for (int z = 0; z < 9; z++) {
                            int absoluteX = dx * 8 + x;
                            int absoluteY = dy * 8 + y;
                            int absoluteZ = dz * 8 + z;
                            Point3D pos = new Point3D(absoluteX, absoluteY, absoluteZ);

                            if (SpatialUtils.getBoundaryCount(x, y, z) == 3) {
                                blockMap.put(pos, NBTGenerator.convertToBlockTag(BlockConfig.frameworkId)); // Framework

                                // 1.1. Framework extension
                                for (int i = -2; i <= 2; ++i) {
                                    Point3D point = new Point3D(absoluteX + i, absoluteY, absoluteZ);
                                    if (!blockMap.containsKey(point))
                                        blockMap.put(point, NBTGenerator.convertToBlockTag(BlockConfig.frameworkId));
                                }
                                for (int j = -2; j <= 2; ++j) {
                                    Point3D point = new Point3D(absoluteX, absoluteY + j, absoluteZ);
                                    if (!blockMap.containsKey(point))
                                        blockMap.put(point, NBTGenerator.convertToBlockTag(BlockConfig.frameworkId));
                                }
                                for (int k = -2; k <= 2; ++k) {
                                    Point3D point = new Point3D(absoluteX, absoluteY, absoluteZ + k);
                                    if (!blockMap.containsKey(point))
                                        blockMap.put(point, NBTGenerator.convertToBlockTag(BlockConfig.frameworkId));
                                }
                                // 1.2. Outline transitions
                                int xx = x == 0 ? -1 : 9;
                                int yy = y == 0 ? -1 : 9;
                                int zz = z == 0 ? -1 : 9;
                                Point3D newPosX = new Point3D(absoluteX, dy * 8 + yy, dz * 8 + zz);
                                Point3D newPosY = new Point3D(dx * 8 + xx, absoluteY, dz * 8 + zz);
                                Point3D newPosZ = new Point3D(dx * 8 + xx, dy * 8 + yy, absoluteZ);

                                if (SpatialUtils.isValidPlacement(layers, x, yy, zz, dx, dy, dz) && !blockMap.containsKey(newPosX)) {
                                    blockMap.put(newPosX, NBTGenerator.convertToBlockTag(BlockConfig.frameworkId));
                                }
                                if (SpatialUtils.isValidPlacement(layers, xx, y, zz, dx, dy, dz) && !blockMap.containsKey(newPosY)) {
                                    blockMap.put(newPosY, NBTGenerator.convertToBlockTag(BlockConfig.frameworkId));
                                }
                                if (SpatialUtils.isValidPlacement(layers, xx, yy, z, dx, dy, dz) && !blockMap.containsKey(newPosZ)) {
                                    blockMap.put(newPosZ, NBTGenerator.convertToBlockTag(BlockConfig.frameworkId));
                                }
                            } else if (SpatialUtils.getBoundaryCount(x, y, z) == 2) {
                                if (!blockMap.containsKey(pos))
                                    blockMap.put(pos, NBTGenerator.convertToBlockTag(BlockConfig.frameworkId)); // Framework

                                // 1.3. Rows and columns
                                if (x == 0 || x == 8) {
                                    for (int bs = (y == 8 || y == 0) ? 1 : 2; bs > 0; bs--) {
                                        Point3D newPos = new Point3D(absoluteX + (x == 0 ? -bs : bs), absoluteY, absoluteZ);
                                        if (SpatialUtils.isValidPlacement(layers, x + (x == 0 ? -bs : bs), y, z, dx, dy, dz))
                                            blockMap.put(newPos, NBTGenerator.convertToBlockTag((y == 0 || y == 8) ? BlockConfig.rowId : BlockConfig.columnId));
                                    }
                                }
                                if (y == 0 || y == 8) {
                                    Point3D newPos = new Point3D(absoluteX, absoluteY + (y == 0 ? -1 : 1), absoluteZ);
                                    if (SpatialUtils.isValidPlacement(layers, x, y + (y == 0 ? -1 : 1), z, dx, dy, dz))
                                        blockMap.put(newPos, NBTGenerator.convertToBlockTag(BlockConfig.wallId));
                                }
                                if (z == 0 || z == 8) {
                                    for (int bs = (y == 8 || y == 0) ? 1 : 2; bs > 0; bs--) {
                                        Point3D newPos = new Point3D(absoluteX, absoluteY, absoluteZ + (z == 0 ? -bs : bs));
                                        if (SpatialUtils.isValidPlacement(layers, x, y, z + (z == 0 ? -bs : bs), dx, dy, dz))
                                            blockMap.put(newPos, NBTGenerator.convertToBlockTag((y == 0 || y == 8) ? BlockConfig.rowId : BlockConfig.columnId));
                                    }
                                }

                                // 1.4. Building corner columns
                                if(y != 0 && y != 8) {
                                    int xbs = (x == 0) ? -1 : 1;
                                    int zbs = (z == 0) ? -1 : 1;
                                    Point3D newPos = new Point3D(absoluteX + xbs, absoluteY, absoluteZ + zbs);

                                    if (!SpatialUtils.layersContains(layers, dx + xbs, dy, dz + zbs)
                                        && !SpatialUtils.layersContains(layers, dx, dy, dz + zbs)
                                        && !SpatialUtils.layersContains(layers, dx + xbs, dy, dz)) {

                                        blockMap.put(newPos, NBTGenerator.convertToBlockTag(BlockConfig.columnId));
                                    }
                                }
                            }

                            // 1.5. Floor or Ceiling
                            if ((y == 0 || y == 8) && x > 0 && x < 8 && z > 0 && z < 8) {
                                blockMap.put(pos, NBTGenerator.convertToBlockTag(BlockConfig.floorId));
                            }
                        }

                // 2. Process roofs
                if (SpatialUtils.layersContains(layers, dx, dy + 1, dz) && BlockConfig.roofTag != null)
                    BlockNBTConverter.putStructure(blockMap, dx * 8, dy * 8 + 9, dz * 8, BlockConfig.roofTag);

                // We treat the current unit (dx, dz) as the North-West corner.
                QCUnit uNW = currentUnit;
                QCUnit uNE = currentLayerMap.get(new Point2D(dx + 1, dz));
                QCUnit uSW = currentLayerMap.get(new Point2D(dx, dz + 1));
                QCUnit uSE = currentLayerMap.get(new Point2D(dx + 1, dz + 1));

                // 3. Verify all 4 units exist to form a complete square for the inner column
                if (BlockConfig.innerColumnTag != null && uNE != null && uSW != null && uSE != null) {

                    // 3.1. Check the internal cross for any conflicting walls
                    // (Adjust the method names if your QCUnit getters are named differently)
                    boolean hasConflictingWalls =
                            uNW.hasWallE() || uNW.hasWallS() ||
                                    uNE.hasWallW() || uNE.hasWallS() ||
                                    uSW.hasWallE() || uSW.hasWallN() ||
                                    uSE.hasWallW() || uSE.hasWallN();

                    // 3.2. Place the column exactly at the intersection
                    if (!hasConflictingWalls || BlockConfig.innerWallTag == null) {
                        // The intersection point converges at x=8, z=8 of the NW unit
                        int intersectX = dx * 8 + 8;
                        int intersectY = dy * 8;
                        int intersectZ = dz * 8 + 8;

                        BlockNBTConverter.putStructure(blockMap, intersectX - 1, intersectY + 1, intersectZ - 1, BlockConfig.innerColumnTag);
                    }
                }

                // 4.1. Verify it can form an inner wall with the south unit
                if (BlockConfig.innerWallTag != null && uSW != null && uSW.hasWallN() && uNW.hasWallS()) {
                    int intersectX = dx * 8;
                    int intersectY = dy * 8;
                    int intersectZ = dz * 8 + 8;

                    BlockNBTConverter.putStructure(blockMap, intersectX + 1, intersectY + 1, intersectZ, BlockConfig.innerWallTag);
                }

                // 4.2. Verify it can form an inner wall with the east unit
                if (BlockConfig.innerWallTag != null && uNE != null && uNE.hasWallW() && uNW.hasWallE()) {
                    int intersectX = dx * 8 + 8;
                    int intersectY = dy * 8;
                    int intersectZ = dz * 8;

                    BlockNBTConverter.putStructure(blockMap, intersectX, intersectY + 1, intersectZ + 1, BlockConfig.innerWallTag, "90");
                }

                // 5. Process outer walls
                if (BlockConfig.outerWallTag != null) {
                    // 5.1. Dynamically read the Z-depth of the loaded NBT structure
                    ListTag<IntTag> sizeTag = BlockConfig.outerWallTag.getListTag("size").asIntTagList();
                    int zDepth = sizeTag.get(2).asInt();

                    // 5.2. If the wall is 2 blocks deep, the extra layer goes OUTSIDE.
                    // We shift the placement anchor outward by (depth - 1).
                    // A 1-deep wall has a shift of 0, keeping it exactly on the boundary.
                    int outShift = Math.max(0, zDepth - 1);

                    int absX = dx * 8;
                    int absY = dy * 8;
                    int absZ = dz * 8;

                    // North face (Exposed to -Z)
                    if (!SpatialUtils.layersContains(layers, dx, dy, dz - 1)) {
                        // Anchor is at X+1 to avoid the column. Z is pushed negative (outward) if thickness > 1.
                        BlockNBTConverter.putStructure(blockMap, absX + 1, absY + 1, absZ - outShift, BlockConfig.outerWallTag, "0");
                    }

                    // East face (Exposed to +X)
                    if (!SpatialUtils.layersContains(layers, dx + 1, dy, dz)) {
                        // Anchor is placed at the +X boundary (8). X is pushed positive (outward) if thickness > 1.
                        BlockNBTConverter.putStructure(blockMap, absX + 8 + outShift, absY + 1, absZ + 1, BlockConfig.outerWallTag, "90");
                    }

                    // South face (Exposed to +Z)
                    if (!SpatialUtils.layersContains(layers, dx, dy, dz + 1)) {
                        // Anchor is placed at the +Z boundary (8). Z is pushed positive (outward) if thickness > 1.
                        BlockNBTConverter.putStructure(blockMap, absX + 7, absY + 1, absZ + 8 + outShift, BlockConfig.outerWallTag, "180");
                    }

                    // West face (Exposed to -X)
                    if (!SpatialUtils.layersContains(layers, dx - 1, dy, dz)) {
                        // Anchor is placed at the -X boundary (0). X is pushed negative (outward) if thickness > 1.
                        BlockNBTConverter.putStructure(blockMap, absX - outShift, absY + 1, absZ + 7, BlockConfig.outerWallTag, "270");
                    }
                }
            }
        }

        return blockMap;
    }

    // Main logic that converts a map of blocks into a map of units and also configures the block config
    public static Map<Integer, Map<Point2D, QCUnit>> convertBlockMapToLayers(Map<Point3D, CompoundTag> blockMap) {
        Map<Integer, Map<Point2D, QCUnit>> layers = new HashMap<>();

        // 1. Guessing the framework block
        int ox = -1, oy = -1, oz = -1;

        for (Map.Entry<Point3D, CompoundTag> entry : blockMap.entrySet()) {
            Point3D pos = entry.getKey();
            CompoundTag frameworkCandidate = entry.getValue();

            boolean flag = true;
            for (int i = 0; i < 9; i++)
                for (int j = 0; j < 9; j++)
                    for (int k = 0; k < 9; k++) {
                        if (SpatialUtils.getBoundaryCount(i, j, k) >= 2 && blockMap.get(new Point3D(pos.x() + i, pos.y() + j, pos.z() + k)) != frameworkCandidate) {
                            flag = false;
                            break;
                        }
                    }

            if (flag) {
                ox = pos.x() % 8;
                oy = pos.y() % 8;
                oz = pos.z() % 8;
                BlockConfig.frameworkId = frameworkCandidate.getString("Name");
                break;
            }
        }
        if (ox == -1) throw new IllegalStateException("No framework block found");

        // 2. Converting to layers itself
        int maxX = -1, maxY = -1, maxZ = -1; // The southeast corner unit coordinate in the highest layer to extract block config

        int sumDx = 0, sumDz = 0;
        int count = 0;

        for (Map.Entry<Point3D, CompoundTag> entry : blockMap.entrySet()) {
            Point3D pos = entry.getKey();
            CompoundTag blockTag = entry.getValue();

            // Ensure block is the corner of a framework with the lowest (x, y, z)
            if (!blockTag.getString("Name").equals(BlockConfig.frameworkId)) continue;
            if ((pos.x() - ox) % 8 != 0 || (pos.y() - oy) % 8 != 0 || (pos.z() - oz) % 8 != 0) continue;
            boolean flag = true;
            for (int i = 0; i < 9; i++)
                for (int j = 0; j < 9; j += 8)
                    for (int k = 0; k < 9; k++) {
                        CompoundTag targetBlockTag = blockMap.get(new Point3D(pos.x() + i, pos.y() + j, pos.z() + k));
                        if (SpatialUtils.getBoundaryCount(i, j, k) >= 2 && (targetBlockTag == null || !targetBlockTag.getString("Name").equals(BlockConfig.frameworkId))) {
                            flag = false;
                            break;
                        }
                    }
            if (!flag) continue;
//            System.out.println("Qualifying framework corner: " + (pos.x() - ox) + ", " + (pos.y() - oy) + ", " + (pos.z() - oz));

            // Calculate unit coordinates by dividing absolute position by 8
            int dx = Math.floorDiv(pos.x() - ox, 8);
            int dy = Math.floorDiv(pos.y() - oy, 8);
            int dz = Math.floorDiv(pos.z() - oz, 8);

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

        // 3. Extract the rest of single blocks and structures
        if (maxX != -1 && maxY != -1 && maxZ != -1) {
            int baseX = ox + maxX * 8 + 8;
            int baseY = oy + maxY * 8 + 8;
            int baseZ = oz + maxZ * 8 + 8;

            // Extract other single block configuration from the southeast corner unit
            BlockConfig.columnId = blockMap.getOrDefault(new Point3D(baseX, baseY - 1, baseZ + 1),
                    NBTGenerator.convertToBlockTag(BlockConfig.columnId)).getString("Name");
            BlockConfig.rowId = blockMap.getOrDefault(new Point3D(baseX - 1, baseY, baseZ + 1),
                    NBTGenerator.convertToBlockTag(BlockConfig.rowId)).getString("Name");
            BlockConfig.floorId = blockMap.getOrDefault(new Point3D(baseX - 1, baseY - 8, baseZ - 1),
                    NBTGenerator.convertToBlockTag(BlockConfig.floorId)).getString("Name");
            BlockConfig.wallId = blockMap.getOrDefault(new Point3D(baseX - 1, baseY + 1, baseZ),
                    NBTGenerator.convertToBlockTag(BlockConfig.wallId)).getString("Name");

            // Extract .nbt structure configuration for roof and outer wall
            BlockConfig.roofTag = BlockNBTConverter.extractStructureFromBlockMap(blockMap, baseX - 7, baseY + 1, baseZ - 7, baseX - 1, baseY + 7, baseZ - 1, "0");
            BlockConfig.roofPath = BlockConfig.roofTag != null ? "[Extracted from Opened File]" : "[Not Configured]";

            BlockConfig.outerWallTag = BlockNBTConverter.extractStructureFromBlockMap(blockMap, baseX - 7, baseY - 7, baseZ, baseX - 1, baseY - 1, baseZ + 1, "180");
            BlockConfig.outerWallPath = BlockConfig.outerWallTag != null ? "[Extracted from Opened File]" : "[Not Configured]";
        }

        // 4. Detect inner walls and inner columns
        detectInnerStructures(blockMap, layers, ox, oy, oz);

        // 5. Center the layers to the center of the canvas
        for (Map<Point2D, QCUnit> layer : layers.values()) {
            Map<Point2D, QCUnit> newLayer = new HashMap<>();
            for (Map.Entry<Point2D, QCUnit> entry : layer.entrySet()) {
                Point2D unitPos = entry.getKey();
                int centeredPosition = MainApp.getCellSize() / 2;
                newLayer.put(new Point2D(centeredPosition - avgDx + unitPos.x(), centeredPosition - avgDz + unitPos.z()), entry.getValue());
            }
            layer.clear();
            layer.putAll(newLayer);
        }

        return layers;
    }

    private static void detectInnerStructures(Map<Point3D, CompoundTag> blockMap, Map<Integer, Map<Point2D, QCUnit>> layers, int ox, int oy, int oz) {
        boolean extractedInnerWall = false;
        boolean extractedInnerColumn = false;

        for (Map.Entry<Integer, Map<Point2D, QCUnit>> layerEntry : layers.entrySet()) {
            int dy = layerEntry.getKey() - 1;
            Map<Point2D, QCUnit> currentLayer = layerEntry.getValue();

            for (Map.Entry<Point2D, QCUnit> unitEntry : currentLayer.entrySet()) {
                int dx = unitEntry.getKey().x();
                int dz = unitEntry.getKey().z();
                QCUnit unit = unitEntry.getValue();

                int absX = ox + dx * 8;
                int absY = oy + dy * 8;
                int absZ = oz + dz * 8;

                QCUnit eastUnit = currentLayer.get(new Point2D(dx + 1, dz));
                if (eastUnit != null && hasAnyBlock(blockMap,
                        absX + 8, absY + 1, absZ + 2,
                        absX + 8, absY + 7, absZ + 6)) {
                    unit.setWallE(true);
                    eastUnit.setWallW(true);

                    if (!extractedInnerWall) {
                        BlockConfig.innerWallTag = BlockNBTConverter.extractStructureFromBlockMap(blockMap,
                                absX + 8, absY + 1, absZ + 1,
                                absX + 8, absY + 7, absZ + 7,
                                "90");
                        BlockConfig.innerWallPath = BlockConfig.innerWallTag != null ? "[Extracted from Opened File]" : "[Not Configured]";
                        extractedInnerWall = BlockConfig.innerWallTag != null;
                    }
                }

                QCUnit southUnit = currentLayer.get(new Point2D(dx, dz + 1));
                if (southUnit != null && hasAnyBlock(blockMap,
                        absX + 2, absY + 1, absZ + 8,
                        absX + 6, absY + 7, absZ + 8)) {
                    unit.setWallS(true);
                    southUnit.setWallN(true);

                    if (!extractedInnerWall) {
                        BlockConfig.innerWallTag = BlockNBTConverter.extractStructureFromBlockMap(blockMap,
                                absX + 1, absY + 1, absZ + 8,
                                absX + 7, absY + 7, absZ + 8,
                                "0");
                        BlockConfig.innerWallPath = BlockConfig.innerWallTag != null ? "[Extracted from Opened File]" : "[Not Configured]";
                        extractedInnerWall = BlockConfig.innerWallTag != null;
                    }
                }

                QCUnit southEastUnit = currentLayer.get(new Point2D(dx + 1, dz + 1));
                if (!extractedInnerColumn && eastUnit != null && southUnit != null && southEastUnit != null) {
                    boolean hasInnerColumn = hasAnyBlock(blockMap,
                            absX + 7, absY + 1, absZ + 7,
                            absX + 9, absY + 7, absZ + 9, BlockConfig.frameworkId);

                    if (hasInnerColumn) {
                        BlockConfig.innerColumnTag = BlockNBTConverter.extractStructureFromBlockMap(blockMap,
                                absX + 7, absY + 1, absZ + 7,
                                absX + 9, absY + 7, absZ + 9,
                                "0");
                        BlockConfig.innerColumnPath = BlockConfig.innerColumnTag != null ? "[Extracted from Opened File]" : "[Not Configured]";
                        extractedInnerColumn = BlockConfig.innerColumnTag != null;
                    }
                }
            }
        }

        if (!extractedInnerWall) {
            BlockConfig.innerWallTag = null;
            BlockConfig.innerWallPath = "[Not Configured]";
        }

        for (Map.Entry<Integer, Map<Point2D, QCUnit>> layerEntry : layers.entrySet()) {
            int dy = layerEntry.getKey() - 1;
            Map<Point2D, QCUnit> currentLayer = layerEntry.getValue();

            for (Map.Entry<Point2D, QCUnit> unitEntry : currentLayer.entrySet()) {
                int dx = unitEntry.getKey().x();
                int dz = unitEntry.getKey().z();
                QCUnit unit = unitEntry.getValue();

                int absX = ox + dx * 8;
                int absY = oy + dy * 8;
                int absZ = oz + dz * 8;

                QCUnit eastUnit = currentLayer.get(new Point2D(dx + 1, dz));
                QCUnit southUnit = currentLayer.get(new Point2D(dx, dz + 1));
                QCUnit southEastUnit = currentLayer.get(new Point2D(dx + 1, dz + 1));
                if (!extractedInnerColumn && eastUnit != null && southUnit != null && southEastUnit != null) {
                    boolean hasConflictingWalls =
                            unit.hasWallE() || unit.hasWallS() ||
                                    eastUnit.hasWallW() || eastUnit.hasWallS() ||
                                    southUnit.hasWallE() || southUnit.hasWallN() ||
                                    southEastUnit.hasWallW() || southEastUnit.hasWallN();

                    if (!hasConflictingWalls || !extractedInnerWall) {

                        boolean hasInnerColumn = hasAnyBlock(blockMap,
                                absX + 7, absY + 1, absZ + 7,
                                absX + 9, absY + 7, absZ + 9, BlockConfig.frameworkId);

                        if (hasInnerColumn) {
                            BlockConfig.innerColumnTag = BlockNBTConverter.extractStructureFromBlockMap(blockMap,
                                    absX + 7, absY + 1, absZ + 7,
                                    absX + 9, absY + 7, absZ + 9,
                                    "0");
                            BlockConfig.innerColumnPath = BlockConfig.innerColumnTag != null ? "[Extracted from Opened File]" : "[Not Configured]";
                            extractedInnerColumn = BlockConfig.innerColumnTag != null;
                        }
                    }
                }
            }
        }

        if (!extractedInnerColumn) {
            BlockConfig.innerColumnTag = null;
            BlockConfig.innerColumnPath = "[Not Configured]";
        }
    }

    private static boolean hasAnyBlock(Map<Point3D, CompoundTag> blockMap, int x, int y, int z, int X, int Y, int Z, String noBlock) {
        int minX = min(x, X), maxX = max(x, X);
        int minY = min(y, Y), maxY = max(y, Y);
        int minZ = min(z, Z), maxZ = max(z, Z);

        for (int absX = minX; absX <= maxX; absX++) {
            for (int absY = minY; absY <= maxY; absY++) {
                for (int absZ = minZ; absZ <= maxZ; absZ++) {
                    if (blockMap.containsKey(new Point3D(absX, absY, absZ)) && !blockMap.get(new Point3D(absX, absY, absZ)).getString("Name").equals(noBlock)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private static boolean hasAnyBlock(Map<Point3D, CompoundTag> blockMap, int x, int y, int z, int X, int Y, int Z) {
        return hasAnyBlock(blockMap, x, y, z, X, Y, Z, "");
    }
}
