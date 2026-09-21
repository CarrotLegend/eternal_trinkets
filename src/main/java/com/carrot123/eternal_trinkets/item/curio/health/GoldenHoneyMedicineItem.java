package com.carrot123.eternal_trinkets.item.curio.health;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import com.carrot123.eternal_trinkets.item.curio.BaseCurioItem;
import com.carrot123.eternal_trinkets.misc.ModConfig;
import com.carrot123.eternal_trinkets.misc.ModEffects;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import top.theillusivec4.curios.api.SlotContext;

public final class GoldenHoneyMedicineItem extends BaseCurioItem {

    public static final String BELT_SLOT = "belt";
    public static final double EMPOWER_CHANCE = 0.5D;
    public static final int MAX_HONEYED_LEVEL = 3;
    public static final int DECAY_INTERVAL_TICKS = 100;

    private static final String TAG_HONEYED_LEVEL = "HoneyedLevel";
    private static final String TAG_NEXT_DECAY_TIME = "NextHoneyedDecayTime";
    private static final int MAINTAINED_EFFECT_DURATION = 30;
    private static final int REFRESH_THRESHOLD = 10;

    public GoldenHoneyMedicineItem() {
        super(Rarity.UNCOMMON);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(
            SlotContext slotContext, UUID slotUuid, ItemStack stack) {
        if (!BELT_SLOT.equals(slotContext.identifier())) {
            return ImmutableMultimap.of();
        }
        double bonus = ModConfig.GOLDEN_HONEY_MEDICINE_MAX_HEALTH_BONUS.get();
        if (bonus <= 0.0D) {
            return ImmutableMultimap.of();
        }
        return ImmutableMultimap.of(
                Attributes.MAX_HEALTH,
                new AttributeModifier(slotUuid, "golden_honey_medicine_max_health",
                        bonus, AttributeModifier.Operation.ADDITION));
    }

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        // The belt inventory has one position. A global duplicate scan is both
        // unnecessary and incorrect when Curios revalidates the occupied stack.
        return BELT_SLOT.equals(slotContext.identifier());
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        return BELT_SLOT.equals(slotContext.identifier());
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        LivingEntity wearer = slotContext.entity();
        if (wearer == null || wearer.level().isClientSide || !BELT_SLOT.equals(slotContext.identifier())) {
            return;
        }

        decayIfNeeded(stack, wearer.level().getGameTime());
        int desiredAmplifier = getHoneyedLevel(stack) - 1;
        MobEffectInstance current = wearer.getEffect(ModEffects.HONEY_INFUSED.get());
        if (current == null
                || current.getAmplifier() < desiredAmplifier
                || current.getAmplifier() == desiredAmplifier
                && current.getDuration() <= REFRESH_THRESHOLD) {
            wearer.addEffect(new MobEffectInstance(
                    ModEffects.HONEY_INFUSED.get(),
                    MAINTAINED_EFFECT_DURATION,
                    desiredAmplifier,
                    false, false, true));
        }
    }

    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
        // Curios also invokes this callback when stack NBT changes.
        if (!newStack.is(this)) {
            resetTemporaryState(stack);
            clampHealth(slotContext.entity());
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltip, TooltipFlag flag) {
        String chance = formatNumber(EMPOWER_CHANCE * 100.0D);
        tooltip.add(Component.translatable(
                        "tooltip.eternal_trinkets.golden_honey_medicine.honeyed",
                        Component.translatable(ModEffects.HONEY_INFUSED.get().getDescriptionId())));
        tooltip.add(Component.translatable(
                        "tooltip.eternal_trinkets.golden_honey_medicine.empower", chance));
        tooltip.add(Component.translatable(
                        "tooltip.eternal_trinkets.golden_honey_medicine.decay"));
    }

    public static int getHoneyedLevel(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(TAG_HONEYED_LEVEL)) {
            return 1;
        }
        return Math.max(1, Math.min(MAX_HONEYED_LEVEL, tag.getInt(TAG_HONEYED_LEVEL)));
    }

    public static void empower(ItemStack stack, long gameTime) {
        setHoneyedLevel(stack, Math.min(MAX_HONEYED_LEVEL, getHoneyedLevel(stack) + 1));
        stack.getOrCreateTag().putLong(TAG_NEXT_DECAY_TIME, gameTime + DECAY_INTERVAL_TICKS);
    }

    public static void resetTemporaryState(ItemStack stack) {
        if (!stack.hasTag()) {
            return;
        }
        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return;
        }
        tag.remove(TAG_HONEYED_LEVEL);
        tag.remove(TAG_NEXT_DECAY_TIME);
        if (tag.isEmpty()) {
            stack.setTag(null);
        }
    }

    private static void decayIfNeeded(ItemStack stack, long gameTime) {
        int level = getHoneyedLevel(stack);
        if (level <= 1) {
            return;
        }

        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(TAG_NEXT_DECAY_TIME)) {
            stack.getOrCreateTag().putLong(
                    TAG_NEXT_DECAY_TIME, gameTime + DECAY_INTERVAL_TICKS);
            return;
        }

        long nextDecay = tag.getLong(TAG_NEXT_DECAY_TIME);
        if (gameTime < nextDecay) {
            return;
        }

        level--;
        setHoneyedLevel(stack, level);
        if (level > 1) {
            tag.putLong(TAG_NEXT_DECAY_TIME, gameTime + DECAY_INTERVAL_TICKS);
        } else {
            tag.remove(TAG_NEXT_DECAY_TIME);
        }
    }

    private static void setHoneyedLevel(ItemStack stack, int level) {
        int clamped = Math.max(1, Math.min(MAX_HONEYED_LEVEL, level));
        if (clamped == 1) {
            CompoundTag tag = stack.getTag();
            if (tag != null) {
                tag.remove(TAG_HONEYED_LEVEL);
            }
        } else {
            stack.getOrCreateTag().putInt(TAG_HONEYED_LEVEL, clamped);
        }
    }

    private static String formatNumber(double value) {
        return BigDecimal.valueOf(value).stripTrailingZeros().toPlainString();
    }
}
