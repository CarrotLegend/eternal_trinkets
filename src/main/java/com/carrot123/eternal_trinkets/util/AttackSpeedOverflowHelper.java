package com.carrot123.eternal_trinkets.util;

import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

public final class AttackSpeedOverflowHelper {
    private AttackSpeedOverflowHelper() {
    }

    public static double overflow(Player player) {
        AttributeInstance instance = player.getAttribute(Attributes.ATTACK_SPEED);
        if (instance == null) {
            return 0.0D;
        }
        double subtotal = instance.getBaseValue();
        for (AttributeModifier modifier : instance.getModifiers(AttributeModifier.Operation.ADDITION)) {
            subtotal += modifier.getAmount();
        }
        if (!Double.isFinite(subtotal)) {
            return 0.0D;
        }
        double value = subtotal;
        for (AttributeModifier modifier : instance.getModifiers(AttributeModifier.Operation.MULTIPLY_BASE)) {
            value += subtotal * modifier.getAmount();
        }
        for (AttributeModifier modifier : instance.getModifiers(AttributeModifier.Operation.MULTIPLY_TOTAL)) {
            value *= 1.0D + modifier.getAmount();
        }
        return Double.isFinite(value) ? Math.max(0.0D, value - 4.0D) : 0.0D;
    }
}
