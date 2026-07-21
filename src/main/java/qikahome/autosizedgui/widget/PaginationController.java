package qikahome.autosizedgui.widget;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

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
        prevButton.setPosition(centerX - 30, buttonY);
        nextButton.setPosition(centerX + 10, buttonY);
    }

    /** Set total page count (must be >= 1). Reset to page 0 if current is out of range. */
    public void setTotalPages(int totalPages) {
        this.totalPages = Math.max(1, totalPages);
        if (currentPage >= this.totalPages) {
            currentPage = this.totalPages - 1;
        }
        this.visible = this.totalPages > 1;
    }

    public int getCurrentPage() { return currentPage; }

    public int getTotalPages() { return totalPages; }

    public boolean isVisible() { return visible; }

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
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        if (!visible) return;
        String pageText = (currentPage + 1) + "/" + totalPages;
        int textWidth = 20; // approximate
        guiGraphics.drawString(
                net.minecraft.client.Minecraft.getInstance().font,
                pageText,
                centerX - textWidth / 2,
                buttonY + 3,
                0xFFFFFFFF,
                true
        );
        prevButton.render(guiGraphics, mouseX, mouseY, partialTicks);
        nextButton.render(guiGraphics, mouseX, mouseY, partialTicks);
    }

    /** Delegate mouse click to buttons. */
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!visible) return false;
        if (prevButton.mouseClicked(mouseX, mouseY, button)) return true;
        if (nextButton.mouseClicked(mouseX, mouseY, button)) return true;
        return false;
    }
}
