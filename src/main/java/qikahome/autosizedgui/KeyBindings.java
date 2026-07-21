package qikahome.autosizedgui;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

public class KeyBindings {

    public static final String CATEGORY = "key.categories.autosizedgui";

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
        event.register(PREV_PAGE);
        event.register(NEXT_PAGE);
    }
}
