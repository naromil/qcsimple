package com.naromil.qcsimple.logic;

import com.naromil.qcsimple.data.BlockPalette;
import com.naromil.qcsimple.geom.Point2D;
import com.naromil.qcsimple.geom.Point3D;
import net.querz.nbt.tag.CompoundTag;

import java.util.Map;

import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 * Everything a component needs while an opened structure is read back into the editor: the block
 * map that was read from disk, the layers being built, the grid origin and the flags that tell
 * which structures have been found so far.
 */
public final class UnitImportContext {

    private final Map<Point3D, CompoundTag> blockMap;
    private final Map<Integer, Map<Point2D, QCUnit>> layers;
    private final BlockPalette palette;
    private final int originX;
    private final int originY;
    private final int originZ;

    private UnitGridStats stats = UnitGridStats.EMPTY;
    private boolean innerWallExtracted;
    private boolean gateExtracted;
    private boolean innerColumnExtracted;

    UnitImportContext(Map<Point3D, CompoundTag> blockMap, Map<Integer, Map<Point2D, QCUnit>> layers,
                      BlockPalette palette, int originX, int originY, int originZ) {
        this.blockMap = blockMap;
        this.layers = layers;
        this.palette = palette;
        this.originX = originX;
        this.originY = originY;
        this.originZ = originZ;
    }

    public BlockPalette palette() {
        return palette;
    }

    public Map<Point3D, CompoundTag> blockMap() {
        return blockMap;
    }

    /** The unit layers being built, keyed by {@code dy + 1}. */
    public Map<Integer, Map<Point2D, QCUnit>> layers() {
        return layers;
    }

    public int originX() {
        return originX;
    }

    public int originY() {
        return originY;
    }

    public int originZ() {
        return originZ;
    }

    public int absX(int dx) {
        return originX + dx * 8;
    }

    public int absY(int dy) {
        return originY + dy * 8;
    }

    public int absZ(int dz) {
        return originZ + dz * 8;
    }

    public String frameworkId() {
        return palette.frameworkBlock().getBlockId();
    }

    public UnitGridStats stats() {
        return stats;
    }

    void setStats(UnitGridStats stats) {
        this.stats = stats;
    }

    /**
     * The corner one unit past the south-east unit of the grid, the anchor the single block ids are
     * probed from. {@code null} when the grid has no detected units.
     */
    public Point3D southeastBase() {
        if (!stats.hasSoutheastCorner()) {
            return null;
        }
        return new Point3D(
                originX + stats.maxUnitX() * 8 + 8,
                originY + stats.maxUnitY() * 8 + 8,
                originZ + stats.maxUnitZ() * 8 + 8);
    }

    /** The name of the block at that position, or the (normalised) fallback id when it is empty. */
    public String nameOrDefault(Point3D pos, String fallbackId) {
        CompoundTag blockTag = blockMap.get(pos);
        if (blockTag != null) {
            return blockTag.getString("Name");
        }
        return NBTGenerator.convertToBlockTag(fallbackId).getString("Name");
    }

    /** Whether anything is stored inside the box spanned by the two corners. */
    public boolean hasAnyBlock(int x, int y, int z, int X, int Y, int Z) {
        return hasAnyBlock(x, y, z, X, Y, Z, "");
    }

    /** Whether anything other than {@code noBlock} is stored inside the box spanned by the two corners. */
    public boolean hasAnyBlock(int x, int y, int z, int X, int Y, int Z, String noBlock) {
        int minX = min(x, X), maxX = max(x, X);
        int minY = min(y, Y), maxY = max(y, Y);
        int minZ = min(z, Z), maxZ = max(z, Z);

        for (int absX = minX; absX <= maxX; absX++) {
            for (int absY = minY; absY <= maxY; absY++) {
                for (int absZ = minZ; absZ <= maxZ; absZ++) {
                    CompoundTag blockTag = blockMap.get(new Point3D(absX, absY, absZ));
                    if (blockTag != null && !blockTag.getString("Name").equals(noBlock)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public boolean innerWallExtracted() {
        return innerWallExtracted;
    }

    public void markInnerWallExtracted() {
        innerWallExtracted = true;
    }

    public boolean gateExtracted() {
        return gateExtracted;
    }

    public void markGateExtracted() {
        gateExtracted = true;
    }

    public boolean innerColumnExtracted() {
        return innerColumnExtracted;
    }

    public void markInnerColumnExtracted() {
        innerColumnExtracted = true;
    }
}
