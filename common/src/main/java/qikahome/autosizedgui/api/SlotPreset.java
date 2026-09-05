package qikahome.autosizedgui.api;

/**
 * Preset slot container configurations for common Minecraft inventory sizes (1-54).
 * <p>
 * Each preset defines columns, rows, total slots, and whether the last row is partial.
 * <pre>{@code
 * SlotPreset preset = SlotPreset.forSlots(27);
 * // preset.columns == 9, preset.rows == 3
 * }</pre>
 */
public record SlotPreset(int columns, int rows, int totalSlots, boolean hasPartialLastRow) {

    private static final SlotPreset[] PRESETS = buildPresets();

    /**
     * Look up the preset for a given slot count.
     *
     * @param totalSlots number of inventory slots (1-54, or any number)
     * @return the matching preset, or a computed one if no predefined match exists
     * @throws IllegalArgumentException if totalSlots <= 0
     */
    public static SlotPreset forSlots(int totalSlots) {
        if (totalSlots <= 0) throw new IllegalArgumentException("Slot count must be > 0");

        // Check predefined presets
        int idx = totalSlots - 1;
        if (idx >= 0 && idx < PRESETS.length) {
            return PRESETS[idx];
        }

        // Fallback: compute dynamically
        return compute(totalSlots);
    }

    /** Get the slot positions for this preset, each entry is {col, row}. */
    public int[][] getSlotPositions() {
        int[][] positions = new int[totalSlots][2];
        for (int i = 0; i < totalSlots; i++) {
            positions[i][0] = i % columns;
            positions[i][1] = i / columns;
        }
        return positions;
    }

    // =====================================================================
    //  Internal
    // =====================================================================

    private static SlotPreset[] buildPresets() {
        SlotPreset[] p = new SlotPreset[54];
        for (int i = 0; i < 54; i++) {
            p[i] = compute(i + 1);
        }
        return p;
    }

    /**
     * Compute optimal columns/rows for a given slot count.
     * <p>
     * Strategy: start from 9 columns (standard chest width), reduce if needed.
     */
    private static SlotPreset compute(int total) {
        if (total <= 9) {
            return new SlotPreset(total, 1, total, false);
        }

        // Try 9 columns first
        int cols = 9;
        int rows = (int) Math.ceil((double) total / cols);
        boolean partial = total % cols != 0;

        // If 9 columns gives reasonable row count, use it
        if (rows <= 6) {
            return new SlotPreset(cols, rows, total, partial);
        }

        // For >54 slots, try to balance col/row
        cols = (int) Math.ceil(Math.sqrt(total));
        if (cols > 18) cols = 18; // reasonable max
        rows = (int) Math.ceil((double) total / cols);
        partial = total % cols != 0;
        return new SlotPreset(cols, rows, total, partial);
    }
}
