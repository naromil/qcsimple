package com.naromil.qcsimple.logic;

/**
 * The cell of the editor grid an imported layout is centred on. Half of the editor's 32x32 cell
 * canvas, kept here so the import does not have to reach into the JavaFX canvas for it.
 */
public final class UnitGrid {

    public static final int CENTER_X = 16;
    public static final int CENTER_Z = 16;

    private UnitGrid() {
    }
}
