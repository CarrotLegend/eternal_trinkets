package com.carrot123.eternal_trinkets.item.curio.combat;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import com.carrot123.eternal_trinkets.item.curio.BaseCurioItem;
import com.carrot123.eternal_trinkets.misc.ModConfig;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;

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

/**
 * 暴躁河豚 —— 手饰。
 * 佩戴时提供攻击力加成（可配置）。免疫中毒 & 近战反弹
 * 荆棘伤害由 {@code GrumpyPufferfishEvents} 处理。
 */
public class GrumpyPufferfishItem extends BaseCurioItem {

    private static final UUID ATTACK_UUID = UUID.fromString("6f2a1b3c-4d5e-6f70-8192-a3b4c5d6e7f8");

    public GrumpyPufferfishItem() {
        super(Rarity.COMMON);
    }

    @Override
    @SuppressWarnings("null")
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(
            SlotContext slotContext, UUID uuid, ItemStack stack) {
        double attack = ModConfig.GRUMPY_PUFFERFISH_ATTACK_DAMAGE.get();
        if (attack <= 0) {
            return ImmutableMultimap.of();
        }
        return ImmutableMultimap.of(
                Attributes.ATTACK_DAMAGE,
                new AttributeModifier(ATTACK_UUID, "grumpy_pufferfish_attack",
                        attack, AttributeModifier.Operation.ADDITION));
    }

    // ── tooltip ──────────────────────────────────────────────────
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                 List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.eternal_trinkets.grumpy_pufferfish.reflect"));
        tooltip.add(Component.translatable("tooltip.eternal_trinkets.grumpy_pufferfish.poison_immune"));
    }
}
