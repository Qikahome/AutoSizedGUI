package qikahome.autosizedgui.api;

/**
 * Controls how content overflow is handled when elements don't fit within the panel bounds.
 */
public enum OverflowMode {
    /** Add pagination buttons when content overflows */
    PAGINATE,
    /** Add a scrollbar when content overflows */
    SCROLL
}
