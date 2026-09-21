package com.carrot123.eternal_trinkets.item.curio.luck;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import com.carrot123.eternal_trinkets.item.curio.BaseCurioItem;
import com.carrot123.eternal_trinkets.misc.ModConfig;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
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
 * 超幸运的四叶草！
 * 佩戴时：提供幸运属性（默认 1），并在挖矿/击杀时提供时运、抢夺等级（由事件处理器实现）。
 * 数值均可在 item.toml 的 lucky_clover 段配置。
 */
public class LuckyCloverItem extends BaseCurioItem {

    private static final UUID LUCK_UUID = UUID.fromString("253e2b9a-cfd6-460f-a93d-47b51df35fe6");

    public LuckyCloverItem() {
        super(Rarity.RARE);
    }

    // ── 幸运属性（动态读取配置）─────────────────────────────────
    @Override
    @SuppressWarnings("null")
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(
            SlotContext slotContext, UUID uuid, ItemStack stack) {
        int luck = ModConfig.CLOVER_LUCK.get();
        if (luck <= 0) {
            return ImmutableMultimap.of();
        }
        return ImmutableMultimap.of(
                Attributes.LUCK,
                new AttributeModifier(LUCK_UUID, "lucky_clover_luck",
                        luck, AttributeModifier.Operation.ADDITION));
    }

    // ── tooltip（按住 Shift 显示隐藏效果）───────────────────────
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                 List<Component> tooltip, TooltipFlag flag) {

        double luck = ModConfig.CLOVER_LUCK.get();
        double fortune = ModConfig.CLOVER_FORTUNE.get();
        double looting = ModConfig.CLOVER_LOOTING.get();
        Component luckComponent = Component.literal(String.valueOf(luck)).withStyle(ChatFormatting.GREEN);
        Component fortuneComponent = Component.literal(String.valueOf(fortune)).withStyle(ChatFormatting.GREEN);
        Component lootingComponent = Component.literal(String.valueOf(looting)).withStyle(ChatFormatting.GREEN);
        tooltip.add(Component.translatable("tooltip.eternal_trinkets.lucky_clover.line1"));

        if (Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("tooltip.eternal_trinkets.lucky_clover.hidden_stone"));
            tooltip.add(Component.translatable("tooltip.eternal_trinkets.lucky_clover.hidden_luck", luckComponent));
            tooltip.add(Component.translatable("tooltip.eternal_trinkets.lucky_clover.hidden_ench", fortuneComponent, lootingComponent));
        } else {
            tooltip.add(Component.translatable("tooltip.eternal_trinkets.lucky_clover.shift"));
        }
    }
}
