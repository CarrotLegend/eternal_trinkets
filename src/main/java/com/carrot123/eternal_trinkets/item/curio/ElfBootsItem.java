package com.carrot123.eternal_trinkets.item.curio;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import top.theillusivec4.curios.api.SlotContext;

public final class ElfBootsItem extends BaseCurioItem {
    public static final UUID MOVEMENT_SPEED_UUID =
            UUID.fromString("ed19fe67-aa02-4dc8-9e7e-f8713c5e5d36");
    public static final UUID RANGED_DAMAGE_UUID =
            UUID.fromString("35315172-2a6d-4bf9-94e1-184581e11c7d");
    private static final double SPEED_PER_STEP = 0.02D;
    private static final double RANGED_BONUS_PER_STEP = 0.05D;
    private static final double STEP_EPSILON = 1.0E-8D;

    public static double rangedBonus(LivingEntity entity) {
        double speed = entity.getAttributeValue(Attributes.MOVEMENT_SPEED);
        int steps = Double.isFinite(speed) && speed > 0.0D
                ? (int) Math.floor(speed / SPEED_PER_STEP + STEP_EPSILON) : 0;
        return steps * RANGED_BONUS_PER_STEP;
    }

    public ElfBootsItem() {
        super(Rarity.RARE);
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
            SlotContext context, UUID slotUuid, ItemStack stack) {
        if (!"feet".equals(context.identifier())) {
            return ImmutableMultimap.of();
        }
        return ImmutableMultimap.of(Attributes.MOVEMENT_SPEED,
                new AttributeModifier(MOVEMENT_SPEED_UUID, "elf_boots_movement_speed",
                        0.10D, AttributeModifier.Operation.MULTIPLY_TOTAL));
    }

    @Override
    public void curioTick(SlotContext context, ItemStack stack) {
        if (context.entity().level().isClientSide || !"feet".equals(context.identifier())) {
            return;
        }
        Attribute rangedDamage = CurioRangedDamage.get();
        AttributeInstance instance = rangedDamage == null
                ? null : context.entity().getAttribute(rangedDamage);
        if (instance == null) {
            return;
        }
        double target = rangedBonus(context.entity());
        AttributeModifier current = instance.getModifier(RANGED_DAMAGE_UUID);
        if (current != null && current.getOperation() == AttributeModifier.Operation.MULTIPLY_BASE
                && Double.compare(current.getAmount(), target) == 0) {
            return;
        }
        if (current != null) {
            instance.removeModifier(RANGED_DAMAGE_UUID);
        }
        if (target > 0.0D) {
            instance.addTransientModifier(new AttributeModifier(RANGED_DAMAGE_UUID,
                    "elf_boots_speed_ranged_damage", target,
                    AttributeModifier.Operation.MULTIPLY_BASE));
        }
    }

    @Override
    public void onUnequip(SlotContext context, ItemStack newStack, ItemStack stack) {
        if (newStack.is(this)) {
            return;
        }
        Attribute rangedDamage = CurioRangedDamage.get();
        AttributeInstance instance = rangedDamage == null
                ? null : context.entity().getAttribute(rangedDamage);
        if (instance != null) {
            instance.removeModifier(RANGED_DAMAGE_UUID);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.translatable("tooltip.eternal_trinkets.elf_boots.speed")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.eternal_trinkets.elf_boots.ranged")
                .withStyle(ChatFormatting.GRAY));
    }
}
