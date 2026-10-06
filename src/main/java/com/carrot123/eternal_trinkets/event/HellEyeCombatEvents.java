package com.carrot123.eternal_trinkets.event;

import com.carrot123.eternal_trinkets.item.curio.ModCurioItems;
import com.carrot123.eternal_trinkets.item.curio.curse.CursedCurioItem;
import com.carrot123.eternal_trinkets.item.curio.curse.HellEyeItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class HellEyeCombatEvents {
    private static final int FIRE_SECONDS = 5;

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onDamage(LivingDamageEvent event) {
        LivingEntity target = event.getEntity();
        if (target.level().isClientSide || event.getAmount() <= 0.0F
                || !Float.isFinite(event.getAmount())
                || !(event.getSource().getEntity() instanceof ServerPlayer attacker)
                || event.getSource().getDirectEntity() == null
                || target == attacker
                || !CursedCurioItem.isActive(attacker, ModCurioItems.HELL_EYE.get(), "mystic_eye")) {
            return;
        }
        boolean wasBurning = target.isOnFire();
        if (wasBurning) {
            event.setAmount((float) Math.min(Float.MAX_VALUE, event.getAmount() * 1.10D));
        }
        target.setSecondsOnFire(FIRE_SECONDS);
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.player instanceof ServerPlayer player) {
            syncResistance(player);
        }
    }

    private static void syncResistance(ServerPlayer player) {
        Attribute attribute = HellEyeItem.resistanceAttribute();
        AttributeInstance instance = attribute == null ? null : player.getAttribute(attribute);
        if (instance == null) {
            return;
        }
        boolean active = CursedCurioItem.isActive(player, ModCurioItems.HELL_EYE.get(), "mystic_eye");
        AttributeModifier current = instance.getModifier(HellEyeItem.RESISTANCE_UUID);
        if (!active) {
            if (current != null) {
                instance.removeModifier(HellEyeItem.RESISTANCE_UUID);
            }
            return;
        }
        if (current == null || current.getOperation() != AttributeModifier.Operation.MULTIPLY_BASE
                || Double.compare(current.getAmount(), 0.08D) != 0) {
            if (current != null) {
                instance.removeModifier(HellEyeItem.RESISTANCE_UUID);
            }
            instance.addTransientModifier(HellEyeItem.resistanceModifier());
        }
    }

    private static void clear(ServerPlayer player) {
        Attribute attribute = HellEyeItem.resistanceAttribute();
        AttributeInstance instance = attribute == null ? null : player.getAttribute(attribute);
        if (instance != null) {
            instance.removeModifier(HellEyeItem.RESISTANCE_UUID);
        }
    }

    @SubscribeEvent
    public void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            clear(player);
        }
    }

    @SubscribeEvent
    public void onDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            clear(player);
        }
    }

    @SubscribeEvent
    public void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            clear(player);
        }
    }
}
