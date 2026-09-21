package com.carrot123.eternal_trinkets.integration.jei;

import com.carrot123.eternal_trinkets.EternalTrinkets;
import com.carrot123.eternal_trinkets.item.ModItems;
import com.carrot123.eternal_trinkets.item.curio.ModCurioItems;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IRecipeRegistration;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

@JeiPlugin
public class JEIPlugin implements IModPlugin {
    private static final ResourceLocation UID =
            ResourceLocation.fromNamespaceAndPath(EternalTrinkets.MODID, "jei_plugin");

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addIngredientInfo(
                new ItemStack(ModItems.WARPED_CORE.get()),
                VanillaTypes.ITEM_STACK,
                Component.translatable("jei.eternal_trinkets.warped_core.desc"));

        registration.addIngredientInfo(
                new ItemStack(ModItems.WARPED_FUNGUS_SPRITE_SPAWN_EGG.get()),
                VanillaTypes.ITEM_STACK,
                Component.translatable("jei.eternal_trinkets.warped_fungus_sprite.desc"));

        registration.addIngredientInfo(
                new ItemStack(ModItems.WARPED_FUNGUS_UMBRELLA_SPAWN_EGG.get()),
                VanillaTypes.ITEM_STACK,
                Component.translatable("jei.eternal_trinkets.warped_fungus_umbrella.desc"));

        registration.addIngredientInfo(
                new ItemStack(ModCurioItems.LUCKY_CLOVER.get()),
                VanillaTypes.ITEM_STACK,
                Component.translatable("jei.eternal_trinkets.lucky_clover.desc"));
    }
}
