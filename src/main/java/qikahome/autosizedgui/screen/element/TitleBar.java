package qikahome.autosizedgui.screen.element;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import qikahome.autosizedgui.api.AttachPosition;
import qikahome.autosizedgui.api.ILayoutElement;

public class TitleBar implements ILayoutElement {
    protected final Component text;
    protected final int color;
    protected int x, y;

    public TitleBar(Component text) {
        this(text, 4210752);
    }

    public TitleBar(Component text, int color) {
        this.text = text;
        this.color = color;
    }

    @Override
    public int getWidth() {
        return 162;
    }

    @Override
    public int getHeight() {
        return 10;
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    @Override
    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public AttachPosition getAttachPosition() {
        return AttachPosition.TOP_LEFT;
    }

    @Override
    public int getPriority() {
        return Integer.MAX_VALUE / 2;
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        render(g, () -> net.minecraft.client.Minecraft.getInstance().font);
    }

    public void render(GuiGraphics g, FontGetter font) {
        g.drawString(
                font.getFont(),
                text,
                x + 1, y - 1,
                color, false);
    }

    public static interface FontGetter {
        Font getFont();
    }
}
