package com.naromil.qcsimple.data;

import com.naromil.qcsimple.geom.Point3D;
import com.naromil.qcsimple.logic.BlockNBTConverter;
import com.naromil.qcsimple.logic.NBTGenerator;
import com.naromil.qcsimple.logic.UnitExportContext;
import com.naromil.qcsimple.logic.UnitImportContext;
import net.querz.nbt.tag.CompoundTag;

/**
 * The outer wall: a 7x7x1..3 structure closing every face of a unit that has no neighbour. A
 * structure deeper than one voxel grows outward, away from the unit.
 */
public class OuterWall extends QCComponent {

    public OuterWall() {
        super();
    }

    public OuterWall(String path, CompoundTag tag) {
        super(path, tag);
    }

    @Override
    public void applyDefaultConfig() {
        setTag(NBTGenerator.generateSimpleStructureTag("minecraft:deepslate_bricks", new byte[][][]{
                {{-1, 0, 0}, {-1, 0, 0}, {-1, 0, 0}, {-1, 0, -1}, {-1, 0, -1}, {-1, 0, -1}, {-1, 0, -1}},
                {{0, 0, -1}, {0, 0, -1}, {0, 0, -1}, {0, 0, -1}, {0, 0, -1}, {-1, 0, -1}, {-1, 0, -1}},
                {{-1, -1, -1}, {-1, -1, -1}, {-1, -1, -1}, {-1, -1, -1}, {0, 0, -1}, {-1, 0, 0}, {-1, 0, 0}},
                {{-1, -1, -1}, {-1, -1, -1}, {-1, -1, -1}, {-1, -1, -1}, {0, 0, -1}, {-1, 0, 0}, {-1, 0, 0}},
                {{-1, -1, -1}, {-1, -1, -1}, {-1, -1, -1}, {-1, -1, -1}, {0, 0, -1}, {-1, 0, 0}, {-1, 0, 0}},
                {{0, 0, -1}, {0, 0, -1}, {0, 0, -1}, {0, 0, -1}, {0, 0, -1}, {-1, 0, -1}, {-1, 0, -1}},
                {{-1, 0, 0}, {-1, 0, 0}, {-1, 0, 0}, {-1, 0, -1}, {-1, 0, -1}, {-1, 0, -1}, {-1, 0, -1}},
        }));
        setPath("[Generated Default: Deepslate Bricks]");
    }

    @Override
    public boolean isValidSize(int x, int y, int z) {
        return x == 7 && y == 7 && z <= 3;
    }

    @Override
    public void exportUnit(UnitExportContext ctx) {
        if (isEmpty()) {
            return;
        }

        // If the wall is 2 or more blocks deep the extra layers go outward; a 1 deep wall stays
        // exactly on the boundary.
        int outShift = (getZ() >= 2) ? 1 : 0;

        int absX = ctx.absX(0);
        int absY = ctx.absY(0);
        int absZ = ctx.absZ(0);

        if (!ctx.unitExists(ctx.dx(), ctx.dy(), ctx.dz() - 1)) {
            ctx.putStructure(absX + 1, absY + 1, absZ - outShift, getTag(), "0", false);
        }
        if (!ctx.unitExists(ctx.dx(), ctx.dy(), ctx.dz() + 1)) {
            ctx.putStructure(absX + 7, absY + 1, absZ + 8 + outShift, getTag(), "180", false);
        }
        if (!ctx.unitExists(ctx.dx() + 1, ctx.dy(), ctx.dz())) {
            ctx.putStructure(absX + 8 + outShift, absY + 1, absZ + 1, getTag(), "90", false);
        }
        if (!ctx.unitExists(ctx.dx() - 1, ctx.dy(), ctx.dz())) {
            ctx.putStructure(absX - outShift, absY + 1, absZ + 7, getTag(), "270", false);
        }
    }

    @Override
    public void extract(UnitImportContext ctx) {
        Point3D base = ctx.southeastBase();
        if (base == null) {
            return;
        }
        setExtracted(BlockNBTConverter.extractStructure(ctx.blockMap(),
                base.x() - 7, base.y() - 7, base.z() - 1,
                base.x() - 1, base.y() - 1, base.z() + 1,
                "180"));
    }
}
