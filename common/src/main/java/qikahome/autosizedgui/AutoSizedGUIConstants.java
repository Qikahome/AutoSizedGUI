package qikahome.autosizedgui;

import net.minecraft.resources.ResourceLocation;

/**
 * 跨 loader 共享的常量。原位于 AutoSizedGUI 入口类，common 化后独立成类。
 */
public final class AutoSizedGUIConstants {

    public static final String MOD_ID = "autosizedgui";
    public static final ResourceLocation BUILT_IN_GUI_TEXTURE =
            new ResourceLocation(MOD_ID, "textures/gui/gui.png");

    private AutoSizedGUIConstants() {
    }
}
