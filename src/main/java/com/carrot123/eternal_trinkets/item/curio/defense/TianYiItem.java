package com.carrot123.eternal_trinkets.item.curio.defense;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import com.carrot123.eternal_trinkets.item.ModRarity;
import com.carrot123.eternal_trinkets.item.curio.BaseCurioItem;
import com.carrot123.eternal_trinkets.misc.ModEffects;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerLevel;
import top.theillusivec4.curios.api.SlotContext;

/** 天仪·混元 —— 强化佩戴者并使周围生物阴阳失谐。 */
public class TianYiItem extends BaseCurioItem {

    private static final double AURA_RADIUS = 5.0D;
    private static final double AURA_RADIUS_SQUARED = AURA_RADIUS * AURA_RADIUS;
    private static final int AURA_PERIOD_TICKS = 20;
    private static final int DISSONANCE_DURATION_TICKS = 30;

    private static final UUID MAX_HEALTH_UUID = stableUuid(
            "eternal_trinkets:hunyuan/max_health");
    private static final UUID MOVEMENT_SPEED_UUID = stableUuid(
            "eternal_trinkets:hunyuan/movement_speed");
    private static final UUID ATTACK_SPEED_UUID = stableUuid(
            "eternal_trinkets:hunyuan/attack_speed");

    public TianYiItem() {
        super(ModRarity.YIN_YANG);
    }

    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
        super.onUnequip(slotContext, newStack, stack);
        clampHealth(slotContext.entity());
    }

    @Override
    @SuppressWarnings("null")
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(
            SlotContext slotContext, UUID uuid, ItemStack stack) {
        return ImmutableMultimap.<Attribute, AttributeModifier>builder()
                .put(Attributes.MAX_HEALTH,
                        new AttributeModifier(MAX_HEALTH_UUID, "hunyuan_max_health",
                                0.25D, AttributeModifier.Operation.MULTIPLY_TOTAL))
                .put(Attributes.MOVEMENT_SPEED,
                        new AttributeModifier(MOVEMENT_SPEED_UUID, "hunyuan_movement_speed",
                                0.05D, AttributeModifier.Operation.MULTIPLY_TOTAL))
                .put(Attributes.ATTACK_SPEED,
                        new AttributeModifier(ATTACK_SPEED_UUID, "hunyuan_attack_speed",
                                0.05D, AttributeModifier.Operation.MULTIPLY_TOTAL))
                .build();
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        LivingEntity wearer = slotContext.entity();
        if (!(wearer.level() instanceof ServerLevel level)
                || wearer.tickCount % AURA_PERIOD_TICKS != 0) {
            return;
        }

        for (LivingEntity target : level.getEntitiesOfClass(
                LivingEntity.class,
                wearer.getBoundingBox().inflate(AURA_RADIUS),
                target -> target != wearer
                        && target.distanceToSqr(wearer) <= AURA_RADIUS_SQUARED)) {
            target.addEffect(new MobEffectInstance(
                    ModEffects.YIN_YANG_DISSONANCE.get(),
                    DISSONANCE_DURATION_TICKS,
                    0,
                    false,
                    true,
                    true));
        }
    }

    @Override
    public List<Component> getAttributesTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.clear();
        tooltips.add(attributeLine("tooltip.eternal_trinkets.tian_yi.value.25_percent",
                "tooltip.eternal_trinkets.tian_yi.all_damage"));
        tooltips.add(attributeLine("tooltip.eternal_trinkets.tian_yi.value.25_percent",
                "tooltip.eternal_trinkets.tian_yi.max_health"));
        tooltips.add(attributeLine("tooltip.eternal_trinkets.tian_yi.value.5_percent",
                "tooltip.eternal_trinkets.tian_yi.movement_speed"));
        tooltips.add(attributeLine("tooltip.eternal_trinkets.tian_yi.value.5_percent",
                "tooltip.eternal_trinkets.tian_yi.attack_speed"));
        tooltips.add(Component.empty());
        tooltips.add(Component.translatable("tooltip.eternal_trinkets.tian_yi.aura.prefix")
                .withStyle(ChatFormatting.BLACK)
                .append(Component.translatable("tooltip.eternal_trinkets.tian_yi.aura.effect")
                        .withStyle(ChatFormatting.WHITE))
                .append(Component.translatable("tooltip.eternal_trinkets.tian_yi.aura.suffix")
                        .withStyle(ChatFormatting.BLACK)));
        return tooltips;
    }

    private static Component attributeLine(String valueKey, String labelKey) {
        return Component.translatable(valueKey)
                .withStyle(ChatFormatting.BLACK)
                .append(Component.literal(" ").withStyle(ChatFormatting.WHITE))
                .append(Component.translatable(labelKey).withStyle(ChatFormatting.WHITE));
    }

    private static UUID stableUuid(String salt) {
        return UUID.nameUUIDFromBytes(salt.getBytes(StandardCharsets.UTF_8));
    }
}
