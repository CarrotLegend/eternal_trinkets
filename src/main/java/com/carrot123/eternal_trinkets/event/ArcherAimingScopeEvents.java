package com.carrot123.eternal_trinkets.event;

import com.carrot123.eternal_trinkets.item.curio.ArcherAimingScopeItem;
import com.carrot123.eternal_trinkets.item.curio.ModCurioItems;
import com.carrot123.eternal_trinkets.util.CuriosUtils;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class ArcherAimingScopeEvents {
    private static final String TRACKING_TAG = "EternalTrinketsArcherAimingScope";
    private static final double SEARCH_RADIUS = 20.0D;
    private static final double SEARCH_RADIUS_SQUARED = SEARCH_RADIUS * SEARCH_RADIUS;
    private static final double TURN_STRENGTH = 0.20D;
    private static final double MIN_SPEED_SQUARED = 1.0E-6D;
    private static final Map<ServerLevel, Map<UUID, UUID>> TRACKED = new HashMap<>();

    @SubscribeEvent
    public void onArrowJoin(EntityJoinLevelEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)
                || !(event.getEntity() instanceof AbstractArrow arrow)
                || arrow instanceof ThrownTrident || arrow.inGround) {
            return;
        }
        if (arrow.getPersistentData().getBoolean(TRACKING_TAG)) {
            TRACKED.computeIfAbsent(level, ignored -> new HashMap<>()).put(arrow.getUUID(), null);
            return;
        }
        if (event.loadedFromDisk() || !(arrow.getOwner() instanceof ServerPlayer shooter)
                || !CuriosUtils.hasFunctionalCurio(shooter, ModCurioItems.ARCHER_AIMING_SCOPE.get(),
                        ArcherAimingScopeItem.SLOT)
                || !holdingBow(shooter)) {
            return;
        }
        arrow.getPersistentData().putBoolean(TRACKING_TAG, true);
        TRACKED.computeIfAbsent(level, ignored -> new HashMap<>()).put(arrow.getUUID(), null);
    }

    @SubscribeEvent
    public void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.level instanceof ServerLevel level)) {
            return;
        }
        Map<UUID, UUID> tracked = TRACKED.get(level);
        if (tracked == null) {
            return;
        }
        Iterator<Map.Entry<UUID, UUID>> iterator = tracked.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, UUID> entry = iterator.next();
            Entity entity = level.getEntity(entry.getKey());
            if (!(entity instanceof AbstractArrow arrow) || arrow.isRemoved()
                    || arrow.inGround || arrow.getDeltaMovement().lengthSqr() <= MIN_SPEED_SQUARED) {
                iterator.remove();
                continue;
            }
            if (!(arrow.getOwner() instanceof Player owner)) {
                continue;
            }
            LivingEntity target = entry.getValue() != null
                    && level.getEntity(entry.getValue()) instanceof LivingEntity living
                    ? living : null;
            if (!validTarget(arrow, owner, target)) {
                target = findTarget(level, arrow, owner);
                entry.setValue(target == null ? null : target.getUUID());
            }
            if (target != null) {
                steer(arrow, target);
            }
        }
        if (tracked.isEmpty()) {
            TRACKED.remove(level);
        }
    }

    @SubscribeEvent
    public void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel level) {
            TRACKED.remove(level);
        }
    }

    private static boolean holdingBow(Player player) {
        return bow(player.getMainHandItem()) || bow(player.getOffhandItem());
    }

    private static boolean bow(ItemStack stack) {
        return stack.getItem() instanceof BowItem || stack.getItem() instanceof CrossbowItem;
    }

    private static boolean validTarget(AbstractArrow arrow, Player owner, LivingEntity target) {
        return target != null && target instanceof Enemy && target.isAlive()
                && !target.isRemoved() && target.isAttackable()
                && target != owner && !owner.isAlliedTo(target) && !target.isAlliedTo(owner)
                && (!(target instanceof Player player) || !player.isCreative() && !player.isSpectator())
                && target.distanceToSqr(arrow) <= SEARCH_RADIUS_SQUARED;
    }

    private static LivingEntity findTarget(ServerLevel level, AbstractArrow arrow, Player owner) {
        LivingEntity nearest = null;
        double nearestDistance = SEARCH_RADIUS_SQUARED;
        for (LivingEntity candidate : level.getEntitiesOfClass(LivingEntity.class,
                arrow.getBoundingBox().inflate(SEARCH_RADIUS),
                target -> validTarget(arrow, owner, target))) {
            double distance = candidate.distanceToSqr(arrow);
            if (distance < nearestDistance) {
                nearest = candidate;
                nearestDistance = distance;
            }
        }
        return nearest;
    }

    private static void steer(AbstractArrow arrow, LivingEntity target) {
        Vec3 velocity = arrow.getDeltaMovement();
        double speed = velocity.length();
        Vec3 desired = target.getEyePosition().subtract(arrow.position());
        if (!Double.isFinite(speed) || speed * speed <= MIN_SPEED_SQUARED
                || desired.lengthSqr() <= MIN_SPEED_SQUARED) {
            return;
        }
        Vec3 direction = velocity.normalize().lerp(desired.normalize(), TURN_STRENGTH).normalize();
        Vec3 adjusted = direction.scale(speed);
        arrow.setDeltaMovement(adjusted);
        arrow.hasImpulse = true;
        arrow.setYRot((float) (Mth.atan2(adjusted.x, adjusted.z) * 180.0D / Math.PI));
        arrow.setXRot((float) (Mth.atan2(adjusted.y, adjusted.horizontalDistance()) * 180.0D / Math.PI));
    }
}
