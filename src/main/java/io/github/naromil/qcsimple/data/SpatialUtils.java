package io.github.naromil.qcsimple.data;

import io.github.naromil.qcsimple.editor.Point2D;

import java.util.Map;

public class SpatialUtils {
    // 1. The persistent cache field
    private static int[][][] boundaryCache = null;

    // 2. The public lookup method that auto-computes when needed
    public static int getBoundaryCount(int x, int y, int z) {
        // Auto-compute on first use (Lazy Initialization)
        if (boundaryCache == null) {
            computeBoundaryCache();
        }

        // Return the cached value instantly
        return boundaryCache[x][y][z];
    }

    // 3. Private helper to build the data structure
    private static void computeBoundaryCache() {
        System.out.println("Initializing boundary count cache..."); // Optional log
        boundaryCache = new int[9][9][9];

        for (int x = 0; x < 9; x++) {
            for (int y = 0; y < 9; y++) {
                for (int z = 0; z < 9; z++) {
                    int count = 0;
                    if (x == 0 || x == 8) count++;
                    if (y == 0 || y == 8) count++;
                    if (z == 0 || z == 8) count++;

                    boundaryCache[x][y][z] = count;
                }
            }
        }
    }

    // Check if a relative position (x, y, z) in unit (dx, dy, dz) has another unit on it
    static boolean isValidPlacement(Map<Integer, Map<Point2D, QCUnit>> layers, int x, int y, int z, int dx, int dy, int dz) {
        // 1. Shift the block offset coordinates if the internal coordinate spills past the 0-8 boundaries
        if (x < 0) dx--;
        else if (x > 8) dx++;

        if (y < 0) dy--;
        else if (y > 8) dy++;

        if (z < 0) dz--;
        else if (z > 8) dz++;

        // 2. Base check: Does the current root chunk exist?
        boolean res = layersContains(layers, dx, dy, dz);

        // 3. Boundary Neighbor Verification:
        // If an internal coordinate sits exactly on an outer shell (0 or 8),
        // ensure the adjacent chunk in that specific direction also exists.

        // X-Boundaries
        if (x == 0) res |= layersContains(layers, dx - 1, dy, dz);
        if (x == 8) res |= layersContains(layers, dx + 1, dy, dz);

        // Y-Boundaries
        if (y == 0) res |= layersContains(layers, dx, dy - 1, dz);
        if (y == 8) res |= layersContains(layers, dx, dy + 1, dz);

        // Z-Boundaries
        if (z == 0) res |= layersContains(layers, dx, dy, dz - 1);
        if (z == 8) res |= layersContains(layers, dx, dy, dz + 1);

        return !res;
    }

    // Check if the map of QCUnit contains a Unit at a position
    static boolean layersContains(Map<Integer, Map<Point2D, QCUnit>> layers, int dx, int dy, int dz) {
        if (!layers.containsKey(dy)) return false;
        return layers.get(dy).containsKey(new Point2D(dx, dz));
    }
}
