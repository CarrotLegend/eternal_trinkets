package com.carrot123.eternal_trinkets.text;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.StringDecomposer;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Shared, side-safe marker parser. Based on Core's RainbowTextHelper toggle
 * scanner (including unclosed spans extending to the end and UTF-16 indices).
 * Animation is deliberately NOT evaluated here: cached visual-order text must
 * retain its format, not freeze the color at the time of translation.
 *
 * Format metadata uses a reversible font ID on visitation-only Style copies.
 * The original font, click/hover events, insertion and all other style fields
 * survive. Serialized Components and item NBT retain their original markup.
 */
public final class SpecialText {
    private static final String NAMESPACE = "eternal_trinkets";
    private static final String PREFIX = "special_text/";
    private static final SpecialTextFormat[] FORMATS = SpecialTextFormat.values();
    private static final ThreadLocal<Boolean> RAW_VISIT = ThreadLocal.withInitial(() -> false);
    private static final Map<Style, Style> CLEAN_STYLES = new LinkedHashMap<>(256, .75F, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<Style, Style> entry) { return size() > 512; }
    };
    private static final Map<FormattedCharSequence, Parsed> VISUAL_CACHE = new LinkedHashMap<>(256, .75F, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<FormattedCharSequence, Parsed> entry) {
            return size() > 256;
        }
    };

    private SpecialText() { }

    public record Glyph(Style style, int codePoint) { }

    public record Parsed(List<Glyph> glyphs, boolean changed, boolean special) {
        public boolean handled() { return changed || special; }

        public boolean has(SpecialTextFormat format) {
            return glyphs.stream().anyMatch(g -> format(g.style()) == format);
        }

        public FormattedCharSequence sequence() { return sequenceOf(glyphs); }

        public String plain() {
            StringBuilder result = new StringBuilder();
            glyphs.forEach(g -> result.appendCodePoint(g.codePoint()));
            return result.toString();
        }

        public FormattedText formatted() { return formattedOf(glyphs); }
    }

    public static boolean rawVisit() { return RAW_VISIT.get(); }

    public static boolean containsMarker(String text) {
        if (text == null || !text.contains("{/")) return false;
        for (SpecialTextFormat format : FORMATS) {
            if (text.contains(format.marker)) return true;
        }
        return false;
    }

    /** Collect the WHOLE component before scanning, including translation args/siblings. */
    public static Parsed component(Component component, Style parent) {
        boolean previous = RAW_VISIT.get();
        RAW_VISIT.set(true);
        try {
            return parse(formattedSequence(component, parent));
        } finally {
            RAW_VISIT.set(previous);
        }
    }

    public static String plainComponent(Component component) {
        StringBuilder raw = new StringBuilder();
        boolean previous = RAW_VISIT.get();
        RAW_VISIT.set(true);
        try {
            component.visit(text -> {
                raw.append(text);
                return Optional.empty();
            });
        } finally {
            RAW_VISIT.set(previous);
        }
        String value = raw.toString();
        // This uses the SAME scanner; it intentionally leaves vanilla section
        // codes alone, matching Component.getString's pre-existing semantics.
        return containsMarker(value) ? parse(FormattedCharSequence.forward(value, Style.EMPTY)).plain() : value;
    }

    public static Parsed parse(String text) {
        return parse(sink -> StringDecomposer.iterateFormatted(text, Style.EMPTY, sink));
    }

    public static Parsed parse(FormattedText text) {
        return parse(formattedSequence(text, Style.EMPTY));
    }

    public static Parsed parse(FormattedText text, Style parent) {
        return parse(formattedSequence(text, parent));
    }

    private static FormattedCharSequence formattedSequence(FormattedText text, Style parent) {
        List<Glyph> glyphs = new ArrayList<>();
        text.visit((style, value) -> {
            StringDecomposer.iterateFormatted(value, style, (i, s, cp) -> {
                glyphs.add(new Glyph(s, cp));
                return true;
            });
            return Optional.empty();
        }, parent);
        return sequenceOf(glyphs);
    }

    public static Parsed parse(FormattedCharSequence source) {
        if (source == null) return new Parsed(List.of(), false, false);
        synchronized (VISUAL_CACHE) {
            Parsed cached = VISUAL_CACHE.get(source);
            if (cached != null) return cached;
        }
        List<Glyph> input = new ArrayList<>();
        source.accept((index, style, cp) -> { input.add(new Glyph(style, cp)); return true; });
        List<Glyph> output = new ArrayList<>(input.size());
        EnumSet<SpecialTextFormat> active = EnumSet.noneOf(SpecialTextFormat.class);
        boolean changed = false;
        boolean special = false;
        for (int i = 0; i < input.size();) {
            SpecialTextFormat marker = null;
            if (input.get(i).codePoint() == '{') {
                for (SpecialTextFormat candidate : FORMATS) {
                    if (matches(input, i, candidate.codePoints)) { marker = candidate; break; }
                }
            }
            if (marker != null) {
                if (!active.remove(marker)) active.add(marker);
                changed = true;
                i += marker.codePoints.length;
                continue;
            }
            Glyph current = input.get(i++);
            // Crossed/nested toggles have deterministic precedence, never recursion.
            SpecialTextFormat effect = active.contains(SpecialTextFormat.ANGEL) ? SpecialTextFormat.ANGEL
                    : active.contains(SpecialTextFormat.YINYANG) ? SpecialTextFormat.YINYANG
                    : active.contains(SpecialTextFormat.RAINBOW) ? SpecialTextFormat.RAINBOW : null;
            Style style = effect == null ? current.style() : encode(current.style(), effect);
            special |= format(style) != null;
            output.add(new Glyph(style, current.codePoint()));
        }
        Parsed result = new Parsed(List.copyOf(output), changed, special);
        synchronized (VISUAL_CACHE) { VISUAL_CACHE.put(source, result); }
        return result;
    }

    private static boolean matches(List<Glyph> input, int start, int[] marker) {
        if (start + marker.length > input.size()) return false;
        for (int i = 0; i < marker.length; i++) {
            if (input.get(start + i).codePoint() != marker[i]) return false;
        }
        return true;
    }

    private static Style encode(Style style, SpecialTextFormat format) {
        ResourceLocation original = originalFont(style.getFont());
        return style.withFont(ResourceLocation.fromNamespaceAndPath(NAMESPACE,
                PREFIX + format.id + "/" + original.getNamespace() + "/" + original.getPath()));
    }

    public static SpecialTextFormat format(Style style) {
        ResourceLocation font = style.getFont();
        if (!font.getNamespace().equals(NAMESPACE)) return null;
        for (SpecialTextFormat format : FORMATS) {
            if (font.getPath().startsWith(PREFIX + format.id + "/")) return format;
        }
        return null;
    }

    public static ResourceLocation originalFont(ResourceLocation font) {
        if (!font.getNamespace().equals(NAMESPACE)) return font;
        for (SpecialTextFormat format : FORMATS) {
            String prefix = PREFIX + format.id + "/";
            if (!font.getPath().startsWith(prefix)) continue;
            String encoded = font.getPath().substring(prefix.length());
            int slash = encoded.indexOf('/');
            if (slash > 0 && slash < encoded.length() - 1) {
                return ResourceLocation.fromNamespaceAndPath(encoded.substring(0, slash), encoded.substring(slash + 1));
            }
        }
        return font;
    }

    public static Style clearFormat(Style style) {
        if (format(style) == null) return style;
        synchronized (CLEAN_STYLES) {
            return CLEAN_STYLES.computeIfAbsent(style, key -> key.withFont(originalFont(key.getFont())));
        }
    }

    public static FormattedCharSequence sequenceOf(List<Glyph> glyphs) {
        return sink -> {
            int index = 0;
            for (Glyph glyph : glyphs) {
                if (!sink.accept(index, glyph.style(), glyph.codePoint())) return false;
                index += Character.charCount(glyph.codePoint());
            }
            return true;
        };
    }

    public static FormattedText formattedOf(List<Glyph> glyphs) {
        List<FormattedText> parts = new ArrayList<>();
        Style style = null;
        StringBuilder text = new StringBuilder();
        for (Glyph glyph : glyphs) {
            if (style != null && !style.equals(glyph.style())) {
                parts.add(FormattedText.of(text.toString(), style));
                text.setLength(0);
            }
            style = glyph.style();
            text.appendCodePoint(glyph.codePoint());
        }
        if (style != null) parts.add(FormattedText.of(text.toString(), style));
        return FormattedText.composite(parts);
    }
}
