package qikahome.autosizedgui.api;

/**
 * Exposes panel geometry to {@link ILayoutElement}s.
 * <p>
 * An element's {@code getX()/getY()} are relative to the panel content origin;
 * adding {@code getOriginX()/getOriginY()} yields absolute screen coordinates.
 * The panel passes itself to the mouse event delegation methods so elements can
 * convert between the two coordinate spaces.
 */
public interface IPanelInfoGetter {

    /** Panel content origin X, in absolute screen coordinates. */
    int getOriginX();

    /** Panel content origin Y, in absolute screen coordinates. */
    int getOriginY();
}
