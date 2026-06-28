package io.github.naromil.qcsimple.data;

public class QCUnit {
    private boolean wallN, wallW, wallS, wallE;

    public QCUnit() {
        wallN = wallW = wallS = wallE = false;
    }

    public QCUnit(QCUnit unit) {
        wallN = unit.wallN;
        wallW = unit.wallW;
        wallS = unit.wallS;
        wallE = unit.wallE;
    }

    public void setWallN(boolean val) { wallN = val; }
    public void setWallS(boolean val) { wallS = val; }
    public void setWallW(boolean val) { wallW = val; }
    public void setWallE(boolean val) { wallE = val; }

    // Getters for rendering
    public boolean hasWallN() { return wallN; }
    public boolean hasWallS() { return wallS; }
    public boolean hasWallW() { return wallW; }
    public boolean hasWallE() { return wallE; }
}
