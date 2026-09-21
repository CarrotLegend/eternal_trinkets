package com.carrot123.eternal_trinkets.client.text;

import com.carrot123.eternal_trinkets.text.SpecialText;
import com.carrot123.eternal_trinkets.text.SpecialText.Glyph;
import com.carrot123.eternal_trinkets.text.SpecialTextFormat;
import net.minecraft.Util;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Effects for parsed spans; knows nothing about items, rarity, Tooltip or HUD. */
public final class SpecialTextRenderer {
    public static final float HORIZONTAL_AMPLITUDE = 4.5F;
    public static final float VERTICAL_AMPLITUDE = 1.5F;
    public static final long ANIMATION_PERIOD_NANOS = 2_000_000_000L;
    public static final double SHADOW_PHASE_DELAY = Math.PI / 4.0;
    private static final float MAIN_TEXT_OUTLINE_OFFSET = 1.0F;
    private static final int SHADOW_ALPHA = 0x80;
    private static final int SHADOW_OUTLINE_ALPHA = 0x70;
    private static final int SHADOW_OUTLINE_RGB = 0xD5D5D5;
    // Copied from Core's RainbowTextHelper, unchanged.
    public static final long RAINBOW_CYCLE_TIME_MS = 3000L;
    public static final float CHARACTER_HUE_OFFSET = 0.08F;
    private static final ThreadLocal<Boolean> DRAWING = ThreadLocal.withInitial(() -> false);
    private static final Map<SpecialText.Parsed, List<Run>> RUN_CACHE = new LinkedHashMap<>(256, .75F, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<SpecialText.Parsed, List<Run>> entry) {
            return size() > 256;
        }
    };

    private record Run(SpecialTextFormat format, FormattedCharSequence text, int characters) { }
    private SpecialTextRenderer() { }

    public static boolean isDrawing() { return DRAWING.get(); }

    private static List<Run> runs(SpecialText.Parsed parsed) {
        List<Run> cached = RUN_CACHE.get(parsed);
        if (cached != null) return cached;
        List<Run> runs = new ArrayList<>();
        List<Glyph> current = new ArrayList<>();
        SpecialTextFormat previous = null;
        for (Glyph glyph : parsed.glyphs()) {
            SpecialTextFormat format = SpecialText.format(glyph.style());
            if (!current.isEmpty() && format != previous) {
                runs.add(new Run(previous, SpecialText.sequenceOf(List.copyOf(current)), current.size()));
                current.clear();
            }
            previous = format;
            current.add(new Glyph(SpecialText.clearFormat(glyph.style()), glyph.codePoint()));
        }
        if (!current.isEmpty()) runs.add(new Run(previous, SpecialText.sequenceOf(List.copyOf(current)), current.size()));
        List<Run> result = List.copyOf(runs);
        RUN_CACHE.put(parsed, result);
        return result;
    }

    public static int draw(Font font, SpecialText.Parsed parsed, float x, float y, int color,
                            boolean dropShadow, Matrix4f matrix, MultiBufferSource buffer,
                            Font.DisplayMode mode, int background, int light, Integer outline) {
        boolean previous = DRAWING.get();
        DRAWING.set(true);
        try {
            return drawParsed(font, parsed, x, y, color, dropShadow, matrix, buffer, mode, background, light, outline);
        } finally {
            DRAWING.set(previous);
        }
    }

    private static int drawParsed(Font font, SpecialText.Parsed parsed, float x, float y, int color,
                                   boolean dropShadow, Matrix4f matrix, MultiBufferSource buffer,
                                   Font.DisplayMode mode, int background, int light, Integer outline) {
        float cursor = x;
        int rainbowIndex = 0;
        float baseHue = (System.currentTimeMillis() % RAINBOW_CYCLE_TIME_MS) / (float) RAINBOW_CYCLE_TIME_MS;
        for (Run run : runs(parsed)) {
            float width = font.getSplitter().stringWidth(run.text());
            if (run.format() == SpecialTextFormat.YINYANG) {
                float baseX = cursor + SpecialTextLayout.HORIZONTAL_PADDING
                        + width * (SpecialTextLayout.SHADOW_SCALE - 1) * .5F;
                drawYinYang(font, run.text(), width, baseX, y, color, matrix, buffer, mode, light);
                cursor += width * SpecialTextLayout.SHADOW_SCALE + 2 * SpecialTextLayout.HORIZONTAL_PADDING;
            } else {
                FormattedCharSequence text = run.format() == SpecialTextFormat.RAINBOW
                        ? rainbow(run.text(), baseHue, rainbowIndex) : run.text();
                if (outline != null) font.drawInBatch8xOutline(text, cursor, y, color, outline, matrix, buffer, light);
                else font.drawInBatch(text, cursor, y, color, dropShadow, matrix, buffer, mode, background, light);
                cursor += width;
                if (run.format() == SpecialTextFormat.RAINBOW) rainbowIndex += run.characters();
            }
        }
        return (int) cursor + (dropShadow ? 1 : 0);
    }

    private static void drawYinYang(Font font, FormattedCharSequence text, float width,
                                    float x, float y, int color, Matrix4f matrix,
                                    MultiBufferSource buffer, Font.DisplayMode mode, int light) {
        // Same alpha convention as vanilla Font.adjustColor, including fading HUD text.
        int alpha = (color & 0xFC000000) == 0 ? 255 : color >>> 24;
        double phase = Math.floorMod(Util.getNanos(), ANIMATION_PERIOD_NANOS)
                * (Math.PI * 2.0) / ANIMATION_PERIOD_NANOS;
        double shadowPhase = phase - SHADOW_PHASE_DELAY;
        float mainX = x + HORIZONTAL_AMPLITUDE * (float) Math.sin(phase);
        float mainY = y + VERTICAL_AMPLITUDE * (float) Math.sin(2 * phase);
        float shadowX = x + HORIZONTAL_AMPLITUDE * (float) Math.sin(shadowPhase);
        float shadowY = y + VERTICAL_AMPLITUDE * (float) Math.sin(2 * shadowPhase);
        int gray = (int) Math.round((1 - Math.cos(phase)) * .5 * 255);
        int shadowRgb = gray << 16 | gray << 8 | gray;
        int mainColor = alpha << 24 | 0xFFFFFF;
        int black = alpha << 24;
        int shadowColor = Math.round(SHADOW_ALPHA * alpha / 255F) << 24 | shadowRgb;
        int shadowOutline = Math.round(SHADOW_OUTLINE_ALPHA * alpha / 255F) << 24 | SHADOW_OUTLINE_RGB;

        float centerX = shadowX + width * .5F;
        float centerY = shadowY + font.lineHeight * .5F;
        Matrix4f shadowMatrix = new Matrix4f(matrix).translate(centerX, centerY, 0)
                .scale(SpecialTextLayout.SHADOW_SCALE).translate(-centerX, -centerY, 0);
        font.drawInBatch8xOutline(recolor(text, shadowRgb), shadowX, shadowY,
                shadowColor, shadowOutline, shadowMatrix, buffer, light);

        Matrix4f mainMatrix = new Matrix4f(matrix);
        FormattedCharSequence blackText = recolor(text, 0);
        font.drawInBatch(blackText, mainX - MAIN_TEXT_OUTLINE_OFFSET, mainY,
                black, false, mainMatrix, buffer, mode, 0, light);
        font.drawInBatch(blackText, mainX + MAIN_TEXT_OUTLINE_OFFSET, mainY,
                black, false, mainMatrix, buffer, mode, 0, light);
        font.drawInBatch(blackText, mainX, mainY - MAIN_TEXT_OUTLINE_OFFSET,
                black, false, mainMatrix, buffer, mode, 0, light);
        font.drawInBatch(blackText, mainX, mainY + MAIN_TEXT_OUTLINE_OFFSET,
                black, false, mainMatrix, buffer, mode, 0, light);
        font.drawInBatch8xOutline(recolor(text, 0xFFFFFF), mainX, mainY,
                mainColor, black, mainMatrix, buffer, light);
    }

    private static FormattedCharSequence recolor(FormattedCharSequence text, int rgb) {
        return sink -> text.accept((index, style, cp) -> sink.accept(index, style.withColor(rgb), cp));
    }

    private static FormattedCharSequence rainbow(FormattedCharSequence text, float hue, int startIndex) {
        return sink -> text.accept(new net.minecraft.util.FormattedCharSink() {
            private int characterIndex = startIndex;
            @Override
            public boolean accept(int index, Style style, int cp) {
                return sink.accept(index, style.withColor(hsvToRgb(
                        (hue + characterIndex++ * CHARACTER_HUE_OFFSET) % 1F, 1F, 1F)), cp);
            }
        });
    }

    // Core's HSV calculation, unchanged (including rounding instead of truncation).
    public static int hsvToRgb(float hue, float saturation, float value) {
        hue -= (float) Math.floor(hue);
        float h = hue * 6F;
        int sector = (int) Math.floor(h);
        float fraction = h - sector;
        float p = value * (1F - saturation);
        float q = value * (1F - saturation * fraction);
        float t = value * (1F - saturation * (1F - fraction));
        float red, green, blue;
        switch (sector % 6) {
            case 0 -> { red = value; green = t; blue = p; }
            case 1 -> { red = q; green = value; blue = p; }
            case 2 -> { red = p; green = value; blue = t; }
            case 3 -> { red = p; green = q; blue = value; }
            case 4 -> { red = t; green = p; blue = value; }
            default -> { red = value; green = p; blue = q; }
        }
        return clampColor(Math.round(red * 255F)) << 16
                | clampColor(Math.round(green * 255F)) << 8 | clampColor(Math.round(blue * 255F));
    }

    private static int clampColor(int value) { return Math.max(0, Math.min(255, value)); }
}
