package qikahome.autosizedgui.screen;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import qikahome.autosizedgui.AutoSizedGUI;
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
            menu.slots.set(i, ItemSlot.of(menu.slots.get(i)));
        }
    }

    protected final int containerSize;
    protected final AutoLayoutPanel panel = new AutoLayoutPanel();

    @Override
    protected void init() {
        super.init();
        panel.clearElements();
        populatePanel();
        panel.reflow(width, height);

        this.leftPos = 1;
        this.topPos = 1;
        this.imageWidth = panel.getLayoutWidth();
        this.imageHeight = panel.getLayoutHeight();
    }

    protected void populatePanel() {
        panel.setBackground(new NinePatchRenderer(
                AutoSizedGUI.BUILT_IN_GUI_TEXTURE,
                0, 0,
                15, 15,
                256, 128,
                7, 7, 7, 7));

        panel.addElement(new TitleBar(title));

        for (int i = 0; i < containerSize; i++) {
            panel.addElement((ItemSlot) menu.slots.get(i));
        }
        List<ItemSlot> slots = new ArrayList<>();
        for (int i = containerSize; i < menu.slots.size(); i++) {
            slots.add((ItemSlot) menu.slots.get(i));
        }
        panel.addElement(new PlayerInventory(playerInventoryTitle, slots));
    }

    @Override
    public void render(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        // super.render() calls this.renderBackground() → dark overlay + panel bg &
        // elements,
        // then renders item icons in the correct scrolled positions (panel syncs them).
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // Title is rendered by TitleBar element — nothing to do here
    }

    @Override
    protected void renderBg(@Nonnull GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        panel.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    // ========== Input delegation ==========

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (panel.mouseClicked(mouseX, mouseY, button))
            return true;
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (panel.mouseReleased(mouseX, mouseY, button))
            return true;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (panel.mouseDragged(mouseX, mouseY, button, dragX, dragY))
            return true;
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
        if (panel.mouseScrolled(mouseX, mouseY, deltaY))
            return true;
        return super.mouseScrolled(mouseX, mouseY, deltaX, deltaY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (panel.keyPressed(keyCode, scanCode, modifiers))
            return true;
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (panel.charTyped(codePoint, modifiers))
            return true;
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public int getGuiLeft() {
        return panel.getLayoutLeft();
    }

    @Override
    public int getGuiTop() {
        return panel.getLayoutTop();
    }

}
