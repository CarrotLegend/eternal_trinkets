package com.carrot123.eternal_trinkets.item.curio;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import top.theillusivec4.curios.api.SlotContext;

public final class ElfBootsItem extends BaseCurioItem {

    public static final UUID MOVEMENT_SPEED_UUID =
            UUID.fromString("ed19fe67-aa02-4dc8-9e7e-f8713c5e5d36");

    private static final double SPEED_PER_STEP = 0.02D;
    private static final double RANGED_BONUS_PER_STEP = 0.05D;
    private static final double STEP_EPSILON = 1.0E-8D;

    public ElfBootsItem() {
        super(Rarity.RARE);
    }

    public static double rangedBonus(LivingEntity entity) {
        double speed = entity.getAttributeValue(Attributes.MOVEMENT_SPEED);

        if (!Double.isFinite(speed) || speed <= 0.0D) {
            return 0.0D;
        }

        int steps = (int) Math.floor(
                speed / SPEED_PER_STEP + STEP_EPSILON
        );

        return steps * RANGED_BONUS_PER_STEP;
    }

    @Override
    public boolean canEquip(SlotContext context, ItemStack stack) {
        return "feet".equals(context.identifier());
    }

    @Override
    public boolean canEquipFromUse(SlotContext context, ItemStack stack) {
        return canEquip(context, stack);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(
            SlotContext context,
            UUID slotUuid,
            ItemStack stack) {

        if (!"feet".equals(context.identifier())) {
            return ImmutableMultimap.of();
        }

        return ImmutableMultimap.of(
                Attributes.MOVEMENT_SPEED,
                new AttributeModifier(
                        MOVEMENT_SPEED_UUID,
                        "elf_boots_movement_speed",
                        0.10D,
                        AttributeModifier.Operation.MULTIPLY_TOTAL
                )
        );
    }

    @Override
    public void appendHoverText(ItemStack stack,
                                @Nullable Level level,
                                List<Component> tooltip,
                                TooltipFlag flag) {

        super.appendHoverText(stack, level, tooltip, flag);

        tooltip.add(Component.translatable(
                "tooltip.eternal_trinkets.elf_boots.ranged")
                .withStyle(ChatFormatting.GRAY));
    }
}