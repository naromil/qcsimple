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
    public static String frameworkId = "";
    public static String columnId = "";
    public static String rowId = "";
    public static String floorId = "";

    // Main logic that converts a map of units into a map of blocks
    public static Map<Point3D, CompoundTag> convertToBlockMap(Map<Integer, Map<Point2D, QCUnit>> layers) {
        // Guard clause: Prevent crashes if exporting an empty canvas
        if (layers.isEmpty()) {
            System.out.println("Warning: Attempted to export empty layers.");
            return new HashMap<Point3D, CompoundTag>();
        }

        Map<Point3D, CompoundTag> blockMap = new HashMap<Point3D, CompoundTag>();

        for (Map.Entry<Integer, Map<Point2D, QCUnit>> layerEntry : layers.entrySet()) {
            int dy = layerEntry.getKey() * 8;
            for (Map.Entry<Point2D, QCUnit> pointEntry : layerEntry.getValue().entrySet()) {
                int dx = pointEntry.getKey().x() * 8;
                int dz = pointEntry.getKey().z() * 8;

                for (int i = 0; i < 9; i++)  for (int j = 0; j < 9; j++) for (int k = 0; k < 9; k++) {
                    // Count how many dimensions are on the outer boundaries (0 or 8)
                    int boundaryCount = 0;
                    if (i == 0 || i == 8) boundaryCount++;
                    if (j == 0 || j == 8) boundaryCount++;
                    if (k == 0 || k == 8) boundaryCount++;

                    // Edges require at least 2 dimensions to be on the boundary
                    // (Note: corners have a count of 3, which are part of the framework)
                    if (boundaryCount >= 2) {
                        blockMap.put(new Point3D(dx + i, dy + j, dz + k), convertToBlockTag(frameworkId));
                    }
                }
            }
        }

        return blockMap;
    }

    private static CompoundTag convertToBlockTag(String blockId) {
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

    public static Map<Integer, Map<Point2D, QCUnit>> tagToMap(CompoundTag rootCompoundTag) {
        return null;
        // TODO: Add QC-style conversion here
    }
}
