package com.naromil.qcsimple.main;

import com.naromil.qcsimple.data.BlockPalette;
import com.naromil.qcsimple.editor.EditorState;
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

    private final BlockPalette palette = BlockPalette.getInstance();

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
        blocFramework.setText(palette.frameworkBlock().getBlockId() != null ? palette.frameworkBlock().getBlockId() : "");
        blocColumn.setText(palette.columnBlock().getBlockId() != null ? palette.columnBlock().getBlockId() : "");
        blocRow.setText(palette.rowBlock().getBlockId() != null ? palette.rowBlock().getBlockId() : "");
        blocFloor.setText(palette.floorBlock().getBlockId() != null ? palette.floorBlock().getBlockId() : "");
        blocWall.setText(palette.wallBlock().getBlockId() != null ? palette.wallBlock().getBlockId() : "");

        // 2. Load NBT File Paths (so the user sees what was previously selected)
        pathInnerWall.setText(palette.innerWall().getPath() != null ? palette.innerWall().getPath() : "");
        pathOuterWall.setText(palette.outerWall().getPath() != null ? palette.outerWall().getPath() : "");
        pathInnerColumn.setText(palette.innerColumn().getPath() != null ? palette.innerColumn().getPath() : "");
        pathGate.setText(palette.gate().getPath() != null ? palette.gate().getPath() : "");

        // 3. Pre-load the temporary tags with the existing global tags
        tempInnerWallTag = palette.innerWall().getTag();
        tempOuterWallTag = palette.outerWall().getTag();
        tempInnerColumnTag = palette.innerColumn().getTag();
        tempGateTag = palette.gate().getTag();
    }

    // --- File Browse Actions ---

    @FXML
    protected void onBrowseInnerWall() {
        tempInnerWallTag = configureComponent(tempInnerWallTag, pathInnerWall, palette.innerWall()::isValidSize);
        if(tempInnerWallTag == null) pathInnerWall.clear();
    }

    @FXML
    protected void onBrowseGate() {
        tempGateTag = configureComponent(tempGateTag, pathGate, palette.gate()::isValidSize);
        if(tempGateTag == null) pathGate.clear();
    }

    @FXML
    protected void onBrowseOuterWall() {
        tempOuterWallTag = configureComponent(tempOuterWallTag, pathOuterWall, palette.outerWall()::isValidSize);
        if(tempOuterWallTag == null) pathOuterWall.clear();
    }

    @FXML
    protected void onBrowseInnerColumn() {
        tempInnerColumnTag = configureComponent(tempInnerColumnTag, pathInnerColumn, palette.innerColumn()::isValidSize);
        if(tempInnerColumnTag == null) pathInnerColumn.clear();
    }

    @FunctionalInterface
    interface TriPredicate {
        boolean run(int x, int y, int z);
    }

    private CompoundTag configureComponent(CompoundTag currentTag, TextField displayField, TriPredicate isValidSize) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select NBT Structure");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Minecraft NBT Files (*.nbt)", "*.nbt"));

        CompoundTag newTag = null;
        // Open dialog over the current window
        File file = fileChooser.showOpenDialog(displayField.getScene().getWindow());

        if (file != null) {
            try {
                // Parse the NBT file directly into memory
                NamedTag namedTag = NBTUtil.read(file);

                if (namedTag.getTag() instanceof CompoundTag) {
                    displayField.setText(file.getAbsolutePath());
                    newTag = (CompoundTag) namedTag.getTag();
                } else {
                    System.err.println("Error: The selected NBT file does not contain a CompoundTag root.");
                }
            } catch (Exception e) {
                System.err.println("Failed to read NBT file: " + e.getMessage());
            }
        }

        if (newTag != null) {
            ListTag<IntTag> size = (ListTag<IntTag>) newTag.get("size", ListTag.class);
            int x = size.get(0).asInt(), y = size.get(1).asInt(), z = size.get(2).asInt();

            if (isValidSize.run(x, y, z)) {
                return newTag; // Return the new valid tag
            } else {
                System.err.println("Error: The size of the selected NBT file is invalid.");
            }
        }
        return currentTag; // Return the unchanged tag if validation fails or newTag is null
    }

    // --- Save and Cancel Actions ---

    @FXML
    protected void onSaveAction() {
        // 1. Save String IDs
        palette.frameworkBlock().setBlockId(blocFramework.getText().trim());
        palette.columnBlock().setBlockId(blocColumn.getText().trim());
        palette.rowBlock().setBlockId(blocRow.getText().trim());
        palette.floorBlock().setBlockId(blocFloor.getText().trim());
        palette.wallBlock().setBlockId(blocWall.getText().trim());

        // 2. Save File Paths
        palette.innerWall().set(tempInnerWallTag, pathInnerWall.getText().trim());
        palette.outerWall().set(tempOuterWallTag, pathOuterWall.getText().trim());
        palette.innerColumn().set(tempInnerColumnTag, pathInnerColumn.getText().trim());
        palette.gate().set(tempGateTag, pathGate.getText().trim());

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
