package com.carrot123.eternal_trinkets.event;

import com.carrot123.eternal_trinkets.item.curio.ModCurioItems;
import com.carrot123.eternal_trinkets.misc.ModEffects;
import com.carrot123.eternal_trinkets.util.CuriosUtils;
import com.carrot123.eternal_trinkets.util.PlayerDamageAttribution;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/** Damage multipliers belonging to the reset Yin-Yang item set. */
public final class YinYangCombatEvents {

    private static final float HUNYUAN_DAMAGE_MULTIPLIER = 1.25F;
    private static final float DISSONANCE_DAMAGE_PER_LEVEL = 0.20F;

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onLivingDamage(LivingDamageEvent event) {
        if (event.getEntity().level().isClientSide) {
            return;
        }

        float amount = event.getAmount();
        if (amount <= 0.0F || !Float.isFinite(amount)) {
            return;
        }

        MobEffectInstance dissonance = event.getEntity().getEffect(
                ModEffects.YIN_YANG_DISSONANCE.get());
        if (dissonance != null) {
            amount *= 1.0F + DISSONANCE_DAMAGE_PER_LEVEL
                    * (dissonance.getAmplifier() + 1);
        }

        ServerPlayer attacker = PlayerDamageAttribution.resolve(event.getSource());
        if (attacker != null
                && attacker != event.getEntity()
                && CuriosUtils.hasCurioEquipped(attacker, ModCurioItems.TIAN_YI.get())) {
            amount *= HUNYUAN_DAMAGE_MULTIPLIER;
        }

        if (Float.isFinite(amount)) {
            event.setAmount(amount);
        }
    }
}
