package io.github.naromil.qcsimple.main;

import io.github.naromil.qcsimple.data.NBTIO;
import io.github.naromil.qcsimple.data.BlockConfig;
import io.github.naromil.qcsimple.editor.EditorController;
import io.github.naromil.qcsimple.editor.EditorState;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import net.querz.nbt.tag.CompoundTag;

import java.io.File;
import java.io.IOException;

public class MainController {

    // JavaFX automatically links this variable to the embedded Canvas Controller
    // The naming convention MUST exactly be: [fx:id] + "Controller"
    @FXML
    private EditorController editorComponentController;

    @FXML
    public void initialize() {
        // Initialize the sub-canvas with the default Layer 1 empty map on boot
        syncCanvasWithCurrentLayer();
        // Now you can orchestrate the canvas from the main controller
        System.out.println("Main layout loaded, canvas controller linked successfully.");
    }

    /**
     * Call this from your MainApp class right after loading the scene to capture global shortcuts!
     */
    public void setupGlobalShortcuts(javafx.scene.Scene scene) {
        scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            switch (event.getCode()) {
                case PAGE_UP -> {
                    onLayerUpAction();
                    event.consume();
                }
                case PAGE_DOWN -> {
                    onLayerDownAction();
                    event.consume();
                }
            }
        });
    }

    @FXML
    private Label layerLabel;

    @FXML
    protected void onLayerUpAction() {
        changeLayer(1);
    }

    @FXML
    protected void onLayerDownAction() {
        // Prevent dropping into negative or zero layer indices if you want strict positive limits
        if (EditorState.getInstance().getCurrentLayerIndex() > 1) {
            changeLayer(-1);
        }
    }

//    @FXML
//    protected void onLayerCopyUpAction() {
//        // TODO: Implement layer up action that also copies the current layer data to the new layer
//    }

    private void changeLayer(int offset) {
        EditorState state = EditorState.getInstance();
        int newLayer = state.getCurrentLayerIndex() + offset;

        // 1. Commit new depth level to memory
        state.setCurrentLayerIndex(newLayer);

        // 2. Update ToolBar Interface label text
        layerLabel.setText("Layer: " + newLayer);

        // 3. Hot-swap the rendering target array context
        syncCanvasWithCurrentLayer();
    }

    private void syncCanvasWithCurrentLayer() {
        EditorState state = EditorState.getInstance();
        int activeLayer = state.getCurrentLayerIndex();

        // Fetch the unique 2D grid pointer belonging to this level and force paint
        editorComponentController.loadLayerData(state.getMapForLayer(activeLayer));
    }

    public int getCellSize() {
        return editorComponentController.getCellSize();
    }

    @FXML
    protected void onOpenConfigMenuAction() {
        try {
            // 1. Load the Configuration Window layout
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("block-config.fxml"));
            Scene scene = new Scene(fxmlLoader.load());

            // 2. Create a totally fresh operating system Stage
            Stage configStage = new Stage();
            configStage.setTitle("Configure Block IDs");
            configStage.setScene(scene);

            // 3. Set Window Modality (Crucial for settings dialogs)
            // This prevents clicking or breaking anything in the background main editor canvas
            Stage mainStage = (Stage) Stage.getWindows().stream().filter(Window::isShowing).findFirst().orElse(null);
            configStage.initModality(Modality.WINDOW_MODAL);
            configStage.initOwner(mainStage);

            // Prevent user resizing if you want a fixed form layout look
            configStage.setResizable(false);

            // 4. Fire open the window!
            configStage.showAndWait();

        } catch (IOException e) {
            System.err.println("Failed to load block configuration window: " + e.getMessage());
//            e.printStackTrace();
        }
    }

    @FXML
    protected void onApplyDefaultConfigAction() {
        BlockConfig.applyDefaultConfig();
        EditorState.getInstance().setDirty(true);
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
                CompoundTag parsedNbt = NBTIO.readNbt(selectedFile);

                // 4. Commit to the global State
                EditorState state = EditorState.getInstance();
                state.setCurrentFile(selectedFile);
                state.setRootCompoundTag(parsedNbt);
                state.syncMap();
                state.setDirty(false);

                System.out.println("Successfully loaded: " + selectedFile.getName());

                syncCanvasWithCurrentLayer();

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
        if(!state.isDirty()) return;

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
        if (!state.isDirty()) {
//            showErrorDialog("Save Error", "No structure data found in memory to save.");
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
            // If DataConverter is not configured, apply default config
            if(!BlockConfig.isConfigured()) {
                System.out.println("DataConverter is not configured. Applying default config.");
                BlockConfig.applyDefaultConfig();
            }

            EditorState state = EditorState.getInstance();
            state.syncRootCompoundTag();
            CompoundTag nbtData = state.getRootCompoundTag();

            NBTIO.writeNbt(nbtData, file);

            // Update state info
            state.setCurrentFile(file);
            state.setDirty(false);
            System.out.println("Successfully saved data to: " + file.getAbsolutePath());

        } catch (Exception e) {
//            e.printStackTrace(); // Keep this on while debugging!
            showErrorDialog("Save Failed", "Could not commit structure data to disk:\n" + e.getMessage());
        }
    }
}