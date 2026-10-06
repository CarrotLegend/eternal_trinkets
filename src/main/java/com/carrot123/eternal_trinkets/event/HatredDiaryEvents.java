package com.carrot123.eternal_trinkets.event;

import com.carrot123.eternal_trinkets.item.curio.HatredDiaryItem;
import com.carrot123.eternal_trinkets.item.curio.ModCurioItems;
import com.carrot123.eternal_trinkets.misc.ModEffects;
import com.carrot123.eternal_trinkets.util.CuriosUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class HatredDiaryEvents {
    private static final int EFFECT_DURATION = Integer.MAX_VALUE;

    @SubscribeEvent
    public void onPlayerDamaged(LivingDamageEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || event.getAmount() <= 0 || !Float.isFinite(event.getAmount())
                || !CuriosUtils.hasFunctionalCurio(player, ModCurioItems.HATRED_DIARY.get(),
                        HatredDiaryItem.SLOT)) {
            return;
        }
        LivingEntity attacker = attacker(event.getSource());
        if (attacker != null && attacker != player) {
            attacker.addEffect(new MobEffectInstance(ModEffects.REVENGE_TARGET.get(),
                    EFFECT_DURATION, 0, false, false, true));
        }
    }

    @SubscribeEvent
    public void onTargetHurt(LivingDamageEvent event) {
        if (event.getEntity().level().isClientSide
                || !event.getEntity().hasEffect(ModEffects.REVENGE_TARGET.get())
                || event.getAmount() <= 0 || !Float.isFinite(event.getAmount())) {
            return;
        }
        float amount = event.getAmount() * 1.20F;
        if (Float.isFinite(amount)) {
            event.setAmount(amount);
        }
    }

    private static LivingEntity attacker(DamageSource source) {
        Entity entity = source.getEntity();
        if (entity instanceof LivingEntity living) {
            return living;
        }
        Entity direct = source.getDirectEntity();
        if (direct instanceof Projectile projectile && projectile.getOwner() instanceof LivingEntity living) {
            return living;
        }
        if (direct instanceof OwnableEntity ownable) {
            return ownable.getOwner();
        }
        return direct instanceof LivingEntity living ? living : null;
    }
}
