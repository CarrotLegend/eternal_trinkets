package com.carrot123.eternal_trinkets.event;

import com.carrot123.eternal_trinkets.EternalTrinkets;
import com.carrot123.eternal_trinkets.item.ModItems;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = EternalTrinkets.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class HoneyInfusedFruitEvents {

    private HoneyInfusedFruitEvents() {
    }

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.is(ModItems.HONEY_INFUSED_FRUIT.get())) {
            event.getToolTip().add(Component.translatable(
                    "tooltip.eternal_trinkets.honey_infused_fruit.heal")
                    .withStyle(ChatFormatting.GOLD));
            event.getToolTip().add(Component.translatable(
                    "tooltip.eternal_trinkets.honey_infused_fruit.effect")
                    .withStyle(ChatFormatting.GOLD));
        }
    }
}
