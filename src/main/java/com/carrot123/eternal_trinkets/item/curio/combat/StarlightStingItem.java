package com.carrot123.eternal_trinkets.item.curio.combat;

import java.util.UUID;

import com.carrot123.eternal_trinkets.item.curio.BaseCurioItem;
import com.carrot123.eternal_trinkets.misc.ModConfig;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import top.theillusivec4.curios.api.SlotContext;

public class StarlightStingItem extends BaseCurioItem {

    private static final UUID ATTACK_DAMAGE_UUID =
            UUID.fromString("965131dd-9c0f-4ae5-81b4-01ea2f1d654b");

    public StarlightStingItem() {
        super(Rarity.COMMON);
    }

    @Override
    @SuppressWarnings("null")
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(
            SlotContext slotContext, UUID uuid, ItemStack stack) {
        double attackDamage = ModConfig.STARLIGHT_STING_ATTACK_DAMAGE.get();

        if (attackDamage <= 0.0D) {
            return ImmutableMultimap.of();
        }

        return ImmutableMultimap.of(
                Attributes.ATTACK_DAMAGE,
                new AttributeModifier(
                        ATTACK_DAMAGE_UUID,
                        "starlight_sting_attack_damage",
                        attackDamage,
                        AttributeModifier.Operation.ADDITION));
    }
}