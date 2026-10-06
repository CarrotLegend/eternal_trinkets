package com.carrot123.eternal_trinkets.integration;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraftforge.fml.ModList;
import org.confluence.terra_curio.misc.ModAttributes;

public final class TerraCritChanceCompat {
    private TerraCritChanceCompat() {
    }

    public static Attribute get() {
        return ModList.get().isLoaded("terra_curio")
                ? ModAttributes.getCriticalChance() : null;
    }
}
