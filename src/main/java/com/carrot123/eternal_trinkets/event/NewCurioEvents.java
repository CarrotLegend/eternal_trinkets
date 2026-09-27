package com.carrot123.eternal_trinkets.event;

import com.carrot123.eternal_trinkets.item.curio.ModCurioItems;
import com.carrot123.eternal_trinkets.item.curio.OptionalAttributeCurioItem;
import com.carrot123.eternal_trinkets.util.CuriosUtils;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.Collections;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

public final class NewCurioEvents {
    private static final ResourceLocation PURE_RESISTANCE =
            new ResourceLocation("enigmaticaddons", "pure_resistance");
    private static final ResourceLocation ALL_DAMAGE =
            new ResourceLocation("until_eternity", "all_damage");
    private static final ResourceLocation CHARGE_SPEED =
            new ResourceLocation("until_eternity", "charge_speed");
    private static final UUID COUNTER_UUID = UUID.nameUUIDFromBytes(
            "eternal_trinkets:counter_eye/all_damage".getBytes(StandardCharsets.UTF_8));
    private static final Map<UUID, Integer> COUNTER_EXPIRY = new HashMap<>();
    private static final Map<net.minecraft.world.entity.LivingEntity, Double> CHARGE_FRACTION =
            Collections.synchronizedMap(new WeakHashMap<>());

    @SubscribeEvent(priority = EventPriority.MONITOR)
    public void onDamage(LivingDamageEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || event.getAmount() <= 0.0F) {
            return;
        }
        if (CuriosUtils.hasCurioEquipped(player, ModCurioItems.CRYSTAL_NECKLACE.get())
                && ModList.get().isLoaded("enigmaticaddons")) {
            MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(PURE_RESISTANCE);
            if (effect != null) {
                MobEffectInstance current = player.getEffect(effect);
                if (current == null || current.getAmplifier() == 0 && current.getDuration() < 100) {
                    player.addEffect(new MobEffectInstance(effect, 100, 0));
                }
            }
        }
        if (CuriosUtils.hasCurioEquipped(player, ModCurioItems.COUNTER_EYE.get())) {
            refreshCounter(player);
        }
    }

    private static void refreshCounter(ServerPlayer player) {
        Attribute attribute = ModList.get().isLoaded("until_eternity")
                ? ForgeRegistries.ATTRIBUTES.getValue(ALL_DAMAGE) : null;
        AttributeInstance instance = attribute == null ? null : player.getAttribute(attribute);
        if (instance == null) {
            return;
        }
        instance.removeModifier(COUNTER_UUID);
        instance.addTransientModifier(new AttributeModifier(COUNTER_UUID,
                "counter_eye_all_damage", 0.25D, AttributeModifier.Operation.MULTIPLY_BASE));
        COUNTER_EXPIRY.put(player.getUUID(), player.server.getTickCount() + 100);
    }

    public static void clearCounter(net.minecraft.world.entity.LivingEntity wearer) {
        if (!(wearer instanceof ServerPlayer player)) {
            return;
        }
        COUNTER_EXPIRY.remove(player.getUUID());
        Attribute attribute = ModList.get().isLoaded("until_eternity")
                ? ForgeRegistries.ATTRIBUTES.getValue(ALL_DAMAGE) : null;
        AttributeInstance instance = attribute == null ? null : player.getAttribute(attribute);
        if (instance != null) {
            instance.removeModifier(COUNTER_UUID);
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) {
            return;
        }
        Integer expiry = COUNTER_EXPIRY.get(player.getUUID());
        if (expiry != null && (player.server.getTickCount() >= expiry
                || !CuriosUtils.hasCurioEquipped(player, ModCurioItems.COUNTER_EYE.get()))) {
            clearCounter(player);
        }
    }

    @SubscribeEvent
    public void onUsingItem(LivingEntityUseItemEvent.Tick event) {
        if (!(event.getEntity() instanceof net.minecraft.world.entity.player.Player player)
                || !(event.getItem().getItem() instanceof BowItem
                || event.getItem().getItem() instanceof CrossbowItem)
                || !ModList.get().isLoaded("until_eternity")) {
            return;
        }
        Attribute attribute = ForgeRegistries.ATTRIBUTES.getValue(CHARGE_SPEED);
        if (attribute == null || player.getAttribute(attribute) == null) {
            return;
        }
        double extra = Math.max(0.0D, Math.min(4.0D, player.getAttributeValue(attribute) - 1.0D));
        if (extra == 0.0D) {
            CHARGE_FRACTION.remove(player);
            return;
        }
        double accumulated = CHARGE_FRACTION.getOrDefault(player, 0.0D) + extra;
        int bonusTicks = (int) accumulated;
        CHARGE_FRACTION.put(player, accumulated - bonusTicks);
        if (bonusTicks > 0) {
            event.setDuration(Math.max(1, event.getDuration() - bonusTicks));
        }
    }

    @SubscribeEvent
    public void onUseStop(LivingEntityUseItemEvent.Stop event) {
        CHARGE_FRACTION.remove(event.getEntity());
    }

    @SubscribeEvent
    public void onUseFinish(LivingEntityUseItemEvent.Finish event) {
        CHARGE_FRACTION.remove(event.getEntity());
    }

    @SubscribeEvent
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            clearCounter(player);
        }
        CHARGE_FRACTION.remove(event.getEntity());
    }

    @SubscribeEvent
    public void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            clearCounter(player);
        }
    }
}
