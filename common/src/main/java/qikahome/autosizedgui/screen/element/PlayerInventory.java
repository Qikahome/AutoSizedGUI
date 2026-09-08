package qikahome.autosizedgui.screen.element;

import java.util.List;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import qikahome.autosizedgui.AutoSizedGUIConstants;
import qikahome.autosizedgui.api.AttachPosition;
import qikahome.autosizedgui.api.ILayoutElement;

public class PlayerInventory extends TitleBar {
    private static final int WIDTH = 162;
    private static final int HEIGHT = 88;
    private static final int[] ROW_Y = { 12, 30, 48, 70 };

    /**
     * 
     * @param text
     * @param slots make sure these are player slots;
     */
    public PlayerInventory(Component text, List<ILayoutElement> slots) {
        this(text, slots, 0xFF404040);
    }

    public PlayerInventory(Component text, List<ILayoutElement> slots, int color) {
        super(text, color);
        this.slots = slots;
    }

    protected final List<ILayoutElement> slots;

    @Override
    public int getWidth() {
        return WIDTH;
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }

    @Override
    public void setPosition(int x, int y) {
        super.setPosition(x, y);

        for (int i = 0; i < 36; i++) {
            int col = i % 9;
            int row = i / 9;
            slots.get(i).setPosition(x + col * 18, y + ROW_Y[row]);
        }
    }

    @Override
    public AttachPosition getAttachPosition() {
        return AttachPosition.BOTTOM;
    }

    @Override
    public int getPriority() {
        return Integer.MAX_VALUE / 2;
    }

    @Override
    public void render(GuiGraphicsExtractor g, FontGetter font) {
        // Render slot from texture (0, 16) in gui.png
        g.blit(RenderPipelines.GUI_TEXTURED, AutoSizedGUIConstants.BUILT_IN_GUI_TEXTURE,
                x, y, 18, 0, WIDTH, HEIGHT, WIDTH, HEIGHT, 256, 128);
        g.text(font.getFont(),
                text,
                x + 1, y + 1,
                color, false);
    }
}
