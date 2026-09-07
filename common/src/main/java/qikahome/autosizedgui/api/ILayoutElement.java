package qikahome.autosizedgui.api;

import net.minecraft.client.gui.GuiGraphics;

/**
 * Base interface for any element that can be placed and rendered within an {@code AutoLayoutPanel}.
 * <p>
 * Elements can participate in the normal flow layout or be fixed at a specific position
 * within the panel (see {@link #getAttachPosition()}).
 */
public interface ILayoutElement {

    /** The width of this element in pixels. */
    int getWidth();

    /** The height of this element in pixels. */
    int getHeight();

    /**
     * How this element is attached/positioned within the panel.
     * <p>
     * {@link AttachPosition#NONE} means the element participates in normal flow layout
     * and is affected by scrolling/pagination.
     * Any other value fixes the element at that position.
     */
    default AttachPosition getAttachPosition() {
        return AttachPosition.NONE;
    }

    /**
     * Priority for ordering within a fixed group.
     * Higher priority elements are placed further from the flow content anchor
     * (e.g. higher for TOP, lower for BOTTOM, further right for LEFT).
     * Default priority is 0.
     */
    default int getPriority() {
        return 0;
    }

    /**
     * The x-coordinate assigned by the layout engine.
     * This is relative to the panel's content area.
     */
    int getX();

    /**
     * The y-coordinate assigned by the layout engine.
     * This is relative to the panel's content area.
     */
    int getY();

    /** Set the position assigned by the layout engine. */
    void setPosition(int x, int y);

    /** Render this element at its assigned position. */
    void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks);

    /**
     * Whether this element should be rendered this frame.
     * Used to skip off-viewport elements for performance.
     */
    default boolean shouldRender() {
        return true;
    }

    /**
     * Whether this element is active (should be rendered and receive input).
     * Used by container screens to hide off-page slots.
     */
    default boolean isActive() {
        return true;
    }

    /**
     * Set whether this element is active.
     * Override {@link #isActive()} to return the stored value.
     */
    default void setActive(boolean active) {
        // no-op by default; override when needed
    }

    // -- Event delegation --
    //
    // Mouse events carry ABSOLUTE screen coordinates. An element's getX()/getY()
    // are relative to the panel content origin — add the panel's origin
    // (panel.getOriginX()/getOriginY()) to convert your own bounds to screen
    // space, or subtract it from the event coordinates to compare directly.

    /// @deprecated use #mouseClicked(IPanelInfoGetter, double, double, int) instead.
    @Deprecated(forRemoval = true)
    default boolean mouseClicked(double mouseX, double mouseY, int button) {
        return false;
    }

    @SuppressWarnings("deprecation")
    default boolean mouseClicked(IPanelInfoGetter panel, double mouseX, double mouseY, int button) {
        return mouseClicked(mouseX, mouseY, button);
    }

    /// @deprecated use #mouseReleased(IPanelInfoGetter, double, double, int) instead.
    @Deprecated(forRemoval = true)
    default boolean mouseReleased(double mouseX, double mouseY, int button) {
        return false;
    }

    @SuppressWarnings("deprecation")
    default boolean mouseReleased(IPanelInfoGetter panel, double mouseX, double mouseY, int button) {
        return mouseReleased(mouseX, mouseY, button);
    }

    /// @deprecated use #mouseDragged(IPanelInfoGetter, double, double, int, double, double) instead.
    @Deprecated(forRemoval = true)
    default boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return false;
    }

    @SuppressWarnings("deprecation")
    default boolean mouseDragged(IPanelInfoGetter panel, double mouseX, double mouseY, int button, double dragX, double dragY) {
        return mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    /// @deprecated use #mouseScrolled(IPanelInfoGetter, double, double, double) instead.
    @Deprecated(forRemoval = true)
    default boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        return false;
    }

    @SuppressWarnings("deprecation")
    default boolean mouseScrolled(IPanelInfoGetter panel, double mouseX, double mouseY, double delta) {
        return mouseScrolled(mouseX, mouseY, delta);
    }

    default boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    default boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    default boolean charTyped(char codePoint, int modifiers) {
        return false;
    }
}
