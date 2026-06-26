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

public class DataConverter {
    // Defaulting to empty strings or basic Minecraft blocks
    protected static String frameworkId = "";
    protected static String columnId = "";
    protected static String rowId = "";
    protected static String floorId = "";
    protected static String wallId = "";

    // NBT File Paths (for the UI to remember)
    protected static String innerWallPath = "";
    protected static String outerWallPath = "";
    protected static String innerColumnPath = "";
    protected static String roofPath = "";

    // Parsed NBT Data (for your actual structure compiler)
    protected static CompoundTag innerWallTag = null;
    protected static CompoundTag outerWallTag = null;
    protected static CompoundTag innerColumnTag = null;
    protected static CompoundTag roofTag = null;

    public static boolean isConfigured() {
        return innerWallPath != null && !innerWallPath.isEmpty() &&
                outerWallPath != null && !outerWallPath.isEmpty() &&
                innerColumnPath != null && !innerColumnPath.isEmpty() &&
                roofPath != null && !roofPath.isEmpty();
    }

    // Apply default config conveniently for better testing
    public static void applyDefaultConfig() {
        // 1. Default String IDs: Balanced between Deepslate and Spruce
        frameworkId = "minecraft:polished_deepslate"; // Sharp edges for framing
        columnId = "minecraft:spruce_log";         // Vertical pillars
        rowId = "minecraft:chiseled_deepslate";   // Horizontal structural rows
        floorId = "minecraft:spruce_planks";      // Clean, warm flooring
        wallId = "minecraft:deepslate_tiles"; // Solid backdrop walls

        // 2. Clear default text indicators for the UI
        innerWallPath = "[Generated Default: Glass]";
        outerWallPath = "[Generated Default: Deepslate Bricks]";
        innerColumnPath = "[Generated Default: Stripped Spruce Log]";
        roofPath = "[Not Configured]";

        // 3. Default .nbt structure tags
        innerWallTag = NBTHandler.generateSimpleStructureTag("minecraft:glass", new byte[][][]{
                {{0}, {0}, {0}, {0}, {0}, {0}, {0}},
                {{0}, {0}, {0}, {0}, {0}, {0}, {0}},
                {{-1}, {-1}, {-1}, {-1}, {0}, {0}, {0}},
                {{-1}, {-1}, {-1}, {-1}, {0}, {0}, {0}},
                {{-1}, {-1}, {-1}, {-1}, {0}, {0}, {0}},
                {{0}, {0}, {0}, {0}, {0}, {0}, {0}},
                {{0}, {0}, {0}, {0}, {0}, {0}, {0}}
        });

        outerWallTag = NBTHandler.generateSimpleStructureTag("minecraft:deepslate_bricks", new byte[][][]{
                {{-1, 0}, {-1, 0}, {-1, 0}, {-1, 0}, {-1, 0}, {-1, 0}, {-1, 0}},
                {{0, 0}, {0, 0}, {0, 0}, {0, 0}, {0, 0}, {-1, 0}, {-1, 0}},
                {{-1, -1}, {-1, -1}, {-1, -1}, {-1, -1}, {0, 0}, {-1, 0}, {-1, 0}},
                {{-1, -1}, {-1, -1}, {-1, -1}, {-1, -1}, {0, 0}, {-1, 0}, {-1, 0}},
                {{-1, -1}, {-1, -1}, {-1, -1}, {-1, -1}, {0, 0}, {-1, 0}, {-1, 0}},
                {{0, 0}, {0, 0}, {0, 0}, {0, 0}, {0, 0}, {-1, 0}, {-1, 0}},
                {{-1, 0}, {-1, 0}, {-1, 0}, {-1, 0}, {-1, 0}, {-1, 0}, {-1, 0}},
        });

        innerColumnTag = NBTHandler.generateSimpleStructureTag("minecraft:stripped_spruce_log", new byte[][][]{
                {{-1, 0, -1}, {-1, -1, -1}, {-1, -1, -1}, {-1, -1, -1}, {-1, -1, -1}, {-1, -1, -1}, {-1, 0, -1}},
                {{0, 0, 0}, {-1, 0, -1}, {-1, 0, -1}, {-1, 0, -1}, {-1, 0, -1}, {-1, 0, -1}, {0, 0, 0}},
                {{-1, 0, -1}, {-1, -1, -1}, {-1, -1, -1}, {-1, -1, -1}, {-1, -1, -1}, {-1, -1, -1}, {-1, 0, -1}},
        });
    }

    // 1. The persistent cache field
    private static int[][][] boundaryCache = null;

    // 2. The public lookup method that auto-computes when needed
    public static int getBoundaryCount(int x, int y, int z) {
        // Auto-compute on first use (Lazy Initialization)
        if (boundaryCache == null) {
            computeBoundaryCache();
        }

        // Return the cached value instantly
        return boundaryCache[x][y][z];
    }

    // 3. Private helper to build the data structure
    private static void computeBoundaryCache() {
        System.out.println("Initializing boundary count cache..."); // Optional log
        boundaryCache = new int[9][9][9];

        for (int x = 0; x < 9; x++) {
            for (int y = 0; y < 9; y++) {
                for (int z = 0; z < 9; z++) {
                    int count = 0;
                    if (x == 0 || x == 8) count++;
                    if (y == 0 || y == 8) count++;
                    if (z == 0 || z == 8) count++;

                    boundaryCache[x][y][z] = count;
                }
            }
        }
    }

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

                            if (getBoundaryCount(x, y, z) == 3) {
                                blockMap.put(pos, NBTHandler.convertToBlockTag(frameworkId)); // Framework

                                // 1.1. Framework extension
                                for (int i = -2; i <= 2; ++i) {
                                    Point3D point = new Point3D(absoluteX + i, absoluteY, absoluteZ);
                                    if (!blockMap.containsKey(point))
                                        blockMap.put(point, NBTHandler.convertToBlockTag(frameworkId));
                                }
                                for (int j = -2; j <= 2; ++j) {
                                    Point3D point = new Point3D(absoluteX, absoluteY + j, absoluteZ);
                                    if (!blockMap.containsKey(point))
                                        blockMap.put(point, NBTHandler.convertToBlockTag(frameworkId));
                                }
                                for (int k = -2; k <= 2; ++k) {
                                    Point3D point = new Point3D(absoluteX, absoluteY, absoluteZ + k);
                                    if (!blockMap.containsKey(point))
                                        blockMap.put(point, NBTHandler.convertToBlockTag(frameworkId));
                                }
                                // 1.2. Outline transitions
                                int xx = x == 0 ? -1 : 9;
                                int yy = y == 0 ? -1 : 9;
                                int zz = z == 0 ? -1 : 9;
                                Point3D newPosX = new Point3D(absoluteX, dy * 8 + yy, dz * 8 + zz);
                                Point3D newPosY = new Point3D(dx * 8 + xx, absoluteY, dz * 8 + zz);
                                Point3D newPosZ = new Point3D(dx * 8 + xx, dy * 8 + yy, absoluteZ);

                                if (isValidPlacement(layers, x, yy, zz, dx, dy, dz) && !blockMap.containsKey(newPosX)) {
                                    blockMap.put(newPosX, NBTHandler.convertToBlockTag(frameworkId));
                                }
                                if (isValidPlacement(layers, xx, y, zz, dx, dy, dz) && !blockMap.containsKey(newPosY)) {
                                    blockMap.put(newPosY, NBTHandler.convertToBlockTag(frameworkId));
                                }
                                if (isValidPlacement(layers, xx, yy, z, dx, dy, dz) && !blockMap.containsKey(newPosZ)) {
                                    blockMap.put(newPosZ, NBTHandler.convertToBlockTag(frameworkId));
                                }
                            } else if (getBoundaryCount(x, y, z) == 2) {
                                if (!blockMap.containsKey(pos))
                                    blockMap.put(pos, NBTHandler.convertToBlockTag(frameworkId)); // Framework

                                // 1.3. Rows and columns
                                if (x == 0 || x == 8) {
                                    for (int bs = (y == 8 || y == 0) ? 1 : 2; bs > 0; bs--) {
                                        Point3D newPos = new Point3D(absoluteX + (x == 0 ? -bs : bs), absoluteY, absoluteZ);
                                        if (isValidPlacement(layers, x + (x == 0 ? -bs : bs), y, z, dx, dy, dz))
                                            blockMap.put(newPos, NBTHandler.convertToBlockTag((y == 0 || y == 8) ? rowId : columnId));
                                    }
                                }
                                if (y == 0 || y == 8) {
                                    Point3D newPos = new Point3D(absoluteX, absoluteY + (y == 0 ? -1 : 1), absoluteZ);
                                    if (isValidPlacement(layers, x, y + (y == 0 ? -1 : 1), z, dx, dy, dz))
                                        blockMap.put(newPos, NBTHandler.convertToBlockTag(wallId));
                                }
                                if (z == 0 || z == 8) {
                                    for (int bs = (y == 8 || y == 0) ? 1 : 2; bs > 0; bs--) {
                                        Point3D newPos = new Point3D(absoluteX, absoluteY, absoluteZ + (z == 0 ? -bs : bs));
                                        if (isValidPlacement(layers, x, y, z + (z == 0 ? -bs : bs), dx, dy, dz))
                                            blockMap.put(newPos, NBTHandler.convertToBlockTag((y == 0 || y == 8) ? rowId : columnId));
                                    }
                                }

                                // 1.4. Building corner columns
                                if(y != 0 && y != 8) {
                                    int xbs = (x == 0) ? -1 : 1;
                                    int zbs = (z == 0) ? -1 : 1;
                                    Point3D newPos = new Point3D(absoluteX + xbs, absoluteY, absoluteZ + zbs);

                                    if (!layersContains(layers, dx + xbs, dy, dz + zbs)
                                        && !layersContains(layers, dx, dy, dz + zbs)
                                        && !layersContains(layers, dx + xbs, dy, dz)) {

                                        blockMap.put(newPos, NBTHandler.convertToBlockTag(columnId));
                                    }
                                }
                            }

                            // 1.5. Floor or Ceiling
                            if ((y == 0 || y == 8) && x > 0 && x < 8 && z > 0 && z < 8) {
                                blockMap.put(pos, NBTHandler.convertToBlockTag(floorId));
                            }
                        }

                // 2. Process roofs
                if (layersContains(layers, dx, dy + 1, dz) && roofTag != null)
                    NBTHandler.putStructure(blockMap, dx * 8, dy * 8 + 9, dz * 8, roofTag);

                // We treat the current unit (dx, dz) as the North-West corner.
                QCUnit uNW = currentUnit;
                QCUnit uNE = currentLayerMap.get(new Point2D(dx + 1, dz));
                QCUnit uSW = currentLayerMap.get(new Point2D(dx, dz + 1));
                QCUnit uSE = currentLayerMap.get(new Point2D(dx + 1, dz + 1));

                // 3. Verify all 4 units exist to form a complete square for the inner column
                if (innerColumnTag != null && uNE != null && uSW != null && uSE != null) {

                    // 3.1. Check the internal cross for any conflicting walls
                    // (Adjust the method names if your QCUnit getters are named differently)
                    boolean hasConflictingWalls =
                            uNW.hasWallE() || uNW.hasWallS() ||
                                    uNE.hasWallW() || uNE.hasWallS() ||
                                    uSW.hasWallE() || uSW.hasWallN() ||
                                    uSE.hasWallW() || uSE.hasWallN();

                    // 3.2. Place the column exactly at the intersection
                    if (!hasConflictingWalls || innerWallTag == null) {
                        // The intersection point converges at x=8, z=8 of the NW unit
                        int intersectX = dx * 8 + 8;
                        int intersectY = dy * 8;
                        int intersectZ = dz * 8 + 8;

                        NBTHandler.putStructure(blockMap, intersectX - 1, intersectY + 1, intersectZ - 1, innerColumnTag);
                    }
                }

                // 4.1. Verify it can form an inner wall with the south unit
                if (innerWallTag != null && uSW != null && uSW.hasWallN() && uNW.hasWallS()) {
                    int intersectX = dx * 8;
                    int intersectY = dy * 8;
                    int intersectZ = dz * 8 + 8;

                    NBTHandler.putStructure(blockMap, intersectX + 1, intersectY + 1, intersectZ, innerWallTag);
                }

                // 4.2. Verify it can form an inner wall with the east unit
                if (innerWallTag != null && uNE != null && uNE.hasWallW() && uNW.hasWallE()) {
                    int intersectX = dx * 8 + 8;
                    int intersectY = dy * 8;
                    int intersectZ = dz * 8;

                    NBTHandler.putStructure(blockMap, intersectX, intersectY + 1, intersectZ + 1, innerWallTag, "90");
                }

                // 5. Process outer walls
                if (outerWallTag != null) {
                    // 5.1. Dynamically read the Z-depth of the loaded NBT structure
                    ListTag<IntTag> sizeTag = outerWallTag.getListTag("size").asIntTagList();
                    int zDepth = sizeTag.get(2).asInt();

                    // 5.2. If the wall is 2 blocks deep, the extra layer goes OUTSIDE.
                    // We shift the placement anchor outward by (depth - 1).
                    // A 1-deep wall has a shift of 0, keeping it exactly on the boundary.
                    int outShift = Math.max(0, zDepth - 1);

                    int absX = dx * 8;
                    int absY = dy * 8;
                    int absZ = dz * 8;

                    // North face (Exposed to -Z)
                    if (!layersContains(layers, dx, dy, dz - 1)) {
                        // Anchor is at X+1 to avoid the column. Z is pushed negative (outward) if thickness > 1.
                        NBTHandler.putStructure(blockMap, absX + 1, absY + 1, absZ - outShift, outerWallTag, "0");
                    }

                    // East face (Exposed to +X)
                    if (!layersContains(layers, dx + 1, dy, dz)) {
                        // Anchor is placed at the +X boundary (8). X is pushed positive (outward) if thickness > 1.
                        NBTHandler.putStructure(blockMap, absX + 8 + outShift, absY + 1, absZ + 1, outerWallTag, "90");
                    }

                    // South face (Exposed to +Z)
                    if (!layersContains(layers, dx, dy, dz + 1)) {
                        // Anchor is placed at the +Z boundary (8). Z is pushed positive (outward) if thickness > 1.
                        NBTHandler.putStructure(blockMap, absX + 7, absY + 1, absZ + 8 + outShift, outerWallTag, "180");
                    }

                    // West face (Exposed to -X)
                    if (!layersContains(layers, dx - 1, dy, dz)) {
                        // Anchor is placed at the -X boundary (0). X is pushed negative (outward) if thickness > 1.
                        NBTHandler.putStructure(blockMap, absX - outShift, absY + 1, absZ + 7, outerWallTag, "270");
                    }
                }
            }
        }

        return blockMap;
    }

    // Check if a relative position (x, y, z) in unit (dx, dy, dz) has another unit on it
    private static boolean isValidPlacement(Map<Integer, Map<Point2D, QCUnit>> layers, int x, int y, int z, int dx, int dy, int dz) {
        // 1. Shift the block offset coordinates if the internal coordinate spills past the 0-8 boundaries
        if (x < 0) dx--;
        else if (x > 8) dx++;

        if (y < 0) dy--;
        else if (y > 8) dy++;

        if (z < 0) dz--;
        else if (z > 8) dz++;

        // 2. Base check: Does the current root chunk exist?
        boolean res = layersContains(layers, dx, dy, dz);

        // 3. Boundary Neighbor Verification:
        // If an internal coordinate sits exactly on an outer shell (0 or 8),
        // ensure the adjacent chunk in that specific direction also exists.

        // X-Boundaries
        if (x == 0) res |= layersContains(layers, dx - 1, dy, dz);
        if (x == 8) res |= layersContains(layers, dx + 1, dy, dz);

        // Y-Boundaries
        if (y == 0) res |= layersContains(layers, dx, dy - 1, dz);
        if (y == 8) res |= layersContains(layers, dx, dy + 1, dz);

        // Z-Boundaries
        if (z == 0) res |= layersContains(layers, dx, dy, dz - 1);
        if (z == 8) res |= layersContains(layers, dx, dy, dz + 1);

        return !res;
    }

    // Check if the map of QCUnit contains a Unit at a position
    private static boolean layersContains(Map<Integer, Map<Point2D, QCUnit>> layers, int dx, int dy, int dz) {
        if (!layers.containsKey(dy)) return false;
        return layers.get(dy).containsKey(new Point2D(dx, dz));
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
                        if (getBoundaryCount(i, j, k) >= 2 && blockMap.get(new Point3D(pos.x() + i, pos.y() + j, pos.z() + k)) != frameworkCandidate) {
                            flag = false;
                            break;
                        }
                    }

            if (flag) {
                ox = pos.x() % 8;
                oy = pos.y() % 8;
                oz = pos.z() % 8;
                frameworkId = frameworkCandidate.getString("Name");
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
            if (!blockTag.getString("Name").equals(frameworkId)) continue;
            if ((pos.x() - ox) % 8 != 0 || (pos.y() - oy) % 8 != 0 || (pos.z() - oz) % 8 != 0) continue;
            boolean flag = true;
            for (int i = 0; i < 9; i++)
                for (int j = 0; j < 9; j += 8)
                    for (int k = 0; k < 9; k++) {
                        CompoundTag targetBlockTag = blockMap.get(new Point3D(pos.x() + i, pos.y() + j, pos.z() + k));
                        if (getBoundaryCount(i, j, k) >= 2 && (targetBlockTag == null || !targetBlockTag.getString("Name").equals(frameworkId))) {
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
            columnId = blockMap.getOrDefault(new Point3D(baseX, baseY - 1, baseZ + 1),
                    NBTHandler.convertToBlockTag(columnId)).getString("Name");
            rowId = blockMap.getOrDefault(new Point3D(baseX - 1, baseY, baseZ + 1),
                    NBTHandler.convertToBlockTag(rowId)).getString("Name");
            floorId = blockMap.getOrDefault(new Point3D(baseX - 1, baseY - 8, baseZ - 1),
                    NBTHandler.convertToBlockTag(floorId)).getString("Name");
            wallId = blockMap.getOrDefault(new Point3D(baseX - 1, baseY + 1, baseZ),
                    NBTHandler.convertToBlockTag(wallId)).getString("Name");

            // Extract .nbt structure configuration for roof and outer wall
            roofTag = NBTHandler.extractStructureTagFromBlockMap(blockMap, baseX - 7, baseY + 1, baseZ - 7, baseX - 1, baseY + 7, baseZ - 1, "0");
            roofPath = roofTag != null ? "[Extracted from Opened File]" : "[Not Configured]";

            outerWallTag = NBTHandler.extractStructureTagFromBlockMap(blockMap, baseX - 7, baseY - 7, baseZ, baseX - 1, baseY - 1, baseZ + 1, "180");
            outerWallPath = outerWallTag != null ? "[Extracted from Opened File]" : "[Not Configured]";
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
                        innerWallTag = NBTHandler.extractStructureTagFromBlockMap(blockMap,
                                absX + 8, absY + 1, absZ + 1,
                                absX + 8, absY + 7, absZ + 7,
                                "90");
                        innerWallPath = innerWallTag != null ? "[Extracted from Opened File]" : "[Not Configured]";
                        extractedInnerWall = innerWallTag != null;
                    }
                }

                QCUnit southUnit = currentLayer.get(new Point2D(dx, dz + 1));
                if (southUnit != null && hasAnyBlock(blockMap,
                        absX + 2, absY + 1, absZ + 8,
                        absX + 6, absY + 7, absZ + 8)) {
                    unit.setWallS(true);
                    southUnit.setWallN(true);

                    if (!extractedInnerWall) {
                        innerWallTag = NBTHandler.extractStructureTagFromBlockMap(blockMap,
                                absX + 1, absY + 1, absZ + 8,
                                absX + 7, absY + 7, absZ + 8,
                                "0");
                        innerWallPath = innerWallTag != null ? "[Extracted from Opened File]" : "[Not Configured]";
                        extractedInnerWall = innerWallTag != null;
                    }
                }

                QCUnit southEastUnit = currentLayer.get(new Point2D(dx + 1, dz + 1));
                if (!extractedInnerColumn && eastUnit != null && southUnit != null && southEastUnit != null) {
                    boolean hasInnerColumn = hasAnyBlock(blockMap,
                            absX + 7, absY + 1, absZ + 7,
                            absX + 9, absY + 7, absZ + 9, frameworkId);

                    if (hasInnerColumn) {
                        innerColumnTag = NBTHandler.extractStructureTagFromBlockMap(blockMap,
                                absX + 7, absY + 1, absZ + 7,
                                absX + 9, absY + 7, absZ + 9,
                                "0");
                        innerColumnPath = innerColumnTag != null ? "[Extracted from Opened File]" : "[Not Configured]";
                        extractedInnerColumn = innerColumnTag != null;
                    }
                }
            }
        }

        if (!extractedInnerWall) {
            innerWallTag = null;
            innerWallPath = "[Not Configured]";
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
                                absX + 9, absY + 7, absZ + 9, frameworkId);

                        if (hasInnerColumn) {
                            innerColumnTag = NBTHandler.extractStructureTagFromBlockMap(blockMap,
                                    absX + 7, absY + 1, absZ + 7,
                                    absX + 9, absY + 7, absZ + 9,
                                    "0");
                            innerColumnPath = innerColumnTag != null ? "[Extracted from Opened File]" : "[Not Configured]";
                            extractedInnerColumn = innerColumnTag != null;
                        }
                    }
                }
            }
        }

        if (!extractedInnerColumn) {
            innerColumnTag = null;
            innerColumnPath = "[Not Configured]";
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
        boolean value = hasAnyBlock(blockMap, x, y, z, X, Y, Z, "");
        System.out.println("hasAnyBlock (" + (X - x + 1) + ", " + (Y - y + 1) + ", " + (Z - z + 1) + "): " + value);
        return value;
    }
}
