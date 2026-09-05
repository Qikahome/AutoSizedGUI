package qikahome.autosizedgui.api;

/**
 * Defines how an element is attached (positioned) within the panel.
 * <p>
 * {@link #NONE} elements participate in normal flow layout and are affected by scrolling/pagination.
 * All other values mean the element is fixed in that position and unaffected by scrolling/pagination.
 */
public enum AttachPosition {
    /** Normal flow layout, participates in scrolling/pagination */
    NONE,
    /** Fixed at the top-left corner */
    TOP_LEFT,
    /** Fixed at the top edge, centered horizontally */
    TOP,
    /** Fixed at the top-right corner */
    TOP_RIGHT,
    /** Fixed at the left edge, centered vertically */
    LEFT,
    /** Fixed at the right edge, centered vertically */
    RIGHT,
    /** Fixed at the bottom-left corner */
    BOTTOM_LEFT,
    /** Fixed at the bottom edge, centered horizontally */
    BOTTOM,
    /** Fixed at the bottom-right corner */
    BOTTOM_RIGHT
}
