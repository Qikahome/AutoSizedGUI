package qikahome.autosizedgui.widget;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;

/**
 * Handles pagination state and renders prev/next buttons.
 * <p>
 * Buttons are rendered centered at the bottom of the panel area.
 */
public class PaginationController {

    private int currentPage;
    private int totalPages;
    private final Button prevButton;
    private final Button nextButton;
    private int buttonY;
    private int centerX;
    private boolean visible;

    public PaginationController() {
        this.prevButton = Button.builder(Component.literal("<"), this::onPrev)
                .size(20, 14)
                .build();
        this.nextButton = Button.builder(Component.literal(">"), this::onNext)
                .size(20, 14)
                .build();
        this.currentPage = 0;
        this.totalPages = 1;
        this.visible = false;
    }

    /** Recalculate button positions based on panel geometry. */
    public void updateBounds(int centerX, int buttonY) {
        this.centerX = centerX;
        this.buttonY = buttonY;
        int textLen = String.valueOf(totalPages).length() * 6;
        prevButton.setPosition(centerX - 25 - textLen, buttonY);
        nextButton.setPosition(centerX + 5 + textLen, buttonY);
    }

    /**
     * Set total page count (must be >= 1). Reset to page 0 if current is out of
     * range.
     */
    public void setTotalPages(int totalPages) {
        this.totalPages = Math.max(1, totalPages);
        if (currentPage >= this.totalPages) {
            currentPage = this.totalPages - 1;
        }
        this.visible = this.totalPages > 1;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public boolean isVisible() {
        return visible;
    }

    public boolean nextPage() {
        if (currentPage < totalPages - 1) {
            currentPage++;
            return true;
        }
        return false;
    }

    public boolean prevPage() {
        if (currentPage > 0) {
            currentPage--;
            return true;
        }
        return false;
    }

    private void onPrev(Button btn) {
        prevPage();
    }

    private void onNext(Button btn) {
        nextPage();
    }

    /** Render the pagination buttons. */
    public void render(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        if (!visible)
            return;
        String pageText = (currentPage + 1) + "/" + totalPages;
        guiGraphics.centeredText(
                net.minecraft.client.Minecraft.getInstance().font,
                pageText,
                centerX,
                buttonY + 3,
                0xFFFFFFFF);
        prevButton.extractRenderState(guiGraphics, mouseX, mouseY, partialTicks);
        nextButton.extractRenderState(guiGraphics, mouseX, mouseY, partialTicks);
    }

    /** Delegate mouse click to buttons. */
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (!visible)
            return false;
        if (prevButton.mouseClicked(event, doubleClick))
            return true;
        if (nextButton.mouseClicked(event, doubleClick))
            return true;
        return false;
    }
}
