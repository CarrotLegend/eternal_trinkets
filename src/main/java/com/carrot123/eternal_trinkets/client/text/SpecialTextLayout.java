package com.carrot123.eternal_trinkets.client.text;

import com.carrot123.eternal_trinkets.text.SpecialText;
import com.carrot123.eternal_trinkets.text.SpecialText.Glyph;
import com.carrot123.eternal_trinkets.text.SpecialTextFormat;
import net.minecraft.client.StringSplitter;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;

import java.util.List;
import java.util.function.BiConsumer;

/** One footprint calculation shared by measuring, wrapping and hit testing. */
public final class SpecialTextLayout {
    public static final float SHADOW_SCALE = 1.1F;
    public static final int HORIZONTAL_PADDING = 8;
    public static final int VERTICAL_PADDING = 4;
    public static final int LINE_HEIGHT = 18;

    private SpecialTextLayout() { }

    public static float advance(List<Glyph> glyphs, int i, StringSplitter.WidthProvider widths) {
        Glyph glyph = glyphs.get(i);
        float width = widths.getWidth(glyph.codePoint(), SpecialText.clearFormat(glyph.style()));
        if (SpecialText.format(glyph.style()) != SpecialTextFormat.YINYANG) return width;
        width *= SHADOW_SCALE;
        if (i == 0 || SpecialText.format(glyphs.get(i - 1).style()) != SpecialTextFormat.YINYANG) {
            width += HORIZONTAL_PADDING;
        }
        if (i + 1 == glyphs.size() || SpecialText.format(glyphs.get(i + 1).style()) != SpecialTextFormat.YINYANG) {
            width += HORIZONTAL_PADDING;
        }
        return width;
    }

    public static float width(List<Glyph> glyphs, StringSplitter.WidthProvider widths) {
        float result = 0;
        for (int i = 0; i < glyphs.size(); i++) result += advance(glyphs, i, widths);
        return result;
    }

    public static Style styleAt(List<Glyph> glyphs, int x, StringSplitter.WidthProvider widths) {
        float position = 0;
        for (int i = 0; i < glyphs.size(); i++) {
            position += advance(glyphs, i, widths);
            if (position > x) return SpecialText.clearFormat(glyphs.get(i).style());
        }
        return null;
    }

    public static FormattedText head(List<Glyph> glyphs, int maxWidth, StringSplitter.WidthProvider widths) {
        int end = 0;
        float used = 0;
        boolean inYinYang = false;
        while (end < glyphs.size()) {
            Glyph glyph = glyphs.get(end);
            boolean yinYang = SpecialText.format(glyph.style()) == SpecialTextFormat.YINYANG;
            float next = widths.getWidth(glyph.codePoint(), SpecialText.clearFormat(glyph.style()))
                    * (yinYang ? SHADOW_SCALE : 1);
            if (yinYang && !inYinYang) next += 2 * HORIZONTAL_PADDING;
            if (used + next > maxWidth) break;
            used += next;
            inYinYang = yinYang;
            end++;
        }
        return SpecialText.formattedOf(glyphs.subList(0, end));
    }

    /** Greedy vanilla-style word wrapping, with complete margins on EACH wrapped span. */
    public static void split(List<Glyph> glyphs, int maxWidth, StringSplitter.WidthProvider widths,
                             BiConsumer<FormattedText, Boolean> consumer) {
        int start = 0;
        boolean continuation = false;
        while (start < glyphs.size()) {
            int end = start;
            int space = -1;
            float used = 0;
            boolean inYinYang = false;
            boolean newline = false;
            while (end < glyphs.size()) {
                Glyph glyph = glyphs.get(end);
                if (glyph.codePoint() == '\n') { newline = true; break; }
                boolean yinYang = SpecialText.format(glyph.style()) == SpecialTextFormat.YINYANG;
                float next = widths.getWidth(glyph.codePoint(), SpecialText.clearFormat(glyph.style()))
                        * (yinYang ? SHADOW_SCALE : 1);
                if (yinYang && !inYinYang) next += HORIZONTAL_PADDING * 2;
                // Always accept one glyph, including widths smaller than a single glyph.
                if (end > start && used + next > maxWidth) break;
                used += next;
                if (glyph.codePoint() == ' ') space = end;
                inYinYang = yinYang;
                end++;
            }
            int nextStart;
            if (newline) {
                nextStart = end + 1;
            } else if (end < glyphs.size() && space >= start) {
                end = space;
                nextStart = space + 1;
            } else {
                nextStart = end;
            }
            consumer.accept(SpecialText.formattedOf(glyphs.subList(start, end)), continuation);
            continuation = !newline;
            start = nextStart;
            if (newline && start == glyphs.size()) consumer.accept(FormattedText.EMPTY, false);
        }
    }
}
