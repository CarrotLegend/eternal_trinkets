package com.carrot123.eternal_trinkets.item.curio;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

public final class CurioRangedDamage {
    public static final ResourceLocation ID = new ResourceLocation("puffish_attributes", "ranged_damage");

    private CurioRangedDamage() {
    }

    public static Attribute get() {
        return ModList.get().isLoaded(ID.getNamespace())
                ? ForgeRegistries.ATTRIBUTES.getValue(ID) : null;
    }
}
