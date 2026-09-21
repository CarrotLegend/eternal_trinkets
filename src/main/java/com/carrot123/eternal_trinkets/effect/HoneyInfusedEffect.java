package com.carrot123.eternal_trinkets.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/**
 * 蜜浸 — 提供等同于效果等级的生命恢复效果的正面效果（不与生命恢复效果冲突，可同时存在）。
 * <p>
 * 治愈频率等同于原版生命恢复：
 * 等级 I 每 50 tick 恢复 1 HP，等级 II 每 25 tick，等级 III 每 12 tick。
 */
public class HoneyInfusedEffect extends MobEffect {

    private static final int REGENERATION_BASE_INTERVAL_TICKS = 50;
    private static final float HEAL_PER_APPLICATION = 1.0F;

    public HoneyInfusedEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xFFC800);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        // Matches MobEffect's 1.20.1 REGENERATION branch exactly.
        int interval = REGENERATION_BASE_INTERVAL_TICKS >> amplifier;
        if (interval > 0) {
            return duration % interval == 0;
        }
        return true;
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.getHealth() < entity.getMaxHealth()) {
            entity.heal(HEAL_PER_APPLICATION);
        }
    }
}
