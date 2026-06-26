package io.github.naromil.qcsimple.editor;

import io.github.naromil.qcsimple.data.DataConverter;
import io.github.naromil.qcsimple.data.NBTHandler;
import io.github.naromil.qcsimple.data.QCUnit;
import io.github.naromil.qcsimple.data.Point3D;
import net.querz.nbt.tag.CompoundTag;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class EditorState {
    private static final EditorState instance = new EditorState();

    private File currentFile = null;
    private CompoundTag rootCompoundTag = null; // Replace 'Object' with your NBT library's Tag type (e.g., CompoundTag)
    private boolean isDirty = false;       // Tracks if there are unsaved changes

    // Multi-layer structure tracking: Y-level -> (XZ coordinates -> Block)
    private Map<Integer, Map<Point2D, QCUnit>> layers = new HashMap<>();
    private int currentLayer = 1; // Default initial layer to 1

    private EditorState() {}

    public static EditorState getInstance() {
        return instance;
    }

    // Getters and Setters
    public Optional<File> getCurrentFile() { return Optional.ofNullable(currentFile); }
    public void setCurrentFile(File file) { this.currentFile = file; }

    public CompoundTag getRootCompoundTag() { return rootCompoundTag; }
    public void setRootCompoundTag(CompoundTag tag) {
        this.rootCompoundTag = tag;
        this.isDirty = true;
    }

    // Synchronize root compound tag with layers
    public void syncRootCompoundTag() {
        Map<Point3D, CompoundTag> blockMap = DataConverter.convertLayersToBlockMap(layers);
        rootCompoundTag = NBTHandler.convertMapToTag(blockMap);
    }
    public void syncMap() {
        Map<Point3D, CompoundTag> blockMap = NBTHandler.convertTagToMap(rootCompoundTag);
        layers = DataConverter.convertBlockMapToLayers(blockMap);
    }

    public boolean isDirty() { return isDirty; }
    public void setDirty(boolean dirty) { this.isDirty = dirty; }

    /**
     * Gets the 2D coordinate map for the requested layer.
     * If the layer has never been modified, it lazily creates a blank map for it.
     */
    public Map<Point2D, QCUnit> getMapForLayer(int yLevel) {
        return layers.computeIfAbsent(yLevel, k -> new HashMap<>());
    }

    // Layer Navigation Management
    public int getCurrentLayerIndex() { return currentLayer; }
    public void setCurrentLayerIndex(int layer) { this.currentLayer = layer; }
}