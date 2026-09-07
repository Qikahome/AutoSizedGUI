package qikahome.autosizedgui;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
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

    public AutoSizedGUI() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register Forge client config; on (re)load mirror values into common settings
        ModLoadingContext.get().registerConfig(Type.CLIENT, ModConfig.CLIENT_SPEC);
        modBus.addListener(ModConfig::onConfigChanged);

        // Register key bindings on the client only
        if (FMLLoader.getDist() == Dist.CLIENT) {
            modBus.addListener(this::onRegisterKeyMappings);
        }

        // Register test content (dev environment only)
        if (!FMLLoader.isProduction()) {
            ModRegistries.register(modBus);
        }

        // Register command on the Forge event bus
        MinecraftForge.EVENT_BUS.addListener(this::onRegisterCommands);
    }

    private void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(KeyBindings.PREV_PAGE);
        event.register(KeyBindings.NEXT_PAGE);
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        if (!FMLLoader.isProduction()) {
            ModRegistries.registerCommands(event.getDispatcher());
        }
    }
}
