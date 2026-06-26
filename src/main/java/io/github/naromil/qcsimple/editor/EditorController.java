package io.github.naromil.qcsimple.editor;

import io.github.naromil.qcsimple.data.QCUnit;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.Map;

public class EditorController {

    @FXML
    private Canvas editorCanvas;

    private GraphicsContext gc;

    private final int cellSize = 32, gridWidth = 32, gridHeight = 32;

    // 1. Updated: The Map now holds your abstract QCUnit objects
    private Map<Point2D, QCUnit> currentLayerData = new HashMap<>();

    @FXML
    public void initialize() {
        gc = editorCanvas.getGraphicsContext2D();
        editorCanvas.setWidth(gridWidth * cellSize);
        editorCanvas.setHeight(gridHeight * cellSize);

        setupMouseEvents();
        redraw();
    }

    public void loadLayerData(Map<Point2D, QCUnit> mapForLayer) {
        currentLayerData = mapForLayer;
        redraw();
    }

    private void setupMouseEvents() {
        editorCanvas.setOnMousePressed(this::handleMouseInput);
        editorCanvas.setOnMouseDragged(this::handleMouseInput);
    }

    private void handleMouseInput(MouseEvent event) {
        int gridX = (int) (event.getX() / cellSize);
        int gridZ = (int) (event.getY() / cellSize);

        if (gridX >= 0 && gridX < gridWidth && gridZ >= 0 && gridZ < gridHeight) {

            Point2D clickedPoint = new Point2D(gridX, gridZ);

            if (event.getButton() == MouseButton.PRIMARY) {

                if (event.isShiftDown()) {
                    // Shift + Left-Click: Erase wall placement
                    handleWallPlacement(event.getX(), event.getY(), gridX, gridZ, false);

                } else {
                    // Left-Click: Erase block
                    currentLayerData.remove(clickedPoint);
                }

            } else if (event.getButton() == MouseButton.SECONDARY) {

                if (event.isShiftDown()) {
                    // Shift + Right-Click: Place wall placement
                    handleWallPlacement(event.getX(), event.getY(), gridX, gridZ, true);

                } else {
                    // Regular Right Click: Place block
                    currentLayerData.put(clickedPoint, new QCUnit());
                }
            }

            EditorState state = EditorState.getInstance();
            state.setDirty(true);
            redraw();
        }
    }

    public int getCellSize() { return cellSize; }

    private void handleWallPlacement(double mouseX, double mouseY, int gridX, int gridZ, boolean newState) {
        // 1. Local coordinates inside the cell
        double localX = mouseX % cellSize;
        double localZ = mouseY % cellSize;

        // 2. Distance to edges
        double distN = localZ;
        double distS = cellSize - localZ;
        double distW = localX;
        double distE = cellSize - localX;
        double min = Math.min(Math.min(distN, distS), Math.min(distW, distE));

        // 3. Get the existing block at the clicked cell – do NOT create one
        QCUnit centerUnit = currentLayerData.get(new Point2D(gridX, gridZ));
        if (centerUnit == null) {
            return;   // no block here → cannot attach a wall
        }

        // 4. For each edge that the click targets, toggle only if the neighbor exists
        if (min > cellSize / 4.0 || min == distN) {
            tryToggleWall(centerUnit, gridX, gridZ - 1, newState, Direction.NORTH);
        }
        if (min > cellSize / 4.0 || min == distS) {
            tryToggleWall(centerUnit, gridX, gridZ + 1, newState, Direction.SOUTH);
        }
        if (min > cellSize / 4.0 || min == distW) {
            tryToggleWall(centerUnit, gridX - 1, gridZ, newState, Direction.WEST);
        }
        if (min > cellSize / 4.0 || min == distE) {
            tryToggleWall(centerUnit, gridX + 1, gridZ, newState, Direction.EAST);
        }
    }

    /**
     * Attempts to toggle a shared wall between centerUnit and the neighbor at (nx, nz).
     * Does nothing if the neighbor block is missing or out of bounds.
     */
    private void tryToggleWall(QCUnit centerUnit, int nx, int nz, boolean wallState, Direction dir) {
        // Bounds check
        if (nx < 0 || nx >= gridWidth || nz < 0 || nz >= gridHeight) {
            return;
        }

        // Get neighbor without creating
        QCUnit neighbour = currentLayerData.get(new Point2D(nx, nz));
        if (neighbour == null) {
            return;   // no block to share the wall with
        }

        // Toggle on the center cell
        switch (dir) {
            case NORTH -> centerUnit.setWallN(wallState);
            case SOUTH -> centerUnit.setWallS(wallState);
            case WEST  -> centerUnit.setWallW(wallState);
            case EAST  -> centerUnit.setWallE(wallState);
        }

        // Toggle the inverse on the neighbor
        switch (dir) {
            case NORTH -> neighbour.setWallS(wallState);
            case SOUTH -> neighbour.setWallN(wallState);
            case WEST  -> neighbour.setWallE(wallState);
            case EAST  -> neighbour.setWallW(wallState);
        }
    }

    // Add a simple enum for readability (optional)
    private enum Direction { NORTH, SOUTH, WEST, EAST }

    public void redraw() {
        gc.clearRect(0, 0, editorCanvas.getWidth(), editorCanvas.getHeight());

        // 3. Updated: Draw the abstract QCUnits
        for (Map.Entry<Point2D, QCUnit> entry : currentLayerData.entrySet()) {
            Point2D pos = entry.getKey();
            QCUnit unit = entry.getValue();

            double startX = pos.x() * cellSize;
            double startZ = pos.z() * cellSize;

            // --- DRAW BASE BLOCK ---
            gc.setFill(Color.web("#5b5b5b"));
            gc.fillRect(startX, startZ, cellSize, cellSize);
            gc.setFill(Color.web("#757575"));
            gc.fillRect(startX + 2, startZ + 2, cellSize - 4, cellSize - 4);

            // --- DRAW WALLS ---
            gc.setStroke(Color.web("#222222")); // Dark, thick color for walls
            gc.setLineWidth(4.0); // Thick lines so they are highly visible

            if (unit.hasWallN()) {
                gc.strokeLine(startX, startZ, startX + cellSize, startZ);
            }
            if (unit.hasWallS()) {
                gc.strokeLine(startX, startZ + cellSize, startX + cellSize, startZ + cellSize);
            }
            if (unit.hasWallW()) {
                gc.strokeLine(startX, startZ, startX, startZ + cellSize);
            }
            if (unit.hasWallE()) {
                gc.strokeLine(startX + cellSize, startZ, startX + cellSize, startZ + cellSize);
            }
        }

        // Draw the Grid Lines
        gc.setStroke(Color.LIGHTGRAY);
        gc.setLineWidth(1.0);

        for (int x = 0; x <= gridWidth; x++) {
            double pixelX = x * cellSize;
            gc.strokeLine(pixelX, 0, pixelX, editorCanvas.getHeight());
        }

        for (int z = 0; z <= gridHeight; z++) {
            double pixelZ = z * cellSize;
            gc.strokeLine(0, pixelZ, editorCanvas.getWidth(), pixelZ);
        }
    }
}
