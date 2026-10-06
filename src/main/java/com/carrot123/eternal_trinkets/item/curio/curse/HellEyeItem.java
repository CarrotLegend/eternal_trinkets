package com.carrot123.eternal_trinkets.item.curio.curse;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;
import top.theillusivec4.curios.api.SlotContext;

public final class HellEyeItem extends CursedCurioItem {
    public static final ResourceLocation RESISTANCE_ID =
            new ResourceLocation("puffish_attributes", "resistance");
    public static final UUID RESISTANCE_UUID = UUID.nameUUIDFromBytes(
            "eternal_trinkets:hell_eye/resistance".getBytes(StandardCharsets.UTF_8));

    public HellEyeItem() {
        super("mystic_eye");
    }

    public static Attribute resistanceAttribute() {
        return ModList.get().isLoaded("puffish_attributes")
                ? ForgeRegistries.ATTRIBUTES.getValue(RESISTANCE_ID) : null;
    }

    public static AttributeModifier resistanceModifier() {
        return new AttributeModifier(RESISTANCE_UUID, "hell_eye_resistance", 0.08D,
                AttributeModifier.Operation.MULTIPLY_BASE);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(
            SlotContext context, UUID slotUuid, ItemStack stack) {
        if (context == null || !"mystic_eye".equals(context.identifier())
                || context.cosmetic() || !(context.entity() instanceof net.minecraft.world.entity.player.Player player)
                || !com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler.isTheCursedOne(player)) {
            return ImmutableMultimap.of();
        }
        Attribute attribute = resistanceAttribute();
        return attribute == null ? ImmutableMultimap.of()
                : ImmutableMultimap.of(attribute, resistanceModifier());
    }

    @Override
    public void onUnequip(SlotContext context, ItemStack newStack, ItemStack stack) {
        if (context.entity().level().isClientSide) {
            return;
        }
        Attribute attribute = resistanceAttribute();
        AttributeInstance instance = attribute == null ? null : context.entity().getAttribute(attribute);
        if (instance != null) {
            instance.removeModifier(RESISTANCE_UUID);
        }
    }
}
