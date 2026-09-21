package com.carrot123.eternal_trinkets.item;

import net.minecraft.ChatFormatting;
import net.minecraft.world.item.Rarity;

public final class ModRarity {
    public static final Rarity YIN_YANG = Rarity.create("eternal_trinkets:yin_yang", ChatFormatting.WHITE);

    private ModRarity() {
        throw new UnsupportedOperationException("utility class");
    }
}
