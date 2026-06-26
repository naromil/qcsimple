package io.github.naromil.qcsimple.data;

import net.querz.nbt.io.NBTUtil;
import net.querz.nbt.io.NamedTag;
import net.querz.nbt.tag.CompoundTag;

import java.io.File;
import java.io.IOException;

public class NBTIO {
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
