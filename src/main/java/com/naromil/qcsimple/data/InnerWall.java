package com.naromil.qcsimple.data;

import com.naromil.qcsimple.logic.NBTGenerator;
import com.naromil.qcsimple.logic.UnitImportContext;
import net.querz.nbt.tag.CompoundTag;

/**
 * The inner wall: a 7x7x1 or 7x7x3 structure closing the border between two units whose shared
 * wall flags are set without a gate.
 */
public class InnerWall extends InnerStructure {

    public InnerWall() {
        super();
    }

    public InnerWall(String path, CompoundTag tag) {
        super(path, tag);
    }

    @Override
    public void applyDefaultConfig() {
        setTag(NBTGenerator.generateSimpleStructureTag("minecraft:deepslate_bricks", new byte[][][]{
                {{0, 0, 0}, {0, 0, 0}, {0, 0, 0}, {-1, 0, -1}, {-1, 0, -1}, {-1, 0, -1}, {-1, 0, -1}},
                {{-1, 0, -1}, {-1, 0, -1}, {-1, 0, -1}, {-1, 0, -1}, {-1, 0, -1}, {-1, 0, -1}, {-1, 0, -1}},
                {{-1, 0, -1}, {-1, 0, -1}, {-1, 0, -1}, {-1, 0, -1}, {-1, 0, -1}, {-1, 0, -1}, {-1, 0, -1}},
                {{-1, 0, -1}, {-1, 0, -1}, {-1, 0, -1}, {-1, 0, -1}, {-1, 0, -1}, {-1, 0, -1}, {-1, 0, -1}},
                {{-1, 0, -1}, {-1, 0, -1}, {-1, 0, -1}, {-1, 0, -1}, {-1, 0, -1}, {-1, 0, -1}, {-1, 0, -1}},
                {{-1, 0, -1}, {-1, 0, -1}, {-1, 0, -1}, {-1, 0, -1}, {-1, 0, -1}, {-1, 0, -1}, {-1, 0, -1}},
                {{0, 0, 0}, {0, 0, 0}, {0, 0, 0}, {-1, 0, -1}, {-1, 0, -1}, {-1, 0, -1}, {-1, 0, -1}}
        }));
        setPath("[Generated Default: Deepslate Bricks]");
    }

    @Override
    public boolean isValidSize(int x, int y, int z) {
        return x == 7 && y == 7 && (z == 1 || z == 3);
    }

    @Override
    protected boolean acceptsGateSides() {
        return false;
    }

    @Override
    public void onDetectedSide(UnitImportContext ctx, int absX, int absY, int absZ, UnitSide side) {
        if (ctx.innerWallExtracted()) {
            return;
        }
        extractSide(ctx, absX, absY, absZ, side);
        if (!isEmpty()) {
            ctx.markInnerWallExtracted();
        }
    }
}
