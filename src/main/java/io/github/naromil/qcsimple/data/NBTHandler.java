package io.github.naromil.qcsimple.data;

import io.github.naromil.qcsimple.editor.Point2D;
import net.querz.nbt.io.NBTUtil;
import net.querz.nbt.io.NamedTag;
import net.querz.nbt.tag.CompoundTag;
import net.querz.nbt.tag.IntTag;
import net.querz.nbt.tag.ListTag;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class NBTHandler {

    private static Map<Integer, Map<Point2D, QCUnit>> layers;
    private static int x;
    private static int y;
    private static int z;
    private static CompoundTag roofTag;

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
    public static CompoundTag convertToStructureTag(int x, int y, int z, String blockId) {
        Map<Point3D, CompoundTag> blockMap = new HashMap<>();
        CompoundTag structure = convertToBlockTag(blockId);
        for (int i = 0; i < x; i++) for(int j = 0; j < y; j++) for(int k = 0; k < z; k++) {
            blockMap.put(new Point3D(i, j, k), structure);
        }
        return convertMapToTag(blockMap);
    }

    // Put a structure in root compound form into a blockMap
    public static void putStructure(Map<Point3D, CompoundTag> blockMap, int x, int y, int z, CompoundTag structure, String rotation) {
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
                case "90" -> new Point3D(k, j, -i);
                case "180" -> new Point3D(-i, j, -k);
                case "270" -> new Point3D(-k, j, i);
                default -> throw new IllegalStateException("Unexpected rotation value: " + rotation);
            };

            Point3D absolutePos = new Point3D(x + rotatedPos.x(), y + rotatedPos.y(), z + rotatedPos.z());

            // Grab the original state, rotate it (which safely clones it), and map it
            CompoundTag originalState = palette.get(stateIndex);
            CompoundTag rotatedState = rotateBlockState(originalState, rotation);

            blockMap.put(absolutePos, palette.get(stateIndex));
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

    /**
     * Reads and parses a Minecraft NBT file, automatically managing GZIP streams.
     */
    public static CompoundTag readNbt(File file) throws IOException {
        NamedTag namedTag = NBTUtil.read(file);

        if (namedTag.getTag() instanceof CompoundTag) {
            return (CompoundTag) namedTag.getTag();
        } else {
            throw new IOException("Invalid NBT structure: Root node is not a CompoundTag.");
        }
    }

    public static void writeNbt(CompoundTag nbtData, File file) {
        // The Root must be wrapped in a NamedTag with an empty string.
        NamedTag rootTag = new NamedTag("", nbtData);
        try {
            NBTUtil.write(rootTag, file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // TODO: Fix the problem that the GZIP stream doesn't end inside NBTUtil
//        // 1. Manually open the file and wrap it in a GZIP stream.
//        // The try-with-resources block () ensures these streams are ALWAYS closed.
//        try (FileOutputStream fos = new FileOutputStream(file);
//             GZIPOutputStream gzipOut = new GZIPOutputStream(fos)) {
//
//            // 2. Pass the STREAM to NBTUtil, and set compression to FALSE.
//            // NBTUtil writes raw bytes, and our gzipOut handles the compression safely.
//            NBTUtil.write(rootTag, gzipOut.toString(), false);
//
//            // 3. Explicitly tell the GZIP stream to write its trailer and flush.
//            gzipOut.finish();
//            gzipOut.flush();
//        } catch(Exception e) {
//            throw new RuntimeException(e);
//            e.printStackTrace(); // Keep this on while debugging!
//        }
    }
}
