package com.carrot123.eternal_trinkets.event;

import com.carrot123.eternal_trinkets.item.curio.ArcherAimingScopeItem;
import com.carrot123.eternal_trinkets.item.curio.ModCurioItems;
import com.carrot123.eternal_trinkets.util.CuriosUtils;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.phys.Vec3;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class ArcherAimingScopeEvents {

    private static final String TRACKING_TAG =
            "EternalTrinketsArcherAimingScope";

    private static final double SEARCH_RADIUS =
            12.0D;

    private static final double MIN_DIRECTION_LENGTH_SQUARED =
            1.0E-8D;

    private static final Map<ServerLevel, Set<UUID>> TRACKED =
            new HashMap<>();

    @SubscribeEvent
    public void onArrowJoin(
            EntityJoinLevelEvent event
    ) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }

        if (!(event.getEntity() instanceof AbstractArrow arrow)) {
            return;
        }

        if (arrow instanceof ThrownTrident || arrow.inGround) {
            return;
        }

        if (arrow.getPersistentData().getBoolean(TRACKING_TAG)) {
            addTrackedArrow(level, arrow);
            return;
        }

        if (event.loadedFromDisk()) {
            return;
        }

        if (!(arrow.getOwner() instanceof ServerPlayer shooter)) {
            return;
        }

        if (!CuriosUtils.hasFunctionalCurio(
                shooter,
                ModCurioItems.ARCHER_AIMING_SCOPE.get(),
                ArcherAimingScopeItem.SLOT
        )) {
            return;
        }

        arrow.getPersistentData().putBoolean(
                TRACKING_TAG,
                true
        );

        addTrackedArrow(level, arrow);
    }

    private static void addTrackedArrow(
            ServerLevel level,
            AbstractArrow arrow
    ) {
        TRACKED.computeIfAbsent(
                level,
                ignored -> new HashSet<>()
        ).add(arrow.getUUID());
    }

    @SubscribeEvent
    public void onLevelTick(
            TickEvent.LevelTickEvent event
    ) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        if (!(event.level instanceof ServerLevel level)) {
            return;
        }

        Set<UUID> tracked =
                TRACKED.get(level);

        if (tracked == null || tracked.isEmpty()) {
            return;
        }

        Iterator<UUID> iterator =
                tracked.iterator();

        while (iterator.hasNext()) {
            UUID arrowUUID =
                    iterator.next();

            Entity entity =
                    level.getEntity(arrowUUID);

            if (!(entity instanceof AbstractArrow arrow)) {
                iterator.remove();
                continue;
            }

            if (!arrow.isAlive()
                    || arrow.isRemoved()
                    || arrow.inGround) {
                iterator.remove();
                continue;
            }

            if (!(arrow.getOwner() instanceof Player owner)) {
                iterator.remove();
                continue;
            }

            LivingEntity target =
                    findNearestTarget(
                            level,
                            arrow,
                            owner
                    );

            if (target == null) {
                continue;
            }

            moveTowardsTarget(
                    arrow,
                    target
            );
        }

        if (tracked.isEmpty()) {
            TRACKED.remove(level);
        }
    }

    private static LivingEntity findNearestTarget(
            ServerLevel level,
            AbstractArrow arrow,
            Player owner
    ) {
        List<LivingEntity> entities =
                level.getEntitiesOfClass(
                        LivingEntity.class,
                        arrow.getBoundingBox().inflate(
                                SEARCH_RADIUS
                        ),
                        target -> validTarget(
                                owner,
                                target
                        )
                );

        if (entities.isEmpty()) {
            return null;
        }

        return entities.stream()
                .min(
                        Comparator.comparingDouble(
                                entity -> entity.distanceToSqr(arrow)
                        )
                )
                .orElse(null);
    }

    private static boolean validTarget(
            Player owner,
            LivingEntity target
    ) {
        if (target == owner) {
            return false;
        }

        if (!target.isAlive()
                || target.isRemoved()
                || !target.isAttackable()) {
            return false;
        }

        if (owner.isAlliedTo(target)
                || target.isAlliedTo(owner)) {
            return false;
        }

        if (target instanceof Player) {
            return false;
        }

        if (target instanceof Enemy) {
            return true;
        }

        if (target instanceof Mob mob) {
            return mob.getTarget() == owner
                    || mob.getLastHurtByMob() == owner;
        }

        return false;
    }

    private static void moveTowardsTarget(
            AbstractArrow arrow,
            LivingEntity target
    ) {
        Vec3 difference =
                target.position()
                        .add(
                                0.0D,
                                target.getBbHeight() / 2.0D,
                                0.0D
                        )
                        .subtract(
                                arrow.position()
                        );

        if (difference.lengthSqr()
                <= MIN_DIRECTION_LENGTH_SQUARED) {
            return;
        }

        Vec3 currentMotion =
                arrow.getDeltaMovement();

        Vec3 newMotion =
                currentMotion
                        .add(
                                difference.normalize()
                        )
                        .scale(
                                0.75D
                        );

        if (!Double.isFinite(newMotion.x)
                || !Double.isFinite(newMotion.y)
                || !Double.isFinite(newMotion.z)) {
            return;
        }

        arrow.setDeltaMovement(
                newMotion
        );

        arrow.hasImpulse = true;
    }

    @SubscribeEvent
    public void onLevelUnload(
            LevelEvent.Unload event
    ) {
        if (event.getLevel() instanceof ServerLevel level) {
            TRACKED.remove(level);
        }
    }
}