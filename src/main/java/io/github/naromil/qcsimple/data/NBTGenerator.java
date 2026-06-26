package io.github.naromil.qcsimple.data;

import net.querz.nbt.tag.CompoundTag;

import java.util.HashMap;
import java.util.Map;

public class NBTGenerator {
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
        return BlockNBTConverter.convertMapToTag(blockMap);
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
}
