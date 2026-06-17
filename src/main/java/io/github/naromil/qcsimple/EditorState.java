package io.github.naromil.qcsimple;

import net.querz.nbt.tag.CompoundTag;

import java.io.File;
import java.util.Optional;

public class EditorState {
    private static final EditorState instance = new EditorState();

    private File currentFile = null;
    private CompoundTag rootCompoundTag = null; // Replace 'Object' with your NBT library's Tag type (e.g., CompoundTag)
    private boolean isDirty = false;       // Tracks if there are unsaved changes

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

    public boolean isDirty() { return isDirty; }
    public void setDirty(boolean dirty) { this.isDirty = dirty; }
}