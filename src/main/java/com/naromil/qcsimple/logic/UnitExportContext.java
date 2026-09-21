package com.naromil.qcsimple.logic;

import com.naromil.qcsimple.data.BlockPalette;
import com.naromil.qcsimple.geom.Point2D;
import com.naromil.qcsimple.geom.Point3D;
import net.querz.nbt.tag.CompoundTag;

import java.util.Map;

/**
 * Everything a component needs while the export walks the layers: the map it writes into, the
 * unit that is currently being exported and the queries that decide where a block may go.
 */
public final class UnitExportContext {

    private final Map<Integer, Map<Point2D, QCUnit>> layers;
    private final Map<Point3D, CompoundTag> blockMap;
    private final BlockPalette palette;

    private int dx;
    private int dy;
    private int dz;
    private Map<Point2D, QCUnit> layerMap;

    UnitExportContext(Map<Integer, Map<Point2D, QCUnit>> layers, Map<Point3D, CompoundTag> blockMap, BlockPalette palette) {
        this.layers = layers;
        this.blockMap = blockMap;
        this.palette = palette;
    }

    void beginUnit(int dx, int dy, int dz, Map<Point2D, QCUnit> layerMap) {
        this.dx = dx;
        this.dy = dy;
        this.dz = dz;
        this.layerMap = layerMap;
    }

    public BlockPalette palette() {
        return palette;
    }

    public int dx() {
        return dx;
    }

    public int dy() {
        return dy;
    }

    public int dz() {
        return dz;
    }

    /** The unit map of the layer that is currently being exported. */
    public Map<Point2D, QCUnit> layerMap() {
        return layerMap;
    }

    public Map<Point3D, CompoundTag> blockMap() {
        return blockMap;
    }

    public int absX(int x) {
        return dx * 8 + x;
    }

    public int absY(int y) {
        return dy * 8 + y;
    }

    public int absZ(int z) {
        return dz * 8 + z;
    }

    public Point3D abs(int x, int y, int z) {
        return new Point3D(absX(x), absY(y), absZ(z));
    }

    /** Writes a block, replacing whatever sits at that position. */
    public void put(Point3D pos, String blockId) {
        blockMap.put(pos, NBTGenerator.convertToBlockTag(blockId));
    }

    /** Writes a block only when that position is still empty. */
    public void putIfAbsent(Point3D pos, String blockId) {
        if (!blockMap.containsKey(pos)) {
            blockMap.put(pos, NBTGenerator.convertToBlockTag(blockId));
        }
    }

    public void putStructure(int x, int y, int z, CompoundTag structure, String rotation, boolean preserve) {
        BlockNBTConverter.putStructure(blockMap, x, y, z, structure, rotation, preserve);
    }

    public void putStructure(Point3D pos, CompoundTag structure, String rotation, boolean preserve) {
        BlockNBTConverter.putStructure(blockMap, pos, structure, rotation, preserve);
    }

    /**
     * Whether a relative position of the current unit reaches into empty space, i.e. no unit sits
     * there to attach a block to.
     */
    public boolean isFree(int x, int y, int z) {
        return SpatialUtils.isValidPlacement(layers, x, y, z, dx, dy, dz);
    }

    public boolean unitExists(int dx, int dy, int dz) {
        return SpatialUtils.layersContains(layers, dx, dy, dz);
    }
}
