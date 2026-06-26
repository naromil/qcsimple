package io.github.naromil.qcsimple.data;

import io.github.naromil.qcsimple.editor.Point2D;
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
        columnId    = "minecraft:spruce_log";         // Vertical pillars
        rowId       = "minecraft:chiseled_deepslate";   // Horizontal structural rows
        floorId     = "minecraft:spruce_planks";      // Clean, warm flooring
        wallId      = "minecraft:deepslate_tiles"; // Solid backdrop walls

        // 2. Clear default text indicators for the UI
        innerWallPath   = "[Generated Default: Glass]";
        outerWallPath   = "[Generated Default: Deepslate Bricks]";
        innerColumnPath = "[Generated Default: Stripped Spruce Log]";
        roofPath        = "[Not Configured]";

        // 3. Default .nbt structure tags
        innerWallTag   = NBTHandler.convertToStructureTag(7, 7, 1, "minecraft:glass");

        outerWallTag   = NBTHandler.convertToStructureTag(7, 7, 1, "minecraft:deepslate_bricks");
        // Customized carving for outer wall
        ListTag<CompoundTag> blocksList = outerWallTag.getListTag("blocks").asCompoundTagList();
        for (int i = blocksList.size() - 1; i >= 0; i--) {
            CompoundTag blockTag = blocksList.get(i);
            int x = blockTag.getListTag("pos").asIntTagList().get(0).asInt(), y = blockTag.getListTag("pos").asIntTagList().get(1).asInt();

            if (y < 4 && x > 1 && x < 6) {
                blocksList.remove(i);
            }
        }

        innerColumnTag = NBTHandler.convertToStructureTag(3, 7, 3, "minecraft:stripped_spruce_log");
        // Customized carving for inner column
        blocksList = innerColumnTag.getListTag("blocks").asCompoundTagList();
        for (int i = blocksList.size() - 1; i >= 0; i--) {
            CompoundTag blockTag = blocksList.get(i);
            int x = blockTag.getListTag("pos").asIntTagList().get(0).asInt();
            int y = blockTag.getListTag("pos").asIntTagList().get(1).asInt();
            int z = blockTag.getListTag("pos").asIntTagList().get(2).asInt();

            if (y > 0 && y < 6 && (x != 1 || z != 1)) {
                blocksList.remove(i);
            }
        }
    }

    // Main logic that converts a map of units into a map of blocks
    public static Map<Point3D, CompoundTag> convertToBlockMap(Map<Integer, Map<Point2D, QCUnit>> layers) {
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

                // Process all single blocks
                for (int x = 0; x < 9; x++)  for (int y = 0; y < 9; y++) for (int z = 0; z < 9; z++) {
                    int boundaryCount = 0;
                    if (x == 0 || x == 8) boundaryCount++;
                    if (y == 0 || y == 8) boundaryCount++;
                    if (z == 0 || z == 8) boundaryCount++;

                    int absoluteX = dx * 8 + x;
                    int absoluteY = dy * 8 + y;
                    int absoluteZ = dz * 8 + z;
                    Point3D pos = new Point3D(absoluteX, absoluteY, absoluteZ);

                    if (boundaryCount == 3) {
                        blockMap.put(pos, NBTHandler.convertToBlockTag(frameworkId)); // Framework

                        // Framework extension
                        for (int i = -2; i <= 2; ++i)
                            blockMap.put(new Point3D(absoluteX + i, absoluteY, absoluteZ), NBTHandler.convertToBlockTag(frameworkId));
                        for (int j = -2; j <= 2; ++j)
                            blockMap.put(new Point3D(absoluteX, absoluteY + j, absoluteZ), NBTHandler.convertToBlockTag(frameworkId));
                        for (int k = -2; k <= 2; ++k)
                            blockMap.put(new Point3D(absoluteX, absoluteY, absoluteZ + k), NBTHandler.convertToBlockTag(frameworkId));

                        // Outline transitions
                        int xx = x == 0 ? -1 : 9;
                        int yy = y == 0 ? -1 : 9;
                        int zz = z == 0 ? -1 : 9;
                        if (isValidPlacement(layers, x, yy, zz, dx, dy, dz))
                            blockMap.put(new Point3D(absoluteX, dy * 8 + yy, dz * 8 + zz), NBTHandler.convertToBlockTag(frameworkId));
                        if (isValidPlacement(layers, xx, y, zz, dx, dy, dz))
                            blockMap.put(new Point3D(dx * 8 + xx, absoluteY, dz * 8 + zz), NBTHandler.convertToBlockTag(frameworkId));
                        if (isValidPlacement(layers, xx, yy, z, dx, dy, dz))
                            blockMap.put(new Point3D(dx * 8 + xx, dy * 8 + yy, absoluteZ), NBTHandler.convertToBlockTag(frameworkId));
                    }
                    else if (boundaryCount == 2) {
                        blockMap.put(pos, NBTHandler.convertToBlockTag(frameworkId)); // Framework

                        // Rows and columns
                        if (x == 0 || x == 8) {
                            for(int bs = (y == 8 || y == 0) ? 1 : 2; bs > 0; bs--) {
                                Point3D newPos = new Point3D(absoluteX + (x == 0 ? -bs : bs), absoluteY, absoluteZ);
                                if(isValidPlacement(layers, x + (x == 0 ? -bs : bs), y, z, dx, dy, dz))
                                    blockMap.put(newPos, NBTHandler.convertToBlockTag((y == 0 || y == 8) ? rowId : columnId));
                            }
                        }
                        if (y == 0 || y == 8) {
                            Point3D newPos = new Point3D(absoluteX, absoluteY + (y == 0 ? -1 : 1), absoluteZ);
                            if(isValidPlacement(layers, x, y + (y == 0 ? -1 : 1), z, dx, dy, dz))
                                blockMap.put(newPos, NBTHandler.convertToBlockTag(wallId));
                        }
                        if (z == 0 || z == 8) {
                            for(int bs = (y == 8 || y == 0) ? 1 : 2; bs > 0; bs--) {
                                Point3D newPos = new Point3D(absoluteX, absoluteY, absoluteZ + (z == 0 ? -bs : bs));
                                if(isValidPlacement(layers, x, y, z + (z == 0 ? -bs : bs), dx, dy, dz))
                                    blockMap.put(newPos, NBTHandler.convertToBlockTag((y == 0 || y == 8) ? rowId : columnId));
                            }
                        }
                    }

                    // Floor or Ceiling
                    if((y == 0 || y == 8) && x > 0 && x < 8 && z > 0 && z < 8) {
                        blockMap.put(pos, NBTHandler.convertToBlockTag(floorId));
                    }
                }

                // Process roofs
                if(layersContains(layers, dx, dy + 1, dz) && roofTag != null)
                    NBTHandler.putStructure(blockMap, dx * 8, dy * 8 + 9, dz * 8, roofTag);

                // We treat the current unit (dx, dz) as the North-West corner.
                QCUnit uNW = currentUnit;
                QCUnit uNE = currentLayerMap.get(new Point2D(dx + 1, dz));
                QCUnit uSW = currentLayerMap.get(new Point2D(dx, dz + 1));
                QCUnit uSE = currentLayerMap.get(new Point2D(dx + 1, dz + 1));

                // 1. Verify all 4 units exist to form a complete square for the inner column
                if (innerColumnTag != null && uNE != null && uSW != null && uSE != null) {

                    // 2. Check the internal cross for any conflicting walls
                    // (Adjust the method names if your QCUnit getters are named differently)
                    boolean hasConflictingWalls =
                            uNW.hasWallE() || uNW.hasWallS() ||
                                    uNE.hasWallW() || uNE.hasWallS() ||
                                    uSW.hasWallE() || uSW.hasWallN() ||
                                    uSE.hasWallW() || uSE.hasWallN();

                    // 3. Place the column exactly at the intersection
                    if (!hasConflictingWalls) {
                        // The intersection point converges at x=8, z=8 of the NW unit
                        int intersectX = dx * 8 + 8;
                        int intersectY = dy * 8;
                        int intersectZ = dz * 8 + 8;

                        NBTHandler.putStructure(blockMap, intersectX - 1, intersectY + 1, intersectZ - 1, innerColumnTag);
                    }
                }

                // 2. Verify it can form an inner wall with the south unit
                if (innerWallTag != null && uSW != null && uSW.hasWallN() && uNW.hasWallS()) {
                    int intersectX = dx * 8;
                    int intersectY = dy * 8;
                    int intersectZ = dz * 8 + 8;

                    NBTHandler.putStructure(blockMap, intersectX + 1, intersectY + 1, intersectZ, innerColumnTag);
                }

                // 3. Verify it can form an inner wall with the east unit
                if (innerWallTag != null && uNE != null && uNE.hasWallW() && uNW.hasWallE()) {
                    int intersectX = dx * 8 + 8;
                    int intersectY = dy * 8;
                    int intersectZ = dz * 8;

                    NBTHandler.putStructure(blockMap, intersectX, intersectY + 1, intersectZ + 1, innerColumnTag, "270");
                }

                // 4. Process outer walls
                if (outerWallTag != null) {
                    // 1. Dynamically read the Z-depth of the loaded NBT structure
                    ListTag<IntTag> sizeTag = outerWallTag.getListTag("size").asIntTagList();
                    int zDepth = sizeTag.get(2).asInt();

                    // 2. If the wall is 2 blocks deep, the extra layer goes OUTSIDE.
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
        if(!layers.containsKey(dy)) return false;
        return layers.get(dy).containsKey(new Point2D(dx, dz));
    }

    public static Map<Integer, Map<Point2D, QCUnit>> tagToMap(CompoundTag rootCompoundTag) {
        return null;
        // TODO: Add QC-style conversion here
    }
}
