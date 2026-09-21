package com.carrot123.eternal_trinkets.text;

/** The rainbow toggle grammar copied from Core, extended with one more format. */
public enum SpecialTextFormat {
    RAINBOW("{/rainbow/}", "rainbow"),
    YINYANG("{/yinyang}", "yinyang");

    public final String marker;
    public final String id;
    final int[] codePoints;

    SpecialTextFormat(String marker, String id) {
        this.marker = marker;
        this.id = id;
        this.codePoints = marker.codePoints().toArray();
    }
}
