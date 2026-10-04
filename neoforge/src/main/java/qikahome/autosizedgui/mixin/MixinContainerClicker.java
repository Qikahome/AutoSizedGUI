package qikahome.autosizedgui.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import org.anti_ad.mc.ipnext.inventory.ContainerClicker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import qikahome.autosizedgui.screen.AutoSizedContainerScreen;
import qikahome.autosizedgui.screen.element.ItemSlot;

/**
 * Inventory Profiles Next 兼容：阻止 IPN 对"当前不可见槽位"的 QUICK_MOVE / THROW。
 * <p>
 * IPN 的 swipe move（Shift+左键拖动）会遍历 {@code ContainerClicker.shiftClick} 的入参
 * 所在菜单槽，并按 {@code Slot.x/y} 做命中测试（不检查可见性）。AutoSizedGUI 滚动时
 * 隐藏槽位仍保留在 {@code menu.slots} 中，会被误命中而移动。这里在 IPN 真正发点击包前，
 * 若当前界面是我们的 {@link AutoSizedContainerScreen} 且目标槽位未显示则取消。
 * <p>
 * 仅在 {@link AutoSizedContainerScreen} 内生效，其它界面（含 IPN 自身的排序/合成/交易等）
 * 一律放行。1.21.1/26.x 所有槽位恒为 active，用 {@link ItemSlot#isInViewport()} 判定显示。
 */
@Pseudo
@Mixin(ContainerClicker.class)
public class MixinContainerClicker {

    @Inject(method = "shiftClick", at = @At("HEAD"), cancellable = true, remap = false)
    private void autosizedgui$guardQuickMove(int slotId, CallbackInfo ci) {
        if (autosizedgui$isHiddenSlotAction(slotId)) {
            ci.cancel();
        }
    }

    @Inject(method = "qClick", at = @At("HEAD"), cancellable = true, remap = false)
    private void autosizedgui$guardThrow(int slotId, CallbackInfo ci) {
        if (autosizedgui$isHiddenSlotAction(slotId)) {
            ci.cancel();
        }
    }

    private static boolean autosizedgui$isHiddenSlotAction(int slotId) {
        Screen screen = Minecraft.getInstance().screen;
        if (!(screen instanceof AutoSizedContainerScreen<?> autoScreen)) {
            return false;
        }
        AbstractContainerMenu menu = autoScreen.getMenu();
        if (slotId < 0 || slotId >= menu.slots.size()) {
            return false;
        }
        Slot slot = menu.slots.get(slotId);
        return slot instanceof ItemSlot itemSlot && !itemSlot.isInViewport();
    }
}
