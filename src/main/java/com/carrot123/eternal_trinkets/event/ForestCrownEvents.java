package com.carrot123.eternal_trinkets.event;

import com.carrot123.eternal_trinkets.item.curio.ModCurioItems;
import com.carrot123.eternal_trinkets.item.curio.CurioRangedDamage;
import com.carrot123.eternal_trinkets.item.curio.ElfBootsItem;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.ArrowLooseEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import top.theillusivec4.curios.api.CuriosApi;

public final class ForestCrownEvents {
    private static final String TAG_FULL_DRAW = "eternal_trinkets:forest_crown_full_draw";
    private static final String TAG_SHOOTER = "eternal_trinkets:forest_crown_shooter";
    private static final String TAG_HIT_TARGETS = "eternal_trinkets:forest_crown_hit_targets";
    private static final Map<UUID, PendingShot> PENDING = new HashMap<>();

    private record PendingShot(net.minecraft.resources.ResourceKey<Level> dimension, long tick) {
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onArrowLoose(ArrowLooseEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        UUID playerId = player.getUUID();
        if (!(event.getBow().getItem() instanceof BowItem)
                || BowItem.getPowerForTime(event.getCharge()) < 1.0F - 1.0E-6F
                || !hasCrown(player)) {
            PENDING.remove(playerId);
            return;
        }
        PENDING.put(playerId, new PendingShot(player.level().dimension(),
                player.level().getGameTime()));
    }

    private static boolean hasCrown(ServerPlayer player) {
        return CuriosApi.getCuriosInventory(player).resolve()
                .flatMap(handler -> handler.findFirstCurio(ModCurioItems.FOREST_CROWN.get()))
                .filter(result -> "head".equals(result.slotContext().identifier())
                        && !result.slotContext().cosmetic())
                .isPresent();
    }

    private static boolean hasBoots(ServerPlayer player) {
        return CuriosApi.getCuriosInventory(player).resolve()
                .flatMap(handler -> handler.findFirstCurio(ModCurioItems.ELF_BOOTS.get()))
                .filter(result -> "feet".equals(result.slotContext().identifier())
                        && !result.slotContext().cosmetic())
                .isPresent();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onArrowJoin(EntityJoinLevelEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)
                || event.loadedFromDisk()
                || !(event.getEntity() instanceof AbstractArrow arrow)
                || arrow instanceof ThrownTrident
                || !(arrow.getOwner() instanceof ServerPlayer shooter)) {
            return;
        }
        PendingShot pending = PENDING.get(shooter.getUUID());
        if (pending == null || level.getGameTime() < pending.tick()
                || level.getGameTime() - pending.tick() > 5L
                || !pending.dimension().equals(level.dimension())) {
            return;
        }
        PENDING.remove(shooter.getUUID());
        CompoundTag data = arrow.getPersistentData();
        data.putBoolean(TAG_FULL_DRAW, true);
        data.putUUID(TAG_SHOOTER, shooter.getUUID());
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onArrowDamage(LivingHurtEvent event) {
        if (!(event.getEntity().level() instanceof ServerLevel)
                || !(event.getSource().getEntity() instanceof ServerPlayer shooter)
                || !(event.getAmount() > 0.0F)
                || !Float.isFinite(event.getAmount())) {
            return;
        }
        double multiplier = 1.0D;
        if (event.getSource().is(DamageTypeTags.IS_PROJECTILE)
                && (CurioRangedDamage.get() == null
                        || shooter.getAttribute(CurioRangedDamage.get()) == null)) {
            if (hasCrown(shooter)) {
                multiplier += 0.20D;
            }
            if (hasBoots(shooter)) {
                multiplier += ElfBootsItem.rangedBonus(shooter);
            }
        }
        if (event.getSource().getDirectEntity() instanceof AbstractArrow arrow) {
            CompoundTag data = arrow.getPersistentData();
            if (data.getBoolean(TAG_FULL_DRAW)
                    && data.hasUUID(TAG_SHOOTER)
                    && data.getUUID(TAG_SHOOTER).equals(shooter.getUUID())
                    && arrow.getOwner() == shooter) {
                String targetId = event.getEntity().getUUID().toString();
                CompoundTag hitTargets = data.getCompound(TAG_HIT_TARGETS);
                if (!hitTargets.getBoolean(targetId)) {
                    multiplier *= 1.5D;
                    hitTargets.putBoolean(targetId, true);
                    data.put(TAG_HIT_TARGETS, hitTargets);
                }
            }
        }
        if (multiplier != 1.0D) {
            event.setAmount((float) Math.min(Float.MAX_VALUE, event.getAmount() * multiplier));
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END
                || !(event.player instanceof ServerPlayer player)) {
            return;
        }
        PendingShot pending = PENDING.get(player.getUUID());
        if (pending != null && (player.level().getGameTime() - pending.tick() > 5L
                || !pending.dimension().equals(player.level().dimension()))) {
            PENDING.remove(player.getUUID());
        }
    }

    @SubscribeEvent
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        PENDING.remove(event.getEntity().getUUID());
    }
}
