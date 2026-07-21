package qikahome.autosizedgui.api;

import java.util.List;

/**
 * The result of a layout calculation pass.
 * Contains positioned elements, page information, and content dimensions.
 */
public class LayoutResult {

    private final List<PositionedElement> normalElements;
    private final List<PositionedElement> fixedElements;
    private final int columns;
    private final int rowsPerPage;
    private final int totalPages;
    private final int contentWidth;
    private final int flowAreaHeight;
    private final int flowAreaLeft;
    private final int flowAreaTop;
    private final int contentMinX;
    private final int contentMinY;
    private final int contentMaxX;
    private final int contentMaxY;

    public LayoutResult(List<PositionedElement> normalElements,
                        List<PositionedElement> fixedElements,
                        int columns, int rowsPerPage, int totalPages,
                        int contentWidth, int flowAreaHeight,
                        int flowAreaLeft, int flowAreaTop,
                        int contentMinX, int contentMinY,
                        int contentMaxX, int contentMaxY) {
        this.normalElements = normalElements;
        this.fixedElements = fixedElements;
        this.columns = columns;
        this.rowsPerPage = rowsPerPage;
        this.totalPages = totalPages;
        this.contentWidth = contentWidth;
        this.flowAreaHeight = flowAreaHeight;
        this.flowAreaLeft = flowAreaLeft;
        this.flowAreaTop = flowAreaTop;
        this.contentMinX = contentMinX;
        this.contentMinY = contentMinY;
        this.contentMaxX = contentMaxX;
        this.contentMaxY = contentMaxY;
    }

    /** Get the elements (with positions) that belong to the given page. */
    public List<PositionedElement> getElementsForPage(int page) {
        if (page < 0 || page >= totalPages) return List.of();
        if (columns < 0) {
            // Flex layout: each element has an explicit page assignment
            return normalElements.stream()
                    .filter(pe -> pe.page() == page)
                    .collect(java.util.stream.Collectors.toList());
        }
        // Uniform grid: use formula for fast lookup
        int start = page * rowsPerPage * columns;
        int end = Math.min(start + (rowsPerPage * columns), normalElements.size());
        if (start >= normalElements.size()) return List.of();
        return normalElements.subList(start, end);
    }

    /** All normal elements with their computed positions. */
    public List<PositionedElement> getNormalPositions() {
        return normalElements;
    }

    /** All fixed elements (visible on every page). */
    public List<PositionedElement> getFixedElements() {
        return fixedElements;
    }

    // -- Getters --

    public int getColumns() { return columns; }

    public int getRowsPerPage() { return rowsPerPage; }

    public int getTotalPages() { return totalPages; }

    public int getContentWidth() { return contentWidth; }

    public int getFlowAreaHeight() { return flowAreaHeight; }

    public int getFlowAreaLeft() { return flowAreaLeft; }

    public int getFlowAreaTop() { return flowAreaTop; }

    public int getContentMinX() { return contentMinX; }
    public int getContentMinY() { return contentMinY; }
    public int getContentMaxX() { return contentMaxX; }
    public int getContentMaxY() { return contentMaxY; }

    /** An element with its computed position and page assignment. */
    public record PositionedElement(ILayoutElement element, int x, int y, int page) {}
}
