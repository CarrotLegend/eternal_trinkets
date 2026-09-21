package com.carrot123.eternal_trinkets.event;

import com.carrot123.eternal_trinkets.EternalTrinkets;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = EternalTrinkets.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class HoneyBottleEvents {

    private static final float HEAL_AMOUNT = 2.0F;

    private HoneyBottleEvents() {
    }

    @SubscribeEvent
    public static void onItemUseFinished(LivingEntityUseItemEvent.Finish event) {
        if (!event.getItem().is(Items.HONEY_BOTTLE)
                || event.getEntity().level().isClientSide
                || !(event.getEntity() instanceof Player player)) {
            return;
        }

        player.heal(HEAL_AMOUNT);
    }

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        if (event.getItemStack().is(Items.HONEY_BOTTLE)) {
            event.getToolTip().add(Component.translatable("tooltip.eternal_trinkets.honey_bottle.heal")
                    .withStyle(ChatFormatting.RED));
        }
    }
}
