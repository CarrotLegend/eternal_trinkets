package com.carrot123.eternal_trinkets.event;

import com.carrot123.eternal_trinkets.entity.vehicle.WarpedFungusCapBoat;

import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class WarpedFungusCapBoatEvents {

    @SubscribeEvent
    public void onLivingAttack(LivingAttackEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.getVehicle() instanceof WarpedFungusCapBoat
                && event.getSource().is(DamageTypes.LAVA)) {
            event.setCanceled(true);
        }
    }
}
