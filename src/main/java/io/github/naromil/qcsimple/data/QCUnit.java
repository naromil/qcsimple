package io.github.naromil.qcsimple.data;

public class QCUnit {
    private Boolean wallN, wallW, wallS, wallE;

    public QCUnit() {
        wallN = wallW = wallS = wallE = false;
    }

    public QCUnit(Boolean newN, Boolean newW, Boolean newS, Boolean newE) {
        wallN = newN;
        wallW = newW;
        wallS = newS;
        wallE = newE;
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
