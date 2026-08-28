package qikahome.autosizedgui;

import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import qikahome.autosizedgui.test.ModRegistries;
import qikahome.autosizedgui.test.TestCommand;

/**
 * AutoSizedGUI — a library mod for automatic GUI layout with pagination.
 * <p>
 * This mod itself registers nothing. Other mods use the provided API
 * ({@code qikahome.autosizedgui.api}) and widgets
 * ({@code qikahome.autosizedgui.widget})
 * to build auto-sizing GUIs.
 */
@Mod(AutoSizedGUI.MOD_ID)
public class AutoSizedGUI {
    public static final Identifier BUILT_IN_GUI_TEXTURE = Identifier.fromNamespaceAndPath("autosizedgui", "textures/gui/gui.png");
    public static final String MOD_ID = "autosizedgui";

    public AutoSizedGUI(IEventBus modBus, ModContainer modContainer) {
        // Register NeoForge client config
        modContainer.registerConfig(net.neoforged.fml.config.ModConfig.Type.CLIENT, qikahome.autosizedgui.ModConfig.CLIENT_SPEC);

        // Register key bindings on the client only
        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            modBus.addListener(KeyBindings::register);
        }

        // Register test content (dev environment only)
        if (!FMLLoader.getCurrent().isProduction()) {
            ModRegistries.register(modBus);
        }

        // Register commands on the game bus
        NeoForge.EVENT_BUS.addListener(this::onRegisterCommands);
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        if (!FMLLoader.getCurrent().isProduction()) {
            TestCommand.register(event.getDispatcher());
        }
    }
}
