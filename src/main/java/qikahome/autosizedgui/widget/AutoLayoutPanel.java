package qikahome.autosizedgui.widget;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import qikahome.autosizedgui.ModConfig;
import qikahome.autosizedgui.api.AttachPosition;
import qikahome.autosizedgui.api.ILayoutElement;
import qikahome.autosizedgui.api.IPanelInfoGetter;
import qikahome.autosizedgui.api.LayoutConfig;
import qikahome.autosizedgui.api.LayoutResult;
import qikahome.autosizedgui.api.OverflowMode;
import qikahome.autosizedgui.engine.LayoutEngine;

/**
 * The main auto-layout container widget.
 * <p>
 * Manages a list of {@link ILayoutElement}s, runs the layout engine to compute positions,
 * handles pagination, and delegates rendering and input events to the elements.
 * <p>
 * Usage:
 * <pre>{@code
 * AutoLayoutPanel panel = new AutoLayoutPanel();
 * panel.setConfig(new LayoutConfig().setMinColumns(9));
 * panel.addElement(new MySlotElement(27));
 * panel.reflow(width, height);
 * // in render:
 * panel.render(guiGraphics, mouseX, mouseY, partialTicks);
 * }</pre>
 */
public class AutoLayoutPanel implements IPanelInfoGetter {

    private final List<ILayoutElement> elements = new ArrayList<>();
    private final LayoutEngine engine = new LayoutEngine();
    private final PaginationController paginationController = new PaginationController();
    private final ScrollController scrollController = new ScrollController();
    private LayoutConfig config = new LayoutConfig();
    private LayoutResult currentLayout;
    private int screenWidth;
    private int screenHeight;
    private boolean dirty = true;
    private int lastSyncedPage = -1;
    /** Last scroll offset applied to element positions via syncPositions(). */
    private int lastAppliedOffset = Integer.MIN_VALUE;

    /** Origin offset: subtracted from layout positions to produce relative coords
     *  (typically set to {@link #getLayoutLeft()}/{@link #getLayoutTop()}). */
    private int originX, originY;

    // Background
    private NinePatchRenderer background;
    private int bgX, bgY, bgWidth, bgHeight;

    // Scroll step: uniform height of all normal (dynamic) elements
    private int elementStep = 18;
    // Aligned viewport height (multiple of elementStep), used for scissor clip
    private int scrollViewH;

    // ========== Element management ==========

    /** Add an element (normal or fixed). Triggers reflow on next render. */
    public void addElement(ILayoutElement element) {
        elements.add(element);
        markDirty();
    }

    /** Remove an element. Triggers reflow on next render. */
    public void removeElement(ILayoutElement element) {
        elements.remove(element);
        markDirty();
    }

    /** Remove all elements. */
    public void clearElements() {
        elements.clear();
        markDirty();
    }

    /** Get all elements (read-only). */
    public List<ILayoutElement> getElements() {
        return List.copyOf(elements);
    }

    // ========== Configuration ==========

    public void setConfig(LayoutConfig config) {
        this.config = config;
        markDirty();
    }

    public LayoutConfig getConfig() {
        return config;
    }

    /**
     * Set the origin offset. All element positions will be offset by (-ox, -oy)
     * from the layout engine's absolute positions, making them relative to this origin.
     * Should be set to ({@link #getLayoutLeft()}, {@link #getLayoutTop()}).
     */
    public void setOrigin(int ox, int oy) {
        this.originX = ox;
        this.originY = oy;
    }

    @Override
    public int getOriginX() {
        return originX;
    }

    @Override
    public int getOriginY() {
        return originY;
    }

    // ========== Background ==========

    /** Set a 9-patch background renderer. Pass null to disable. */
    public void setBackground(NinePatchRenderer background) {
        this.background = background;
    }

    public NinePatchRenderer getBackground() {
        return background;
    }

    // ========== Layout ==========

    /** Mark the layout as needing recalculation. */
    public void markDirty() {
        this.dirty = true;
    }

    /**
     * Recalculate layout for the given screen dimensions.
     * Called automatically on first render after {@link #markDirty()}.
     */
    public void reflow(int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        // Force slot active state and position sync after layout recalculation
        lastSyncedPage = -1;
        lastAppliedOffset = Integer.MIN_VALUE;

        // Calculate with full width; setupScrollMode recalculates if scrollbar space is needed
        this.currentLayout = engine.calculate(elements, config, screenWidth, screenHeight);

        if (config.getOverflowMode() == OverflowMode.SCROLL && currentLayout != null) {
            // Scroll mode: set up scroll controller
            setupScrollMode();
        } else {
            // Pagination mode
            scrollController.reset();
            if (currentLayout != null && currentLayout.getTotalPages() > 1) {
                paginationController.setTotalPages(currentLayout.getTotalPages());
                int pY = findTitleBarRowY();
                paginationController.updateBounds(screenWidth / 2, pY);
            } else {
                paginationController.setTotalPages(1);
            }
        }

        // Compute background bounds from all positioned elements
        computeBackgroundBounds();

        this.dirty = false;
    }

    /**
     * Find the Y position just above the first row of dynamic elements,
     * so pagination buttons sit centered, directly above the flow content.
     */
    private int findTitleBarRowY() {
        if (currentLayout == null) return 0;
        int topY = Integer.MAX_VALUE;
        for (LayoutResult.PositionedElement pe : currentLayout.getElementsForPage(0)) {
            if (pe.y() < topY) topY = pe.y();
        }
        if (topY != Integer.MAX_VALUE) return topY - 14; // 14=buttonH, touches dynamic elements
        return currentLayout.getFlowAreaTop();
    }

    // ========== Scroll mode support ==========

    /** Intermediate computation result for scroll layout metrics. */
    private static class ScrollMetrics {
        int elementStep, contentH, flowTop, flowLeft;
    }

    /** Compute elementStep and content height from a layout result. */
    private ScrollMetrics computeScrollMetrics(LayoutResult layout) {
        int flowTop = layout.getFlowAreaTop();
        int flowLeft = layout.getFlowAreaLeft();
        int maxContentBottom = 0, maxH = 0;
        Integer firstH = null;
        boolean allSame = true;
        for (LayoutResult.PositionedElement pe : layout.getNormalPositions()) {
            int h = pe.element().getHeight();
            if (firstH == null) firstH = h;
            else if (h != firstH) allSame = false;
            if (h > maxH) maxH = h;
            int b = pe.y() + h;
            if (b > maxContentBottom) maxContentBottom = b;
        }
        ScrollMetrics m = new ScrollMetrics();
        m.flowTop = flowTop;
        m.flowLeft = flowLeft;
        m.elementStep = allSame && firstH != null ? firstH : maxH;
        m.contentH = Math.max(0, maxContentBottom - flowTop);
        return m;
    }

    /** Set up scroll controller and determine the uniform element step. */
    private void setupScrollMode() {
        // ---- Pass 1: evaluate using the full-width layout ----
        ScrollMetrics m = computeScrollMetrics(currentLayout);
        int maxViewH = currentLayout.getFlowAreaHeight();
        elementStep = m.elementStep;
        scrollViewH = computeScrollViewH(m.contentH, maxViewH, elementStep);

        // ---- Pass 2: only reserve scrollbar space if content actually overflows ----
        boolean needsScrollbar = m.contentH > scrollViewH;

        if (needsScrollbar) {
            int original = config.getMaxWidth();
            config.setMaxWidth(original - ScrollController.getRequiredWidth());
            this.currentLayout = engine.calculate(elements, config, screenWidth, screenHeight);
            config.setMaxWidth(original);

            m = computeScrollMetrics(currentLayout);
            elementStep = m.elementStep;
            scrollViewH = computeScrollViewH(m.contentH, maxViewH, elementStep);
            needsScrollbar = m.contentH > scrollViewH;    // re-check after narrower layout
        }

        // ---- Pass 3: adjust centering and close BOTTOM gap for scroll viewport ----
        adjustScrollViewCentering(m.flowTop);

        // Viewport width includes scrollbar track only when scrollbar is visible
        int scrollW = needsScrollbar
            ? currentLayout.getContentWidth() + ScrollController.getRequiredWidth()
            : currentLayout.getContentWidth();

        scrollController.setContent(m.flowLeft, currentLayout.getFlowAreaTop(), scrollW, scrollViewH, m.contentH);
        scrollController.setStep(elementStep);
    }

    /**
     * If scroll viewport height is smaller than the full flow area, shrink the GUI
     * to its actual content height (fixedTop + scrollViewH + fixedBottom),
     * re-center vertically on the screen, and pull BOTTOM-fixed elements up to
     * remove the gap between content end and the viewport bottom.
     */
    private void adjustScrollViewCentering(int oldFlowTop) {
        if (currentLayout == null) return;
        if (currentLayout.getNormalPositions().isEmpty()) return;

        int maxH = config.resolveMaxHeight(screenHeight);
        int areaTop = (screenHeight - maxH) / 2;
        int fixedTopH = oldFlowTop - areaTop;
        int flowAreaH = currentLayout.getFlowAreaHeight();
        int fixedBottomH = maxH - flowAreaH - fixedTopH;

        int actualH = fixedTopH + scrollViewH + fixedBottomH;
        if (actualH >= maxH) return;                       // content fills the area — nothing to do

        int newAreaTop = (screenHeight - actualH) / 2;
        int delta = newAreaTop - areaTop;                   // vertical shift for centering
        int gap = flowAreaH - scrollViewH;                  // extra space between content and BOTTOM area

        // Build new LayoutResult with shifted positions & recompute bounds
        int newMinX = Integer.MAX_VALUE, newMinY = Integer.MAX_VALUE;
        int newMaxX = Integer.MIN_VALUE, newMaxY = Integer.MIN_VALUE;

        List<LayoutResult.PositionedElement> newNormals = new ArrayList<>();
        for (LayoutResult.PositionedElement pe : currentLayout.getNormalPositions()) {
            int ny = pe.y() + delta;
            pe.element().setPosition(pe.x(), ny);
            newNormals.add(new LayoutResult.PositionedElement(pe.element(), pe.x(), ny, pe.page()));
            int r = pe.x() + pe.element().getWidth();
            if (pe.x() < newMinX) newMinX = pe.x();
            if (r > newMaxX) newMaxX = r;
            // scroll mode: normal elements don't contribute to Y bounds
            // but adjustScrollViewCentering only runs in scroll mode
        }

        List<LayoutResult.PositionedElement> newFixed = new ArrayList<>();
        for (LayoutResult.PositionedElement pe : currentLayout.getFixedElements()) {
            int ny = pe.y() + delta;
            if (isBottomFixed(pe.element())) {
                ny -= gap;                                  // close the gap for BOTTOM elements
            }
            pe.element().setPosition(pe.x(), ny);
            newFixed.add(new LayoutResult.PositionedElement(pe.element(), pe.x(), ny, -1));
            int r = pe.x() + pe.element().getWidth();
            int b = ny + pe.element().getHeight();
            if (pe.x() < newMinX) newMinX = pe.x();
            if (ny < newMinY) newMinY = ny;
            if (r > newMaxX) newMaxX = r;
            if (b > newMaxY) newMaxY = b;
        }

        currentLayout = new LayoutResult(
                newNormals, newFixed,
                currentLayout.getColumns(), currentLayout.getRowsPerPage(), currentLayout.getTotalPages(),
                currentLayout.getContentWidth(), currentLayout.getFlowAreaHeight(),
                currentLayout.getFlowAreaLeft(),
                oldFlowTop + delta,
                newMinX, newMinY, newMaxX, newMaxY
        );
    }

    private static boolean isBottomFixed(ILayoutElement e) {
        var pos = e.getAttachPosition();
        return pos == AttachPosition.BOTTOM
            || pos == AttachPosition.BOTTOM_LEFT
            || pos == AttachPosition.BOTTOM_RIGHT;
    }

    /**
     * Compute the scroll viewport height: content height capped at the flow area
     * maximum, then ceiling-aligned to elementStep so small overhangs don't
     * trigger an unnecessary scrollbar.
     */
    private static int computeScrollViewH(int contentH, int maxViewH, int step) {
        if (step <= 0) return 0;
        int raw = Math.min(contentH, maxViewH);
        // Ceiling division: (n + d - 1) / d
        int aligned = ((raw + step - 1) / step) * step;
        // Cap at the area maximum (floor-aligned)
        if (aligned > maxViewH) {
            aligned = (maxViewH / step) * step;
        }
        return Math.max(aligned, Math.min(step, maxViewH));
    }

    /** Scroll down by one uniform-height step. */
    private void scrollDownOneRow() {
        if (currentLayout == null) return;
        int off = scrollController.getScrollOffset();
        int newOff = Math.min(scrollController.getMaxScroll(), off + elementStep);
        if (newOff != off) {
            scrollController.scrollTo(newOff);
            markSlotDirty();
        }
    }

    /** Scroll up by one uniform-height step. */
    private void scrollUpOneRow() {
        if (currentLayout == null) return;
        int off = scrollController.getScrollOffset();
        int newOff = Math.max(0, off - elementStep);
        if (newOff != off) {
            scrollController.scrollTo(newOff);
            markSlotDirty();
        }
    }

    /** Recompute the screen-space bounding box for the background. */
    private void computeBackgroundBounds() {
        if (currentLayout == null) return;
        if (background == null) return;

        int minX = currentLayout.getContentMinX();
        int minY = currentLayout.getContentMinY();
        int maxX = currentLayout.getContentMaxX();
        int maxY = currentLayout.getContentMaxY();

        if (minX == Integer.MAX_VALUE) return;

        // In scroll mode, extend the background rightward to cover the scrollbar (only when visible)
        if (config.getOverflowMode() == OverflowMode.SCROLL && scrollController.isVisible()) {
            maxX += ScrollController.getRequiredWidth();
        }

        // Expand by the 9-patch border sizes
        bgX = minX - background.getLeft();
        bgY = minY - background.getTop();
        bgWidth  = maxX - minX + background.getLeft() + background.getRight();
        bgHeight = maxY - minY + background.getTop() + background.getBottom();
    }

    /** Ensure layout is up-to-date. */
    private void ensureLayout() {
        if (dirty) {
            reflow(screenWidth, screenHeight);
        }
    }

    /**
     * Set {@link Slot#isActive()} on all ItemSlot instances based on current page/scroll.
     * Only runs when the page actually changes.
     */
    private void syncSlotActiveStates() {
        if (currentLayout == null) return;

        boolean scrollMode = config.getOverflowMode() == OverflowMode.SCROLL;

        if (scrollMode) {
            // Scroll mode: all normal elements are active
            int off = scrollController.getScrollOffset();
            if (off == lastSyncedPage) return;
            lastSyncedPage = off;

            int flowTop = currentLayout.getFlowAreaTop();
            int flowBottom = flowTop + scrollViewH;

            for (LayoutResult.PositionedElement pe : currentLayout.getNormalPositions()) {
                int sy = pe.y() - off;
                // Active only if within the aligned viewport
                boolean visible = sy + pe.element().getHeight() > flowTop && sy < flowBottom;
                pe.element().setActive(visible);
            }
            // Fixed elements always active
            for (LayoutResult.PositionedElement pe : currentLayout.getFixedElements()) {
                pe.element().setActive(true);
            }
        } else {
            int currentPage = paginationController.getCurrentPage();
            if (currentPage == lastSyncedPage) return;
            lastSyncedPage = currentPage;

            // Deactivate all slots first
            for (LayoutResult.PositionedElement pe : currentLayout.getNormalPositions()) {
                pe.element().setActive(false);
            }
            for (LayoutResult.PositionedElement pe : currentLayout.getFixedElements()) {
                pe.element().setActive(false);
            }

            // Activate slots on current page
            for (LayoutResult.PositionedElement pe : currentLayout.getElementsForPage(currentPage)) {
                pe.element().setActive(true);
            }
            // Fixed elements are always active
            for (LayoutResult.PositionedElement pe : currentLayout.getFixedElements()) {
                pe.element().setActive(true);
            }
        }
    }

    /** Call to force slot active state refresh on next render. */
    public void markSlotDirty() {
        lastSyncedPage = -1;
    }

    /**
     * Return normal (flow) elements visible on the current page / scroll position.
     * In scroll mode: all normal positions.
     * In paginate mode: only elements on the current page.
     */
    private List<LayoutResult.PositionedElement> visibleNormalElements() {
        return config.getOverflowMode() == OverflowMode.SCROLL
            ? currentLayout.getNormalPositions()
            : currentLayout.getElementsForPage(paginationController.getCurrentPage());
    }

    // ========== Rendering ==========

    /**
     * Render the panel and all its elements.
     * Should be called from {@code Screen.render()}.
     */
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        ensureLayout();
        if (currentLayout == null) return;

        // Advance scrollbar track auto-repeat before syncing positions
        scrollController.tick(mouseX, mouseY, Util.getMillis());

        // Sync slot active states and positions before rendering
        syncSlotActiveStates();
        syncPositions();

        // Render background (if set) — at absolute screen position
        if (background != null) {
            background.render(guiGraphics, bgX, bgY, bgWidth, bgHeight);
        }

        boolean scrollMode = config.getOverflowMode() == OverflowMode.SCROLL;

        // Push origin so elements render at absolute screen positions
        guiGraphics.pose().pushPose();
        if (originX != 0 || originY != 0) {
            guiGraphics.pose().translate(originX, originY, 0F);
        }

        if (scrollMode) {
            renderScrollMode(guiGraphics, mouseX, mouseY, partialTicks);
        } else {
            renderPaginateMode(guiGraphics, mouseX, mouseY, partialTicks);
        }

        guiGraphics.pose().popPose();

        // Render controls outside origin translate (they use absolute screen coordinates)
        if (scrollMode) {
            scrollController.render(guiGraphics, mouseX, mouseY);
        } else if (paginationController.isVisible()) {
            paginationController.render(guiGraphics, mouseX, mouseY, partialTicks);
        }
    }

    private void renderScrollMode(GuiGraphics gui, int mouseX, int mouseY, float partialTicks) {
        int off = scrollController.getScrollOffset();
        int flowTop = currentLayout.getFlowAreaTop();
        int flowLeft = currentLayout.getFlowAreaLeft();
        int flowW = currentLayout.getContentWidth();

        // 1. Render fixed elements (no offset, no clip — they stay in place)
        for (LayoutResult.PositionedElement pe : currentLayout.getFixedElements()) {
            pe.element().render(gui, mouseX, mouseY, partialTicks);
        }

        // 2. Clip viewport (aligned height) and render scrollable content
        gui.enableScissor(flowLeft, flowTop, flowLeft + flowW, flowTop + scrollViewH);

        // Render normal elements (positions already synced by syncPositions())
        for (LayoutResult.PositionedElement pe : currentLayout.getNormalPositions()) {
            if (pe.element().shouldRender())
                pe.element().render(gui, mouseX, mouseY, partialTicks);
        }

        gui.disableScissor();
    }

    private void renderPaginateMode(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        // Render normal elements for current page
        for (LayoutResult.PositionedElement pe : visibleNormalElements()) {
            pe.element().render(guiGraphics, mouseX, mouseY, partialTicks);
        }

        // Render fixed elements (visible on every page)
        for (LayoutResult.PositionedElement pe : currentLayout.getFixedElements()) {
            pe.element().render(guiGraphics, mouseX, mouseY, partialTicks);
        }
    }

    // ========== Input delegation ==========

    /**
     * Update all element positions to reflect current scroll offset / page.
     * No-op unless offset or page actually changed.
     * Must be called before input handling so slot x/y values are correct.
     */
    public void syncPositions() {
        ensureLayout();
        if (currentLayout == null) return;
        boolean scrollMode = config.getOverflowMode() == OverflowMode.SCROLL;
        int off = scrollMode ? scrollController.getScrollOffset() : 0;
        if (off == lastAppliedOffset) return;
        lastAppliedOffset = off;

        for (LayoutResult.PositionedElement pe : currentLayout.getNormalPositions()) {
            pe.element().setPosition(pe.x() - originX, pe.y() - off - originY);
        }
        for (LayoutResult.PositionedElement pe : currentLayout.getFixedElements()) {
            pe.element().setPosition(pe.x() - originX, pe.y() - originY);
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        ensureLayout();
        if (currentLayout == null) return false;

        if (config.getOverflowMode() == OverflowMode.SCROLL) {
            if (scrollController.mouseClicked(mouseX, mouseY, button)) return true;
        } else if (paginationController.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }

        for (LayoutResult.PositionedElement pe : visibleNormalElements()) {
            if (pe.element().mouseClicked(this, mouseX, mouseY, button)) return true;
        }
        for (LayoutResult.PositionedElement pe : currentLayout.getFixedElements()) {
            if (pe.element().mouseClicked(this, mouseX, mouseY, button)) return true;
        }
        return false;
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        ensureLayout();
        if (currentLayout == null) return false;

        if (config.getOverflowMode() == OverflowMode.SCROLL
            && scrollController.mouseReleased())
            return true;

        for (LayoutResult.PositionedElement pe : visibleNormalElements()) {
            if (pe.element().mouseReleased(this, mouseX, mouseY, button)) return true;
        }
        for (LayoutResult.PositionedElement pe : currentLayout.getFixedElements()) {
            if (pe.element().mouseReleased(this, mouseX, mouseY, button)) return true;
        }
        return false;
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        ensureLayout();
        if (currentLayout == null) return false;

        if (config.getOverflowMode() == OverflowMode.SCROLL
            && scrollController.mouseDragged(mouseX, mouseY, button))
            return true;

        for (LayoutResult.PositionedElement pe : visibleNormalElements()) {
            if (pe.element().mouseDragged(this, mouseX, mouseY, button, dragX, dragY)) return true;
        }
        for (LayoutResult.PositionedElement pe : currentLayout.getFixedElements()) {
            if (pe.element().mouseDragged(this, mouseX, mouseY, button, dragX, dragY)) return true;
        }
        return false;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        ensureLayout();
        if (currentLayout == null) return false;

        if (config.getOverflowMode() == OverflowMode.SCROLL) {
            if (scrollController.isVisible()) {
                if (delta < 0) {
                    scrollDownOneRow();
                } else {
                    scrollUpOneRow();
                }
                return true;
            }
            return false;
        }

        // Pagination via scroll wheel (controlled by config)
        if (ModConfig.ENABLE_SCROLL_PAGINATION.get() && paginationController.isVisible()) {
            if (delta < 0) return paginationController.nextPage();
            if (delta > 0) return paginationController.prevPage();
        }

        for (LayoutResult.PositionedElement pe : visibleNormalElements()) {
            if (pe.element().mouseScrolled(this, mouseX, mouseY, delta)) return true;
        }
        for (LayoutResult.PositionedElement pe : currentLayout.getFixedElements()) {
            if (pe.element().mouseScrolled(this, mouseX, mouseY, delta)) return true;
        }
        return false;
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        ensureLayout();
        if (currentLayout == null) return false;

        if (config.getOverflowMode() == OverflowMode.SCROLL) {
            // Scroll via keyboard (up/down arrows)
            if (scrollController.isVisible()) {
                if (keyCode == 265) { // up arrow
                    scrollUpOneRow();
                    return true;
                }
                if (keyCode == 264) { // down arrow
                    scrollDownOneRow();
                    return true;
                }
            }
        } else if (paginationController.isVisible()) {
            if (qikahome.autosizedgui.KeyBindings.PREV_PAGE.matches(keyCode, scanCode))
                return paginationController.prevPage();
            if (qikahome.autosizedgui.KeyBindings.NEXT_PAGE.matches(keyCode, scanCode))
                return paginationController.nextPage();
        }

        for (LayoutResult.PositionedElement pe : visibleNormalElements()) {
            if (pe.element().keyPressed(keyCode, scanCode, modifiers)) return true;
        }
        for (LayoutResult.PositionedElement pe : currentLayout.getFixedElements()) {
            if (pe.element().keyPressed(keyCode, scanCode, modifiers)) return true;
        }
        return false;
    }

    public boolean charTyped(char codePoint, int modifiers) {
        ensureLayout();
        if (currentLayout == null) return false;

        for (LayoutResult.PositionedElement pe : visibleNormalElements()) {
            if (pe.element().charTyped(codePoint, modifiers)) return true;
        }
        for (LayoutResult.PositionedElement pe : currentLayout.getFixedElements()) {
            if (pe.element().charTyped(codePoint, modifiers)) return true;
        }
        return false;
    }

    // ========== Query ==========

    public int getCurrentPage() {
        return paginationController.getCurrentPage();
    }

    public int getTotalPages() {
        return currentLayout != null ? currentLayout.getTotalPages() : 1;
    }

    public LayoutResult getCurrentLayout() {
        return currentLayout;
    }

    /** Get the current scroll offset. */
    public int getScrollOffset() {
        return scrollController.getScrollOffset();
    }

    /**
     * Left edge of the background (includes 9-patch border).
     * Useful for setting {@code AbstractContainerScreen.leftPos}.
     */
    public int getLayoutLeft() {
        return bgX;
    }

    /**
     * Top edge of the background (includes 9-patch border).
     * Useful for setting {@code AbstractContainerScreen.topPos}.
     */
    public int getLayoutTop() {
        return bgY;
    }

    /**
     * Full background width (includes 9-patch border).
     * Useful for setting {@code AbstractContainerScreen.imageWidth}.
     */
    public int getLayoutWidth() {
        return bgWidth;
    }

    /**
     * Full background height (includes 9-patch border).
     * Useful for setting {@code AbstractContainerScreen.imageHeight}.
     */
    public int getLayoutHeight() {
        return bgHeight;
    }
}
