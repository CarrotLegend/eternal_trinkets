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
    public static final float ANGEL_FOG_SCALE = 1.06F;
    public static final int ANGEL_HORIZONTAL_PADDING = 6;
    public static final int ANGEL_VERTICAL_PADDING = 3;
    public static final int ANGEL_LINE_HEIGHT = 16;

    private SpecialTextLayout() { }

    public static float advance(List<Glyph> glyphs, int i, StringSplitter.WidthProvider widths) {
        Glyph glyph = glyphs.get(i);
        float width = widths.getWidth(glyph.codePoint(), SpecialText.clearFormat(glyph.style()));
        SpecialTextFormat format = SpecialText.format(glyph.style());
        if (format != SpecialTextFormat.YINYANG && format != SpecialTextFormat.ANGEL) return width;
        width *= scale(format);
        if (i == 0 || SpecialText.format(glyphs.get(i - 1).style()) != format) {
            width += padding(format);
        }
        if (i + 1 == glyphs.size() || SpecialText.format(glyphs.get(i + 1).style()) != format) {
            width += padding(format);
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
        SpecialTextFormat previous = null;
        while (end < glyphs.size()) {
            Glyph glyph = glyphs.get(end);
            SpecialTextFormat format = SpecialText.format(glyph.style());
            float next = widths.getWidth(glyph.codePoint(), SpecialText.clearFormat(glyph.style()))
                    * scale(format);
            if (format != previous && padding(format) > 0) next += 2 * padding(format);
            if (used + next > maxWidth) break;
            used += next;
            previous = format;
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
            SpecialTextFormat previous = null;
            boolean newline = false;
            while (end < glyphs.size()) {
                Glyph glyph = glyphs.get(end);
                if (glyph.codePoint() == '\n') { newline = true; break; }
                SpecialTextFormat format = SpecialText.format(glyph.style());
                float next = widths.getWidth(glyph.codePoint(), SpecialText.clearFormat(glyph.style()))
                        * scale(format);
                if (format != previous && padding(format) > 0) next += padding(format) * 2;
                // Always accept one glyph, including widths smaller than a single glyph.
                if (end > start && used + next > maxWidth) break;
                used += next;
                if (glyph.codePoint() == ' ') space = end;
                previous = format;
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

    public static int padding(SpecialTextFormat format) {
        return format == SpecialTextFormat.YINYANG ? HORIZONTAL_PADDING
                : format == SpecialTextFormat.ANGEL ? ANGEL_HORIZONTAL_PADDING : 0;
    }

    public static float scale(SpecialTextFormat format) {
        return format == SpecialTextFormat.YINYANG ? SHADOW_SCALE
                : format == SpecialTextFormat.ANGEL ? ANGEL_FOG_SCALE : 1.0F;
    }
}
