package qikahome.autosizedgui.fabric;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType.ExtendedFactory;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
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
import qikahome.autosizedgui.test.TestContainer;
import static qikahome.autosizedgui.AutoSizedGUIConstants.MOD_ID;

/**
 * Fabric 侧 dev 测试注册中枢（对应 NeoForge 侧 test/ModRegistries）。
 * <p>
 * 单个 MenuType（{@link ExtendedMenuType}）+ extra data 动态容量：
 * 服务端打开时经 {@link ExtendedMenuProvider} 提供档位（StreamCodec 编码），
 * 客户端菜单工厂 (syncId, inv, data) 重建同槽数菜单 —— 与 NeoForge 的
 * IMenuTypeExtension 机制语义等价，任意档位（如 /autosizedgui 128）都能打开。
 * <p>
 * 用法：/autosizedgui &lt;1..MAX&gt;，或放置创造页签里的 test_9 ~ test_256 方块右键打开。
 * 仅在 dev 环境由入口类调用。方块/容器本体在 common 的 test 包共享。
 */
public final class ModRegistries {

    private static final int[] SIZES = {9, 27, 54, 256};

    // 单 MenuType + data(档位) 动态容量：任意槽数客户端都能重建（ExtendedMenuType）
    // 注意：lambda 里不能直接引用 TEST_MENU（javac 静态初始化自引用），经 testMenu() 间接取
    public static final MenuType<TestContainer> TEST_MENU = registerMenu("test",
            (syncId, inv, size) -> new TestContainer(testMenu(), syncId, inv, size),
            ByteBufCodecs.VAR_INT);

    private static MenuType<TestContainer> testMenu() {
        return TEST_MENU;
    }

    private static <M extends AbstractContainerMenu, D> MenuType<M> registerMenu(String name,
            ExtendedFactory<M, D> factory, StreamCodec<? super RegistryFriendlyByteBuf, D> codec) {
        return Registry.register(BuiltInRegistries.MENU,
                Identifier.fromNamespaceAndPath(MOD_ID, name),
                new ExtendedMenuType<>(factory, codec));
    }

    private ModRegistries() {
    }

    /** 注册 4 个测试方块 + BlockItem + 创造页签（对应 NeoForge 侧 ModRegistries）。 */
    public static void registerBlocks() {
        BlockItem[] items = new BlockItem[SIZES.length];
        for (int i = 0; i < SIZES.length; i++) {
            int size = SIZES[i];
            Identifier id = Identifier.fromNamespaceAndPath(MOD_ID, "test_" + size);
            Block block = Registry.register(BuiltInRegistries.BLOCK, id, new TestBlock(size,
                    BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS)
                            .setId(ResourceKey.create(Registries.BLOCK, id))));
            BlockItem item = Registry.register(BuiltInRegistries.ITEM, id, new BlockItem(block,
                    new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id))));
            items[i] = item;
        }

        // 26 vanilla 的 builder 需要 (Row, index)（NeoForge 额外提供无参重载，纯 vanilla/fabric 用显式位置）
        CreativeModeTab tab = CreativeModeTab.builder(CreativeModeTab.Row.TOP, 100)
                .title(Component.literal("AutoSizedGUI Test"))
                .icon(() -> items[1].getDefaultInstance())
                .displayItems((params, output) -> {
                    for (BlockItem item : items) {
                        output.accept(item.getDefaultInstance());
                    }
                })
                .build();
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB,
                Identifier.fromNamespaceAndPath(MOD_ID, "test"), tab);
    }

    /** 客户端屏幕注册：AutoSizedContainerScreen 非抽象，直接以构造 lambda 注册即可。 */
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

    /** 服务端打开测试容器（供命令与 TestBlock 共用）：ExtendedMenuProvider 把档位作为打开数据发送。 */
    public static int openTest(ServerPlayer player, int slotCount) {
        player.openMenu(new ExtendedMenuProvider<Integer>() {
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
