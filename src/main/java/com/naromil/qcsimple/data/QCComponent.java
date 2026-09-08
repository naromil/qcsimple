package com.naromil.qcsimple.data;

import net.querz.nbt.tag.CompoundTag;
import net.querz.nbt.tag.IntTag;
import net.querz.nbt.tag.ListTag;

public abstract class QCComponent {
    private String path = "";
    private CompoundTag tag = null;

    public QCComponent() {}
    public QCComponent(String path, CompoundTag tag) {
        this.path = path;
        this.tag = tag;
    }

    public String getPath() {
        return path;
    }
    public void setPath(String path) {
        this.path = path;
    }
    public CompoundTag getTag() {
        return tag;
    }
    public void setTag(CompoundTag tag) {
        this.tag = tag;
    }

    public int getX () {
        ListTag<IntTag> sizeTag = tag.getListTag("size").asIntTagList();
        return sizeTag.get(0).asInt();
    }
    public int getY () {
        ListTag<IntTag> sizeTag = tag.getListTag("size").asIntTagList();
        return sizeTag.get(1).asInt();
    }
    public int getZ () {
        ListTag<IntTag> sizeTag = tag.getListTag("size").asIntTagList();
        return sizeTag.get(2).asInt();
    }

    public void set(CompoundTag tag, String path) {
        if (tag == null) return;
        this.tag = tag;
        this.path = path;
    }
    public void setExtracted(CompoundTag tag) {
        this.tag = tag;
        this.path = tag != null ? "[Extracted from Opened File]" : "[Not Configured]";
    }

    public boolean isEmpty() {
        return tag == null;
    }
}
