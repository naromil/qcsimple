package com.naromil.qcsimple.data;

import com.naromil.qcsimple.geom.Point3D;
import com.naromil.qcsimple.logic.UnitExportContext;
import com.naromil.qcsimple.logic.UnitImportContext;

/**
 * The floor block: the interior surface of every ceiling and floor level.
 */
public class FloorBlock extends BlockComponent {

    public static final String DEFAULT_ID = "minecraft:spruce_planks";

    /** Offset of the probe block from the base corner of the south-east unit. */
    private static final int PROBE_DX = -1;
    private static final int PROBE_DY = -8;
    private static final int PROBE_DZ = -1;

    @Override
    public void applyDefaultConfig() {
        setBlockId(DEFAULT_ID);
    }

    @Override
    public void exportVoxel(UnitExportContext ctx, int x, int y, int z, int boundaryCount) {
        if ((y == 0 || y == 8) && x > 0 && x < 8 && z > 0 && z < 8) {
            ctx.put(ctx.abs(x, y, z), getBlockId());
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
