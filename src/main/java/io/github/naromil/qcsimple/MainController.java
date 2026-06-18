package io.github.naromil.qcsimple;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import net.querz.nbt.tag.CompoundTag;

import java.io.File;

public class MainController {
    @FXML
    protected TextField blocFramework, blocColumn, blocRow;

    @FXML
    private StackPane renderPane;

    @FXML
    public void initialize() {
        EditorController.initEditorScene(renderPane);
        System.out.println("initEditorScene completed!");
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