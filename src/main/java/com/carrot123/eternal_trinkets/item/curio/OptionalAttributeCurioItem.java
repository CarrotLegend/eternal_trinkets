package com.carrot123.eternal_trinkets.item.curio;

import com.carrot123.eternal_trinkets.EternalTrinkets;
import com.carrot123.eternal_trinkets.event.NewCurioEvents;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;
import top.theillusivec4.curios.api.SlotContext;

public class OptionalAttributeCurioItem extends BaseCurioItem {
    public enum Kind {
        CRYSTAL_NECKLACE("crystal_necklace", "necklace", Rarity.RARE),
        COUNTER_EYE("counter_eye", "mystic_eye", Rarity.RARE),
        PRECISION_EYE("precision_eye", "mystic_eye", Rarity.RARE),
        NOVICE_MAGE_HAT("novice_mage_hat", "head", Rarity.UNCOMMON),
        FOREST_BRACELET("forest_bracelet", "bracelet", Rarity.RARE);

        private final String id;
        private final String slot;
        private final Rarity rarity;

        Kind(String id, String slot, Rarity rarity) {
            this.id = id;
            this.slot = slot;
            this.rarity = rarity;
        }
    }

    private final Kind kind;

    public OptionalAttributeCurioItem(Kind kind) {
        super(kind.rarity);
        this.kind = kind;
    }

    @Override
    public boolean canEquip(SlotContext context, ItemStack stack) {
        return kind.slot.equals(context.identifier());
    }

    @Override
    public boolean canEquipFromUse(SlotContext context, ItemStack stack) {
        return canEquip(context, stack);
    }

    @Override
    public void onUnequip(SlotContext context, ItemStack newStack, ItemStack stack) {
        if (kind == Kind.COUNTER_EYE && !newStack.is(this)) {
            NewCurioEvents.clearCounter(context.entity());
        }
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(
            SlotContext context, UUID slotUuid, ItemStack stack) {
        if (!kind.slot.equals(context.identifier())) {
            return ImmutableMultimap.of();
        }
        ImmutableMultimap.Builder<Attribute, AttributeModifier> result = ImmutableMultimap.builder();
        switch (kind) {
            case CRYSTAL_NECKLACE -> {
                add(result, Attributes.LUCK, "minecraft:generic.luck", 5.0D,
                        AttributeModifier.Operation.ADDITION);
                add(result, "terra_curio:dodge_chance", 0.05D,
                        AttributeModifier.Operation.ADDITION);
            }
            case PRECISION_EYE -> {
                add(result, "terra_curio:ranged_damage", 0.25D,
                        AttributeModifier.Operation.MULTIPLY_BASE);
                add(result, "until_eternity:charge_speed", 0.10D,
                        AttributeModifier.Operation.MULTIPLY_BASE);
            }
            case NOVICE_MAGE_HAT -> {
                add(result, "irons_spellbooks:spell_power", 0.10D,
                        AttributeModifier.Operation.MULTIPLY_BASE);
                add(result, "irons_spellbooks:max_mana", 100.0D,
                        AttributeModifier.Operation.ADDITION);
                add(result, "irons_spellbooks:cooldown_reduction", 0.05D,
                        AttributeModifier.Operation.MULTIPLY_BASE);
            }
            case FOREST_BRACELET -> {
                add(result, Attributes.LUCK, "minecraft:generic.luck", 12.0D,
                        AttributeModifier.Operation.ADDITION);
                add(result, "terra_curio:ranged_damage", 0.15D,
                        AttributeModifier.Operation.MULTIPLY_BASE);
                add(result, "terra_curio:ranged_velocity", 0.10D,
                        AttributeModifier.Operation.MULTIPLY_BASE);
            }
            case COUNTER_EYE -> {
            }
        }
        return result.build();
    }

    private void add(ImmutableMultimap.Builder<Attribute, AttributeModifier> result,
                     String id, double amount, AttributeModifier.Operation operation) {
        ResourceLocation location = new ResourceLocation(id);
        if (!ModList.get().isLoaded(location.getNamespace())) {
            return;
        }
        Attribute attribute = ForgeRegistries.ATTRIBUTES.getValue(location);
        if (attribute != null) {
            add(result, attribute, id, amount, operation);
        }
    }

    private void add(ImmutableMultimap.Builder<Attribute, AttributeModifier> result,
                     Attribute attribute, String id, double amount,
                     AttributeModifier.Operation operation) {
        UUID uuid = UUID.nameUUIDFromBytes((EternalTrinkets.MODID + ":" + kind.id + "/" + id)
                .getBytes(StandardCharsets.UTF_8));
        result.put(attribute, new AttributeModifier(uuid, kind.id + "/" + id, amount, operation));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        String key = "tooltip.eternal_trinkets." + kind.id;
        int count = switch (kind) {
            case CRYSTAL_NECKLACE, PRECISION_EYE -> 3;
            case COUNTER_EYE -> 1;
            case NOVICE_MAGE_HAT, FOREST_BRACELET -> 3;
        };
        for (int i = 1; i <= count; i++) {
            String dependency = switch (kind) {
                case CRYSTAL_NECKLACE -> i == 2 ? "terra_curio" : i == 3 ? "enigmaticaddons" : null;
                case COUNTER_EYE -> "until_eternity";
                case PRECISION_EYE -> i == 1 ? "terra_curio" : "until_eternity";
                case NOVICE_MAGE_HAT -> "irons_spellbooks";
                case FOREST_BRACELET -> i == 1 ? null : "terra_curio";
            };
            if (dependency != null && !ModList.get().isLoaded(dependency)) {
                tooltip.add(Component.translatable("tooltip.eternal_trinkets.optional_unavailable", dependency)
                        .withStyle(ChatFormatting.DARK_GRAY));
            } else {
                tooltip.add(Component.translatable(key + "." + i).withStyle(ChatFormatting.GRAY));
            }
        }
    }
}
