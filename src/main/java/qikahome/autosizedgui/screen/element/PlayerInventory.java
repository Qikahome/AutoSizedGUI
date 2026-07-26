package qikahome.autosizedgui.screen.element;

import java.util.List;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import qikahome.autosizedgui.AutoSizedGUI;
import qikahome.autosizedgui.api.AttachPosition;

public class PlayerInventory extends TitleBar {
    private static final int WIDTH = 162;
    private static final int HEIGHT = 88;
    private static final int[] ROW_Y = { 12, 30, 48, 70 };

    public PlayerInventory(Component text, List<ItemSlot> slots) {
        this(text, slots, 4210752);
    }

    public PlayerInventory(Component text, List<ItemSlot> slots, int color) {
        super(text, color);
        this.slots = slots;
    }

    protected final List<ItemSlot> slots;

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
    public void render(GuiGraphics g, FontGetter font) {
        // Render slot from texture (0, 16) in gui.png
        g.blit(AutoSizedGUI.BUILT_IN_GUI_TEXTURE, x, y, WIDTH, HEIGHT, 18, 0, WIDTH, HEIGHT, 256, 128);
        g.drawString(
                font.getFont(),
                text,
                x + 1, y + 1,
                color, false);
    }
}
