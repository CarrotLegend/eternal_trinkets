package com.carrot123.eternal_trinkets.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nullable;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.projectile.Projectile;

/** Resolves the server player that owns direct, projectile and modded damage. */
public final class PlayerDamageAttribution {

    private static final int MAX_OWNER_DEPTH = 8;
    private static final ClassValue<Optional<Method>> SOURCE_OWNER_METHODS =
            new ClassValue<>() {
                @Override
                protected Optional<Method> computeValue(Class<?> type) {
                    try {
                        Method method = type.getMethod("getOwner");
                        return Entity.class.isAssignableFrom(method.getReturnType())
                                ? Optional.of(method)
                                : Optional.empty();
                    } catch (NoSuchMethodException ignored) {
                        return Optional.empty();
                    }
                }
            };

    private PlayerDamageAttribution() {
        throw new UnsupportedOperationException("utility class");
    }

    @Nullable
    public static ServerPlayer resolve(DamageSource source) {
        Set<Entity> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        ServerPlayer player = resolveOwner(source.getEntity(), visited, 0);
        if (player != null) {
            return player;
        }
        player = resolveOwner(reflectiveSourceOwner(source), visited, 0);
        return player != null
                ? player
                : resolveOwner(source.getDirectEntity(), visited, 0);
    }

    @Nullable
    private static ServerPlayer resolveOwner(
            @Nullable Entity entity, Set<Entity> visited, int depth) {
        if (entity == null || depth > MAX_OWNER_DEPTH || !visited.add(entity)) {
            return null;
        }
        if (entity instanceof ServerPlayer player) {
            return player;
        }
        if (entity instanceof Projectile projectile) {
            ServerPlayer player = resolveOwner(projectile.getOwner(), visited, depth + 1);
            if (player != null) {
                return player;
            }
        }
        if (entity instanceof OwnableEntity ownable) {
            return resolveOwner(ownable.getOwner(), visited, depth + 1);
        }
        return null;
    }

    @Nullable
    private static Entity reflectiveSourceOwner(DamageSource source) {
        Optional<Method> ownerMethod = SOURCE_OWNER_METHODS.get(source.getClass());
        if (ownerMethod.isEmpty()) {
            return null;
        }
        try {
            Object owner = ownerMethod.get().invoke(source);
            return owner instanceof Entity entity ? entity : null;
        } catch (IllegalAccessException | InvocationTargetException | RuntimeException ignored) {
            return null;
        }
    }
}
