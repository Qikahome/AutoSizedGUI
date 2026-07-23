package qikahome.autosizedgui.test;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import qikahome.autosizedgui.screen.AutoSizedContainerScreen;

/**
 * Test screen that opens a configurable-sized container
 * using the AutoSizedGUI layout panel.
 */
public class TestContainerScreen extends AutoSizedContainerScreen<TestContainer> {

    public TestContainerScreen(TestContainer menu, Inventory inventory, Component title) {
        super(menu, inventory, title, menu.containerSlots);
    }
}
