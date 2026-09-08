package qikahome.autosizedgui.fabric;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qikahome.autosizedgui.AutoSizedGUISettings;
import qikahome.autosizedgui.api.OverflowMode;

/**
 * Fabric 侧客户端配置：config/autosizedgui.properties（无第三方依赖）。
 * <p>
 * 字段与 NeoForge ModConfig / common 的 {@link AutoSizedGUISettings} 对齐。
 * 默认值与 NeoForge 版一致；修改后重启生效（Fabric 无内置热重载）。
 */
public final class ModConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger("autosizedgui");
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("autosizedgui.properties");

    public static void load() {
        Properties props = new Properties();
        if (Files.isReadable(FILE)) {
            try (Reader reader = Files.newBufferedReader(FILE, StandardCharsets.UTF_8)) {
                props.load(reader);
            } catch (IOException e) {
                LOGGER.error("Failed to read config {}, using defaults", FILE, e);
            }
        }

        AutoSizedGUISettings.enableScrollPagination =
                getBool(props, "enableScrollPagination", AutoSizedGUISettings.enableScrollPagination);
        AutoSizedGUISettings.defaultMaxHeight =
                getInt(props, "defaultMaxHeight", AutoSizedGUISettings.defaultMaxHeight);
        AutoSizedGUISettings.defaultMaxWidth =
                getInt(props, "defaultMaxWidth", AutoSizedGUISettings.defaultMaxWidth);
        AutoSizedGUISettings.defaultMinMargin =
                getInt(props, "defaultMinMargin", AutoSizedGUISettings.defaultMinMargin);
        AutoSizedGUISettings.defaultMinColumns =
                getInt(props, "defaultMinColumns", AutoSizedGUISettings.defaultMinColumns);
        AutoSizedGUISettings.defaultMaxColumns =
                getInt(props, "defaultMaxColumns", AutoSizedGUISettings.defaultMaxColumns);
        AutoSizedGUISettings.defaultMaxRows =
                getInt(props, "defaultMaxRows", AutoSizedGUISettings.defaultMaxRows);
        AutoSizedGUISettings.defaultOverflowMode =
                OverflowMode.valueOf(getString(props, "defaultOverflowMode", AutoSizedGUISettings.defaultOverflowMode.name()));
        AutoSizedGUISettings.scrollbarTrackRepeatDelay =
                getInt(props, "scrollbarTrackRepeatDelay", AutoSizedGUISettings.scrollbarTrackRepeatDelay);
        AutoSizedGUISettings.scrollbarTrackRepeatInterval =
                getInt(props, "scrollbarTrackRepeatInterval", AutoSizedGUISettings.scrollbarTrackRepeatInterval);

        if (!Files.exists(FILE)) {
            saveDefaults();
        }
    }

    /** 生成默认配置文件，方便用户按需编辑（NeoForge 侧会在首次加载时自动生成 spec 文件）。 */
    private static void saveDefaults() {
        Properties props = new Properties();
        props.setProperty("enableScrollPagination", String.valueOf(AutoSizedGUISettings.enableScrollPagination));
        props.setProperty("defaultMaxHeight", String.valueOf(AutoSizedGUISettings.defaultMaxHeight));
        props.setProperty("defaultMaxWidth", String.valueOf(AutoSizedGUISettings.defaultMaxWidth));
        props.setProperty("defaultMinMargin", String.valueOf(AutoSizedGUISettings.defaultMinMargin));
        props.setProperty("defaultMinColumns", String.valueOf(AutoSizedGUISettings.defaultMinColumns));
        props.setProperty("defaultMaxColumns", String.valueOf(AutoSizedGUISettings.defaultMaxColumns));
        props.setProperty("defaultMaxRows", String.valueOf(AutoSizedGUISettings.defaultMaxRows));
        props.setProperty("defaultOverflowMode", AutoSizedGUISettings.defaultOverflowMode.name());
        props.setProperty("scrollbarTrackRepeatDelay", String.valueOf(AutoSizedGUISettings.scrollbarTrackRepeatDelay));
        props.setProperty("scrollbarTrackRepeatInterval", String.valueOf(AutoSizedGUISettings.scrollbarTrackRepeatInterval));

        try {
            Files.createDirectories(FILE.getParent());
            try (Writer writer = Files.newBufferedWriter(FILE, StandardCharsets.UTF_8)) {
                props.store(writer, "AutoSizedGUI client settings (Fabric)");
            }
        } catch (IOException e) {
            LOGGER.error("Failed to write default config {}", FILE, e);
        }
    }

    private static boolean getBool(Properties props, String key, boolean fallback) {
        String v = props.getProperty(key);
        return v == null ? fallback : Boolean.parseBoolean(v);
    }

    private static int getInt(Properties props, String key, int fallback) {
        String v = props.getProperty(key);
        if (v == null) {
            return fallback;
        }
        try {
            return Integer.parseInt(v);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private static String getString(Properties props, String key, String fallback) {
        String v = props.getProperty(key);
        return v == null ? fallback : v;
    }

    private ModConfig() {
    }
}
