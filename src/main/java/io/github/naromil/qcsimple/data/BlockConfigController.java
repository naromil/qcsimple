package io.github.naromil.qcsimple.data;

import io.github.naromil.qcsimple.editor.EditorCanvasController;
import io.github.naromil.qcsimple.main.MainController;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import net.querz.nbt.io.NBTUtil;
import net.querz.nbt.io.NamedTag;
import net.querz.nbt.tag.CompoundTag;
import net.querz.nbt.tag.IntTag;
import net.querz.nbt.tag.ListTag;

import java.io.File;

@SuppressWarnings("unchecked")
public class BlockConfigController {
    // String ID Fields
    @FXML
    protected TextField blocFramework, blocColumn, blocRow, blocFloor, blocWall;

    // NBT Path Display Fields
    @FXML
    protected TextField pathInnerWall, pathOuterWall, pathInnerColumn, pathRoof;

    // Temporary storage for parsed tags during this window session
    private CompoundTag tempInnerWallTag = null;
    private CompoundTag tempOuterWallTag = null;
    private CompoundTag tempInnerColumnTag = null;
    private CompoundTag tempRoofTag = null;

    @FXML
    public void initialize() {
        // 1. Load String IDs
        blocFramework.setText(DataConverter.frameworkId != null ? DataConverter.frameworkId : "");
        blocColumn.setText(DataConverter.columnId != null ? DataConverter.columnId : "");
        blocRow.setText(DataConverter.rowId != null ? DataConverter.rowId : "");
        blocFloor.setText(DataConverter.floorId != null ? DataConverter.floorId : "");
        blocWall.setText(DataConverter.wallId != null ? DataConverter.wallId : "");

        // 2. Load NBT File Paths (so the user sees what was previously selected)
        pathInnerWall.setText(DataConverter.innerWallPath != null ? DataConverter.innerWallPath : "");
        pathOuterWall.setText(DataConverter.outerWallPath != null ? DataConverter.outerWallPath : "");
        pathInnerColumn.setText(DataConverter.innerColumnPath != null ? DataConverter.innerColumnPath : "");
        pathRoof.setText(DataConverter.roofPath != null ? DataConverter.roofPath : "");

        // 3. Pre-load the temporary tags with the existing global tags
        tempInnerWallTag = DataConverter.innerWallTag;
        tempOuterWallTag = DataConverter.outerWallTag;
        tempInnerColumnTag = DataConverter.innerColumnTag;
        tempRoofTag = DataConverter.roofTag;
    }

    // --- File Browse Actions ---

    @FXML
    protected void onBrowseInnerWall() {
        CompoundTag tag = handleNbtFileSelection(pathInnerWall);
        if (tag != null) {
            ListTag<IntTag> size = (ListTag<IntTag>) tag.get("size", ListTag.class);
            int x = size.get(0).asInt(), y = size.get(1).asInt(), z = size.get(2).asInt();
            if(x == 7 && y == 7 && (z == 1 || z == 3)) tempInnerWallTag = tag;
            else {
                pathInnerWall.clear();
                System.err.println("Error: The size of the selected NBT file is invalid.");
            }
        }
    }

    @FXML
    protected void onBrowseOuterWall() {
        CompoundTag tag = handleNbtFileSelection(pathOuterWall);
        if (tag != null) {
            ListTag<IntTag> size = (ListTag<IntTag>) tag.get("size", ListTag.class);
            int x = size.get(0).asInt(), y = size.get(1).asInt(), z = size.get(2).asInt();
            if(x == 7 && y == 7 && z <= 2) tempOuterWallTag = tag;
            else {
                pathOuterWall.clear();
                System.err.println("Error: The size of the selected NBT file is invalid.");
            }
        }
    }

    @FXML
    protected void onBrowseInnerColumn() {
        CompoundTag tag = handleNbtFileSelection(pathInnerColumn);
        if (tag != null) {
            ListTag<IntTag> size = (ListTag<IntTag>) tag.get("size", ListTag.class);
            int x = size.get(0).asInt(), y = size.get(1).asInt(), z = size.get(2).asInt();
            if(x == z && y == 7 && (z == 1 || z == 3 || z == 5)) tempInnerColumnTag = tag;
            else {
                pathInnerColumn.clear();
                System.err.println("Error: The size of the selected NBT file is invalid.");
            }
        }
    }

    @FXML
    protected void onBrowseRoof() {
        CompoundTag tag = handleNbtFileSelection(pathRoof);
        if (tag != null) {
            ListTag<IntTag> size = (ListTag<IntTag>) tag.get("size", ListTag.class);
            int x = size.get(0).asInt(), y = size.get(1).asInt(), z = size.get(2).asInt();
            if(x == z && y <= 7 && (z == 7 || z == 9)) tempRoofTag = tag;
            else {
                pathRoof.clear();
                System.err.println("Error: The size of the selected NBT file is invalid.");
            }
        }
    }

    /**
     * Helper method to open a FileChooser, parse the NBT, and update the UI TextField.
     */
    private CompoundTag handleNbtFileSelection(TextField displayField) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select NBT Structure");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Minecraft NBT Files (*.nbt)", "*.nbt"));

        // Open dialog over the current window
        File file = fileChooser.showOpenDialog(displayField.getScene().getWindow());

        if (file != null) {
            try {
                // Parse the NBT file directly into memory
                NamedTag namedTag = NBTUtil.read(file);

                if (namedTag.getTag() instanceof CompoundTag) {
                    displayField.setText(file.getAbsolutePath());
                    return (CompoundTag) namedTag.getTag();
                } else {
                    System.err.println("Error: The selected NBT file does not contain a CompoundTag root.");
                }
            } catch (Exception e) {
                System.err.println("Failed to read NBT file: " + e.getMessage());
            }
        }
        return null;
    }

    // --- Save and Cancel Actions ---

    @FXML
    protected void onSaveAction() {
        // 1. Save String IDs
        DataConverter.frameworkId = blocFramework.getText().trim();
        DataConverter.columnId = blocColumn.getText().trim();
        DataConverter.rowId = blocRow.getText().trim();
        DataConverter.floorId = blocFloor.getText().trim();
        DataConverter.wallId = blocWall.getText().trim();

        // 2. Save File Paths
        DataConverter.innerWallPath = pathInnerWall.getText().trim();
        DataConverter.outerWallPath = pathOuterWall.getText().trim();
        DataConverter.innerColumnPath = pathInnerColumn.getText().trim();
        DataConverter.roofPath = pathRoof.getText().trim();

        // 3. Commit the CompoundTags to the global DataConverter
        DataConverter.innerWallTag = tempInnerWallTag;
        DataConverter.outerWallTag = tempOuterWallTag;
        DataConverter.innerColumnTag = tempInnerColumnTag;
        DataConverter.roofTag = tempRoofTag;

        System.out.println("Saved Block ID and NBT Configuration successfully.");
        closeWindow();
    }

    @FXML
    protected void onCancelAction() {
        // Discard any temporary tags and close
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) blocFramework.getScene().getWindow();
        stage.close();
    }
}