package qikahome.autosizedgui.test;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class TestContainer extends AbstractContainerMenu {

    public final int containerSlots;
    private final Container dummyContainer;

    public TestContainer(int id, Inventory playerInventory, int slotCount) {
        super(ModRegistries.TEST_MENU.get(), id);
        this.containerSlots = slotCount;
        this.dummyContainer = new SimpleContainer(slotCount);

        for (int i = 0; i < slotCount; i++) {
            addSlot(new Slot(dummyContainer, i, 0, 0));
        }

        for (int col = 0; col < 36; col++) {
            addSlot(new Slot(playerInventory, col, 0, 0));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;

        ItemStack stack = slot.getItem();
        ItemStack result = stack.copy();

        if (index < containerSlots) {
            if (!moveItemStackTo(stack, containerSlots, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else {
            if (!moveItemStackTo(stack, 0, containerSlots, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return result;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
