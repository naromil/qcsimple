package io.github.naromil.qcsimple.data;

import io.github.naromil.qcsimple.editor.Point2D;
import io.github.naromil.qcsimple.logic.NBTGenerator;
import io.github.naromil.qcsimple.logic.Point3D;
import io.github.naromil.qcsimple.logic.QCUnit;
import net.querz.nbt.tag.CompoundTag;

import java.util.Map;

public class Gate extends QCComponent {
    public Gate() { super(); }
    public Gate(String path, CompoundTag tag) {
        super(path, tag);
    }

    public void applyDefaultConfig() {
        setTag(NBTGenerator.generateSimpleStructureTag("minecraft:deepslate_bricks", new byte[][][]{
                {{0, 0, 0}, {0, 0, 0}, {0, 0, 0}, {-1, 0, -1}, {-1, 0, -1}, {-1, 0, -1}, {-1, 0, -1}},
                {{-1, 0, -1}, {-1, 0, -1}, {-1, 0, -1}, {-1, 0, -1}, {-1, 0, -1}, {-1, 0, -1}, {-1, 0, -1}},
                {{-1, -1, -1}, {-1, -1, -1}, {-1, -1, -1}, {-1, -1, -1}, {-1, 0, -1}, {-1, 0, -1}, {0, 0, 0}},
                {{-1, -1, -1}, {-1, -1, -1}, {-1, -1, -1}, {-1, -1, -1}, {-1, 0, -1}, {-1, 0, -1}, {0, 0, 0}},
                {{-1, -1, -1}, {-1, -1, -1}, {-1, -1, -1}, {-1, -1, -1}, {-1, 0, -1}, {-1, 0, -1}, {0, 0, 0}},
                {{-1, 0, -1}, {-1, 0, -1}, {-1, 0, -1}, {-1, 0, -1}, {-1, 0, -1}, {-1, 0, -1}, {-1, 0, -1}},
                {{0, 0, 0}, {0, 0, 0}, {0, 0, 0}, {-1, 0, -1}, {-1, 0, -1}, {-1, 0, -1}, {-1, 0, -1}}
        }));
        setPath("[Generated Default: Deepslate Bricks]");
    }

    public static boolean isValidSize(int x, int y, int z) {
        return x == 7 && y == 7 && (z == 1 || z == 3);
    }

    public static boolean canBePlacedSouth(Map<Point2D, QCUnit> currentLayerMap, int dx, int dz) {
        QCUnit uNW = currentLayerMap.get(new Point2D(dx, dz));
        QCUnit uSW = currentLayerMap.get(new Point2D(dx, dz + 1));

        if (uNW != null && uSW != null && uSW.hasWallN() && uNW.hasWallS()) {
            return uSW.isGateN() && uNW.isGateS();
        }
        return false;
    }

    public static Point3D getRelativePosSouth(int dx, int dy, int dz, Gate gate) {
        int intersectX = dx * 8;
        int intersectY = dy * 8;
        int intersectZ = dz * 8 + 8;

        int zDepth = gate.getZ();
        int outShift = (zDepth == 3) ? 1 : 0;

        return new Point3D(intersectX + 1, intersectY + 1, intersectZ - outShift);
    }

    public static boolean canBePlacedEast(Map<Point2D, QCUnit> currentLayerMap, int dx, int dz) {
        QCUnit uNW = currentLayerMap.get(new Point2D(dx, dz));
        QCUnit uNE = currentLayerMap.get(new Point2D(dx + 1, dz));

        if (uNW != null && uNE != null && uNE.hasWallW() && uNW.hasWallE()) {
            return uNE.isGateW() && uNW.isGateE();
        }
        return false;
    }

    public static Point3D getRelativePosEast(int dx, int dy, int dz, Gate gate) {
        int intersectX = dx * 8 + 8;
        int intersectY = dy * 8;
        int intersectZ = dz * 8;

        int zDepth = gate.getZ();
        int outShift = (zDepth == 3) ? 1 : 0;

        return new Point3D(intersectX + outShift, intersectY + 1, intersectZ + 1);
    }
}

