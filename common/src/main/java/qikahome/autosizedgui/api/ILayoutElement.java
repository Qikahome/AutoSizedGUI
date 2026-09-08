package qikahome.autosizedgui.api;

import com.mojang.blaze3d.platform.cursor.CursorType;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import org.jspecify.annotations.Nullable;

/**
 * Base interface for any element that can be placed and rendered within an
 * {@code AutoLayoutPanel}.
 * <p>
 * Elements can participate in the normal flow layout or be fixed at a specific
 * position
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
     * {@link AttachPosition#NONE} means the element participates in normal flow
     * layout
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
    void render(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks);

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

    default boolean mouseClicked(IPanelInfoGetter panel, MouseButtonEvent event, boolean doubleClick) {
        return mouseClicked(panel, event.x(), event.y(), event.button());
    }

    default boolean mouseClicked(IPanelInfoGetter panel, double mouseX, double mouseY, int button) {
        return false;
    }

    default boolean mouseReleased(IPanelInfoGetter panel, MouseButtonEvent event) {
        return mouseReleased(panel, event.x(), event.y(), event.button());
    }

    default boolean mouseReleased(IPanelInfoGetter panel, double mouseX, double mouseY, int button) {
        return false;
    }

    default boolean mouseDragged(IPanelInfoGetter panel, MouseButtonEvent event, double dragX, double dragY) {
        return mouseDragged(panel, event.x(), event.y(), event.button(), dragX, dragY);
    }

    default boolean mouseDragged(IPanelInfoGetter panel, double mouseX, double mouseY, int button, double dragX, double dragY) {
        return false;
    }

    default boolean mouseScrolled(IPanelInfoGetter panel, double mouseX, double mouseY, double delta) {
        return false;
    }

    /**
     * Returns the cursor to display while the mouse is over this element, or
     * {@code null} to leave the cursor unchanged. Called every frame during
     * render state extraction.
     * <p>
     * {@code mouseX/mouseY} are absolute screen coordinates — add the panel
     * origin ({@code panel.getOriginX()/getOriginY()}) to {@code getX()/getY()}
     * to compare against your own bounds. The last non-null cursor requested
     * this frame wins.
     */
    @Nullable
    default CursorType getCursor(IPanelInfoGetter panel, double mouseX, double mouseY) {
        return null;
    }
    
    default boolean keyPressed(KeyEvent event) {
        return keyPressed(event.key(), event.scancode(), event.modifiers());
    }

    /// @deprecated use #keyPressed(KeyEvent) instead.
    @Deprecated(forRemoval = true)
    default boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    default boolean keyReleased(KeyEvent event) {
        return keyReleased(event.key(), event.scancode(), event.modifiers());
    }

    /// @deprecated use #keyReleased(KeyEvent) instead.
    @Deprecated(forRemoval = true)
    default boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    default boolean charTyped(CharacterEvent event) {
        return charTyped((char) event.codepoint(),0);
    }
    /// @deprecated use #charTyped(CharacterEvent) instead.
    @Deprecated(forRemoval = true)
    default boolean charTyped(char codePoint, int modifiers) {
        return false;
    }

}
