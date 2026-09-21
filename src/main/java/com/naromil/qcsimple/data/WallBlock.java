package com.naromil.qcsimple.data;

import com.naromil.qcsimple.geom.Point3D;
import com.naromil.qcsimple.logic.UnitExportContext;
import com.naromil.qcsimple.logic.UnitImportContext;

/**
 * The wall block: the default wall used above the ceiling and below the floor of a unit.
 */
public class WallBlock extends BlockComponent {

    public static final String DEFAULT_ID = "minecraft:deepslate_tiles";

    /** Offset of the probe block from the base corner of the south-east unit. */
    private static final int PROBE_DX = -1;
    private static final int PROBE_DY = 1;
    private static final int PROBE_DZ = 0;

    @Override
    public void applyDefaultConfig() {
        setBlockId(DEFAULT_ID);
    }

    @Override
    public void exportVoxel(UnitExportContext ctx, int x, int y, int z, int boundaryCount) {
        if (boundaryCount != 2 || (y != 0 && y != 8)) {
            return;
        }

        int ny = y + (y == 0 ? -1 : 1);
        if (ctx.isFree(x, ny, z)) {
            ctx.put(new Point3D(ctx.absX(x), ctx.absY(ny), ctx.absZ(z)), getBlockId());
        }
    }

    @Override
    public void extract(UnitImportContext ctx) {
        Point3D base = ctx.southeastBase();
        if (base == null) {
            return;
        }
        setBlockId(ctx.nameOrDefault(new Point3D(base.x() + PROBE_DX, base.y() + PROBE_DY, base.z() + PROBE_DZ), getBlockId()));
    }
}
