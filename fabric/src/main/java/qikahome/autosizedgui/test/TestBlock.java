package qikahome.autosizedgui.test;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Fabric dev 测试方块：右键打开对应档位的测试容器。
 * 打开 API 各 loader 不同（Fabric 用 ExtendedScreenHandlerFactory 数据对象编码档位），
 * 因此 TestBlock 各 loader 各自实现；容器本体 TestContainer 在 common 共享。
 */
public class TestBlock extends Block {

    private final int slotCount;

    public TestBlock(int slotCount, Properties properties) {
        super(properties);
        this.slotCount = slotCount;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (player instanceof ServerPlayer serverPlayer) {
            int count = slotCount;
            serverPlayer.openMenu(new ExtendedScreenHandlerFactory<Integer>() {
                @Override
                public Component getDisplayName() {
                    return Component.literal("Test (" + count + " slots)");
                }

                @Override
                public AbstractContainerMenu createMenu(int id, Inventory inv, Player p) {
                    return new TestContainer(ModRegistries.TEST_MENU, id, inv, count);
                }

                @Override
                public Integer getScreenOpeningData(ServerPlayer p) {
                    return count;
                }
            });
        }
        return InteractionResult.CONSUME;
    }
}
