package qikahome.autosizedgui.api;

import qikahome.autosizedgui.AutoSizedGUISettings;

public class LayoutConfig {

    private int maxHeight = AutoSizedGUISettings.defaultMaxHeight;
    private int maxWidth = AutoSizedGUISettings.defaultMaxWidth;
    private int minMargin = AutoSizedGUISettings.defaultMinMargin;
    private int minColumns = AutoSizedGUISettings.defaultMinColumns;
    private int maxColumns = AutoSizedGUISettings.defaultMaxColumns;
    private int maxRows = AutoSizedGUISettings.defaultMaxRows;
    private int elementSpacing = 0;
    private int defaultSlotSize = 18;
    private int paginationButtonHeight = 14;
    private OverflowMode overflowMode = AutoSizedGUISettings.defaultOverflowMode;

    public int resolveMaxHeight(int screenHeight) {
        int marginLimit = screenHeight - (2 * minMargin);
        if (maxHeight > 0) {
            return Math.min(marginLimit, maxHeight);
        }
        return marginLimit;
    }

    public int resolveMaxWidth(int screenWidth) {
        int marginLimit = screenWidth - (2 * minMargin);
        if (maxWidth > 0) {
            return Math.min(marginLimit, maxWidth);
        }
        return marginLimit;
    }

    public LayoutConfig setMaxHeight(int maxHeight) {
        this.maxHeight = maxHeight;
        return this;
    }

    public LayoutConfig setMaxWidth(int maxWidth) {
        this.maxWidth = maxWidth;
        return this;
    }

    public LayoutConfig setMinMargin(int minMargin) {
        this.minMargin = Math.max(0, minMargin);
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

    public int getMaxHeight() {
        return maxHeight;
    }

    public int getMaxWidth() {
        return maxWidth;
    }

    public int getMinMargin() {
        return minMargin;
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