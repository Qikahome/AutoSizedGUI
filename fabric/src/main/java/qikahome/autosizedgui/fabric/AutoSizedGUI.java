package qikahome.autosizedgui.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import qikahome.autosizedgui.KeyBindings;

/**
 * AutoSizedGUI — a library mod for automatic GUI layout with pagination.
 * <p>
 * Fabric 入口（与 NeoForge 侧入口并存于同一发布 jar，loader 只实例化自己的入口；
 * 类名/包特意不与 NeoForge 侧重复，避免单 jar 内类冲突）。
 * <p>
 * 本 mod 自身不注册业务内容，仅加载配置；其他 mod 使用 common 的
 * {@code qikahome.autosizedgui.api} 构建自适应 GUI。
 */
public class AutoSizedGUI implements ModInitializer, ClientModInitializer {

    @Override
    public void onInitialize() {
        // Test content: dev environment only
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

        KeyMappingHelper.registerKeyMapping(KeyBindings.PREV_PAGE);
        KeyMappingHelper.registerKeyMapping(KeyBindings.NEXT_PAGE);

        // Test screens: dev environment only
        if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
            ModRegistries.registerScreens();
        }
    }
}
