package com.carrot123.eternal_trinkets.client;

import com.aizistral.enigmaticlegacy.helpers.ItemLoreHelper;
import com.carrot123.eternal_trinkets.EternalTrinkets;
import com.carrot123.eternal_trinkets.item.curio.ModCurioItems;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = EternalTrinkets.MODID, value = Dist.CLIENT,
        bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class CursedCurioTooltips {
    private CursedCurioTooltips() {
    }

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        boolean stone = event.getItemStack().is(ModCurioItems.EXTINCTION_STONE.get());
        boolean eye = event.getItemStack().is(ModCurioItems.HELL_EYE.get());
        if (!stone && !eye) {
            return;
        }
        ItemLoreHelper.addLocalizedString(event.getToolTip(), "tooltip.enigmaticlegacy.void");
        ItemLoreHelper.addLocalizedFormattedString(event.getToolTip(),
                stone ? "curios.modifiers.charm" : "curios.modifiers.mystic_eye",
                ChatFormatting.GOLD);
        if (Screen.hasShiftDown()) {
            if (stone) {
                ItemLoreHelper.addLocalizedString(event.getToolTip(),
                        "tooltip.eternal_trinkets.extinction_stone.crit_chance", ChatFormatting.LIGHT_PURPLE);
                ItemLoreHelper.addLocalizedString(event.getToolTip(),
                        "tooltip.eternal_trinkets.extinction_stone.all_damage", ChatFormatting.GOLD);
                ItemLoreHelper.addLocalizedString(event.getToolTip(),
                        "tooltip.eternal_trinkets.extinction_stone.crit_damage", ChatFormatting.LIGHT_PURPLE);
            } else {
                ItemLoreHelper.addLocalizedString(event.getToolTip(),
                        "tooltip.eternal_trinkets.hell_eye.resistance", ChatFormatting.GOLD);
                ItemLoreHelper.addLocalizedString(event.getToolTip(),
                        "tooltip.eternal_trinkets.hell_eye.fire", ChatFormatting.LIGHT_PURPLE);
                ItemLoreHelper.addLocalizedString(event.getToolTip(),
                        "tooltip.eternal_trinkets.hell_eye.burning_damage", ChatFormatting.GOLD);
            }
        } else {
            ItemLoreHelper.addLocalizedString(event.getToolTip(),
                    "tooltip.eternal_trinkets.cursed_hold_shift", ChatFormatting.LIGHT_PURPLE);
        }
        ItemLoreHelper.addLocalizedString(event.getToolTip(), "tooltip.enigmaticlegacy.void");
        ItemLoreHelper.indicateCursedOnesOnly(event.getToolTip());
    }
}
