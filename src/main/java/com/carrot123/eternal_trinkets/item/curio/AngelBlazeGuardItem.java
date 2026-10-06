package com.carrot123.eternal_trinkets.item.curio;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import top.theillusivec4.curios.api.SlotContext;

public final class AngelBlazeGuardItem extends BaseCurioItem {
    public static final String SLOT = "flight";
    public static final UUID SPEED_UUID = UUID.nameUUIDFromBytes(
            "eternal_trinkets:angel_blaze_guard/movement_speed".getBytes(StandardCharsets.UTF_8));
    private static final Multimap<Attribute, AttributeModifier> MODIFIERS = ImmutableMultimap.of(
            Attributes.MOVEMENT_SPEED,
            new AttributeModifier(SPEED_UUID, "angel_blaze_guard_movement_speed", 0.4D,
                    AttributeModifier.Operation.MULTIPLY_TOTAL));

    public AngelBlazeGuardItem() {
        super(Rarity.EPIC);
    }

    private static boolean valid(SlotContext context) {
        return context != null && SLOT.equals(context.identifier()) && !context.cosmetic();
    }

    @Override
    public boolean canEquip(SlotContext context, ItemStack stack) {
        return stack.is(this) && valid(context)
                && com.carrot123.eternal_trinkets.util.CuriosUtils.noSameCurio(context.entity(), this);
    }

    @Override
    public boolean canEquipFromUse(SlotContext context, ItemStack stack) {
        return stack.is(this) && valid(context);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(
            SlotContext context, UUID slotUuid, ItemStack stack) {
        return stack.is(this) && valid(context) ? MODIFIERS : ImmutableMultimap.of();
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.translatable("tooltip.eternal_trinkets.angel_blaze_guard.immunity")
                .withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("tooltip.eternal_trinkets.angel_blaze_guard.undead")
                .withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("tooltip.eternal_trinkets.angel_blaze_guard.flight")
                .withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("tooltip.eternal_trinkets.angel_blaze_guard.flight_speed")
                .withStyle(ChatFormatting.BLUE));
    }
}
