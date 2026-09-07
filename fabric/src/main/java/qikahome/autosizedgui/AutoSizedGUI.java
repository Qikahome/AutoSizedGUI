package qikahome.autosizedgui;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import qikahome.autosizedgui.test.ModRegistries;

/**
 * AutoSizedGUI — a library mod for automatic GUI layout with pagination.
 * <p>
 * Fabric 入口：同时实现 main/client 双入口（loader 按 entrypoint 分别调用）。
 * 与 Forge 版一致：本 mod 自身不注册业务内容，仅加载配置；
 * 其他 mod 使用 common 的 {@code qikahome.autosizedgui.api} 构建自适应 GUI。
 */
public class AutoSizedGUI implements ModInitializer, ClientModInitializer {

    @Override
    public void onInitialize() {
        // Test content: dev environment only（对应 Forge 侧的 dev-only 注册）
        if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
            ModRegistries.registerBlocks();
            CommandRegistrationCallback.EVENT.register(
                    (dispatcher, registryAccess, environment) -> ModRegistries.registerCommands(dispatcher));
        }
    }

    @Override
    public void onInitializeClient() {
        // 客户端配置（properties 文件）→ 镜像到 common 的 AutoSizedGUISettings
        ModConfig.load();

        KeyBindingHelper.registerKeyBinding(KeyBindings.PREV_PAGE);
        KeyBindingHelper.registerKeyBinding(KeyBindings.NEXT_PAGE);

        // Test screens: dev environment only
        if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
            ModRegistries.registerScreens();
        }
    }
}
