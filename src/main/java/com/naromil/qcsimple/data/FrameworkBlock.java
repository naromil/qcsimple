package com.naromil.qcsimple.data;

import com.naromil.qcsimple.geom.Point3D;
import com.naromil.qcsimple.logic.GridOrigin;
import com.naromil.qcsimple.logic.SpatialUtils;
import com.naromil.qcsimple.logic.UnitExportContext;
import net.querz.nbt.tag.CompoundTag;

import java.util.Map;

/**
 * The framework block: the 9x9x9 shell every QC Unit is built from.
 *
 * <p>It owns the corner cubes (three boundary coordinates), the +/-2 axial extensions growing out
 * of them, the outline transitions into diagonally empty neighbours, and the shell voxels that
 * carry two boundary coordinates.
 */
public class FrameworkBlock extends BlockComponent {

    public static final String DEFAULT_ID = "minecraft:polished_deepslate";

    @Override
    public void applyDefaultConfig() {
        setBlockId(DEFAULT_ID);
    }

    @Override
    public void exportVoxel(UnitExportContext ctx, int x, int y, int z, int boundaryCount) {
        if (boundaryCount == 3) {
            exportCorner(ctx, x, y, z);
        } else if (boundaryCount == 2) {
            ctx.putIfAbsent(ctx.abs(x, y, z), getBlockId());
        }
    }

    /** A corner cube: the voxel itself, its axial extensions and the transitions into empty neighbours. */
    private void exportCorner(UnitExportContext ctx, int x, int y, int z) {
        Point3D pos = ctx.abs(x, y, z);
        ctx.put(pos, getBlockId());

        for (int i = -2; i <= 2; ++i) {
            ctx.putIfAbsent(new Point3D(pos.x() + i, pos.y(), pos.z()), getBlockId());
        }
        for (int j = -2; j <= 2; ++j) {
            ctx.putIfAbsent(new Point3D(pos.x(), pos.y() + j, pos.z()), getBlockId());
        }
        for (int k = -2; k <= 2; ++k) {
            ctx.putIfAbsent(new Point3D(pos.x(), pos.y(), pos.z() + k), getBlockId());
        }

        // The transition cubes sit one voxel outside the boundary, so they only appear when the
        // neighbouring unit in that direction is missing.
        int xx = x == 0 ? -1 : 9;
        int yy = y == 0 ? -1 : 9;
        int zz = z == 0 ? -1 : 9;

        if (ctx.isFree(x, yy, zz)) {
            ctx.putIfAbsent(new Point3D(pos.x(), ctx.absY(yy), ctx.absZ(zz)), getBlockId());
        }
        if (ctx.isFree(xx, y, zz)) {
            ctx.putIfAbsent(new Point3D(ctx.absX(xx), pos.y(), ctx.absZ(zz)), getBlockId());
        }
        if (ctx.isFree(xx, yy, z)) {
            ctx.putIfAbsent(new Point3D(ctx.absX(xx), ctx.absY(yy), pos.z()), getBlockId());
        }
    }

    /**
     * Finds the origin voxel of the unit grid by looking for a 9x9x9 shell made of one single block
     * state, and adopts that state's name as this component's block id.
     *
     * @throws IllegalStateException when the structure contains no framework shell at all
     */
    public GridOrigin detectGrid(Map<Point3D, CompoundTag> blockMap) {
        int ox = -1, oy = -1, oz = -1;

        for (Map.Entry<Point3D, CompoundTag> entry : blockMap.entrySet()) {
            Point3D pos = entry.getKey();
            CompoundTag frameworkCandidate = entry.getValue();

            boolean flag = true;
            for (int i = 0; i < 9; i++)
                for (int j = 0; j < 9; j++)
                    for (int k = 0; k < 9; k++) {
                        if (SpatialUtils.getBoundaryCount(i, j, k) >= 2 && blockMap.get(new Point3D(pos.x() + i, pos.y() + j, pos.z() + k)) != frameworkCandidate) {
                            flag = false;
                            break;
                        }
                    }

            if (flag) {
                ox = pos.x() % 8;
                oy = pos.y() % 8;
                oz = pos.z() % 8;
                setBlockId(frameworkCandidate.getString("Name"));
                break;
            }
        }

        if (ox == -1) {
            throw new IllegalStateException("No framework block found");
        }
        return new GridOrigin(ox, oy, oz);
    }
}
