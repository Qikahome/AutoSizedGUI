package qikahome.autosizedgui.test;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * A configurable test container with a dynamic number of slots (1–54).
 */
public class TestContainer extends AbstractContainerMenu {

    public final int containerSlots;
    private final Container dummyContainer;

    /**
     * Server-side: container with explicit slot count.
     * Also used by the client-side factory via buffer.
     */
    public TestContainer(int id, Inventory playerInventory, int slotCount) {
        super(ModRegistries.TEST_MENU.get(), id);
        this.containerSlots = slotCount;
        this.dummyContainer = new SimpleContainer(slotCount);

        // Test inventory: dynamically sized, up to 6 rows of 9
        for (int i = 0; i < slotCount; i++) {
            addSlot(new Slot(dummyContainer, i, 0, 0));
        }

        // Player inventory: 3 rows + hotbar
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
            // Container -> player inventory
            if (!moveItemStackTo(stack, containerSlots, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else {
            // Player inventory -> container
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
