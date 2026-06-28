package io.github.naromil.qcsimple.data;

import io.github.naromil.qcsimple.editor.EditorState;
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
    protected TextField pathInnerWall, pathOuterWall, pathInnerColumn, pathGate;

    // Temporary storage for parsed tags during this window session
    private CompoundTag tempInnerWallTag = null;
    private CompoundTag tempOuterWallTag = null;
    private CompoundTag tempInnerColumnTag = null;
    private CompoundTag tempGateTag = null;

    @FXML
    public void initialize() {
        // 1. Load String IDs
        blocFramework.setText(BlockConfig.frameworkId != null ? BlockConfig.frameworkId : "");
        blocColumn.setText(BlockConfig.columnId != null ? BlockConfig.columnId : "");
        blocRow.setText(BlockConfig.rowId != null ? BlockConfig.rowId : "");
        blocFloor.setText(BlockConfig.floorId != null ? BlockConfig.floorId : "");
        blocWall.setText(BlockConfig.wallId != null ? BlockConfig.wallId : "");

        // 2. Load NBT File Paths (so the user sees what was previously selected)
        pathInnerWall.setText(BlockConfig.innerWallPath != null ? BlockConfig.innerWallPath : "");
        pathOuterWall.setText(BlockConfig.outerWallPath != null ? BlockConfig.outerWallPath : "");
        pathInnerColumn.setText(BlockConfig.innerColumnPath != null ? BlockConfig.innerColumnPath : "");
        pathGate.setText(BlockConfig.gatePath != null ? BlockConfig.gatePath : "");

        // 3. Pre-load the temporary tags with the existing global tags
        tempInnerWallTag = BlockConfig.innerWallTag;
        tempOuterWallTag = BlockConfig.outerWallTag;
        tempInnerColumnTag = BlockConfig.innerColumnTag;
        tempGateTag = BlockConfig.gateTag;
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
    protected void onBrowseGate() {
        CompoundTag tag = handleNbtFileSelection(pathGate);
        if (tag != null) {
            ListTag<IntTag> size = (ListTag<IntTag>) tag.get("size", ListTag.class);
            int x = size.get(0).asInt(), y = size.get(1).asInt(), z = size.get(2).asInt();
            if(x == 7 && y == 7 && (z == 1 || z == 3)) tempGateTag = tag;
            else {
                pathGate.clear();
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
            if(x == 7 && y == 7 && z <= 3) tempOuterWallTag = tag;
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
            if(x == 3 && y == 7 && z == 3) tempInnerColumnTag = tag;
            else {
                pathInnerColumn.clear();
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
        BlockConfig.frameworkId = blocFramework.getText().trim();
        BlockConfig.columnId = blocColumn.getText().trim();
        BlockConfig.rowId = blocRow.getText().trim();
        BlockConfig.floorId = blocFloor.getText().trim();
        BlockConfig.wallId = blocWall.getText().trim();

        // 2. Save File Paths
        BlockConfig.innerWallPath = pathInnerWall.getText().trim();
        BlockConfig.outerWallPath = pathOuterWall.getText().trim();
        BlockConfig.innerColumnPath = pathInnerColumn.getText().trim();
        BlockConfig.gatePath = pathGate.getText().trim();

        // 3. Commit the CompoundTags to the global DataConverter
        BlockConfig.innerWallTag = tempInnerWallTag;
        BlockConfig.outerWallTag = tempOuterWallTag;
        BlockConfig.innerColumnTag = tempInnerColumnTag;
        BlockConfig.gateTag = tempGateTag;

        System.out.println("Saved Block ID and NBT Configuration successfully.");
        EditorState.getInstance().setDirty(true);
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