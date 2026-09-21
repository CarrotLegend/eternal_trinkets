package com.carrot123.eternal_trinkets.effect;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/** A single harmful effect equivalent to Weakness I, Slowness I and Sapped I. */
public final class YinYangDissonanceEffect extends MobEffect {

    private static final String ATTACK_DAMAGE_UUID = stableUuid(
            "eternal_trinkets:effect/yin_yang_dissonance/attack_damage");
    private static final String MOVEMENT_SPEED_UUID = stableUuid(
            "eternal_trinkets:effect/yin_yang_dissonance/movement_speed");

    public YinYangDissonanceEffect() {
        super(MobEffectCategory.HARMFUL, 0x808080);
        addAttributeModifier(Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE_UUID,
                -4.0D, AttributeModifier.Operation.ADDITION);
        addAttributeModifier(Attributes.MOVEMENT_SPEED, MOVEMENT_SPEED_UUID,
                -0.15D, AttributeModifier.Operation.MULTIPLY_TOTAL);
    }

    private static String stableUuid(String salt) {
        return UUID.nameUUIDFromBytes(salt.getBytes(StandardCharsets.UTF_8)).toString();
    }
}
