package com.carrot123.eternal_trinkets.event;

import com.carrot123.eternal_trinkets.item.curio.ModCurioItems;
import com.carrot123.eternal_trinkets.util.CuriosUtils;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * 暴躁河豚的功能事件：
 * 免疫中毒
 * 反弹：受到近战伤害时，对攻击者造成 1~4 点荆棘类型伤害。
 */
public class GrumpyPufferfishEvents {

    // 免疫中毒 —— 阻止中毒效果被施加
    @SubscribeEvent
    public void onPotionApplicable(MobEffectEvent.Applicable event) {
        if (event.getEffectInstance().getEffect() == MobEffects.POISON
                && CuriosUtils.hasCurioEquipped(event.getEntity(), ModCurioItems.GRUMPY_PUFFERFISH.get())) {
            event.setResult(Event.Result.DENY);
        }
    }

    // 近战反弹荆棘伤害
    @SubscribeEvent
    public void onLivingHurt(LivingHurtEvent event) {
        LivingEntity victim = event.getEntity();
        if (victim.level().isClientSide) {
            return;
        }
        DamageSource source = event.getSource();
        // 避免荆棘伤害再次触发荆棘导致的循环
        if (source.is(DamageTypes.THORNS)) {
            return;
        }
        // 近战：直接伤害来源必须是一个生物
        if (!(source.getDirectEntity() instanceof LivingEntity attacker) || attacker == victim) {
            return;
        }
        if (!CuriosUtils.hasCurioEquipped(victim, ModCurioItems.GRUMPY_PUFFERFISH.get())) {
            return;
        }
        int thorns = 1 + victim.getRandom().nextInt(4); // 1 ~ 4
        attacker.hurt(victim.damageSources().thorns(victim), thorns);
    }
}
