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

public class EditorCanvasController {

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
                // Left Click: Erase block
                currentLayerData.remove(clickedPoint);

            } else if (event.getButton() == MouseButton.SECONDARY) {

                if (event.isShiftDown() && event.getEventType() == MouseEvent.MOUSE_PRESSED) {
                    // Shift + Right Click: Trigger wall placement (only on initial press)
                    handleWallPlacement(event.getX(), event.getY(), gridX, gridZ);

                } else if (!event.isShiftDown()) {
                    // Regular Right Click: Place block (blocks if Shift is held)
                    currentLayerData.put(clickedPoint, new QCUnit());
                }
            }

            redraw();
        }
    }

    private void handleWallPlacement(double mouseX, double mouseY, int gridX, int gridZ) {
        // 1. Get the exact coordinates inside the specific cell (values between 0.0 and 31.99)
        double localX = mouseX % cellSize;
        double localZ = mouseY % cellSize;

        // 2. Calculate distance to all four edges
        double distN = localZ;
        double distS = cellSize - localZ;
        double distW = localX;
        double distE = cellSize - localX;

        // Find the absolute closest edge
        double min = Math.min(Math.min(distN, distS), Math.min(distW, distE));

        // 3. Ensure a block exists here to attach a wall to (or create a blank one)
        QCUnit centerUnit = currentLayerData.computeIfAbsent(new Point2D(gridX, gridZ), k -> new QCUnit());

        // 4. Toggle the corresponding wall and sync the neighbor
        if (min == distN) {
            boolean newState = centerUnit.toggleWallN();
            syncNeighborWall(gridX, gridZ - 1, newState, "SOUTH");
        }
        else if (min == distS) {
            boolean newState = centerUnit.toggleWallS();
            syncNeighborWall(gridX, gridZ + 1, newState, "NORTH");
        }
        else if (min == distW) {
            boolean newState = centerUnit.toggleWallW();
            syncNeighborWall(gridX - 1, gridZ, newState, "EAST");
        }
        else if (min == distE) {
            boolean newState = centerUnit.toggleWallE();
            syncNeighborWall(gridX + 1, gridZ, newState, "WEST");
        }
    }

    private void syncNeighborWall(int neighborX, int neighborZ, boolean wallState, String edgeToUpdate) {
        // Don't draw outside the bounds of the map
        if (neighborX < 0 || neighborX >= gridWidth || neighborZ < 0 || neighborZ >= gridHeight) return;

        // Ensure the neighbor exists in memory
        Point2D neighborPoint = new Point2D(neighborX, neighborZ);
        QCUnit neighborUnit = currentLayerData.computeIfAbsent(neighborPoint, k -> new QCUnit());

        // Apply the inverse wall to the neighbor
        switch (edgeToUpdate) {
            case "NORTH" -> neighborUnit.setWallN(wallState);
            case "SOUTH" -> neighborUnit.setWallS(wallState);
            case "WEST"  -> neighborUnit.setWallW(wallState);
            case "EAST"  -> neighborUnit.setWallE(wallState);
        }
    }

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
