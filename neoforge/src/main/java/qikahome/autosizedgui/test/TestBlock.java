package qikahome.autosizedgui.test;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * NeoForge 侧测试方块：右键打开 AutoSizedGUI 测试容器。
 * <p>
 * 打开容器带 extra data（档位）走 NeoForge 的 {@code openMenu(provider, bufWriter)} 扩展；
 * Fabric 侧因打开 API 不同（ExtendedScreenHandlerFactory）各自实现本类。
 */
public class TestBlock extends Block {

    private final int slotCount;

    public TestBlock(int slotCount, Properties properties) {
        super(properties);
        this.slotCount = slotCount;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (player instanceof ServerPlayer serverPlayer) {
            int count = slotCount;
            serverPlayer.openMenu(new MenuProvider() {
                @Override
                public Component getDisplayName() {
                    return Component.literal("Test (" + count + " slots)");
                }

                @Override
                public AbstractContainerMenu createMenu(int id, Inventory inv, Player p) {
                    return new TestContainer(ModRegistries.TEST_MENU.get(), id, inv, count);
                }
            }, buf -> buf.writeVarInt(count));
        }
        return InteractionResult.CONSUME;
    }
}
