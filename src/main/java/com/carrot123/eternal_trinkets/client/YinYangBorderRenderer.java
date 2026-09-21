package com.carrot123.eternal_trinkets.client;

import com.carrot123.eternal_trinkets.EternalTrinkets;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.RenderTooltipEvent;

import org.joml.Vector2ic;

import java.util.List;

public final class YinYangBorderRenderer {

    private static final ResourceLocation TOP_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            EternalTrinkets.MODID, "textures/gui/borders/yin_yang_upper.png");
    private static final ResourceLocation LEFT_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            EternalTrinkets.MODID, "textures/gui/borders/yin_yang_left.png");
    private static final ResourceLocation RIGHT_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            EternalTrinkets.MODID, "textures/gui/borders/yin_yang_right.png");

    // Vanilla TooltipRenderUtil expands the content rectangle by three pixels.
    private static final int FRAME_PADDING_LEFT = 3;
    private static final int FRAME_PADDING_TOP = 3;
    private static final int FRAME_PADDING_RIGHT = 3;
    private static final int FRAME_PADDING_BOTTOM = 3;
    private static final int SCREEN_MARGIN = 1;
    private static final int POSITION_ADJUSTMENT_PASSES = 4;

    // Aligns the lower center of the 68x21 ornament to the frame's top center.
    private static final TextureLayout TOP = new TextureLayout(
            TOP_TEXTURE, 68, 21, 34.0F, 20.0F, 0.0F, 7.0F, 1.0F);

    // Aligns the vertical stroke near x=6 and the top stroke near y=5.
    private static final TextureLayout LEFT = new TextureLayout(
            LEFT_TEXTURE, 32, 28, 6.0F, 5.0F, -2.0F, 2.0F, 1.0F);

    // Uses the dedicated right texture; it is not a mirrored left texture.
    private static final TextureLayout RIGHT = new TextureLayout(
            RIGHT_TEXTURE, 32, 28, 25.0F, 5.0F, 2.0F, 2.0F, 1.0F);

    /*
     * renderPrepared is called after GuiGraphics has translated the tooltip to
     * z=400. A zero relative offset keeps all layers on the same GUI plane;
     * deterministic call order places textures over the background and under
     * the title and remaining components.
     */
    private static final float DECORATION_Z_OFFSET = 0.0F;

    private static final ThreadLocal<DrawContext> PREPARED_CONTEXT = new ThreadLocal<>();

    private YinYangBorderRenderer() {
    }

    /**
     * Moves only the current YIN_YANG tooltip so all three ornaments fit on
     * screen whenever their combined visual bounds can fit.
     */
    public static void adjustTooltipPosition(RenderTooltipEvent.Pre event) {
        TooltipSize size = measure(event.getComponents(), event.getFont());
        int inputX = event.getX();
        int inputY = event.getY();

        for (int pass = 0; pass < POSITION_ADJUSTMENT_PASSES; pass++) {
            Vector2ic positioned = event.getTooltipPositioner().positionTooltip(
                    event.getScreenWidth(), event.getScreenHeight(),
                    inputX, inputY, size.width(), size.height());
            DecorationLayout layout = layout(positioned.x(), positioned.y(), size.width(), size.height());
            int shiftX = shiftToFit(layout.minX(), layout.maxX(), event.getScreenWidth());
            int shiftY = shiftToFit(layout.minY(), layout.maxY(), event.getScreenHeight());

            if (shiftX == 0 && shiftY == 0) {
                break;
            }

            inputX += shiftX;
            inputY += shiftY;
        }

        event.setX(inputX);
        event.setY(inputY);
    }

    /**
     * Captures the final tooltip rectangle from the color event. The context is
     * removed as soon as the title component attempts to render it.
     */
    public static void prepare(RenderTooltipEvent.Color event) {
        clearPrepared();
        TooltipSize size = measure(event.getComponents(), event.getFont());
        PREPARED_CONTEXT.set(new DrawContext(
                event.getGraphics(),
                layout(event.getX(), event.getY(), size.width(), size.height())));
    }

    public static void clearPrepared() {
        PREPARED_CONTEXT.remove();
    }

    /**
     * Draws a prepared overlay once. Calling this without a matching YIN_YANG
     * color event is a no-op.
     */
    public static void renderPrepared() {
        DrawContext context = PREPARED_CONTEXT.get();
        PREPARED_CONTEXT.remove();
        if (context == null) {
            return;
        }

        GuiGraphics graphics = context.graphics();
        PoseStack pose = graphics.pose();
        pose.pushPose();
        try {
            pose.translate(0.0F, 0.0F, DECORATION_Z_OFFSET);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

            draw(graphics, context.layout().left());
            draw(graphics, context.layout().right());
            draw(graphics, context.layout().top());
        } finally {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.disableBlend();
            pose.popPose();
        }
    }

    private static void draw(GuiGraphics graphics, PlacedTexture placed) {
        TextureLayout texture = placed.texture();
        PoseStack pose = graphics.pose();
        pose.pushPose();
        try {
            pose.translate(placed.x(), placed.y(), 0.0F);
            pose.scale(texture.scale(), texture.scale(), 1.0F);
            graphics.blit(texture.texture(), 0, 0, 0.0F, 0.0F,
                    texture.width(), texture.height(), texture.width(), texture.height());
        } finally {
            pose.popPose();
        }
    }

    private static TooltipSize measure(List<ClientTooltipComponent> components, Font font) {
        int width = 0;
        int height = components.size() == 1 ? -2 : 0;

        for (ClientTooltipComponent component : components) {
            width = Math.max(width, component.getWidth(font));
            height += component.getHeight();
        }

        return new TooltipSize(width, height);
    }

    private static DecorationLayout layout(int tooltipX, int tooltipY, int tooltipWidth, int tooltipHeight) {
        int frameLeft = tooltipX - FRAME_PADDING_LEFT;
        int frameTop = tooltipY - FRAME_PADDING_TOP;
        int frameRight = tooltipX + tooltipWidth + FRAME_PADDING_RIGHT;
        int frameBottom = tooltipY + tooltipHeight + FRAME_PADDING_BOTTOM;

        PlacedTexture top = place(TOP, (frameLeft + frameRight) * 0.5F, frameTop);
        PlacedTexture left = place(LEFT, frameLeft, frameTop);
        PlacedTexture right = place(RIGHT, frameRight, frameTop);

        int minX = Math.min(frameLeft, Math.min(top.minX(), Math.min(left.minX(), right.minX())));
        int minY = Math.min(frameTop, Math.min(top.minY(), Math.min(left.minY(), right.minY())));
        int maxX = Math.max(frameRight, Math.max(top.maxX(), Math.max(left.maxX(), right.maxX())));
        int maxY = Math.max(frameBottom, Math.max(top.maxY(), Math.max(left.maxY(), right.maxY())));

        return new DecorationLayout(top, left, right, minX, minY, maxX, maxY);
    }

    private static PlacedTexture place(TextureLayout texture, float targetAnchorX, float targetAnchorY) {
        int drawX = Math.round(targetAnchorX - texture.anchorX() * texture.scale() + texture.offsetX());
        int drawY = Math.round(targetAnchorY - texture.anchorY() * texture.scale() + texture.offsetY());
        int drawWidth = (int) Math.ceil(texture.width() * texture.scale());
        int drawHeight = (int) Math.ceil(texture.height() * texture.scale());
        return new PlacedTexture(texture, drawX, drawY, drawX + drawWidth, drawY + drawHeight);
    }

    private static int shiftToFit(int min, int max, int screenSize) {
        int safeMin = SCREEN_MARGIN;
        int safeMax = screenSize - SCREEN_MARGIN;
        int available = safeMax - safeMin;

        if (max - min > available) {
            return safeMin - min;
        }
        if (min < safeMin) {
            return safeMin - min;
        }
        if (max > safeMax) {
            return safeMax - max;
        }
        return 0;
    }

    private record TextureLayout(ResourceLocation texture, int width, int height,
                                 float anchorX, float anchorY,
                                 float offsetX, float offsetY, float scale) {
    }

    private record PlacedTexture(TextureLayout texture, int x, int y, int maxX, int maxY) {
        int minX() {
            return this.x;
        }

        int minY() {
            return this.y;
        }
    }

    private record TooltipSize(int width, int height) {
    }

    private record DecorationLayout(PlacedTexture top, PlacedTexture left, PlacedTexture right,
                                    int minX, int minY, int maxX, int maxY) {
    }

    private record DrawContext(GuiGraphics graphics, DecorationLayout layout) {
    }
}
