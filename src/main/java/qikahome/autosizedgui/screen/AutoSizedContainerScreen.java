package qikahome.autosizedgui.screen;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import qikahome.autosizedgui.AutoSizedGUI;
import qikahome.autosizedgui.api.ILayoutElement;
import qikahome.autosizedgui.screen.element.ItemSlot;
import qikahome.autosizedgui.screen.element.PlayerInventory;
import qikahome.autosizedgui.screen.element.TitleBar;
import qikahome.autosizedgui.widget.AutoLayoutPanel;
import qikahome.autosizedgui.widget.NinePatchRenderer;

public class AutoSizedContainerScreen<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> {

    public AutoSizedContainerScreen(T menu, Inventory inventory, Component title) {
        this(menu, inventory, title, menu.slots.size() - 36);
    }

    public AutoSizedContainerScreen(T menu, Inventory inventory, Component title, int containerSize) {
        super(menu, inventory, title);
        this.containerSize = containerSize;

        // Wrap all menu slots with ItemSlot for layout support.
        // Works with any AbstractContainerMenu — no need to inherit a specific base
        // class.
        for (int i = 0; i < menu.slots.size(); i++) {
            menu.slots.set(i, getSlotWrapper(menu.slots.get(i)));
        }
    }

    @SuppressWarnings("unchecked")
    protected <S extends Slot & ILayoutElement> S getSlotWrapper(Slot slot) {
        return (S) ItemSlot.of(slot);
    }

    protected final int containerSize;
    protected final AutoLayoutPanel panel = new AutoLayoutPanel();

    @Override
    protected void init() {
        super.init();
        panel.clearElements();
        populatePanel();
        panel.reflow(width, height);

        this.leftPos = panel.getLayoutLeft();
        this.topPos = panel.getLayoutTop();
        this.imageWidth = panel.getLayoutWidth() - 1;
        this.imageHeight = panel.getLayoutHeight() - 1;

        // Content starts 1px inside the 9-patch background border
        this.leftPos++;
        this.topPos++;

        // Make element positions relative to (leftPos, topPos) so they work within
        // AbstractContainerScreen's translate(leftPos, topPos) coordinate system.
        panel.setOrigin(leftPos - 1, topPos - 1);
    }

    protected void populatePanel() {
        addBackground();
        addTitle();
        addContainerSlots();
        addPlayerInventory();
    }

    protected void addBackground() {
        panel.setBackground(new NinePatchRenderer(
                AutoSizedGUI.BUILT_IN_GUI_TEXTURE,
                0, 0,
                15, 15,
                256, 128,
                7, 7, 7, 7));
    }

    protected void addTitle() {
        panel.addElement(new TitleBar(title));
    }

    protected void addContainerSlots() {
        for (int i = 0; i < containerSize; i++) {
            panel.addElement((ILayoutElement) menu.slots.get(i));
        }
    }

    protected void addPlayerInventory() {
        List<ILayoutElement> slots = new ArrayList<>();
        for (int i = containerSize; i < menu.slots.size(); i++) {
            slots.add((ILayoutElement) menu.slots.get(i));
        }
        panel.addElement(new PlayerInventory(playerInventoryTitle, slots));
    }

    @Override
    public void extractRenderState(@Nonnull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTicks);
    }

    @Override
    protected void extractLabels(@Nonnull GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        // Title is rendered by TitleBar element — nothing to do here
    }

    @Override
    public void extractBackground(@Nonnull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        panel.render(graphics, mouseX, mouseY, partialTick);
    }

    // ========== Slot rendering & interaction (scroll-mode compatibility)
    // ==========
    //
    // In scroll mode, all slots are kept active (for IPN 1.21.1 grid detection)
    // with
    // positions clamped to viewport bounds. These overrides skip off-viewport slots
    // for rendering and interaction so clamped slots don't overlap visually.

    @Override
    protected void extractSlot(@Nonnull GuiGraphicsExtractor graphics, Slot slot, int mouseX, int mouseY) {
        if (slot instanceof ItemSlot is && !is.isInViewport())
            return;
        super.extractSlot(graphics, slot, mouseX, mouseY);
    }

    @Override
    protected boolean isHovering(Slot slot, double mouseX, double mouseY) {
        if (slot instanceof ItemSlot is && !is.isInViewport())
            return false;
        return super.isHovering(slot, mouseX, mouseY);
    }

    // ========== Input delegation ==========

    /**
     * Click-outside detection must use the actual panel position (getGuiLeft/Top),
     * not leftPos/topPos which are only used for slot hit-testing translation.
     * Without this, a centered panel would close on clicks inside the GUI and
     * ignore clicks outside it.
     */
    @Override
    public boolean hasClickedOutside(double mouseX, double mouseY, int guiLeft, int guiTop) {
        int left = getLeftPos();
        int top = getTopPos();
        return mouseX < left || mouseY < top
                || mouseX >= left + this.imageWidth
                || mouseY >= top + this.imageHeight;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (panel.mouseClicked(event, doubleClick))
            return true;
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (panel.mouseReleased(event))
            return true;
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (panel.mouseDragged(event, dragX, dragY))
            return true;
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
        if (panel.mouseScrolled(mouseX, mouseY, deltaY))
            return true;
        return super.mouseScrolled(mouseX, mouseY, deltaX, deltaY);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (panel.keyPressed(event))
            return true;
        return super.keyPressed(event);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        if (panel.charTyped(event))
            return true;
        return super.charTyped(event);
    }

}
