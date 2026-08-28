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
 * Test block that opens an AutoSizedGUI test container on right-click.
 * Passes slot count through the menu's extra data buffer so the client
 * creates a matching container.
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
                    return new TestContainer(id, inv, count);
                }
            }, buf -> buf.writeVarInt(count));
        }
        return InteractionResult.CONSUME;
    }
}
