package qikahome.autosizedgui.test;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * 可配置槽数的测试容器（Forge / Fabric 共用，dev-only）。
 * 槽数经菜单打开时的 extra data（Forge buffer / Fabric ExtendedScreenHandlerFactory）
 * 从服务端传给客户端，因此注册单个 MenuType 即可覆盖任意档位。
 */
public class TestContainer extends AbstractContainerMenu {

    public final int containerSlots;
    private final Container dummyContainer;

    public TestContainer(MenuType<? extends TestContainer> type, int id, Inventory playerInventory, int slotCount) {
        super(type, id);
        this.containerSlots = slotCount;
        this.dummyContainer = new SimpleContainer(slotCount);

        // Test inventory: dynamically sized, up to 6 rows of 9
        for (int i = 0; i < slotCount; i++) {
            addSlot(new Slot(dummyContainer, i, 0, 0));
        }

        // Player inventory: 主背包(27)在前、快捷栏(9)在后 —— 与 26.x 顺序一致，
        // 保证 PlayerInventory 布局按 3 主行 + 快捷栏逐行绘制时位置正确
        for (int col = 9; col < 36; col++) {
            addSlot(new Slot(playerInventory, col, 0, 0));
        }

        for (int col = 0; col < 9; col++) {
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
