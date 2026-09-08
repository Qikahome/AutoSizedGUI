package qikahome.autosizedgui.screen.element;

import javax.annotation.Nonnull;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import qikahome.autosizedgui.AutoSizedGUIConstants;
import qikahome.autosizedgui.api.ILayoutElement;

public class ItemSlot extends Slot implements ILayoutElement {
    private static final int SLOT_SIZE = 18;
    private boolean active = true;
    private boolean inViewport = true;

    @Nonnull
    public static ItemSlot of(Slot slot) {
        if (slot instanceof ItemSlot iSlot) {
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

    public void setInViewport(boolean inViewport) {
        this.inViewport = inViewport;
    }

    public boolean isInViewport() {
        return inViewport;
    }

    @Override
    public boolean shouldRender() {
        return inViewport;
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
    public void render(GuiGraphicsExtractor g, int mx, int my, float pt) {
        // Render slot from texture (0, 16) in gui.png
        g.blit(RenderPipelines.GUI_TEXTURED, AutoSizedGUIConstants.BUILT_IN_GUI_TEXTURE,
                x, y, 0, 16, SLOT_SIZE, SLOT_SIZE, SLOT_SIZE, SLOT_SIZE, 256, 128);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return container.canPlaceItem(this.getContainerSlot(), stack) && super.mayPlace(stack);
    }
}
