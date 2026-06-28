package io.github.naromil.qcsimple.data;

import net.querz.nbt.tag.CompoundTag;

public class RotationUtils {
    /**
     * Deep clones a block state and updates directional properties based on rotation.
     */
    static CompoundTag rotateBlockState(CompoundTag originalState, String rotation) {
        if (rotation.equals("0") || !originalState.containsKey("Properties")) {
            return originalState; // No rotation needed, or block has no directional states
        }

        // 1. Deep clone to prevent corrupting the global palette
        CompoundTag newState = originalState.clone();
        CompoundTag properties = newState.getCompoundTag("Properties");

        // 2. Rotate Facing (Stairs, Chests, Furnaces, Torches, etc.)
        if (properties.containsKey("facing")) {
            String currentFacing = properties.getString("facing");
            properties.putString("facing", rotateFacing(currentFacing, rotation));
        }

        // 3. Rotate Axis (Logs, Pillars, Basalt)
        if (properties.containsKey("axis")) {
            String currentAxis = properties.getString("axis");
            if (rotation.equals("90") || rotation.equals("270")) {
                if (currentAxis.equals("x")) properties.putString("axis", "z");
                else if (currentAxis.equals("z")) properties.putString("axis", "x");
            }
        }

        // 4. Rotate Glass Pane connections (and similar blocks like iron bars, fences, walls)
        if (properties.containsKey("north") && properties.containsKey("south") &&
                properties.containsKey("east") && properties.containsKey("west")) {

            String north = properties.getString("north");
            String south = properties.getString("south");
            String east = properties.getString("east");
            String west = properties.getString("west");

            switch (rotation) {
                case "90" -> { // 90° clockwise
                    properties.putString("north", west);
                    properties.putString("east", north);
                    properties.putString("south", east);
                    properties.putString("west", south);
                }
                case "180" -> { // 180° clockwise
                    properties.putString("north", south);
                    properties.putString("east", west);
                    properties.putString("south", north);
                    properties.putString("west", east);
                }
                case "270" -> { // 270° clockwise
                    properties.putString("north", east);
                    properties.putString("east", south);
                    properties.putString("south", west);
                    properties.putString("west", north);
                }
            }
        }

        return newState;
    }

    /**
     * Maps a cardinal direction string to its new direction after a Clockwise rotation.
     */
    private static String rotateFacing(String facing, String rotation) {
        return switch (rotation) {
            case "90" -> switch (facing) { // 90 Clockwise
                case "north" -> "east";
                case "east" -> "south";
                case "south" -> "west";
                case "west" -> "north";
                default -> facing; // Ignores "up" and "down"
            };
            case "180" -> switch (facing) { // 180 Clockwise
                case "north" -> "south";
                case "east" -> "west";
                case "south" -> "north";
                case "west" -> "east";
                default -> facing;
            };
            case "270" -> switch (facing) { // 270 Clockwise
                case "north" -> "west";
                case "east" -> "north";
                case "south" -> "east";
                case "west" -> "south";
                default -> facing;
            };
            default -> facing;
        };
    }

    static Point3D getUnrotatedPos(String rotation, int i, int j, int k) throws IllegalStateException {
        return switch (rotation) {
            case "0" -> new Point3D(i, j, k);
            case "90" -> new Point3D(k, j, -i);
            case "180" -> new Point3D(-i, j, -k);
            case "270" -> new Point3D(-k, j, i);
            default -> throw new IllegalStateException("Unexpected rotation value: " + rotation);
        };
    }
}
