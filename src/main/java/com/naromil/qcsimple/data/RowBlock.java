package com.naromil.qcsimple.data;

import com.naromil.qcsimple.geom.Point3D;
import com.naromil.qcsimple.logic.UnitExportContext;
import com.naromil.qcsimple.logic.UnitImportContext;

/**
 * The row block: the horizontal bar attached to the framework on the outside, on the ceiling and
 * floor levels of a unit.
 */
public class RowBlock extends BlockComponent {

    public static final String DEFAULT_ID = "minecraft:chiseled_deepslate";

    /** Offset of the probe block from the base corner of the south-east unit. */
    private static final int PROBE_DX = -1;
    private static final int PROBE_DY = 0;
    private static final int PROBE_DZ = 1;

    @Override
    public void applyDefaultConfig() {
        setBlockId(DEFAULT_ID);
    }

    @Override
    public void exportVoxel(UnitExportContext ctx, int x, int y, int z, int boundaryCount) {
        if (boundaryCount != 2 || (y != 0 && y != 8)) {
            return;
        }

        // On a ceiling or floor level the row only reaches one voxel out of the unit.
        if (x == 0 || x == 8) {
            int nx = x + (x == 0 ? -1 : 1);
            if (ctx.isFree(nx, y, z)) {
                ctx.put(new Point3D(ctx.absX(nx), ctx.absY(y), ctx.absZ(z)), getBlockId());
            }
        }
        if (z == 0 || z == 8) {
            int nz = z + (z == 0 ? -1 : 1);
            if (ctx.isFree(x, y, nz)) {
                ctx.put(new Point3D(ctx.absX(x), ctx.absY(y), ctx.absZ(nz)), getBlockId());
            }
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
