package com.carrot123.eternal_trinkets.item.curio;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import top.theillusivec4.curios.api.SlotContext;

public final class ForestCrownItem extends BaseCurioItem {
    public static final UUID RANGED_DAMAGE_UUID =
            UUID.fromString("e943247a-4676-4f7e-a227-0d8268595702");

    public ForestCrownItem() {
        super(Rarity.RARE);
    }

    @Override
    public boolean canEquip(SlotContext context, ItemStack stack) {
        return "head".equals(context.identifier());
    }

    @Override
    public boolean canEquipFromUse(SlotContext context, ItemStack stack) {
        return canEquip(context, stack);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(
            SlotContext context, UUID slotUuid, ItemStack stack) {
        Attribute rangedDamage = CurioRangedDamage.get();
        if (!"head".equals(context.identifier()) || rangedDamage == null) {
            return ImmutableMultimap.of();
        }
        return ImmutableMultimap.of(rangedDamage,
                new AttributeModifier(RANGED_DAMAGE_UUID, "forest_crown_ranged_damage",
                        0.20D, AttributeModifier.Operation.MULTIPLY_BASE));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.translatable("tooltip.eternal_trinkets.forest_crown.ranged")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.eternal_trinkets.forest_crown.full_draw")
                .withStyle(ChatFormatting.GOLD));
    }
}
