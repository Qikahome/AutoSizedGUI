package qikahome.autosizedgui.test;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import qikahome.autosizedgui.screen.AutoSizedContainerScreen;

/**
 * Fabric dev 测试注册中枢（对应 NeoForge 侧 test/ModRegistries）。
 * <p>
 * 单个 MenuType（{@link ExtendedScreenHandlerType}）+ extra data 动态容量：
 * 服务端打开时经 {@link ExtendedScreenHandlerFactory} 把档位编码进 StreamCodec，
 * 客户端菜单工厂 (syncId, inv, data) 重建同槽数菜单 —— 与 NeoForge 的
 * IMenuTypeExtension 机制语义等价，任意档位（如 /autosizedgui 128）都能打开。
 * <p>
 * 用法：/autosizedgui &lt;1..256&gt;，或放置创造页签里的 test_9 ~ test_256 方块右键打开。
 * 仅在 dev 环境由入口类调用。
 */
public final class ModRegistries {

    private static final int[] SIZES = {9, 27, 54, 256};

    // 单 MenuType + StreamCodec(档位) 动态容量：任意槽数客户端都能重建（ExtendedScreenHandlerType）
    // 注意：lambda 里不能直接引用 TEST_MENU（javac 静态初始化自引用），经 testMenu() 间接取
    public static final MenuType<TestContainer> TEST_MENU = Registry.register(BuiltInRegistries.MENU,
            ResourceLocation.fromNamespaceAndPath("autosizedgui", "test"),
            new ExtendedScreenHandlerType<TestContainer, Integer>(
                    (syncId, inv, slotCount) -> new TestContainer(testMenu(), syncId, inv, slotCount),
                    ByteBufCodecs.VAR_INT));

    private static MenuType<TestContainer> testMenu() {
        return TEST_MENU;
    }

    private ModRegistries() {
    }

    /** 注册 4 个测试方块 + BlockItem + 创造页签（对应 NeoForge 侧 ModRegistries）。 */
    public static void registerBlocks() {
        BlockBehaviour.Properties props = BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS);
        for (int size : SIZES) {
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath("autosizedgui", "test_" + size);
            Block block = Registry.register(BuiltInRegistries.BLOCK, id, new TestBlock(size, props));
            Registry.register(BuiltInRegistries.ITEM, id, new BlockItem(block, new Item.Properties()));
        }

        CreativeModeTab tab = CreativeModeTab.builder(CreativeModeTab.Row.TOP, 100)
                .title(Component.literal("AutoSizedGUI Test"))
                .icon(() -> new ItemStack(BuiltInRegistries.ITEM.get(
                        ResourceLocation.fromNamespaceAndPath("autosizedgui", "test_27"))))
                .displayItems((params, output) -> {
                    for (int size : SIZES) {
                        output.accept(new ItemStack(BuiltInRegistries.ITEM.get(
                                ResourceLocation.fromNamespaceAndPath("autosizedgui", "test_" + size))));
                    }
                })
                .build();
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB,
                ResourceLocation.fromNamespaceAndPath("autosizedgui", "test"), tab);
    }

    /** 客户端屏幕注册：AutoSizedContainerScreen 非抽象，直接以构造引用注册即可。 */
    public static void registerScreens() {
        MenuScreens.register(TEST_MENU, (TestContainer menu, Inventory inv, Component title) ->
                new AutoSizedContainerScreen<>(menu, inv, title, menu.containerSlots));
    }

    public static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("autosizedgui")
                .then(Commands.argument("size", IntegerArgumentType.integer(1, Integer.MAX_VALUE))
                        .executes(ctx ->
                                openTest(ctx.getSource().getPlayerOrException(),
                                        IntegerArgumentType.getInteger(ctx, "size")))
                )
        );
    }

    /** 服务端打开测试容器：Extended 工厂把档位经 StreamCodec 传给客户端。 */
    private static int openTest(ServerPlayer player, int slotCount) {
        player.openMenu(new ExtendedScreenHandlerFactory<Integer>() {
            @Override
            public Component getDisplayName() {
                return Component.literal("Test (" + slotCount + " slots)");
            }

            @Override
            public AbstractContainerMenu createMenu(int id, Inventory inv, Player p) {
                return new TestContainer(TEST_MENU, id, inv, slotCount);
            }

            @Override
            public Integer getScreenOpeningData(ServerPlayer p) {
                return slotCount;
            }
        });
        return 1;
    }
}
