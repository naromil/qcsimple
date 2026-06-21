package io.github.naromil.qcsimple.data;

import io.github.naromil.qcsimple.editor.Point2D;
import net.querz.nbt.tag.CompoundTag;

import java.util.Map;

public class DataConverter {
    // Defaulting to empty strings or basic Minecraft blocks
    public static String frameworkId = "";
    public static String columnId = "";
    public static String rowId = "";
    public static String floorId = "";

    public static CompoundTag mapToTag(Map<Integer, Map<Point2D, QCUnit>> layers) {
        return null;
        // TODO: Add QC-style conversion here
    }

    public static Map<Integer, Map<Point2D, QCUnit>> tagToMap(CompoundTag rootCompoundTag) {
        return null;
        // TODO: Add QC-style conversion here
    }
}
