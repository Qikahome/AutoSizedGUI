package qikahome.autosizedgui.widget;

import com.mojang.blaze3d.platform.cursor.CursorTypes;
import net.minecraft.util.Util;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import qikahome.autosizedgui.AutoSizedGUI;
import qikahome.autosizedgui.ModConfig;

/**
 * Manages vertical scrolling state and renders a scrollbar
 * using the built-in GUI texture ({@code gui.png}).
 * <p>
 * The texture layout at the top-right corner:
 * 
 * <pre>
 *   Track   (18×112)  — u=238, v=0   — top & bottom 1px rows fixed, middle stretchable
 *   Thumb   (12×15)   — u=226, v=0   — unselected state
 *   Thumb   (12×15)   — u=214, v=0   — selected (hovered) state
 * </pre>
 */
public class ScrollController {

    private int scrollOffset;
    private int maxScroll;
    private int viewportHeight;
    private int contentHeight;
    private int viewportLeft;
    private int viewportTop;
    private int viewportWidth;

    /** Scrollbar track width in the texture. */
    private static final int SCROLLBAR_WIDTH = 18;

    /** Total horizontal space to reserve for the scrollbar. */
    public static int getRequiredWidth() {
        return SCROLLBAR_WIDTH;
    }

    // Texture coordinates in gui.png (256×128)
    private static final int TEX_W = 256;
    private static final int TEX_H = 128;

    /** Track: 18×112 at top-right corner. */
    private static final int TRACK_U = 238, TRACK_V = 0, TRACK_W = 18, TRACK_H = 112;
    /** Top & bottom 1-pixel fixed rows of the track. */
    private static final int TRACK_BORDER = 1;
    /** Stretchable middle section of the track. */
    private static final int TRACK_MIDDLE_U = TRACK_U, TRACK_MIDDLE_V = TRACK_V + TRACK_BORDER;
    private static final int TRACK_MIDDLE_W = TRACK_W;
    private static final int TRACK_MIDDLE_H = TRACK_H - 2 * TRACK_BORDER;

    /** Thumb unselected (u=226) and selected (u=214), 12×15 each. */
    private static final int THUMB_W = 12, THUMB_H = 15;
    private static final int THUMB_UNSELECTED_U = 226;
    private static final int THUMB_SELECTED_U = 214;
    private static final int THUMB_V = 0;

    /** Thumb centering within the 18px track, 1px from the right edge. */
    private static final int THUMB_OFFSET_X = SCROLLBAR_WIDTH - THUMB_W - 1;

    // Drag state
    private boolean dragging;
    private double dragStartMouseY;
    private int dragStartOffset;
    private int step = 1;

    // Track auto-repeat state (hold a track click to keep scrolling)
    private int trackRepeatDir;        // 0 = none, -1 = up, +1 = down
    private double trackPressY;        // cursor Y being chased (follows the mouse)
    private long trackRepeatStart;     // timestamp of the last repeat step
    private boolean trackRepeatArmed;  // initial delay elapsed, repeating

    /** Set the snap step for scroll-to and drag (1 = no snap). */
    public void setStep(int step) {
        this.step = Math.max(1, step);
    }

    /** Update with current viewport geometry and total content height. */
    public void setContent(int viewportLeft, int viewportTop, int viewportWidth, int viewportHeight,
            int contentHeight) {
        this.viewportLeft = viewportLeft;
        this.viewportTop = viewportTop;
        this.viewportWidth = viewportWidth;
        this.viewportHeight = viewportHeight;
        this.contentHeight = contentHeight;
        int oldMax = maxScroll;
        maxScroll = Math.max(0, contentHeight - viewportHeight);
        // Snap maxScroll to step
        if (step > 1) {
            maxScroll = (maxScroll / step) * step;
        }
        if (maxScroll != oldMax) {
            scrollOffset = Math.min(scrollOffset, maxScroll);
            snapOffset();
        }
    }

    public boolean isVisible() {
        return maxScroll > 0;
    }

    public int getScrollOffset() {
        return scrollOffset;
    }

    /** Scroll to an absolute offset (clamped and snapped to step). */
    public void scrollTo(int offset) {
        scrollOffset = Math.max(0, Math.min(maxScroll, offset));
        snapOffset();
    }

    /** Snap the current offset to the nearest step. */
    private void snapOffset() {
        if (step > 1) {
            scrollOffset = Math.round((float) scrollOffset / step) * step;
            scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset));
        }
    }

    /** Get the current max scroll offset. */
    public int getMaxScroll() {
        return maxScroll;
    }

    // ========== Scrollbar geometry ==========

    /**
     * Left edge of the scrollbar track (at the right edge of the viewport content).
     */
    private int getScrollbarLeft() {
        return viewportLeft + viewportWidth - SCROLLBAR_WIDTH;
    }

    /** Right edge of the scrollbar track. */
    private int getScrollbarRight() {
        return getScrollbarLeft() + SCROLLBAR_WIDTH;
    }

    /** Top edge of the thumb (1px from top, 1px from bottom within track). */
    private int getThumbTop() {
        if (maxScroll <= 0)
            return viewportTop + 1;
        int available = viewportHeight - THUMB_H - 2;
        return viewportTop + 1 + (int) ((double) scrollOffset / maxScroll * available);
    }

    private boolean isMouseOverThumb(double mouseX, double mouseY) {
        int thumbT = getThumbTop();
        return mouseX >= getScrollbarLeft() + THUMB_OFFSET_X && mouseX < getScrollbarLeft() + THUMB_OFFSET_X + THUMB_W
                && mouseY >= thumbT && mouseY < thumbT + THUMB_H;
    }

    private boolean isMouseOverScrollbar(double mouseX, double mouseY) {
        return mouseX >= getScrollbarLeft() && mouseX < getScrollbarRight()
                && mouseY >= viewportTop && mouseY < viewportTop + viewportHeight;
    }

    // ========== Render ==========

    public void render(GuiGraphicsExtractor gui, int mouseX, int mouseY) {
        if (!isVisible())
            return;

        int trackL = getScrollbarLeft();
        int trackR = getScrollbarRight();
        int texW = TEX_W, texH = TEX_H;

        // --- Track background (three sections) ---
        // Top fixed 1px
        gui.blit(RenderPipelines.GUI_TEXTURED, AutoSizedGUI.BUILT_IN_GUI_TEXTURE,
                trackL, viewportTop,
                TRACK_U, TRACK_V,
                SCROLLBAR_WIDTH, TRACK_BORDER,
                SCROLLBAR_WIDTH, TRACK_BORDER, texW, texH);

        // Middle stretchable
        int midH = viewportHeight - 2 * TRACK_BORDER;
        if (midH > 0) {
            gui.blit(RenderPipelines.GUI_TEXTURED, AutoSizedGUI.BUILT_IN_GUI_TEXTURE,
                    trackL, viewportTop + TRACK_BORDER,
                    TRACK_MIDDLE_U, TRACK_MIDDLE_V,
                    SCROLLBAR_WIDTH, midH,
                    TRACK_MIDDLE_W, TRACK_MIDDLE_H, texW, texH);
        }

        // Bottom fixed 1px
        gui.blit(RenderPipelines.GUI_TEXTURED, AutoSizedGUI.BUILT_IN_GUI_TEXTURE,
                trackL, viewportTop + viewportHeight - TRACK_BORDER,
                TRACK_U, TRACK_V + TRACK_H - TRACK_BORDER,
                SCROLLBAR_WIDTH, TRACK_BORDER,
                SCROLLBAR_WIDTH, TRACK_BORDER, texW, texH);

        // --- Thumb (fixed 12×15, not scaled) ---
        int thumbT = getThumbTop();
        boolean hovered = isMouseOverThumb(mouseX, mouseY);
        // Both the thumb and the track are clickable — show the hand cursor
        if (isMouseOverScrollbar(mouseX, mouseY)) {
            gui.requestCursor(CursorTypes.POINTING_HAND);
        }
        int thumbU = hovered ? THUMB_SELECTED_U : THUMB_UNSELECTED_U;

        gui.blit(RenderPipelines.GUI_TEXTURED, AutoSizedGUI.BUILT_IN_GUI_TEXTURE,
                trackL + THUMB_OFFSET_X, thumbT,
                thumbU, THUMB_V,
                THUMB_W, THUMB_H,
                THUMB_W, THUMB_H, texW, texH);
    }

    // ========== Input ==========

    /** Handle mouse click. Returns true if the scrollbar consumed the event. */
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        return mouseClicked(event.x(), event.y(), event.button());
    }

    @Deprecated
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isVisible() || button != 0)
            return false;
        if (!isMouseOverScrollbar(mouseX, mouseY))
            return false;

        int thumbT = getThumbTop();

        if (mouseY >= thumbT && mouseY < thumbT + THUMB_H) {
            // Start dragging
            dragging = true;
            dragStartMouseY = mouseY;
            dragStartOffset = scrollOffset;
        } else {
            // Click on track — scroll one row toward the click; hold to repeat
            trackRepeatDir = mouseY < thumbT ? -1 : 1;
            trackPressY = mouseY;
            trackRepeatArmed = false;
            trackRepeatStart = Util.getMillis();
            scrollOneRow(trackRepeatDir);
        }
        return true;
    }

    /**
     * Advance the track auto-repeat state. Called every frame from the panel.
     * While the mouse stays held on the track, keeps scrolling one row toward
     * the original click direction, following the cursor; stops when the thumb
     * reaches the cursor or the mouse leaves the scrollbar.
     */
    public void tick(double mouseX, double mouseY, long now) {
        if (trackRepeatDir == 0)
            return;
        // Stop once the mouse leaves the scrollbar
        if (!isMouseOverScrollbar(mouseX, mouseY)) {
            trackRepeatDir = 0;
            trackRepeatArmed = false;
            return;
        }
        // Chase the cursor while held
        trackPressY = mouseY;
        if (!trackRepeatArmed) {
            if (now - trackRepeatStart >= ModConfig.SCROLLBAR_TRACK_REPEAT_DELAY.get()) {
                trackRepeatArmed = true;
                trackRepeatStart = now;
            }
            return;
        }
        if (now - trackRepeatStart < ModConfig.SCROLLBAR_TRACK_REPEAT_INTERVAL.get())
            return;
        // Stop once the thumb reaches/passes the cursor
        int thumbT = getThumbTop();
        if ((trackRepeatDir > 0 && thumbT >= trackPressY)
                || (trackRepeatDir < 0 && thumbT <= trackPressY)) {
            trackRepeatDir = 0;
            trackRepeatArmed = false;
            return;
        }
        if (!scrollOneRow(trackRepeatDir)) {
            trackRepeatDir = 0;
            trackRepeatArmed = false;
            return;
        }
        trackRepeatStart = now;
    }

    /** Scroll one snap step in the given direction (-1 = up, +1 = down). */
    private boolean scrollOneRow(int dir) {
        int old = scrollOffset;
        scrollTo(scrollOffset + dir * step);
        return scrollOffset != old;
    }

    /** Handle mouse drag. Returns true if the scrollbar consumed the event. */
    public boolean mouseDragged(double mouseX, double mouseY, int button) {
        if (!dragging || button != 0)
            return false;

        double dy = mouseY - dragStartMouseY;
        int available = viewportHeight - THUMB_H;
        if (available <= 0)
            return true;

        double ratio = dy / available;
        scrollTo(dragStartOffset + (int) (ratio * maxScroll));
        return true;
    }

    /** Handle mouse release. Returns true if the scrollbar consumed the event. */
    public boolean mouseReleased() {
        trackRepeatDir = 0;
        trackRepeatArmed = false;
        if (dragging) {
            dragging = false;
            return true;
        }
        return false;
    }

    /** Reset scroll state (e.g. on layout change). */
    public void reset() {
        scrollOffset = 0;
        maxScroll = 0;
        dragging = false;
        trackRepeatDir = 0;
        trackRepeatArmed = false;
    }
}
