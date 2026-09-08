package com.naromil.qcsimple.data;

import com.naromil.qcsimple.editor.Point2D;
import com.naromil.qcsimple.logic.NBTGenerator;
import com.naromil.qcsimple.logic.Point3D;
import com.naromil.qcsimple.logic.QCUnit;
import com.naromil.qcsimple.logic.*;
import net.querz.nbt.tag.CompoundTag;

import java.util.Map;

public class InnerColumn extends QCComponent {
    public InnerColumn() { super(); }
    public InnerColumn(String path, CompoundTag tag) {
        super(path, tag);
    }

    public void applyDefaultConfig() {
        setTag(NBTGenerator.generateSimpleStructureTag("minecraft:stripped_spruce_log", new byte[][][]{
                {{-1, 0, -1}, {-1, -1, -1}, {-1, -1, -1}, {-1, -1, -1}, {-1, -1, -1}, {-1, -1, -1}, {-1, 0, -1}},
                {{0, 0, 0}, {-1, 0, -1}, {-1, 0, -1}, {-1, 0, -1}, {-1, 0, -1}, {-1, 0, -1}, {0, 0, 0}},
                {{-1, 0, -1}, {-1, -1, -1}, {-1, -1, -1}, {-1, -1, -1}, {-1, -1, -1}, {-1, -1, -1}, {-1, 0, -1}},
        }));
        setPath("[Generated Default: Stripped Spruce Log]");
    }

    public static boolean isValidSize(int x, int y, int z) {
        return x == 3 && y == 7 && z == 3;
    }

    public static boolean canBePlaced(Map<Point2D, QCUnit> currentLayerMap, int dx, int dz, boolean isWallEmpty) {
        // We treat the current unit (dx, dz) as the North-West corner.
        QCUnit uNW = currentLayerMap.get(new Point2D(dx, dz));
        QCUnit uNE = currentLayerMap.get(new Point2D(dx + 1, dz));
        QCUnit uSW = currentLayerMap.get(new Point2D(dx, dz + 1));
        QCUnit uSE = currentLayerMap.get(new Point2D(dx + 1, dz + 1));

        // Verify all 4 units exist to form a complete square for the inner column
        if (uNW != null && uNE != null && uSW != null && uSE != null) {

            // 1. Check the internal cross for any conflicting walls
            // (Adjust the method names if your QCUnit getters are named differently)
            boolean hasConflictingWalls =
                    uNW.hasAnyE() || uNW.hasAnyS() ||
                            uNE.hasAnyW() || uNE.hasAnyS() ||
                            uSW.hasAnyE() || uSW.hasAnyN() ||
                            uSE.hasAnyW() || uSE.hasAnyN();

            // 2. Place the column exactly at the intersection
            // If no walls are conflicting or if NEITHER inner wall and gate are configured
            // (I could write a smarter method, but it would be more complicated)
            return !hasConflictingWalls || isWallEmpty;
        }
        return false;
    }

    public static Point3D getRelativePos(int dx, int dy, int dz) {
        // The intersection point converges at x=8, z=8 of the NW unit
        int intersectX = dx * 8 + 8;
        int intersectY = dy * 8;
        int intersectZ = dz * 8 + 8;
        return new Point3D(intersectX - 1, intersectY + 1, intersectZ - 1);
    }
}
