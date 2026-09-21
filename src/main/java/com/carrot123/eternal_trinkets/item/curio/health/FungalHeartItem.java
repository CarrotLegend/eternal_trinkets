package com.carrot123.eternal_trinkets.item.curio.health;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import com.carrot123.eternal_trinkets.item.curio.BaseCurioItem;
import com.carrot123.eternal_trinkets.misc.ModConfig;
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

/**
 * 真菌之心 —— 佩戴时增加最大生命值并持续恢复生命。
 */
public class FungalHeartItem extends BaseCurioItem {

    private static final UUID HEALTH_UUID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");

    /** 计时器，每 20 tick（1 秒）触发一次治疗 */
    private int tickCounter;

    public FungalHeartItem() {
        super(Rarity.UNCOMMON);
    }

    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
        super.onUnequip(slotContext, newStack, stack);
        clampHealth(slotContext.entity());
    }

    // ── 动态读取配置的属性修饰符 ─────────────────────────────────

    @Override
    @SuppressWarnings("null")
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(
            SlotContext slotContext, UUID uuid, ItemStack stack) {
        double bonus = ModConfig.FUNGAL_HEART_MAX_HEALTH.get();
        if (bonus <= 0) return ImmutableMultimap.of();
        return ImmutableMultimap.of(
                Attributes.MAX_HEALTH,
                new AttributeModifier(HEALTH_UUID, "fungal_heart_health",
                        bonus, AttributeModifier.Operation.ADDITION));
    }

    // ── 每秒治疗 ─────────────────────────────────────────────────

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        LivingEntity wearer = slotContext.entity();
        if (wearer == null || wearer.level().isClientSide()) return;

        tickCounter++;
        if (tickCounter >= 20) {
            tickCounter = 0;
            float heal = ModConfig.FUNGAL_HEART_HEAL_PER_SECOND.get().floatValue();
            if (heal > 0 && wearer.getHealth() < wearer.getMaxHealth()) {
                wearer.heal(heal);
            }
        }
    }

    // ── tooltip ──────────────────────────────────────────────────

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                 List<Component> tooltip, TooltipFlag flag) {
        double hp = ModConfig.FUNGAL_HEART_MAX_HEALTH.get();
        double hps = ModConfig.FUNGAL_HEART_HEAL_PER_SECOND.get();
        Component hpComponent = Component.literal(formatHeart(hp)).withStyle(ChatFormatting.GREEN);
        Component hpsComponent = Component.literal(formatHeart(hps)).withStyle(ChatFormatting.GREEN);
        tooltip.add(Component.translatable("tooltip.eternal_trinkets.fungal_heart.max_health", hpComponent));
        tooltip.add(Component.translatable("tooltip.eternal_trinkets.fungal_heart.heal", hpsComponent));
    }

    private static String formatHeart(double value) {
        return String.format("%.1f", value);
    }
}
