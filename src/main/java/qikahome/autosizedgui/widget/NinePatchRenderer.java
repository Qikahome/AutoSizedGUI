package qikahome.autosizedgui.widget;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;

import java.io.IOException;

/**
 * 9-patch background renderer.
 * <p>
 * Draws a GUI background consisting of four fixed-size corners, four stretchable edges,
 * and a solid-color center fill &mdash; textured from a source region in the given texture.
 * The center fill color is automatically sampled from the pixel at
 * ({@code texU + left}, {@code texV + top}) in the texture.
 * <p>
 * <pre>
 * Typical usage:
 * {@code
 *   NinePatchRenderer bg = new NinePatchRenderer(TEXTURE, 0, 0, 15, 15, 256, 128, 7, 7, 7, 7);
 *   bg.render(guiGraphics, x, y, width, height);
 * }
 * </pre>
 */
public class NinePatchRenderer {

    private final ResourceLocation texture;
    private final int texU, texV;
    /** Width &amp; height of the source content region in the texture. */
    private final int texWidth, texHeight;
    /** Actual pixel dimensions of the PNG file (for UV normalisation). */
    private final int textureFileWidth, textureFileHeight;
    private final int left, right, top, bottom;

    private int centerColor = 0;
    private boolean centerColorSampled = false;

    /**
     * @param texture           the texture location
     * @param texU              source region X offset in the texture
     * @param texV              source region Y offset in the texture
     * @param texWidth          source region width  (must be &ge; left + right)
     * @param texHeight         source region height (must be &ge; top + bottom)
     * @param textureFileWidth  actual pixel width of the PNG file (for UV normalisation)
     * @param textureFileHeight actual pixel height of the PNG file
     * @param left              left   border in pixels (fixed corner width)
     * @param right             right  border in pixels
     * @param top               top    border in pixels
     * @param bottom            bottom border in pixels
     */
    public NinePatchRenderer(ResourceLocation texture,
                             int texU, int texV,
                             int texWidth, int texHeight,
                             int textureFileWidth, int textureFileHeight,
                             int left, int right, int top, int bottom) {
        this.texture = texture;
        this.texU = texU;
        this.texV = texV;
        this.texWidth = texWidth;
        this.texHeight = texHeight;
        this.textureFileWidth = textureFileWidth;
        this.textureFileHeight = textureFileHeight;
        this.left = left;
        this.right = right;
        this.top = top;
        this.bottom = bottom;
    }

    public int getLeft()   { return left; }
    public int getRight()  { return right; }
    public int getTop()    { return top; }
    public int getBottom() { return bottom; }

    /** Get the auto-sampled center fill color (ensures sampling on first call). */
    public int getCenterColor() {
        if (!centerColorSampled) {
            sampleCenterColor();
        }
        return centerColor;
    }

    /**
     * Sample the center fill color from the texture pixel at ({@code texU + left}, {@code texV + top}).
     * Called once on first render.
     */
    private void sampleCenterColor() {
        int sampleX = texU + left;
        int sampleY = texV + top;
        try {
            Resource res = Minecraft.getInstance().getResourceManager().getResource(texture).orElse(null);
            if (res != null) {
                NativeImage image = NativeImage.read(res.open());
                int abgr = image.getPixelRGBA(sampleX, sampleY);
                image.close();
                // NativeImage stores ABGR (0xAABBGGRR); convert to ARGB (0xAARRGGBB)
                int a = (abgr >> 24) & 0xFF;
                int r = abgr & 0xFF;
                int g = (abgr >> 8) & 0xFF;
                int b = (abgr >> 16) & 0xFF;
                centerColor = (a << 24) | (r << 16) | (g << 8) | b;
            }
        } catch (IOException ignored) {
            // Keep default (0 = transparent black)
        }
        centerColorSampled = true;
    }

    /**
     * Render the 9-patch background into the given rectangle.
     *
     * @param g      GuiGraphics
     * @param x      destination left
     * @param y      destination top
     * @param width  destination width  (must be &ge; left + right)
     * @param height destination height (must be &ge; top + bottom)
     */
    public void render(GuiGraphics g, int x, int y, int width, int height) {
        if (!centerColorSampled) {
            sampleCenterColor();
        }

        int innerW = width  - left - right;
        int innerH = height - top - bottom;
        int srcInnerW = texWidth  - left - right;
        int srcInnerH = texHeight - top - bottom;

        final int tw = textureFileWidth;
        final int th = textureFileHeight;

        // ---- Corners (no stretch) ----
        // top-left
        g.blit(texture, x, y, left, top, texU, texV, left, top, tw, th);
        // top-right
        g.blit(texture, x + width - right, y, right, top,
                texU + texWidth - right, texV, right, top, tw, th);
        // bottom-left
        g.blit(texture, x, y + height - bottom, left, bottom,
                texU, texV + texHeight - bottom, left, bottom, tw, th);
        // bottom-right
        g.blit(texture, x + width - right, y + height - bottom, right, bottom,
                texU + texWidth - right, texV + texHeight - bottom, right, bottom, tw, th);

        // ---- Edges (stretched to fill remaining space) ----
        // top edge
        if (innerW > 0) {
            g.blit(texture, x + left, y, innerW, top,
                    texU + left, texV, srcInnerW, top, tw, th);
        }
        // bottom edge
        if (innerW > 0) {
            g.blit(texture, x + left, y + height - bottom, innerW, bottom,
                    texU + left, texV + texHeight - bottom, srcInnerW, bottom, tw, th);
        }
        // left edge
        if (innerH > 0) {
            g.blit(texture, x, y + top, left, innerH,
                    texU, texV + top, left, srcInnerH, tw, th);
        }
        // right edge
        if (innerH > 0) {
            g.blit(texture, x + width - right, y + top, right, innerH,
                    texU + texWidth - right, texV + top, right, srcInnerH, tw, th);
        }

        // ---- Center (solid fill, color auto-sampled from texture) ----
        if (innerW > 0 && innerH > 0 && centerColor != 0) {
            g.fill(x + left, y + top, x + left + innerW, y + top + innerH, centerColor);
        }
    }
}
