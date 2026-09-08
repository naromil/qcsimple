package com.naromil.qcsimple.logic;

import com.naromil.qcsimple.data.Gate;
import com.naromil.qcsimple.data.InnerColumn;
import com.naromil.qcsimple.data.InnerWall;
import com.naromil.qcsimple.data.OuterWall;

public class BlockConfig {
    // Defaulting to empty strings or basic Minecraft blocks
    protected static String frameworkId = "";
    protected static String columnId = "";
    protected static String rowId = "";
    protected static String floorId = "";
    protected static String wallId = "";

    protected static InnerWall innerWall = new InnerWall();
    protected static Gate gate = new Gate();
    protected static OuterWall outerWall = new OuterWall();
    protected static InnerColumn innerColumn = new InnerColumn();

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

        // 2. Default .nbt structure tags
        innerWall.applyDefaultConfig();
        gate.applyDefaultConfig();
        outerWall.applyDefaultConfig();
        innerColumn.applyDefaultConfig();
    }
}
