package qikahome.autosizedgui.test;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModRegistries {

    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, "autosizedgui");
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, "autosizedgui");
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, "autosizedgui");
    private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, "autosizedgui");

    // Four test blocks with different slot counts
    public static final RegistryObject<TestBlock> TEST_BLOCK_9 = BLOCKS.register("test_9",
            () -> new TestBlock(9, Block.Properties.copy(Blocks.OAK_PLANKS)));
    public static final RegistryObject<TestBlock> TEST_BLOCK_27 = BLOCKS.register("test_27",
            () -> new TestBlock(27, Block.Properties.copy(Blocks.OAK_PLANKS)));
    public static final RegistryObject<TestBlock> TEST_BLOCK_54 = BLOCKS.register("test_54",
            () -> new TestBlock(54, Block.Properties.copy(Blocks.OAK_PLANKS)));
    public static final RegistryObject<TestBlock> TEST_BLOCK_256 = BLOCKS.register("test_256",
            () -> new TestBlock(256, Block.Properties.copy(Blocks.OAK_PLANKS)));

    // BlockItems
    public static final RegistryObject<BlockItem> ITEM_9 = ITEMS.register("test_9",
            () -> new BlockItem(TEST_BLOCK_9.get(), new Item.Properties()));
    public static final RegistryObject<BlockItem> ITEM_27 = ITEMS.register("test_27",
            () -> new BlockItem(TEST_BLOCK_27.get(), new Item.Properties()));
    public static final RegistryObject<BlockItem> ITEM_54 = ITEMS.register("test_54",
            () -> new BlockItem(TEST_BLOCK_54.get(), new Item.Properties()));
    public static final RegistryObject<BlockItem> ITEM_256 = ITEMS.register("test_256",
            () -> new BlockItem(TEST_BLOCK_256.get(), new Item.Properties()));

    // Creative tab
    public static final RegistryObject<CreativeModeTab> TAB = TABS.register("test",
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

    public static final RegistryObject<MenuType<TestContainer>> TEST_MENU = MENUS.register("test",
            () -> IForgeMenuType.create((id, inv, buf) -> new TestContainer(id, inv, buf.readVarInt())));

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        MENUS.register(modBus);
        TABS.register(modBus);
        modBus.addListener(ModRegistries::onClientSetup);
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() ->
            MenuScreens.register(TEST_MENU.get(), TestContainerScreen::new)
        );
    }
}
