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

public final class HatredDiaryItem extends BaseCurioItem {
    public static final String SLOT = "belt";
    public static final UUID DAMAGE_UUID = UUID.nameUUIDFromBytes(
            "eternal_trinkets:hatred_diary/attack_damage".getBytes(StandardCharsets.UTF_8));
    private static final Multimap<Attribute, AttributeModifier> MODIFIERS = ImmutableMultimap.of(
            Attributes.ATTACK_DAMAGE,
            new AttributeModifier(DAMAGE_UUID, "hatred_diary_attack_damage", 4.0D,
                    AttributeModifier.Operation.ADDITION));

    public HatredDiaryItem() {
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
        tooltip.add(Component.translatable("tooltip.eternal_trinkets.hatred_diary.revenge")
                .withStyle(ChatFormatting.LIGHT_PURPLE));
    }
}
