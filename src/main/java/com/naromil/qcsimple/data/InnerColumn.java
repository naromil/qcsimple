package com.naromil.qcsimple.data;

import com.naromil.qcsimple.geom.Point2D;
import com.naromil.qcsimple.geom.Point3D;
import com.naromil.qcsimple.logic.BlockNBTConverter;
import com.naromil.qcsimple.logic.NBTGenerator;
import com.naromil.qcsimple.logic.QCUnit;
import com.naromil.qcsimple.logic.UnitExportContext;
import com.naromil.qcsimple.logic.UnitImportContext;
import net.querz.nbt.tag.CompoundTag;

import java.util.Map;

/**
 * The inner column: a 3x7x3 structure placed on the crossing point of a 2x2 group of units that
 * share no inner walls and no gate.
 */
public class InnerColumn extends QCComponent {

    public InnerColumn() {
        super();
    }

    public InnerColumn(String path, CompoundTag tag) {
        super(path, tag);
    }

    @Override
    public void applyDefaultConfig() {
        setTag(NBTGenerator.generateSimpleStructureTag("minecraft:stripped_spruce_log", new byte[][][]{
                {{-1, 0, -1}, {-1, -1, -1}, {-1, -1, -1}, {-1, -1, -1}, {-1, -1, -1}, {-1, -1, -1}, {-1, 0, -1}},
                {{0, 0, 0}, {-1, 0, -1}, {-1, 0, -1}, {-1, 0, -1}, {-1, 0, -1}, {-1, 0, -1}, {0, 0, 0}},
                {{-1, 0, -1}, {-1, -1, -1}, {-1, -1, -1}, {-1, -1, -1}, {-1, -1, -1}, {-1, -1, -1}, {-1, 0, -1}},
        }));
        setPath("[Generated Default: Stripped Spruce Log]");
    }

    @Override
    public boolean isValidSize(int x, int y, int z) {
        return x == 3 && y == 7 && z == 3;
    }

    @Override
    public void exportUnit(UnitExportContext ctx) {
        if (isEmpty()) {
            return;
        }

        BlockPalette palette = ctx.palette();
        boolean isWallEmpty = palette.innerWall().isEmpty() && palette.gate().isEmpty();
        if (!canBePlaced(ctx.layerMap(), ctx.dx(), ctx.dz(), isWallEmpty)) {
            return;
        }
        ctx.putStructure(relativePos(ctx.dx(), ctx.dy(), ctx.dz()), getTag(), "0", false);
    }

    /**
     * Whether the crossing point of the four units around (dx, dz) can carry a column: the four
     * units have to exist and must not expose conflicting walls, unless no inner wall and no gate
     * is configured at all.
     */
    public boolean canBePlaced(Map<Point2D, QCUnit> layerMap, int dx, int dz, boolean isWallEmpty) {
        // We treat the current unit (dx, dz) as the North-West corner.
        QCUnit uNW = layerMap.get(new Point2D(dx, dz));
        QCUnit uNE = layerMap.get(new Point2D(dx + 1, dz));
        QCUnit uSW = layerMap.get(new Point2D(dx, dz + 1));
        QCUnit uSE = layerMap.get(new Point2D(dx + 1, dz + 1));

        // Verify all 4 units exist to form a complete square for the inner column
        if (uNW == null || uNE == null || uSW == null || uSE == null) {
            return false;
        }

        // 1. Check the internal cross for any conflicting walls
        boolean hasConflictingWalls =
                uNW.hasAnyE() || uNW.hasAnyS() ||
                        uNE.hasAnyW() || uNE.hasAnyS() ||
                        uSW.hasAnyE() || uSW.hasAnyN() ||
                        uSE.hasAnyW() || uSE.hasAnyN();

        // 2. Place the column exactly at the intersection: only when nothing conflicts, or when
        // neither an inner wall nor a gate is configured.
        return !hasConflictingWalls || isWallEmpty;
    }

    /** The intersection point converges at x=8, z=8 of the north-west unit. */
    public Point3D relativePos(int dx, int dy, int dz) {
        return new Point3D(dx * 8 + 7, dy * 8 + 1, dz * 8 + 7);
    }

    @Override
    public void extract(UnitImportContext ctx) {
        for (Map.Entry<Integer, Map<Point2D, QCUnit>> layerEntry : ctx.layers().entrySet()) {
            int dy = layerEntry.getKey() - 1;
            Map<Point2D, QCUnit> currentLayer = layerEntry.getValue();

            for (Map.Entry<Point2D, QCUnit> unitEntry : currentLayer.entrySet()) {
                int dx = unitEntry.getKey().x();
                int dz = unitEntry.getKey().z();
                QCUnit unit = unitEntry.getValue();

                int absX = ctx.absX(dx);
                int absY = ctx.absY(dy);
                int absZ = ctx.absZ(dz);

                QCUnit eastUnit = currentLayer.get(new Point2D(dx + 1, dz));
                QCUnit southUnit = currentLayer.get(new Point2D(dx, dz + 1));
                QCUnit southEastUnit = currentLayer.get(new Point2D(dx + 1, dz + 1));
                if (ctx.innerColumnExtracted() || eastUnit == null || southUnit == null || southEastUnit == null) {
                    continue;
                }

                boolean hasConflictingWalls =
                        unit.hasAnyE() || unit.hasAnyS() ||
                                eastUnit.hasAnyW() || eastUnit.hasAnyS() ||
                                southUnit.hasAnyE() || southUnit.hasAnyN() ||
                                southEastUnit.hasAnyW() || southEastUnit.hasAnyN();

                if (!hasConflictingWalls || (!ctx.innerWallExtracted() && !ctx.gateExtracted())) {
                    boolean hasInnerColumn = ctx.hasAnyBlock(
                            absX + 7, absY + 1, absZ + 7,
                            absX + 9, absY + 7, absZ + 9,
                            ctx.frameworkId());

                    if (hasInnerColumn) {
                        setExtracted(BlockNBTConverter.extractStructure(ctx.blockMap(),
                                absX + 7, absY + 1, absZ + 7,
                                absX + 9, absY + 7, absZ + 9,
                                "0"));
                        if (!isEmpty()) {
                            ctx.markInnerColumnExtracted();
                        }
                    }
                }
            }
        }
    }
}
