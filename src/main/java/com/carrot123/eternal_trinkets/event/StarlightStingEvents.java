package com.carrot123.eternal_trinkets.event;

import com.carrot123.eternal_trinkets.item.curio.ModCurioItems;
import com.carrot123.eternal_trinkets.util.CuriosUtils;
import com.carrot123.eternal_trinkets.util.PlayerDamageAttribution;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class StarlightStingEvents {

    private static final float EXTRA_VOID_DAMAGE = 5.0F;

    private static final ThreadLocal<Boolean> APPLYING_EXTRA_DAMAGE =
            ThreadLocal.withInitial(() -> false);

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onLivingDamage(LivingDamageEvent event) {
        if (Boolean.TRUE.equals(APPLYING_EXTRA_DAMAGE.get())) {
            return;
        }

        LivingEntity target = event.getEntity();

        if (target.level().isClientSide) {
            return;
        }

        float originalDamage = event.getAmount();

        if (originalDamage <= 0.0F || !Float.isFinite(originalDamage)) {
            return;
        }

        ServerPlayer attacker =
                PlayerDamageAttribution.resolve(event.getSource());

        if (attacker == null || attacker == target) {
            return;
        }

        if (!CuriosUtils.hasCurioEquipped(
                attacker,
                ModCurioItems.STARLIGHT_STING.get())) {
            return;
        }

        if (!target.isAlive()) {
            return;
        }

        Holder<DamageType> damageType = target.level()
                .registryAccess()
                .registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(DamageTypes.FELL_OUT_OF_WORLD);

        DamageSource voidDamageSource =
                new DamageSource(
                        damageType,
                        attacker,
                        attacker);

        int previousInvulnerableTime = target.invulnerableTime;

        APPLYING_EXTRA_DAMAGE.set(true);

        try {
            target.invulnerableTime = 0;
            target.hurt(voidDamageSource, EXTRA_VOID_DAMAGE);
        } finally {
            target.invulnerableTime = previousInvulnerableTime;
            APPLYING_EXTRA_DAMAGE.remove();
        }
    }
}