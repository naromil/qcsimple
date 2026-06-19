package io.github.naromil.qcsimple.main;

import io.github.naromil.qcsimple.data.QCUnit;
import io.github.naromil.qcsimple.editor.EditorState;
import io.github.naromil.qcsimple.data.NBTHandler;
import io.github.naromil.qcsimple.editor.Point2D;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import net.querz.nbt.tag.CompoundTag;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class MainController {
    @FXML
    protected TextField blocFramework, blocColumn, blocRow;

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

    private void setupMouseEvents() {
        editorCanvas.setOnMousePressed(this::handleMouseInput);
        editorCanvas.setOnMouseDragged(this::handleMouseInput);
    }

    private void handleMouseInput(MouseEvent event) {
        int gridX = (int) (event.getX() / cellSize);
        int gridZ = (int) (event.getY() / cellSize);

        if (gridX >= 0 && gridX < gridWidth && gridZ >= 0 && gridZ < gridHeight) {

            Point2D clickedPoint = new Point2D(gridX, gridZ);

            if (event.isSecondaryButtonDown()) {
                // 2. Updated: Place a fresh QCUnit into the map
                // We use put() so dragging over an existing block simply overwrites/updates it
                currentLayerData.put(clickedPoint, new QCUnit());
            } else if (event.isPrimaryButtonDown()) {
                currentLayerData.remove(clickedPoint);
            } else if (event.getButton() == MouseButton.MIDDLE && event.getEventType() == MouseEvent.MOUSE_PRESSED) {
                // Only trigger wall logic on the initial click, not while dragging
                handleWallPlacement(event.getX(), event.getY(), gridX, gridZ);
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

    private void redraw() {
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

    /**
     * This method triggers when the user clicks 'Help -> About'
     */
    @FXML
    protected void showAboutDialog() {
        // 1. Create an Information alert dialog box
        Alert alert = new Alert(Alert.AlertType.INFORMATION);

        // 2. Set the text properties
        alert.setTitle("About Quarterchunk Simple");
        alert.setHeaderText("Minecraft Quarterchunk Simple v1.0");
        alert.setContentText(
                """
                        An interactive 3D editor for Minecraft Quarterchunk structure files in .nbt format.
                        
                        Features:
                        • Source management panel
                        • Interactive 3D voxel renderer
                        • Structural rotation and editing tools"""
        );

        // 3. Display the dialog and block user interaction with the main window until closed
        alert.showAndWait();
    }

    @FXML
    protected void onOpenMenuAction() {
        // 1. Create and configure the File Chooser
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Minecraft NBT Structure");

        // Filter to only show .nbt or .schem files
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Minecraft Structure Files (*.nbt)", "*.nbt"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        // 2. Get the current active Stage window to anchor the dialog
        // (Assuming you have a reference or grab it from a UI node)
        Stage stage = (Stage) Window.getWindows().stream().filter(Window::isShowing).findFirst().orElse(null);
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            try {
                // 3. Read the file into memory using your chosen NBT Engine
                CompoundTag parsedNbt = NBTHandler.readNbt(selectedFile);

                // 4. Commit to the global State
                EditorState state = EditorState.getInstance();
                state.setCurrentFile(selectedFile);
                state.setRootCompoundTag((CompoundTag) parsedNbt);
                state.setDirty(false);

                System.out.println("Successfully loaded: " + selectedFile.getName());

                // TODO: Trigger your 3D Renderer to clear and rebuild its voxel mesh here!

            } catch (Exception e) {
                showErrorDialog("Failed to open File", "An error occurred while parsing the NBT file:\n" + e.getMessage());
            }
        }
    }

    private void showErrorDialog(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    protected void onSaveMenuAction() {
        EditorState state = EditorState.getInstance();

        // If there is no open file path, fallback to "Save As" behavior
        if (state.getCurrentFile().isEmpty()) {
            onSaveAsMenuAction();
        } else {
            saveNbtToFile(state.getCurrentFile().get());
        }
    }

    @FXML
    protected void onSaveAsMenuAction() {
        EditorState state = EditorState.getInstance();
        if (state.getRootCompoundTag() == null) {
            showErrorDialog("Save Error", "No structure data found in memory to save.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save NBT Structure As");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Minecraft Structure Files (*.nbt)", "*.nbt"));

        // Default the file name suggestion if available
        state.getCurrentFile().ifPresent(file -> fileChooser.setInitialFileName(file.getName()));

        Stage stage = (Stage) Window.getWindows().stream().filter(Window::isShowing).findFirst().orElse(null);
        File targetFile = fileChooser.showSaveDialog(stage);

        if (targetFile != null) {
            saveNbtToFile(targetFile);
        }
    }

    private void saveNbtToFile(File file) {
        try {
            EditorState state = EditorState.getInstance();
            CompoundTag nbtData = state.getRootCompoundTag();

            // 5. Serialize the memory object back to binary GZIP/NBT structure bytes
            NBTHandler.writeNbt(nbtData, file);

            // Update state info
            state.setCurrentFile(file);
            state.setDirty(false);
            System.out.println("Successfully saved data to: " + file.getAbsolutePath());

        } catch (Exception e) {
            showErrorDialog("Save Failed", "Could not commit structure data to disk:\n" + e.getMessage());
        }
    }
}