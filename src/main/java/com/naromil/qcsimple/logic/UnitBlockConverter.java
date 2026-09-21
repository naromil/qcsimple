package com.naromil.qcsimple.logic;

import com.naromil.qcsimple.data.BlockPalette;
import com.naromil.qcsimple.geom.Point2D;
import com.naromil.qcsimple.geom.Point3D;
import net.querz.nbt.tag.CompoundTag;

import java.util.Map;

/**
 * Entry point for the two conversions between the editor's layer model and a block map: the export
 * drives {@link UnitExporter}, the import drives {@link UnitImporter}.
 */
public final class UnitBlockConverter {

    private UnitBlockConverter() {
    }

    /** Main logic that converts a map of units into a map of blocks with the shared block palette. */
    public static Map<Point3D, CompoundTag> convertLayersToBlockMap(Map<Integer, Map<Point2D, QCUnit>> layers) {
        return convertLayersToBlockMap(layers, BlockPalette.getInstance());
    }

    /** Main logic that converts a map of units into a map of blocks. */
    public static Map<Point3D, CompoundTag> convertLayersToBlockMap(Map<Integer, Map<Point2D, QCUnit>> layers, BlockPalette palette) {
        return UnitExporter.export(layers, palette);
    }

    /** Main logic that converts a map of blocks into a map of units, configuring the palette on the way. */
    public static Map<Integer, Map<Point2D, QCUnit>> convertBlockMapToLayers(Map<Point3D, CompoundTag> blockMap) {
        return convertBlockMapToLayers(blockMap, BlockPalette.getInstance());
    }

    /** Main logic that converts a map of blocks into a map of units. */
    public static Map<Integer, Map<Point2D, QCUnit>> convertBlockMapToLayers(Map<Point3D, CompoundTag> blockMap, BlockPalette palette) {
        return UnitImporter.importLayers(blockMap, palette, UnitGrid.CENTER_X, UnitGrid.CENTER_Z);
    }
}
