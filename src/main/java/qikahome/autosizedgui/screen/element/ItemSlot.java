package qikahome.autosizedgui.screen.element;

import javax.annotation.Nonnull;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import qikahome.autosizedgui.AutoSizedGUI;
import qikahome.autosizedgui.api.ILayoutElement;

public class ItemSlot extends Slot implements ILayoutElement {
    private static final int SLOT_SIZE = 18;
    private boolean active = true;

    @Nonnull
    public static ItemSlot of(Slot slot){
        if(slot instanceof ItemSlot iSlot){
            return iSlot;
        }
        return new ItemSlot(slot.container, slot.getContainerSlot(), slot.index);
    }

    public ItemSlot(Container container, int containerSlot, int index) {
        super(container, containerSlot, 0, 0);
        this.index = index;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public int getWidth() {
        return SLOT_SIZE;
    }

    @Override
    public int getHeight() {
        return SLOT_SIZE;
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    @Override
    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        // Render slot from texture (0, 16) in gui.png
        g.blit(AutoSizedGUI.BUILT_IN_GUI_TEXTURE, x, y, SLOT_SIZE, SLOT_SIZE, 0, 16, SLOT_SIZE, SLOT_SIZE, 256, 128);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return container.canPlaceItem(this.getContainerSlot(), stack) && super.mayPlace(stack);
    }
}
