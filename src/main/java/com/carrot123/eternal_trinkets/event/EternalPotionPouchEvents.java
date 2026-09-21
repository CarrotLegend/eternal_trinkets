package com.carrot123.eternal_trinkets.event;

import com.carrot123.eternal_trinkets.EternalTrinkets;
import com.carrot123.eternal_trinkets.item.EternalPotionPouchData;
import com.carrot123.eternal_trinkets.item.ModItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = EternalTrinkets.MODID,
        bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class EternalPotionPouchEvents {
    private EternalPotionPouchEvents() {
        throw new UnsupportedOperationException("utility class");
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = false)
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            clearAllPouches(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (event.isWasDeath()
                && event.getEntity() instanceof ServerPlayer player) {
            clearAllPouches(player);
        }
    }

    private static void clearAllPouches(ServerPlayer player) {
        for (int index = 0;
             index < player.getInventory().getContainerSize();
             index++) {
            ItemStack stack = player.getInventory().getItem(index);
            if (stack.is(ModItems.ETERNAL_POTION_POUCH.get())) {
                EternalPotionPouchData.clearStoredPotion(stack);
            }
        }
    }
}
