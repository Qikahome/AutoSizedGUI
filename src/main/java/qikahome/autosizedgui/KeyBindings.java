package qikahome.autosizedgui;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

public class KeyBindings {

    public static final KeyMapping.Category CATEGORY = new KeyMapping.Category(Identifier.fromNamespaceAndPath("autosizedgui","page"));

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

    public static void register(RegisterKeyMappingsEvent event) {
        event.registerCategory(CATEGORY);
        event.register(PREV_PAGE);
        event.register(NEXT_PAGE);
    }
}
