package qikahome.autosizedgui;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import qikahome.autosizedgui.test.ModRegistries;

/**
 * AutoSizedGUI — a library mod for automatic GUI layout with pagination.
 * <p>
 * This mod itself registers nothing. Other mods use the provided API
 * ({@code qikahome.autosizedgui.api}) and widgets
 * ({@code qikahome.autosizedgui.widget})
 * to build auto-sizing GUIs.
 */
@Mod(AutoSizedGUIConstants.MOD_ID)
public class AutoSizedGUI {

    public AutoSizedGUI(IEventBus modBus, ModContainer modContainer) {
        // Register NeoForge client config. 注意：registerConfig 后 config 尚未实际加载，
        // spec 值要到 ModConfigEvent(Loading/Reloading) 派发后才可读，因此只在事件里同步 common。
        modContainer.registerConfig(Type.CLIENT, ModConfig.CLIENT_SPEC);
        modBus.addListener(ModConfig::onConfigChanged);

        // Register key bindings on the client only
        if (FMLEnvironment.dist == Dist.CLIENT) {
            modBus.addListener(this::registerKeyMappings);
        }

        // Register test content (dev environment only)
        if (!FMLLoader.isProduction()) {
            ModRegistries.register(modBus);
        }

        // Register commands on the game bus
        NeoForge.EVENT_BUS.addListener(this::onRegisterCommands);
    }

    private void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(KeyBindings.PREV_PAGE);
        event.register(KeyBindings.NEXT_PAGE);
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        if (!FMLLoader.isProduction()) {
            ModRegistries.registerCommands(event.getDispatcher());
        }
    }
}
