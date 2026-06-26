package io.github.naromil.qcsimple.data;

import net.querz.nbt.tag.CompoundTag;
import net.querz.nbt.tag.IntTag;
import net.querz.nbt.tag.ListTag;

import java.util.HashMap;
import java.util.Map;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class NBTHandler {

    // Use blockId to generate an CompoundTag item inside palette
    static CompoundTag convertToBlockTag(String blockId) {
        // 1. Handle null, empty, or pure whitespace strings safely
        if (blockId == null || blockId.isBlank()) {
            blockId = "minecraft:stone";
        }
        // 2. Automatically append the default namespace if it's missing
        else if (!blockId.contains(":")) {
            blockId = "minecraft:" + blockId;
        }

        // 3. Create the CompoundTag, populate it, and RETURN it
        CompoundTag tag = new CompoundTag();
        tag.putString("Name", blockId);

        return tag; // Crucial: Fixes the missing return statement error
    }

    // Use blockId and sizes to create a uniform cuboid structure CompoundTag
    public static CompoundTag generateSimpleStructureTag(String[] blocks, byte[][][] map) {
        if(map == null || map.length == 0 || map[0].length == 0 || map[0][0].length == 0) {
            return null;
        }
        int x = map.length;
        int y = map[0].length;
        int z = map[0][0].length;

        Map<Point3D, CompoundTag> blockMap = new HashMap<>();
        for (int i = 0; i < x; i++) for(int j = 0; j < y; j++) for(int k = 0; k < z; k++) {
            int index = map[i][j][k];
            if(index < 0 || index >= blocks.length) continue;

            CompoundTag blockId = convertToBlockTag(blocks[index]);
            blockMap.put(new Point3D(i, j, k), blockId);
        }
        return convertMapToTag(blockMap);
    }

    public static CompoundTag generateSimpleStructureTag(String blockId, byte[][][] map) {
        return generateSimpleStructureTag(new String[]{blockId}, map);
    }

    public static CompoundTag generateSimpleStructureTag(int x, int y, int z, String blockId) {
        byte[][][] blocks = new byte[x][y][z];
        for(int i=0; i<x; i++) for(int j=0; j<y; j++) for(int k=0; k<z; k++) {
            blocks[i][j][k] = 0;
        }
        return generateSimpleStructureTag((String[]) new String[]{blockId}, blocks);
    }

    // Put a structure tag in root compound form into a blockMap
    public static void putStructure(Map<Point3D, CompoundTag> blockMap, int x, int y, int z, CompoundTag structure, String rotation) throws IllegalStateException {
        if (blockMap == null) throw new IllegalArgumentException("blockMap cannot be null.");
        if (structure == null) throw new IllegalArgumentException("structure cannot be null.");

        ListTag<CompoundTag> palette = structure.getListTag("palette").asCompoundTagList();
        ListTag<CompoundTag> blocks = structure.getListTag("blocks").asCompoundTagList();

        for (CompoundTag block : blocks) {
            ListTag<IntTag> pos = block.getListTag("pos").asIntTagList();
            int stateIndex = block.getInt("state");

            int i = pos.get(0).asInt(), j = pos.get(1).asInt(), k = pos.get(2).asInt();
            Point3D rotatedPos = switch (rotation) {
                case "0" -> new Point3D(i, j, k);
                case "90" -> new Point3D(-k, j, i);
                case "180" -> new Point3D(-i, j, -k);
                case "270" -> new Point3D(k, j, -i);
                default -> throw new IllegalStateException("Unexpected rotation value: " + rotation);
            };

            Point3D absolutePos = new Point3D(x + rotatedPos.x(), y + rotatedPos.y(), z + rotatedPos.z());

            // Grab the original state, rotate it (which safely clones it), and map it
            CompoundTag originalState = palette.get(stateIndex);
            CompoundTag rotatedState = rotateBlockState(originalState, rotation);

            blockMap.put(absolutePos, rotatedState);
        }
    }

    public static void putStructure(Map<Point3D, CompoundTag> blockMap, int x, int y, int z, CompoundTag structure) {
        putStructure(blockMap, x, y, z, structure, "0");
    }

    /**
     * Deep clones a block state and updates directional properties based on rotation.
     */
    private static CompoundTag rotateBlockState(CompoundTag originalState, String rotation) {
        if (rotation.equals("0") || !originalState.containsKey("Properties")) {
            return originalState; // No rotation needed, or block has no directional states
        }

        // 1. Deep clone to prevent corrupting the global palette
        CompoundTag newState = originalState.clone();
        CompoundTag properties = newState.getCompoundTag("Properties");

        // 2. Rotate Facing (Stairs, Chests, Furnaces, Torches)
        if (properties.containsKey("facing")) {
            String currentFacing = properties.getString("facing");
            properties.putString("facing", rotateFacing(currentFacing, rotation));
        }

        // 3. Rotate Axis (Logs, Pillars, Basalt)
        if (properties.containsKey("axis")) {
            String currentAxis = properties.getString("axis");
            // Axis only swaps between X and Z on 90 and 270 degree turns
            if (rotation.equals("90") || rotation.equals("270")) {
                if (currentAxis.equals("x")) properties.putString("axis", "z");
                else if (currentAxis.equals("z")) properties.putString("axis", "x");
            }
        }

        return newState;
    }

    /**
     * Maps a cardinal direction string to its new direction after a Clockwise rotation.
     */
    private static String rotateFacing(String facing, String rotation) {
        return switch (rotation) {
            case "90" -> switch (facing) { // 90 Clockwise
                case "north" -> "east";
                case "east" -> "south";
                case "south" -> "west";
                case "west" -> "north";
                default -> facing; // Ignores "up" and "down"
            };
            case "180" -> switch (facing) { // 180 Clockwise
                case "north" -> "south";
                case "east" -> "west";
                case "south" -> "north";
                case "west" -> "east";
                default -> facing;
            };
            case "270" -> switch (facing) { // 270 Clockwise
                case "north" -> "west";
                case "east" -> "north";
                case "south" -> "east";
                case "west" -> "south";
                default -> facing;
            };
            default -> facing;
        };
    }

    // Convert generated blockMap into complete root CompoundTag ready for file IO
    public static CompoundTag convertMapToTag(Map<Point3D, CompoundTag> blockMap) {
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
        ListTag<CompoundTag> palette = new ListTag<>(CompoundTag.class);
        ListTag<CompoundTag> blocks = new ListTag<>(CompoundTag.class);

        for (Map.Entry<Point3D, CompoundTag> pointEntry : blockMap.entrySet()) {
            int x = pointEntry.getKey().x();
            int y = pointEntry.getKey().y();
            int z = pointEntry.getKey().z();
            minX = min(x, minX);
            minY = min(y, minY);
            minZ = min(z, minZ);
            maxX = max(x, maxX);
            maxY = max(y, maxY);
            maxZ = max(z, maxZ);
        }

        System.out.printf("Bounding box: min(%d, %d, %d), max(%d, %d, %d)%n", minX, minY, minZ, maxX, maxY, maxZ);
        ListTag<IntTag> size = new ListTag<>(IntTag.class);
        size.add(new IntTag(maxX - minX + 1));
        size.add(new IntTag(maxY - minY + 1));
        size.add(new IntTag(maxZ - minZ + 1));

        for (Map.Entry<Point3D, CompoundTag> pointEntry : blockMap.entrySet()) {
            int x = pointEntry.getKey().x();
            int y = pointEntry.getKey().y();
            int z = pointEntry.getKey().z();

            CompoundTag blockTag = pointEntry.getValue();
            if (!palette.contains(blockTag)) palette.add(blockTag);
            int stateIndex = palette.indexOf(blockTag);

            CompoundTag blockCompound = new CompoundTag();
            ListTag<IntTag> pos = new ListTag<>(IntTag.class);
            pos.add(new IntTag(x - minX));
            pos.add(new IntTag(y - minY));
            pos.add(new IntTag(z - minZ));
            blockCompound.put("pos", pos);
            blockCompound.putInt("state", stateIndex); // Map back to the palette
            blocks.add(blockCompound);
        }

        // 4. Assemble Root
        CompoundTag rootCompound = new CompoundTag();
        rootCompound.putInt("DataVersion", 3465);
        rootCompound.put("size", size);
        rootCompound.put("palette", palette);
        rootCompound.put("blocks", blocks);
        rootCompound.put("entities", new ListTag<>(CompoundTag.class)); // Leave entities empty for now

        return rootCompound;
    }

    public static CompoundTag extractStructureTagFromBlockMap(Map<Point3D, CompoundTag> blockMap, int x, int y, int z, int X, int Y, int Z, String rotation) {
        if (blockMap == null) throw new IllegalArgumentException("blockMap cannot be null.");

        Map<Point3D, CompoundTag> extractedMap = new HashMap<>();

        int minX = min(x, X), maxX = max(x, X);
        int minY = min(y, Y), maxY = max(y, Y);
        int minZ = min(z, Z), maxZ = max(z, Z);

        for (int absX = minX; absX <= maxX; absX++) {
            for (int absY = minY; absY <= maxY; absY++) {
                for (int absZ = minZ; absZ <= maxZ; absZ++) {
                    CompoundTag blockTag = blockMap.get(new Point3D(absX, absY, absZ));
                    if (blockTag == null) continue;

                    int rotatedX = absX - x;
                    int rotatedY = absY - y;
                    int rotatedZ = absZ - z;

                    Point3D originalPos = getUnrotatedPos(rotation, rotatedX, rotatedY, rotatedZ);
                    extractedMap.put(originalPos, blockTag);
                }
            }
        }

        if (extractedMap.isEmpty()) return null;
        return convertMapToTag(extractedMap);
    }

    private static Point3D getUnrotatedPos(String rotation, int i, int j, int k) throws IllegalStateException {
        return switch (rotation) {
            case "0" -> new Point3D(i, j, k);
            case "90" -> new Point3D(k, j, -i);
            case "180" -> new Point3D(-i, j, -k);
            case "270" -> new Point3D(-k, j, i);
            default -> throw new IllegalStateException("Unexpected rotation value: " + rotation);
        };
    }

    // Convert a CompoundTag representing a region file into a map of block positions to block-state CompoundTags.
    public static Map<Point3D, CompoundTag> convertTagToMap(CompoundTag rootCompoundTag) {
        if (rootCompoundTag == null) {
            throw new IllegalArgumentException("rootCompoundTag cannot be null.");
        }

        Map<Point3D, CompoundTag> blockMap = new HashMap<>();

        ListTag<CompoundTag> palette = rootCompoundTag.getListTag("palette").asCompoundTagList();
        ListTag<CompoundTag> blocks = rootCompoundTag.getListTag("blocks").asCompoundTagList();

        for (CompoundTag block : blocks) {
            ListTag<IntTag> pos = block.getListTag("pos").asIntTagList();
            int stateIndex = block.getInt("state");

            int x = pos.get(0).asInt();
            int y = pos.get(1).asInt();
            int z = pos.get(2).asInt();

            CompoundTag blockTag = palette.get(stateIndex);
            blockMap.put(new Point3D(x, y, z), blockTag);
        }

        return blockMap;
    }
}
