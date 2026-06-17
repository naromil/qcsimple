package io.github.naromil.qcsimple;

import net.querz.nbt.io.NBTUtil;
import net.querz.nbt.io.NamedTag;
import net.querz.nbt.tag.CompoundTag;
import java.io.File;
import java.io.IOException;

public class NBTHandler {

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

    /**
     * Serializes a CompoundTag back down into a GZIP-compressed binary NBT file.
     */
    public static void writeNbt(CompoundTag compoundTag, File file) throws IOException {
        // Minecraft structure files traditionally leave the internal root tag name blank ("")
        NamedTag namedTag = new NamedTag("", compoundTag);

        // NBTUtil.write compresses via GZIP by default
        NBTUtil.write(namedTag, file);
    }
}