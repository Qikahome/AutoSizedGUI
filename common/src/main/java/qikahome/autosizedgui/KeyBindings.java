package qikahome.autosizedgui;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

/**
 * 跨 loader 共享的按键定义（均为 vanilla API）。
 * <p>
 * 各 loader 自行注册：NeoForge → {@code RegisterKeyMappingsEvent}；
 * Fabric → {@code KeyBindingHelper}。common 代码只引用 KeyMapping 字段本身。
 */
public final class KeyBindings {

    public static final KeyMapping.Category CATEGORY =
            new KeyMapping.Category(Identifier.fromNamespaceAndPath(AutoSizedGUIConstants.MOD_ID, "page"));

    public static final KeyMapping PREV_PAGE = new KeyMapping(
            "key.autosizedgui.prev_page",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_PAGE_UP,
            CATEGORY);

    public static final KeyMapping NEXT_PAGE = new KeyMapping(
            "key.autosizedgui.next_page",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_PAGE_DOWN,
            CATEGORY);

    private KeyBindings() {
    }
}
