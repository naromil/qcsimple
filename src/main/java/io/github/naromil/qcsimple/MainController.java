package io.github.naromil.qcsimple;

import javafx.fxml.FXML;
import javafx.geometry.Point3D;
import javafx.scene.*;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.input.PickResult;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.transform.Rotate;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import net.querz.nbt.tag.CompoundTag;

import java.io.File;

public class MainController {

    @FXML
    protected TextField blocFramework, blocColumn, blocRow;

    @FXML private StackPane renderPane;

    private Group worldRoot;
    private Group cameraPivot;
    private double mousePosX, mousePosY;
    private double mouseOldX, mouseOldY;

    @FXML
    public void initialize() {
        init3DScene();
        System.out.println("init3DScene completed!");

        // Sample block
        for(int i=-2; i<=2; ++i){
            for(int j=-2; j<=2; ++j){
                for(int k=-2; k<=2; ++k){
                    buildStructure(i, j, k, "minecraft:glass");
                }
            }
        }
    }

    private void init3DScene() {
        worldRoot = new Group();

        // 1. Setup Camera and Pivot
        cameraPivot = new Group();
        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setTranslateZ(-50); // Pull the camera back so we can see the center
        camera.setNearClip(0.1);
        camera.setFarClip(1000.0);
        cameraPivot.getChildren().add(camera);
        worldRoot.getChildren().add(cameraPivot);

        // 2. Create the SubScene
        // Using BALANCED antialiasing keeps the edges crisp without killing performance
        SubScene subScene = new SubScene(worldRoot, 800, 600, true, SceneAntialiasing.BALANCED);
        subScene.setFill(Color.web("#3c3f41")); // A nice dark editor background
        subScene.setCamera(camera);

        // Bind the SubScene size to the StackPane so it resizes with the window
        subScene.widthProperty().bind(renderPane.widthProperty());
        subScene.heightProperty().bind(renderPane.heightProperty());

        // 3. Add to UI
        renderPane.getChildren().add(subScene);

        // 4. Attach interactive controls
        setupMouseControls(subScene);
    }

    private void setupMouseControls(SubScene subScene) {
        Rotate rotateX = new Rotate(0, Rotate.X_AXIS);
        Rotate rotateY = new Rotate(0, Rotate.Y_AXIS);
        cameraPivot.getTransforms().addAll(rotateX, rotateY);

        subScene.setOnMousePressed(event -> {
            mousePosX = event.getSceneX();
            mousePosY = event.getSceneY();
            mouseOldX = event.getSceneX();
            mouseOldY = event.getSceneY();
        });

        subScene.setOnMouseDragged(event -> {
            mouseOldX = mousePosX;
            mouseOldY = mousePosY;
            mousePosX = event.getSceneX();
            mousePosY = event.getSceneY();
            double mouseDeltaX = (mousePosX - mouseOldX);
            double mouseDeltaY = (mousePosY - mouseOldY);

            // Adjust rotation based on drag distance
            if (event.isPrimaryButtonDown()) { // Left click drag to orbit
                rotateX.setAngle(rotateX.getAngle() - mouseDeltaY * 0.5); // Pitch
                rotateY.setAngle(rotateY.getAngle() + mouseDeltaX * 0.5); // Yaw
            }
        });

        subScene.setOnMouseClicked(event -> {
            // Prevent building/destroying if the user was just dragging the camera around
            if (!event.isStillSincePress()) return;

            PickResult pickResult = event.getPickResult();
            Node intersectedNode = pickResult.getIntersectedNode();

            // Check if the raycast actually hit a Box (and not empty background space)
            if (intersectedNode instanceof Box) {
                Box clickedBlock = (Box) intersectedNode;

                if (event.getButton() == MouseButton.PRIMARY) {
                    // LEFT CLICK: Destroy block
                    // destroyBlock(clickedBlock);
                    worldRoot.getChildren().remove(clickedBlock);
                    // TODO
                }
                else if (event.getButton() == MouseButton.SECONDARY) {
                    // RIGHT CLICK: Build new block
                    // buildBlock(clickedBlock, pickResult.getIntersectedPoint());
                }
            }
            // Check if the raycast doesn't hit a Box but there are no Box present
            else {
                buildStructure(0, 0, 0, blocFramework.getText());
                // TODO
            }
        });
    }

    public void buildStructure(int x, int y, int z, String blockName) {
        // 1x1x1 unit size for a standard block
        Box block = new Box(1, 1, 1);

        // Position the block. Notice the negative 'y' to flip the axis!
        block.setTranslateX(x);
        block.setTranslateY(-y);
        block.setTranslateZ(z);

        // Apply a basic color based on the block name for testing
        PhongMaterial material = new PhongMaterial();
        if (blockName.contains("stone")) material.setDiffuseColor(Color.GRAY);
        else if (blockName.contains("wood") || blockName.contains("log")) material.setDiffuseColor(Color.BROWN);
        else material.setDiffuseColor(Color.MAGENTA); // Missing texture fallback

        block.setMaterial(material);

        // Add interactivity to individual blocks
        block.setOnMouseClicked(event -> {
            System.out.println("Clicked block at: " + x + ", " + y + ", " + z);
            // Here you can change the material to show it is selected!
        });

        worldRoot.getChildren().add(block);
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