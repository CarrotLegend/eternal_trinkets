package com.carrot123.eternal_trinkets.item.curio.defense;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import com.carrot123.eternal_trinkets.item.ModRarity;
import com.carrot123.eternal_trinkets.item.curio.BaseCurioItem;
import com.carrot123.eternal_trinkets.item.curio.ModCurioItems;
import com.carrot123.eternal_trinkets.util.CuriosUtils;
import com.carrot123.eternal_trinkets.util.YinYangAttributeRefresh;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;

/** 翳玄·其阴 —— 攻击伤害提高，攻速随阴阳组合反转。 */
public class YinXuanItem extends BaseCurioItem {

    private static final UUID ATTACK_DAMAGE_UUID = stableUuid(
            "eternal_trinkets:yin/attack_damage");
    private static final UUID ATTACK_SPEED_UUID = stableUuid(
            "eternal_trinkets:yin/attack_speed");

    public YinXuanItem() {
        super(ModRarity.YIN_YANG);
    }

    @Override
    @SuppressWarnings("null")
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(
            SlotContext slotContext, UUID uuid, ItemStack stack) {
        boolean paired = CuriosUtils.hasCurioEquipped(
                slotContext.entity(), ModCurioItems.JIAO_BAI.get());
        return ImmutableMultimap.<Attribute, AttributeModifier>builder()
                .put(Attributes.ATTACK_DAMAGE,
                        new AttributeModifier(ATTACK_DAMAGE_UUID, "yin_attack_damage",
                                0.20D, AttributeModifier.Operation.MULTIPLY_TOTAL))
                .put(Attributes.ATTACK_SPEED,
                        new AttributeModifier(ATTACK_SPEED_UUID, "yin_attack_speed",
                                paired ? 0.05D : -0.05D,
                                AttributeModifier.Operation.MULTIPLY_TOTAL))
                .build();
    }

    @Override
    public void onEquip(SlotContext slotContext, ItemStack prevStack, ItemStack stack) {
        super.onEquip(slotContext, prevStack, stack);
        YinYangAttributeRefresh.refreshEquipped(
                slotContext.entity(), ModCurioItems.JIAO_BAI.get());
    }

    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
        super.onUnequip(slotContext, newStack, stack);
        YinYangAttributeRefresh.refreshEquipped(
                slotContext.entity(), ModCurioItems.JIAO_BAI.get());
    }

    @Override
    public List<Component> getAttributesTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(Component.empty());
        tooltips.add(Component.translatable("tooltip.eternal_trinkets.yin_xuan.lore")
                .withStyle(ChatFormatting.BOLD, ChatFormatting.ITALIC));
        return tooltips;
    }

    private static UUID stableUuid(String salt) {
        return UUID.nameUUIDFromBytes(salt.getBytes(StandardCharsets.UTF_8));
    }
}
