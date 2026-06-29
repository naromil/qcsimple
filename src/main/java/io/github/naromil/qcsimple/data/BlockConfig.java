package io.github.naromil.qcsimple.data;

import net.querz.nbt.tag.CompoundTag;
import net.querz.nbt.tag.IntTag;
import net.querz.nbt.tag.ListTag;

public class BlockConfig {
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
    protected static String gatePath = "";
    // Parsed NBT Data (for your actual structure compiler)
    protected static CompoundTag innerWallTag = null;
    protected static CompoundTag outerWallTag = null;
    protected static CompoundTag innerColumnTag = null;
    protected static CompoundTag gateTag = null;

    public static boolean isConfigured() {
        return frameworkId != null && !frameworkId.isBlank();
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
        innerWallPath = "[Generated Default: Deepslate Bricks]";
        outerWallPath = "[Generated Default: Deepslate Bricks]";
        innerColumnPath = "[Generated Default: Stripped Spruce Log]";
        gatePath = "[Generated Default: Deepslate Bricks]";

        // 3. Default .nbt structure tags
        innerWallTag = NBTGenerator.generateSimpleStructureTag("minecraft:deepslate_bricks", new byte[][][]{
                {{0, 0, 0}, {0, 0, 0}, {0, 0, 0}, {-1, 0, -1}, {-1, 0, -1}, {-1, 0, -1}, {-1, 0, -1}},
                {{-1, 0, -1}, {-1, 0, -1}, {-1, 0, -1}, {-1, 0, -1}, {-1, 0, -1}, {-1, 0, -1}, {-1, 0, -1}},
                {{-1, 0, -1}, {-1, 0, -1}, {-1, 0, -1}, {-1, 0, -1}, {-1, 0, -1}, {-1, 0, -1}, {-1, 0, -1}},
                {{-1, 0, -1}, {-1, 0, -1}, {-1, 0, -1}, {-1, 0, -1}, {-1, 0, -1}, {-1, 0, -1}, {-1, 0, -1}},
                {{-1, 0, -1}, {-1, 0, -1}, {-1, 0, -1}, {-1, 0, -1}, {-1, 0, -1}, {-1, 0, -1}, {-1, 0, -1}},
                {{-1, 0, -1}, {-1, 0, -1}, {-1, 0, -1}, {-1, 0, -1}, {-1, 0, -1}, {-1, 0, -1}, {-1, 0, -1}},
                {{0, 0, 0}, {0, 0, 0}, {0, 0, 0}, {-1, 0, -1}, {-1, 0, -1}, {-1, 0, -1}, {-1, 0, -1}}
        });

        gateTag = NBTGenerator.generateSimpleStructureTag("minecraft:deepslate_bricks", new byte[][][]{
                {{0, 0, 0}, {0, 0, 0}, {0, 0, 0}, {-1, 0, -1}, {-1, 0, -1}, {-1, 0, -1}, {-1, 0, -1}},
                {{-1, 0, -1}, {-1, 0, -1}, {-1, 0, -1}, {-1, 0, -1}, {-1, 0, -1}, {-1, 0, -1}, {-1, 0, -1}},
                {{-1, -1, -1}, {-1, -1, -1}, {-1, -1, -1}, {-1, -1, -1}, {-1, 0, -1}, {-1, 0, -1}, {0, 0, 0}},
                {{-1, -1, -1}, {-1, -1, -1}, {-1, -1, -1}, {-1, -1, -1}, {-1, 0, -1}, {-1, 0, -1}, {0, 0, 0}},
                {{-1, -1, -1}, {-1, -1, -1}, {-1, -1, -1}, {-1, -1, -1}, {-1, 0, -1}, {-1, 0, -1}, {0, 0, 0}},
                {{-1, 0, -1}, {-1, 0, -1}, {-1, 0, -1}, {-1, 0, -1}, {-1, 0, -1}, {-1, 0, -1}, {-1, 0, -1}},
                {{0, 0, 0}, {0, 0, 0}, {0, 0, 0}, {-1, 0, -1}, {-1, 0, -1}, {-1, 0, -1}, {-1, 0, -1}}
        });

        outerWallTag = NBTGenerator.generateSimpleStructureTag("minecraft:deepslate_bricks", new byte[][][]{
                {{-1, 0, 0}, {-1, 0, 0}, {-1, 0, 0}, {-1, 0, -1}, {-1, 0, -1}, {-1, 0, -1}, {-1, 0, -1}},
                {{0, 0, -1}, {0, 0, -1}, {0, 0, -1}, {0, 0, -1}, {0, 0, -1}, {-1, 0, -1}, {-1, 0, -1}},
                {{-1, -1, -1}, {-1, -1, -1}, {-1, -1, -1}, {-1, -1, -1}, {0, 0, -1}, {-1, 0, 0}, {-1, 0, 0}},
                {{-1, -1, -1}, {-1, -1, -1}, {-1, -1, -1}, {-1, -1, -1}, {0, 0, -1}, {-1, 0, 0}, {-1, 0, 0}},
                {{-1, -1, -1}, {-1, -1, -1}, {-1, -1, -1}, {-1, -1, -1}, {0, 0, -1}, {-1, 0, 0}, {-1, 0, 0}},
                {{0, 0, -1}, {0, 0, -1}, {0, 0, -1}, {0, 0, -1}, {0, 0, -1}, {-1, 0, -1}, {-1, 0, -1}},
                {{-1, 0, 0}, {-1, 0, 0}, {-1, 0, 0}, {-1, 0, -1}, {-1, 0, -1}, {-1, 0, -1}, {-1, 0, -1}},
        });

        innerColumnTag = NBTGenerator.generateSimpleStructureTag("minecraft:stripped_spruce_log", new byte[][][]{
                {{-1, 0, -1}, {-1, -1, -1}, {-1, -1, -1}, {-1, -1, -1}, {-1, -1, -1}, {-1, -1, -1}, {-1, 0, -1}},
                {{0, 0, 0}, {-1, 0, -1}, {-1, 0, -1}, {-1, 0, -1}, {-1, 0, -1}, {-1, 0, -1}, {0, 0, 0}},
                {{-1, 0, -1}, {-1, -1, -1}, {-1, -1, -1}, {-1, -1, -1}, {-1, -1, -1}, {-1, -1, -1}, {-1, 0, -1}},
        });
    }

    public static int getX (CompoundTag tag) {
        ListTag<IntTag> sizeTag = tag.getListTag("size").asIntTagList();
        return sizeTag.get(0).asInt();
    }

    public static int getY (CompoundTag tag) {
        ListTag<IntTag> sizeTag = tag.getListTag("size").asIntTagList();
        return sizeTag.get(1).asInt();
    }

    public static int getZ (CompoundTag tag) {
        ListTag<IntTag> sizeTag = tag.getListTag("size").asIntTagList();
        return sizeTag.get(2).asInt();
    }
}
