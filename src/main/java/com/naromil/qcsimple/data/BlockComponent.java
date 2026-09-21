package com.naromil.qcsimple.data;

/**
 * A component identified by a single block id instead of an uploaded structure: the framework,
 * row, column, wall and floor blocks.
 */
public abstract class BlockComponent implements UnitComponent {

    private String blockId = "";

    public String getBlockId() {
        return blockId;
    }

    public void setBlockId(String blockId) {
        this.blockId = blockId;
    }

    public boolean isEmpty() {
        return blockId == null || blockId.isBlank();
    }

    public boolean isConfigured() {
        return !isEmpty();
    }
}
