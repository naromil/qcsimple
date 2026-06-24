package io.github.naromil.qcsimple.data;

import net.querz.nbt.io.NBTUtil;
import net.querz.nbt.io.NamedTag;
import net.querz.nbt.tag.CompoundTag;
import net.querz.nbt.tag.IntTag;
import net.querz.nbt.tag.ListTag;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

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

    public static CompoundTag convertToStructureTag(int x, int y, int z, String blockId) {
        Map<Point3D, CompoundTag> blockMap = new HashMap<>();
        CompoundTag tag = convertToBlockTag(blockId);
        for (int i = 0; i < x; i++) for(int j = 0; j < y; j++) for(int k = 0; k < z; k++) {
            blockMap.put(new Point3D(i, j, k), tag);
        }
        return convertMapToTag(blockMap);
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

    public static void write(CompoundTag nbtData, File file) {
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