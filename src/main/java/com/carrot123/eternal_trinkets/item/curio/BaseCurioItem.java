package com.carrot123.eternal_trinkets.item.curio;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import com.carrot123.eternal_trinkets.util.CuriosUtils;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

/**
 * 基础饰品物品 —— 不可堆叠，佩戴提供属性修饰符，右键装备，防重复佩戴。
 */
@SuppressWarnings("null")
public class BaseCurioItem extends Item implements ICurioItem {

    private final Multimap<Attribute, AttributeModifier> defaultModifiers;

    public BaseCurioItem(Rarity rarity, Multimap<Attribute, AttributeModifier> modifiers) {
        super(new Item.Properties().stacksTo(1).rarity(rarity));
        this.defaultModifiers = modifiers != null
                ? ImmutableMultimap.copyOf(modifiers)
                : ImmutableMultimap.of();
    }

    public BaseCurioItem(Rarity rarity) {
        this(rarity, ImmutableMultimap.of());
    }

    // ── ICurioItem ──────────────────────────────────────────────

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(
            SlotContext slotContext, UUID uuid, ItemStack stack) {
        return this.defaultModifiers;
    }

    @Override
    public void onEquip(SlotContext slotContext, ItemStack prevStack, ItemStack stack) {
        LivingEntity wearer = slotContext.entity();
        if (wearer != null) {
            wearer.getAttributes().addTransientAttributeModifiers(this.defaultModifiers);
        }
    }

    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
        LivingEntity wearer = slotContext.entity();
        if (wearer != null) {
            wearer.getAttributes().removeAttributeModifiers(this.defaultModifiers);
        }
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        return true;
    }

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        return CuriosUtils.noSameCurio(slotContext.entity(), this);
    }

    // ── Item ────────────────────────────────────────────────────

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                 List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
    }

    @Override
    public Component getName(ItemStack stack) {
        MutableComponent name = super.getName(stack).copy();
        Rarity rarity = stack.getRarity();
        if (rarity != Rarity.COMMON) {
            name.withStyle(rarity.getStyleModifier());
        }
        return name;
    }

    /** 如果当前生命值超过最大生命值，将其裁剪到最大值。用于取下加血饰品时调用。 */
    protected static void clampHealth(LivingEntity entity) {
        if (entity != null && entity.getHealth() > entity.getMaxHealth()) {
            entity.setHealth(entity.getMaxHealth());
        }
    }
}
