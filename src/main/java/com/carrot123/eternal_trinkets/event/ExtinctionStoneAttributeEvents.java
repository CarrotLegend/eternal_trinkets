package com.carrot123.eternal_trinkets.event;

import com.carrot123.eternal_trinkets.integration.TerraCritChanceCompat;
import com.carrot123.eternal_trinkets.item.curio.ModCurioItems;
import com.carrot123.eternal_trinkets.item.curio.curse.CursedCurioItem;
import com.carrot123.eternal_trinkets.util.AttackSpeedOverflowHelper;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

public final class ExtinctionStoneAttributeEvents {
    private static final ResourceLocation ALL_DAMAGE_ID =
            new ResourceLocation("until_eternity", "all_damage");
    private static final ResourceLocation CRITICAL_DAMAGE_ID =
            new ResourceLocation("obscure_api", "critical_damage");
    private static final UUID CRIT_CHANCE_UUID = id("crit_chance");
    private static final UUID ALL_DAMAGE_UUID = id("all_damage");
    private static final UUID CRIT_DAMAGE_UUID = id("crit_damage");

    private static UUID id(String path) {
        return UUID.nameUUIDFromBytes(
                ("eternal_trinkets:extinction_stone/" + path).getBytes(StandardCharsets.UTF_8));
    }

    private static Attribute optionalAttribute(ResourceLocation id) {
        return ModList.get().isLoaded(id.getNamespace())
                ? ForgeRegistries.ATTRIBUTES.getValue(id) : null;
    }

    private static Attribute critChance() {
        return ModList.get().isLoaded("terra_curio") ? TerraCritChanceCompat.get() : null;
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) {
            return;
        }
        double overflow = CursedCurioItem.isActive(player, ModCurioItems.EXTINCTION_STONE.get(), "charm")
                ? AttackSpeedOverflowHelper.overflow(player) : 0.0D;
        sync(player, critChance(), CRIT_CHANCE_UUID, overflow / 4.0D * 0.01D,
                AttributeModifier.Operation.ADDITION);
        sync(player, optionalAttribute(ALL_DAMAGE_ID), ALL_DAMAGE_UUID, overflow * 0.02D,
                AttributeModifier.Operation.MULTIPLY_BASE);
        sync(player, optionalAttribute(CRITICAL_DAMAGE_ID), CRIT_DAMAGE_UUID, overflow * 0.01D,
                AttributeModifier.Operation.ADDITION);
    }

    private static void sync(ServerPlayer player, Attribute attribute, UUID uuid, double amount,
                             AttributeModifier.Operation operation) {
        AttributeInstance instance = attribute == null ? null : player.getAttribute(attribute);
        if (instance == null) {
            return;
        }
        double safeAmount = Double.isFinite(amount) && amount > 0.0D ? amount : 0.0D;
        AttributeModifier current = instance.getModifier(uuid);
        if (current != null && current.getOperation() == operation
                && Double.compare(current.getAmount(), safeAmount) == 0) {
            return;
        }
        if (current != null) {
            instance.removeModifier(uuid);
        }
        if (safeAmount > 0.0D) {
            instance.addTransientModifier(new AttributeModifier(uuid,
                    "extinction_stone_" + ForgeRegistries.ATTRIBUTES.getKey(attribute),
                    safeAmount, operation));
        }
    }

    public static void clear(ServerPlayer player) {
        sync(player, critChance(), CRIT_CHANCE_UUID, 0.0D, AttributeModifier.Operation.ADDITION);
        sync(player, optionalAttribute(ALL_DAMAGE_ID), ALL_DAMAGE_UUID, 0.0D,
                AttributeModifier.Operation.MULTIPLY_BASE);
        sync(player, optionalAttribute(CRITICAL_DAMAGE_ID), CRIT_DAMAGE_UUID, 0.0D,
                AttributeModifier.Operation.ADDITION);
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
