package io.github.naromil.qcsimple.data;

import net.querz.nbt.tag.CompoundTag;

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
        return innerWallPath != null && !innerWallPath.isEmpty() &&
                outerWallPath != null && !outerWallPath.isEmpty() &&
                innerColumnPath != null && !innerColumnPath.isEmpty() &&
                gatePath != null && !gatePath.isEmpty();
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
        innerWallPath = "[Generated Default: Glass]";
        outerWallPath = "[Generated Default: Deepslate Bricks]";
        innerColumnPath = "[Generated Default: Stripped Spruce Log]";
        gatePath = "[Not Configured]";

        // 3. Default .nbt structure tags
        innerWallTag = NBTGenerator.generateSimpleStructureTag("minecraft:glass", new byte[][][]{
                {{0}, {0}, {0}, {0}, {0}, {0}, {0}},
                {{0}, {0}, {0}, {0}, {0}, {0}, {0}},
                {{-1}, {-1}, {-1}, {-1}, {0}, {0}, {0}},
                {{-1}, {-1}, {-1}, {-1}, {0}, {0}, {0}},
                {{-1}, {-1}, {-1}, {-1}, {0}, {0}, {0}},
                {{0}, {0}, {0}, {0}, {0}, {0}, {0}},
                {{0}, {0}, {0}, {0}, {0}, {0}, {0}}
        });

        outerWallTag = NBTGenerator.generateSimpleStructureTag("minecraft:deepslate_bricks", new byte[][][]{
                {{-1, 0}, {-1, 0}, {-1, 0}, {-1, 0}, {-1, 0}, {-1, 0}, {-1, 0}},
                {{0, 0}, {0, 0}, {0, 0}, {0, 0}, {0, 0}, {-1, 0}, {-1, 0}},
                {{-1, -1}, {-1, -1}, {-1, -1}, {-1, -1}, {0, 0}, {-1, 0}, {-1, 0}},
                {{-1, -1}, {-1, -1}, {-1, -1}, {-1, -1}, {0, 0}, {-1, 0}, {-1, 0}},
                {{-1, -1}, {-1, -1}, {-1, -1}, {-1, -1}, {0, 0}, {-1, 0}, {-1, 0}},
                {{0, 0}, {0, 0}, {0, 0}, {0, 0}, {0, 0}, {-1, 0}, {-1, 0}},
                {{-1, 0}, {-1, 0}, {-1, 0}, {-1, 0}, {-1, 0}, {-1, 0}, {-1, 0}},
        });

        innerColumnTag = NBTGenerator.generateSimpleStructureTag("minecraft:stripped_spruce_log", new byte[][][]{
                {{-1, 0, -1}, {-1, -1, -1}, {-1, -1, -1}, {-1, -1, -1}, {-1, -1, -1}, {-1, -1, -1}, {-1, 0, -1}},
                {{0, 0, 0}, {-1, 0, -1}, {-1, 0, -1}, {-1, 0, -1}, {-1, 0, -1}, {-1, 0, -1}, {0, 0, 0}},
                {{-1, 0, -1}, {-1, -1, -1}, {-1, -1, -1}, {-1, -1, -1}, {-1, -1, -1}, {-1, -1, -1}, {-1, 0, -1}},
        });
    }
}
