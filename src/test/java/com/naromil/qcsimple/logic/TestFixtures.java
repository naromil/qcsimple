package com.naromil.qcsimple.logic;

import com.naromil.qcsimple.geom.Point2D;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * The shared editor fixtures the golden tests run against: one layout that covers every component
 * of the export, so both the export and the import baseline describe the same structure.
 */
final class TestFixtures {

    private TestFixtures() {
    }

    /**
     * Fixture that covers every component of the export: framework, rows, columns, floors, outer
     * walls, inner walls, gates and inner columns, on three layers with neighbours, an L-shaped
     * group and an isolated unit.
     *
     * <p>The outer map is a {@link TreeMap} and each layer a {@link LinkedHashMap} so the iteration
     * order the converter sees is fixed by the fixture itself instead of hash table internals.
     */
    static Map<Integer, Map<Point2D, QCUnit>> buildFixtureLayers() {
        Map<Integer, Map<Point2D, QCUnit>> layers = new TreeMap<>();

        // Group 1 (layers 1 and 2): 2x2 unit block at (3,3)-(4,4) carrying every wall and gate case.
        Map<Point2D, QCUnit> layer1 = new LinkedHashMap<>(flaggedSquare(3, 3));
        Map<Point2D, QCUnit> layer2 = new LinkedHashMap<>(flaggedSquare(3, 3));

        // Group 2 (layer 3): isolated unit at (10,10), all four faces exposed.
        Map<Point2D, QCUnit> layer3 = new LinkedHashMap<>();
        layer3.put(new Point2D(10, 10), new QCUnit());

        // Group 3 (layer 1): L-shaped 3-unit group at (20,20),(21,20),(20,21), plain squares.
        layer1.put(new Point2D(20, 20), new QCUnit());
        layer1.put(new Point2D(21, 20), new QCUnit());
        layer1.put(new Point2D(20, 21), new QCUnit());

        // Group 4 (layer 2): 2x2 group of plain squares with no conflicting walls at (14,14)-(15,15),
        // so the inner column actually gets placed. Layer 2 only, to also cover a layer that has no
        // neighbour below it.
        layer2.put(new Point2D(14, 14), new QCUnit());
        layer2.put(new Point2D(15, 14), new QCUnit());
        layer2.put(new Point2D(14, 15), new QCUnit());
        layer2.put(new Point2D(15, 15), new QCUnit());

        layers.put(1, layer1);
        layers.put(2, layer2);
        layers.put(3, layer3);
        return layers;
    }

    /**
     * 2x2 unit block (with the given north-west corner) carrying, for both shared wall
     * orientations, one plain wall pair and one gate pair:
     * <ul>
     *   <li>north-west/north-east: wall without gate, on both faces (inner wall east),</li>
     *   <li>north-west/south-west: wall without gate, on both faces (inner wall south),</li>
     *   <li>south-west/south-east: wall with gate, on both faces (gate east),</li>
     *   <li>north-east/south-east: wall with gate, on both faces (gate south).</li>
     * </ul>
     * The walls also make this square conflicting for the inner column, which is covered by group 4.
     */
    static Map<Point2D, QCUnit> flaggedSquare(int dx, int dz) {
        QCUnit northWest = new QCUnit();
        northWest.setWallE(true);
        northWest.setWallS(true);

        QCUnit northEast = new QCUnit();
        northEast.setWallW(true);
        northEast.setWallS(true);
        northEast.setGateS(true);

        QCUnit southWest = new QCUnit();
        southWest.setWallN(true);
        southWest.setWallE(true);
        southWest.setGateE(true);

        QCUnit southEast = new QCUnit();
        southEast.setWallW(true);
        southEast.setGateW(true);
        southEast.setWallN(true);
        southEast.setGateN(true);

        Map<Point2D, QCUnit> units = new LinkedHashMap<>();
        units.put(new Point2D(dx, dz), northWest);
        units.put(new Point2D(dx + 1, dz), northEast);
        units.put(new Point2D(dx, dz + 1), southWest);
        units.put(new Point2D(dx + 1, dz + 1), southEast);
        return units;
    }
}
