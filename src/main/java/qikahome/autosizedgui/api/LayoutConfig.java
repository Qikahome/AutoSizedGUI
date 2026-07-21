package qikahome.autosizedgui.api;

import qikahome.autosizedgui.ModConfig;

/**
 * Configuration for the auto-layout engine.
 * <p>
 * {@code maxHeight} and {@code maxWidth} interpret positive values as absolute pixel limits,
 * and negative values as the minimum distance (margin) from the corresponding screen edge.
 * Zero is treated as "no limit".
 */
public class LayoutConfig {

    private int maxHeight = ModConfig.DEFAULT_MAX_HEIGHT.get();
    private int maxWidth = ModConfig.DEFAULT_MAX_WIDTH.get();
    private int minColumns = ModConfig.DEFAULT_MIN_COLUMNS.get();
    private int maxColumns = ModConfig.DEFAULT_MAX_COLUMNS.get();        // -1 = auto
    private int maxRows = ModConfig.DEFAULT_MAX_ROWS.get();              // -1 = auto
    private int elementSpacing = 0;
    private int defaultSlotSize = 18;
    private int paginationButtonHeight = 14;
    private OverflowMode overflowMode = ModConfig.DEFAULT_OVERFLOW_MODE.get();

    // -- Helpers to resolve effective limits given screen dimensions --

    public int resolveMaxHeight(int screenHeight) {
        if (maxHeight < 0) return screenHeight - (2 * -maxHeight);
        if (maxHeight == 0) return screenHeight;
        return maxHeight;
    }

    public int resolveMaxWidth(int screenWidth) {
        if (maxWidth < 0) return screenWidth - (2 * -maxWidth);
        if (maxWidth == 0) return screenWidth;
        return maxWidth;
    }

    // -- Builder-style setters --

    public LayoutConfig setMaxHeight(int maxHeight) {
        this.maxHeight = maxHeight;
        return this;
    }

    public LayoutConfig setMaxWidth(int maxWidth) {
        this.maxWidth = maxWidth;
        return this;
    }

    public LayoutConfig setMinColumns(int minColumns) {
        this.minColumns = Math.max(1, minColumns);
        return this;
    }

    public LayoutConfig setMaxColumns(int maxColumns) {
        this.maxColumns = maxColumns;
        return this;
    }

    public LayoutConfig setMaxRows(int maxRows) {
        this.maxRows = maxRows;
        return this;
    }

    public LayoutConfig setElementSpacing(int elementSpacing) {
        this.elementSpacing = elementSpacing;
        return this;
    }

    public LayoutConfig setDefaultSlotSize(int defaultSlotSize) {
        this.defaultSlotSize = defaultSlotSize;
        return this;
    }

    public LayoutConfig setPaginationButtonHeight(int paginationButtonHeight) {
        this.paginationButtonHeight = paginationButtonHeight;
        return this;
    }

    public LayoutConfig setOverflowMode(OverflowMode overflowMode) {
        this.overflowMode = overflowMode;
        return this;
    }

    // -- Getters --

    public int getMaxHeight() {
        return maxHeight;
    }

    public int getMaxWidth() {
        return maxWidth;
    }

    public int getMinColumns() {
        return minColumns;
    }

    public int getMaxColumns() {
        return maxColumns;
    }

    public int getMaxRows() {
        return maxRows;
    }

    public int getElementSpacing() {
        return elementSpacing;
    }

    public int getDefaultSlotSize() {
        return defaultSlotSize;
    }

    public int getPaginationButtonHeight() {
        return paginationButtonHeight;
    }

    public OverflowMode getOverflowMode() {
        return overflowMode;
    }
}
