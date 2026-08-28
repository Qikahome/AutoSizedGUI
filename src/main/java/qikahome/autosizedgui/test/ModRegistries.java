package qikahome.autosizedgui.test;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import static qikahome.autosizedgui.AutoSizedGUI.MOD_ID;

/**
 * Registry for test-only content (menu types, screens, blocks, items).
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

  // Four test blocks with different slot counts
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

  public static final DeferredHolder<MenuType<?>, MenuType<TestContainer>> TEST_MENU = MENUS.register("test",
      () -> IMenuTypeExtension
          .create((id, inv, buf) -> new TestContainer(id, inv, buf.readVarInt())));

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
