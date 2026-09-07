package qikahome.autosizedgui.engine;

import java.util.*;
import qikahome.autosizedgui.api.*;

/**
 * The core auto-layout engine.
 * <p>
 * Supports two layout modes:
 * <ul>
 * <li><b>Uniform grid</b> — when all normal elements have the same width and
 * height.</li>
 * <li><b>Flex wrap</b> — when elements have mixed sizes.</li>
 * </ul>
 * Both modes support pagination and fixed elements.
 */
public class LayoutEngine {

    /**
     * Run a full layout calculation.
     * <p>
     * Algorithm:
     * <ol>
     * <li>Reserve space for fixed TOP/BOTTOM elements → compute flow area
     * height</li>
     * <li>Layout normal (flow) elements within the flow area</li>
     * <li>Position fixed elements relative to the <b>actual flow content
     * bounds</b>:
     * <ul>
     * <li>TOP attached above the flow content, stacked upward</li>
     * <li>BOTTOM attached below the flow content, stacked downward</li>
     * <li>LEFT at the flow content's left edge</li>
     * <li>RIGHT at the flow content's right edge</li>
     * </ul>
     * </li>
     * <li>Center the entire assembly (TOP + flow + BOTTOM) vertically within
     * maxHeight</li>
     * </ol>
     */
    public LayoutResult calculate(List<? extends ILayoutElement> elements,
            LayoutConfig config,
            int screenWidth, int screenHeight) {

        int maxWidth = config.resolveMaxWidth(screenWidth);
        int maxHeight = config.resolveMaxHeight(screenHeight);

        // ---- 1. Separate fixed vs normal elements ----
        List<ILayoutElement> fixedElements = new ArrayList<>();
        List<ILayoutElement> normalElements = new ArrayList<>();
        for (ILayoutElement e : elements) {
            if (e.getAttachPosition() != AttachPosition.NONE) {
                fixedElements.add(e);
            } else {
                normalElements.add(e);
            }
        }

        // ---- 2. Measure fixed element heights ----
        int fixedTopHeight = measureFixedTopHeight(fixedElements);
        int fixedBottomHeight = measureFixedBottomHeight(fixedElements);

        // ---- 3. Compute planned area bounds (centered on screen) ----
        int areaLeft = (screenWidth - maxWidth) / 2;
        int areaTop = (screenHeight - maxHeight) / 2;

        int flowAreaHeight = maxHeight - fixedTopHeight - fixedBottomHeight;
        // Ensure at least one row can fit when normal elements exist
        int minRowHeight = config.getDefaultSlotSize() + config.getElementSpacing();
        if (!normalElements.isEmpty() && flowAreaHeight < minRowHeight) {
            flowAreaHeight = minRowHeight;
        } else if (flowAreaHeight < 1) {
            flowAreaHeight = 1;
        }
        int flowAreaTop = areaTop + fixedTopHeight;

        // ---- 4. Early return if no normal elements ----
        int viewportBottom = areaTop + maxHeight;
        if (normalElements.isEmpty()) {
            List<LayoutResult.PositionedElement> fixedPos = layoutFixedElements(
                    fixedElements, flowAreaTop, flowAreaTop, 0, 0,
                    maxHeight, areaLeft, areaTop, areaLeft, viewportBottom);
            int[] b = computeContentBounds(List.of(), fixedPos, false);
            return new LayoutResult(List.of(), fixedPos,
                    0, 0, 1, 0, flowAreaHeight,
                    areaLeft, flowAreaTop,
                    b[0], b[1], b[2], b[3]);
        }

        // ---- 5. Route to appropriate layout mode ----
        boolean uniform = areUniform(normalElements);
        LayoutResult result;

        if (uniform) {
            result = layoutUniform(normalElements, config, maxWidth, flowAreaHeight, screenWidth, flowAreaTop);
        } else {
            result = layoutFlex(normalElements, config, maxWidth, flowAreaHeight, screenWidth, flowAreaTop);
        }

        // ---- 6. Compute reference flow content bounds ----
        // These define where TOP/BOTTOM/LEFT/RIGHT attach to.
        boolean scrollMode = config.getOverflowMode() == OverflowMode.SCROLL;
        int flowContentTop;
        int flowContentBottom;
        int flowContentLeft = result.getFlowAreaLeft();
        int flowContentRight = flowContentLeft + result.getContentWidth();

        if (scrollMode) {
            // Scroll mode: fixed elements attach to viewport bounds, not content bounds
            flowContentTop = flowAreaTop;
            flowContentBottom = flowAreaTop + flowAreaHeight;
        } else if (uniform && result.getTotalPages() > 0) {
            int slotSize = config.getDefaultSlotSize();
            int spacing = config.getElementSpacing();
            int cellSize = slotSize + spacing;
            int rowsPerPage = result.getRowsPerPage();
            int contentHeightPerPage = rowsPerPage * cellSize - spacing;
            int vCenterOff = Math.max(0, (flowAreaHeight - contentHeightPerPage) / 2);
            flowContentTop = flowAreaTop + vCenterOff;
            flowContentBottom = flowContentTop + contentHeightPerPage;
        } else {
            // Flex mode: compute from page 0's positioned elements
            int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
            for (LayoutResult.PositionedElement pe : result.getNormalPositions()) {
                if (pe.page() == 0) {
                    int t = pe.y(), b = t + pe.element().getHeight();
                    if (t < minY)
                        minY = t;
                    if (b > maxY)
                        maxY = b;
                }
            }
            if (minY == Integer.MAX_VALUE) {
                minY = flowAreaTop;
                maxY = flowAreaTop;
            }
            flowContentTop = minY;
            flowContentBottom = maxY;
        }

        // ---- 7. Position fixed elements relative to flow content bounds ----
        int flowContentHeight = flowContentBottom - flowContentTop;
        int flowContentWidth = flowContentRight - flowContentLeft;

        List<LayoutResult.PositionedElement> fixedPositions = layoutFixedElements(
                fixedElements,
                flowContentTop, flowContentBottom,
                flowContentHeight, flowContentWidth,
                maxHeight, areaLeft, areaTop,
                flowContentLeft, flowContentBottom);

        // ---- 8. Compute total assembly bounds (TOP + flow + BOTTOM) ----
        int blockTop = flowContentTop;
        int blockBottom = flowContentBottom;
        for (LayoutResult.PositionedElement pe : fixedPositions) {
            int t = pe.y();
            int b = t + pe.element().getHeight();
            if (t < blockTop)
                blockTop = t;
            if (b > blockBottom)
                blockBottom = b;
        }
        int blockHeight = blockBottom - blockTop;

        // ---- 9. Center the entire assembly vertically within maxHeight ----
        // (Scroll mode: no centering, content scrolls naturally)
        int totalVerticalOffset = 0;
        if (!scrollMode && blockHeight < maxHeight) {
            totalVerticalOffset = (maxHeight - blockHeight) / 2 - (blockTop - areaTop);
        }

        if (totalVerticalOffset != 0) {
            // Shift normal elements
            for (LayoutResult.PositionedElement pe : result.getNormalPositions()) {
                pe.element().setPosition(pe.x(), pe.y() + totalVerticalOffset);
            }
            // Shift fixed elements
            for (LayoutResult.PositionedElement pe : fixedPositions) {
                pe.element().setPosition(pe.x(), pe.y() + totalVerticalOffset);
            }

            // Rebuild lists with adjusted positions
            List<LayoutResult.PositionedElement> adjustedNormal = new ArrayList<>();
            for (LayoutResult.PositionedElement pe : result.getNormalPositions()) {
                adjustedNormal.add(new LayoutResult.PositionedElement(
                        pe.element(), pe.x(), pe.y() + totalVerticalOffset, pe.page()));
            }
            List<LayoutResult.PositionedElement> adjustedFixed = new ArrayList<>();
            for (LayoutResult.PositionedElement pe : fixedPositions) {
                adjustedFixed.add(new LayoutResult.PositionedElement(
                        pe.element(), pe.x(), pe.y() + totalVerticalOffset, -1));
            }

            int adjustedFlowAreaTop = (uniform ? flowAreaTop : result.getFlowAreaTop()) + totalVerticalOffset;

            int[] bAdj = computeContentBounds(adjustedNormal, adjustedFixed, scrollMode);
            return new LayoutResult(
                    adjustedNormal, adjustedFixed,
                    result.getColumns(), result.getRowsPerPage(), result.getTotalPages(),
                    result.getContentWidth(), flowAreaHeight,
                    result.getFlowAreaLeft(), adjustedFlowAreaTop,
                    bAdj[0], bAdj[1], bAdj[2], bAdj[3]);
        }

        int[] bNorm = computeContentBounds(result.getNormalPositions(), fixedPositions, scrollMode);
        return new LayoutResult(
                result.getNormalPositions(), fixedPositions,
                result.getColumns(), result.getRowsPerPage(), result.getTotalPages(),
                result.getContentWidth(), flowAreaHeight,
                result.getFlowAreaLeft(), flowAreaTop,
                bNorm[0], bNorm[1], bNorm[2], bNorm[3]);
    }

    // =====================================================================
    // Uniform grid layout
    // =====================================================================

    private LayoutResult layoutUniform(List<ILayoutElement> normalElements,
            LayoutConfig config,
            int maxWidth, int flowMaxHeight,
            int screenWidth, int flowAreaTop) {

        int slotSize = config.getDefaultSlotSize();
        int spacing = config.getElementSpacing();
        int cellSize = slotSize + spacing;
        int totalNormal = normalElements.size();

        int cols = findOptimalColumnCount(normalElements, config, maxWidth, flowMaxHeight);
        int totalRows = (cols == 0) ? 0 : (int) Math.ceil((double) totalNormal / cols);
        int contentHeight = totalRows * cellSize - spacing;
        int contentWidth = cols * cellSize - spacing;

        boolean overflow = contentHeight > flowMaxHeight;
        boolean scrollMode = config.getOverflowMode() == OverflowMode.SCROLL;
        int rowsPerPage;
        int totalPages;

        if (overflow && !scrollMode) {
            rowsPerPage = flowMaxHeight / cellSize;
            if (rowsPerPage <= 0)
                rowsPerPage = 1;
            totalPages = (int) Math.ceil((double) totalRows / rowsPerPage);
        } else {
            rowsPerPage = totalRows;
            totalPages = 1;
        }

        int margin = config.getMaxWidth() < 0 ? -config.getMaxWidth() : 0;
        int flowAreaLeft = (screenWidth - contentWidth) / 2;
        if (flowAreaLeft < margin)
            flowAreaLeft = margin;

        List<LayoutResult.PositionedElement> normalPositions = new ArrayList<>(totalNormal);
        int elementsPerPage = rowsPerPage * cols;

        // Per-page vertical centering within the flow area
        // (Scroll mode: content starts from top, no centering)
        int contentHeightPerPage = rowsPerPage * cellSize - spacing;
        int verticalCenterOffset = scrollMode ? 0 : Math.max(0, (flowMaxHeight - contentHeightPerPage) / 2);

        for (int i = 0; i < totalNormal; i++) {
            int col = i % cols;
            int rowInPage = (i % elementsPerPage) / cols;
            int page = i / elementsPerPage;
            int x = flowAreaLeft + col * cellSize;
            int y = flowAreaTop + verticalCenterOffset + rowInPage * cellSize;
            ILayoutElement el = normalElements.get(i);
            el.setPosition(x, y);
            normalPositions.add(new LayoutResult.PositionedElement(el, x, y, page));
        }

        return new LayoutResult(
                normalPositions, List.of(),
                cols, rowsPerPage, totalPages,
                contentWidth, flowMaxHeight,
                flowAreaLeft, flowAreaTop,
                0, 0, 0, 0); // intermediate result; bounds set in calculate()
    }

    // =====================================================================
    // Column packing layout (mixed sizes, compact)
    // =====================================================================

    private LayoutResult layoutFlex(List<ILayoutElement> normalElements,
            LayoutConfig config,
            int maxWidth, int flowMaxHeight,
            int screenWidth, int flowAreaTop) {

        int spacing = config.getElementSpacing();
        int minCols = config.getMinColumns();
        int slotSize = 18;

        int minContentWidth = minCols * slotSize;
        int maxContentWidth = maxWidth;
        int step = slotSize;

        int optimalWidth = findOptimalPackWidth(normalElements, minContentWidth,
                maxContentWidth, step, flowMaxHeight, slotSize, spacing);

        int totalHeight = simulatePackHeight(normalElements, optimalWidth, slotSize, spacing);
        boolean overflow = totalHeight > flowMaxHeight;
        boolean usePagination = overflow && config.getOverflowMode() != OverflowMode.SCROLL;

        List<LayoutResult.PositionedElement> positions = layoutColumnPack(
                normalElements, optimalWidth, flowMaxHeight, slotSize, spacing,
                screenWidth, flowAreaTop, usePagination);

        Set<Integer> pages = new HashSet<>();
        for (LayoutResult.PositionedElement p : positions)
            pages.add(p.page());
        int totalPages = pages.size();

        int margin = config.getMaxWidth() < 0 ? -config.getMaxWidth() : 0;
        int flowAreaLeft = (screenWidth - optimalWidth) / 2;
        if (flowAreaLeft < margin)
            flowAreaLeft = margin;

        return new LayoutResult(
                positions, List.of(),
                -1, -1, totalPages,
                optimalWidth, flowMaxHeight,
                flowAreaLeft, flowAreaTop,
                0, 0, 0, 0); // intermediate result; bounds set in calculate()
    }

    private int findOptimalPackWidth(List<ILayoutElement> elements,
            int minWidth, int maxWidth, int step,
            int flowMaxHeight, int slotSize, int spacing) {
        for (int w = minWidth; w <= maxWidth; w += step) {
            int h = simulatePackHeight(elements, w, slotSize, spacing);
            if (h <= flowMaxHeight)
                return w;
        }
        return maxWidth;
    }

    private int simulatePackHeight(List<ILayoutElement> elements, int width,
            int slotSize, int spacing) {
        int cols = Math.max(1, width / slotSize);
        int[] colY = new int[cols];

        for (ILayoutElement e : elements) {
            int ew = e.getWidth();
            int eh = e.getHeight();
            int needCols = Math.max(1, (ew + slotSize - 1) / slotSize);
            if (needCols > cols)
                needCols = cols;
            int needHeight = eh;

            int bestCol = 0;
            int bestY = Integer.MAX_VALUE;
            for (int c = 0; c <= cols - needCols; c++) {
                int maxY = 0;
                for (int k = 0; k < needCols; k++) {
                    if (colY[c + k] > maxY)
                        maxY = colY[c + k];
                }
                if (maxY < bestY) {
                    bestY = maxY;
                    bestCol = c;
                }
            }

            int placeY = bestY;
            for (int k = 0; k < needCols; k++) {
                colY[bestCol + k] = placeY + needHeight + spacing;
            }
        }

        int maxH = 0;
        for (int y : colY)
            if (y > maxH)
                maxH = y;
        return maxH - spacing;
    }

    private List<LayoutResult.PositionedElement> layoutColumnPack(
            List<ILayoutElement> elements, int width, int flowMaxHeightPerPage,
            int slotSize, int spacing, int screenWidth, int flowAreaTop,
            boolean usePagination) {

        int cols = Math.max(1, width / slotSize);
        int[] colY = new int[cols];
        int page = 0;

        int margin = 2;
        int flowAreaLeft = (screenWidth - width) / 2;
        if (flowAreaLeft < margin)
            flowAreaLeft = margin;

        List<LayoutResult.PositionedElement> result = new ArrayList<>(elements.size());
        int[] pageContentBottom = new int[elements.size() + 1];

        for (ILayoutElement e : elements) {
            int ew = e.getWidth();
            int eh = e.getHeight();
            int needCols = Math.max(1, (ew + slotSize - 1) / slotSize);
            int needHeight = eh;
            int maxAllowedCol = cols - needCols;
            if (needCols > cols) {
                needCols = cols;
                maxAllowedCol = 0;
            }

            int bestCol = findBestCol(colY, maxAllowedCol, needCols);
            int bestY = bestCol < 0 ? 0 : computeMaxColY(colY, bestCol, needCols);

            if (usePagination && bestY + needHeight > flowMaxHeightPerPage) {
                page++;
                java.util.Arrays.fill(colY, 0);
                bestCol = findBestCol(colY, maxAllowedCol, needCols);
                bestY = bestCol < 0 ? 0 : computeMaxColY(colY, bestCol, needCols);
            }

            int x = flowAreaLeft + bestCol * slotSize;
            int absoluteY = flowAreaTop + bestY;
            e.setPosition(x, absoluteY);
            result.add(new LayoutResult.PositionedElement(e, x, absoluteY, page));

            int elementBottom = bestY + needHeight;
            if (elementBottom > pageContentBottom[page]) {
                pageContentBottom[page] = elementBottom;
            }

            int newY = bestY + needHeight + spacing;
            for (int k = 0; k < needCols; k++) {
                colY[bestCol + k] = newY;
            }
        }

        // Per-page vertical centering within the flow area
        for (int p = 0; p <= page; p++) {
            int contentBottom = pageContentBottom[p];
            if (contentBottom > 0 && contentBottom < flowMaxHeightPerPage) {
                int centerOffset = (flowMaxHeightPerPage - contentBottom) / 2;
                for (LayoutResult.PositionedElement pe : result) {
                    if (pe.page() == p) {
                        int newY = pe.y() + centerOffset;
                        pe.element().setPosition(pe.x(), newY);
                    }
                }
            }
        }

        return result;
    }

    private int findBestCol(int[] colY, int maxAllowedCol, int needCols) {
        if (maxAllowedCol < 0)
            return 0;
        int bestCol = 0;
        int bestY = Integer.MAX_VALUE;
        for (int c = 0; c <= maxAllowedCol; c++) {
            int maxY = computeMaxColY(colY, c, needCols);
            if (maxY < bestY) {
                bestY = maxY;
                bestCol = c;
            }
        }
        return bestCol;
    }

    private int computeMaxColY(int[] colY, int startCol, int needCols) {
        int maxY = 0;
        for (int k = 0; k < needCols; k++) {
            if (colY[startCol + k] > maxY)
                maxY = colY[startCol + k];
        }
        return maxY;
    }

    // =====================================================================
    // Column optimization (for uniform layout)
    // =====================================================================

    private int findOptimalColumnCount(List<ILayoutElement> normalElements,
            LayoutConfig config,
            int maxWidth, int flowMaxHeight) {

        int minCols = config.getMinColumns();
        int maxCols = config.getMaxColumns();
        int maxRows = config.getMaxRows();
        int slotSize = config.getDefaultSlotSize();
        int spacing = config.getElementSpacing();
        int total = normalElements.size();

        if (maxCols >= 0)
            return Math.max(minCols, maxCols);

        int cellSize = slotSize + spacing;
        int maxColsByWidth = maxWidth / cellSize;
        if (maxColsByWidth <= 0)
            maxColsByWidth = 1;

        for (int cols = minCols; cols <= maxColsByWidth; cols++) {
            int rows = (int) Math.ceil((double) total / cols);
            if (maxRows >= 0) {
                if (rows <= maxRows)
                    return cols;
                continue;
            }
            int contentHeight = rows * cellSize - spacing;
            if (contentHeight <= flowMaxHeight)
                return cols;
        }

        return maxColsByWidth;
    }

    // =====================================================================
    // Helpers
    // =====================================================================

    private boolean areUniform(List<ILayoutElement> elements) {
        if (elements.isEmpty())
            return true;
        int w = elements.get(0).getWidth();
        int h = elements.get(0).getHeight();
        for (int i = 1; i < elements.size(); i++) {
            if (elements.get(i).getWidth() != w || elements.get(i).getHeight() != h) {
                return false;
            }
        }
        return true;
    }

    private int measureFixedTopHeight(List<ILayoutElement> fixed) {
        int h = 0;
        for (ILayoutElement e : fixed) {
            AttachPosition pos = e.getAttachPosition();
            if (pos == AttachPosition.TOP_LEFT ||
                    pos == AttachPosition.TOP ||
                    pos == AttachPosition.TOP_RIGHT) {
                h += e.getHeight();
            }
        }
        return h;
    }

    private int measureFixedBottomHeight(List<ILayoutElement> fixed) {
        int h = 0;
        for (ILayoutElement e : fixed) {
            AttachPosition pos = e.getAttachPosition();
            if (pos == AttachPosition.BOTTOM_LEFT ||
                    pos == AttachPosition.BOTTOM ||
                    pos == AttachPosition.BOTTOM_RIGHT) {
                h += e.getHeight();
            }
        }
        return h;
    }

    /**
     * Position fixed elements relative to the flow content bounds.
     * <p>
     * Within each group, elements are placed in
     * {@link ILayoutElement#getPriority()} order
     * (ascending). Higher-priority elements are placed further from the flow
     * content anchor.
     * <p>
     * TOP: stack upward from {@code flowContentTop}
     * BOTTOM: stack downward within the reserved block at the bottom of the
     * viewport
     * LEFT: stacked rightward from {@code flowContentLeft}, vertically centered
     * RIGHT: stacked leftward from {@code flowContentLeft + flowContentWidth},
     * vertically centered
     */
    private List<LayoutResult.PositionedElement> layoutFixedElements(
            List<ILayoutElement> fixedElements,
            int flowContentTop, int flowContentBottom,
            int flowContentHeight, int flowContentWidth,
            int maxHeight, int areaLeft, int areaTop,
            int flowContentLeft,
            int viewportBottom) {

        if (fixedElements.isEmpty())
            return List.of();

        java.util.Comparator<ILayoutElement> byPriority = java.util.Comparator
                .comparingInt(ILayoutElement::getPriority);

        List<LayoutResult.PositionedElement> result = new ArrayList<>();

        // Sort fixed elements by type
        List<ILayoutElement> topElements = new ArrayList<>();
        List<ILayoutElement> bottomElements = new ArrayList<>();
        List<ILayoutElement> leftElements = new ArrayList<>();
        List<ILayoutElement> rightElements = new ArrayList<>();

        for (ILayoutElement e : fixedElements) {
            switch (e.getAttachPosition()) {
                case TOP_LEFT, TOP, TOP_RIGHT -> topElements.add(e);
                case BOTTOM_LEFT, BOTTOM, BOTTOM_RIGHT -> bottomElements.add(e);
                case LEFT -> leftElements.add(e);
                case RIGHT -> rightElements.add(e);
            }
        }

        // Sort each group by priority (ascending)
        topElements.sort(byPriority);
        bottomElements.sort(byPriority);
        leftElements.sort(byPriority);
        rightElements.sort(byPriority);

        // ---- TOP: stack upward from the top of flow content ----
        // Low priority first → closest to flow content
        int topY = flowContentTop;
        for (ILayoutElement e : topElements) {
            topY -= e.getHeight();
            int x = getFixedX(e, flowContentWidth, flowContentLeft);
            e.setPosition(x, topY);
            result.add(new LayoutResult.PositionedElement(e, x, topY, -1));
        }

        // ---- BOTTOM: stack upward from viewport bottom ----
        int bottomY = viewportBottom;
        for (ILayoutElement e : bottomElements) {
            int x = getFixedX(e, flowContentWidth, flowContentLeft);
            e.setPosition(x, bottomY);
            result.add(new LayoutResult.PositionedElement(e, x, bottomY, -1));
            bottomY += e.getHeight();
        }

        // ---- LEFT: at flow content left edge, stacked to the right, centered
        // vertically ----
        int leftX = flowContentLeft;
        for (ILayoutElement e : leftElements) {
            int y = flowContentTop + (flowContentHeight - e.getHeight()) / 2;
            e.setPosition(leftX, y);
            result.add(new LayoutResult.PositionedElement(e, leftX, y, -1));
            leftX += e.getWidth();
        }

        // ---- RIGHT: at flow content right edge, stacked to the left, centered
        // vertically ----
        int rightX = flowContentLeft + flowContentWidth;
        for (ILayoutElement e : rightElements) {
            rightX -= e.getWidth();
            int y = flowContentTop + (flowContentHeight - e.getHeight()) / 2;
            e.setPosition(rightX, y);
            result.add(new LayoutResult.PositionedElement(e, rightX, y, -1));
        }

        return result;
    }

    /**
     * Compute X for a fixed element, relative to the flow content's horizontal
     * bounds.
     */
    private int getFixedX(ILayoutElement e, int flowContentWidth, int flowContentLeft) {
        return switch (e.getAttachPosition()) {
            case TOP_LEFT, LEFT -> flowContentLeft;
            case TOP, BOTTOM -> flowContentLeft + (flowContentWidth - e.getWidth()) / 2;
            case TOP_RIGHT, RIGHT -> flowContentLeft + flowContentWidth - e.getWidth();
            default -> flowContentLeft;
        };
    }

    /**
     * Compute the bounding box of all positioned elements.
     * In scroll mode, normal elements' Y bounds are excluded (background Y
     * is determined only by fixed elements).
     */
    private static int[] computeContentBounds(
            List<LayoutResult.PositionedElement> normalPositions,
            List<LayoutResult.PositionedElement> fixedPositions,
            boolean scrollMode) {
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE;
        for (LayoutResult.PositionedElement pe : normalPositions) {
            int r = pe.x() + pe.element().getWidth();
            if (pe.x() < minX) minX = pe.x();
            if (r > maxX) maxX = r;
            if (!scrollMode) {
                int b = pe.y() + pe.element().getHeight();
                if (pe.y() < minY) minY = pe.y();
                if (b > maxY) maxY = b;
            }
        }
        for (LayoutResult.PositionedElement pe : fixedPositions) {
            int r = pe.x() + pe.element().getWidth();
            int b = pe.y() + pe.element().getHeight();
            if (pe.x() < minX) minX = pe.x();
            if (pe.y() < minY) minY = pe.y();
            if (r > maxX) maxX = r;
            if (b > maxY) maxY = b;
        }
        return new int[]{minX, minY, maxX, maxY};
    }
}
