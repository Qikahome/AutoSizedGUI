package qikahome.autosizedgui.test;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import static qikahome.autosizedgui.AutoSizedGUIConstants.MOD_ID;

/**
 * NeoForge 侧 dev 测试注册中枢。
 * <p>
 * 单 MenuType + buffer(档位) 动态容量（IMenuTypeExtension）：服务端打开时把档位写入 extra
 * data buffer，客户端工厂 (id, inv, buf) 重建同槽数菜单 —— 与 Fabric 的
 * ExtendedScreenHandlerType 机制语义等价，任意档位（如 /autosizedgui 128）都能打开。
 * <p>
 * 用法：/autosizedgui &lt;1..MAX&gt;，或放置创造页签里的 test_9 ~ test_256 方块右键打开。
 * 仅在 dev 环境由入口类调用。方块/容器本体在 common 的 test 包共享。
 */
public class ModRegistries {

    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, MOD_ID);
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, MOD_ID);
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, MOD_ID);
    private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister
            .create(Registries.CREATIVE_MODE_TAB, MOD_ID);

    private static final <T> ResourceKey<T> getId(ResourceKey<? extends Registry<T>> registryName, String id) {
        return ResourceKey.create(registryName, Identifier.fromNamespaceAndPath(MOD_ID, id));
    }

    // 单 MenuType + buffer(档位) 动态容量：任意槽数客户端都能重建
    // 注意：lambda 里不能直接引用 TEST_MENU（javac 静态初始化自引用），经 menuType() 间接取
    public static final DeferredHolder<MenuType<?>, MenuType<TestContainer>> TEST_MENU = MENUS.register("test",
            () -> IMenuTypeExtension
                    .create((id, inv, buf) -> new TestContainer(menuType(), id, inv, buf.readVarInt())));

    private static MenuType<TestContainer> menuType() {
        return TEST_MENU.get();
    }

    // Four test blocks with different slot counts（方块本体在 common 的 test 包共享）
    public static final DeferredHolder<Block, TestBlock> TEST_BLOCK_9 = BLOCKS.register("test_9",
            () -> new TestBlock(9, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS)
                    .setId(getId(Registries.BLOCK, "test_9"))));
    public static final DeferredHolder<Block, TestBlock> TEST_BLOCK_27 = BLOCKS.register("test_27",
            () -> new TestBlock(27, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS)
                    .setId(getId(Registries.BLOCK, "test_27"))));
    public static final DeferredHolder<Block, TestBlock> TEST_BLOCK_54 = BLOCKS.register("test_54",
            () -> new TestBlock(54, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS)
                    .setId(getId(Registries.BLOCK, "test_54"))));
    public static final DeferredHolder<Block, TestBlock> TEST_BLOCK_256 = BLOCKS.register("test_256",
            () -> new TestBlock(256, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS)
                    .setId(getId(Registries.BLOCK, "test_256"))));

    // BlockItems
    public static final DeferredHolder<Item, BlockItem> ITEM_9 = ITEMS.register("test_9",
            () -> new BlockItem(TEST_BLOCK_9.get(), new Item.Properties().setId(getId(Registries.ITEM, "test_9"))));
    public static final DeferredHolder<Item, BlockItem> ITEM_27 = ITEMS.register("test_27",
            () -> new BlockItem(TEST_BLOCK_27.get(), new Item.Properties().setId(getId(Registries.ITEM, "test_27"))));
    public static final DeferredHolder<Item, BlockItem> ITEM_54 = ITEMS.register("test_54",
            () -> new BlockItem(TEST_BLOCK_54.get(), new Item.Properties().setId(getId(Registries.ITEM, "test_54"))));
    public static final DeferredHolder<Item, BlockItem> ITEM_256 = ITEMS.register("test_256",
            () -> new BlockItem(TEST_BLOCK_256.get(), new Item.Properties().setId(getId(Registries.ITEM, "test_256"))));

    // Creative tab
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB = TABS.register("test",
            () -> CreativeModeTab.builder()
                    .title(Component.literal("AutoSizedGUI Test"))
                    .icon(() -> ITEM_27.get().getDefaultInstance())
                    .displayItems((params, output) -> {
                        output.accept(ITEM_9.get());
                        output.accept(ITEM_27.get());
                        output.accept(ITEM_54.get());
                        output.accept(ITEM_256.get());
                    })
                    .build());

    /** 打开测试容器（供命令与 TestBlock 共用）：把档位写入 buffer，客户端经菜单工厂重建。 */
    public static int openTest(ServerPlayer player, int slotCount) {
        player.openMenu(new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return Component.literal("Test (" + slotCount + " slots)");
            }

            @Override
            public AbstractContainerMenu createMenu(int id, Inventory inv, Player p) {
                return new TestContainer(TEST_MENU.get(), id, inv, slotCount);
            }
        }, buf -> buf.writeVarInt(slotCount));
        return 1;
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

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        MENUS.register(modBus);
        TABS.register(modBus);
        modBus.addListener(ModRegistries::onRegisterScreens);
    }

    private static void onRegisterScreens(RegisterMenuScreensEvent event) {
        event.register(TEST_MENU.get(), TestContainerScreen::new);
    }
}
