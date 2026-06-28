package io.github.naromil.qcsimple.data;

import net.querz.nbt.tag.CompoundTag;
import net.querz.nbt.tag.IntTag;
import net.querz.nbt.tag.ListTag;

import java.util.HashMap;
import java.util.Map;

import static java.lang.Math.max;
import static java.lang.Math.min;

// Operations involving Block Maps and NBT Tags
public class BlockNBTConverter {

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
            CompoundTag rotatedState = RotationUtils.rotateBlockState(originalState, rotation);

            blockMap.put(absolutePos, rotatedState);
        }
    }

    public static void putStructure(Map<Point3D, CompoundTag> blockMap, int x, int y, int z, CompoundTag structure) {
        putStructure(blockMap, x, y, z, structure, "0");
    }

    // Convert generated blockMap into complete root CompoundTag ready for file IO
    // This ignores original coordinates and rebases them on the min/max coordinates
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

//        System.out.printf("Bounding box: min(%d, %d, %d), max(%d, %d, %d)%n", minX, minY, minZ, maxX, maxY, maxZ);
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

    public static CompoundTag extractStructure(Map<Point3D, CompoundTag> blockMap, int x, int y, int z, int X, int Y, int Z, String rotation) {
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

                    Point3D originalPos = RotationUtils.getUnrotatedPos(rotation, rotatedX, rotatedY, rotatedZ);
                    extractedMap.put(originalPos, blockTag);
                }
            }
        }

        if (extractedMap.isEmpty()) return null;
        return convertMapToTag(extractedMap);
    }

    // Convert a CompoundTag representing a region file into a map of block positions to block-state CompoundTags.
    // This uses the original coordinates from the CompoundTag
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
