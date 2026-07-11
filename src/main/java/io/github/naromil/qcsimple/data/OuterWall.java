package io.github.naromil.qcsimple.data;

import io.github.naromil.qcsimple.logic.NBTGenerator;
import net.querz.nbt.tag.CompoundTag;

public class OuterWall extends QCComponent {
    public OuterWall() { super(); }
    public OuterWall(String path, CompoundTag tag) {
        super(path, tag);
    }

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

    public static boolean isValidSize(int x, int y, int z) {
        return x == 7 && y == 7 && z <= 3;
    }
}
