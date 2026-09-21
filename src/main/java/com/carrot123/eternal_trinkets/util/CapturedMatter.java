package com.carrot123.eternal_trinkets.util;

import java.util.Locale;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

/**
 * Stable, non-localized capture states stored by the Void Capture Device.
 */
public enum CapturedMatter {
    NONE("none", "matter.eternal_trinkets.none", 0.0F),
    AIR("air", "matter.eternal_trinkets.air", 1.0F),
    AETHER("aether", "matter.eternal_trinkets.aether", 2.0F),
    PURE_VOID("pure_void", "matter.eternal_trinkets.pure_void", 3.0F),
    THIN_VOID("thin_void", "matter.eternal_trinkets.thin_void", 4.0F),
    HELL_BREATH("hell_breath", "matter.eternal_trinkets.hell_breath", 5.0F);

    public static final String TAG_CAPTURED_MATTER = "CapturedMatter";

    private final String id;
    private final String translationKey;
    private final float predicateValue;

    CapturedMatter(String id, String translationKey, float predicateValue) {
        this.id = id;
        this.translationKey = translationKey;
        this.predicateValue = predicateValue;
    }

    public String id() {
        return this.id;
    }

    public String translationKey() {
        return this.translationKey;
    }

    public float predicateValue() {
        return this.predicateValue;
    }

    public static CapturedMatter fromStack(ItemStack stack) {
        if (!stack.hasTag()) {
            return NONE;
        }
        CompoundTag tag = stack.getTag();
        return tag == null ? NONE : fromId(tag.getString(TAG_CAPTURED_MATTER));
    }

    public static CapturedMatter fromId(String id) {
        if (id == null || id.isBlank()) {
            return NONE;
        }
        String normalized = id.toLowerCase(Locale.ROOT);
        for (CapturedMatter matter : values()) {
            if (matter.id.equals(normalized)) {
                return matter;
            }
        }
        return NONE;
    }

    public void writeTo(ItemStack stack) {
        if (this == NONE) {
            if (stack.hasTag()) {
                CompoundTag tag = stack.getTag();
                if (tag != null) {
                    tag.remove(TAG_CAPTURED_MATTER);
                    if (tag.isEmpty()) {
                        stack.setTag(null);
                    }
                }
            }
            return;
        }
        stack.getOrCreateTag().putString(TAG_CAPTURED_MATTER, this.id);
    }
}
