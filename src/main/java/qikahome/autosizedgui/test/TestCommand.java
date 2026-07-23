package qikahome.autosizedgui.test;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

/**
 * Command to open the test container screen.
 * Usage:
 *   /autosizedgui 27    — single chest
 *   /autosizedgui 54    — double chest
 *   /autosizedgui 9     — dispenser
 */
public class TestCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("autosizedgui")
                .then(Commands.argument("size", IntegerArgumentType.integer(1, 999))
                        .executes(ctx -> {
                            int size = IntegerArgumentType.getInteger(ctx, "size");
                            return openTest(ctx.getSource().getPlayerOrException(), size);
                        })
                )
        );
    }

    private static int openTest(ServerPlayer player, int size) {
        player.openMenu(new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return Component.literal("Test (" + size + " slots)");
            }

            @Override
            public AbstractContainerMenu createMenu(int id, Inventory inv, Player p) {
                return new TestContainer(id, inv, size);
            }
        }, buf -> buf.writeVarInt(size));
        return 1;
    }
}
