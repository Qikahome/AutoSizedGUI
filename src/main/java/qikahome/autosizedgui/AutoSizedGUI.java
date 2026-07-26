package qikahome.autosizedgui;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
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
    public static final ResourceLocation BUILT_IN_GUI_TEXTURE = new ResourceLocation("autosizedgui", "textures/gui/gui.png");
    public static final String MOD_ID = "autosizedgui";

    public AutoSizedGUI() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register Forge client config
        ModLoadingContext.get().registerConfig(net.minecraftforge.fml.config.ModConfig.Type.CLIENT, ModConfig.CLIENT_SPEC);

        modBus.addListener(KeyBindings::register);

        // Register test content (dev environment only)
        if (!FMLLoader.isProduction()) {
            ModRegistries.register(modBus);
        }

        // Register command on the Forge event bus
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(this::onRegisterCommands);
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        if (!FMLLoader.isProduction()) {
            TestCommand.register(event.getDispatcher());
        }
    }
}
