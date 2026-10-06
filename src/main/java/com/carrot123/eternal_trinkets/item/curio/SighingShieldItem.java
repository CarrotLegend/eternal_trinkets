package com.carrot123.eternal_trinkets.item.curio;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;
import top.theillusivec4.curios.api.SlotContext;

public final class SighingShieldItem extends BaseCurioItem {
    public static final String SLOT = "body";
    private static final ResourceLocation RESISTANCE_ID =
            new ResourceLocation("puffish_attributes", "resistance");
    private static final UUID ARMOR_UUID = uuid("minecraft:generic.armor");
    private static final UUID RESISTANCE_UUID = uuid("puffish_attributes:resistance");

    public SighingShieldItem() {
        super(Rarity.RARE);
    }

    @Override
    public boolean canEquip(SlotContext context, ItemStack stack) {
        return valid(context, stack)
                && com.carrot123.eternal_trinkets.util.CuriosUtils.noSameCurio(context.entity(), this);
    }

    @Override
    public boolean canEquipFromUse(SlotContext context, ItemStack stack) {
        return canEquip(context, stack);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(
            SlotContext context, UUID slotUuid, ItemStack stack) {
        if (!valid(context, stack)) {
            return ImmutableMultimap.of();
        }
        ImmutableMultimap.Builder<Attribute, AttributeModifier> modifiers = ImmutableMultimap.builder();
        modifiers.put(Attributes.ARMOR, new AttributeModifier(ARMOR_UUID,
                "sighing_shield_armor", 0.10D, AttributeModifier.Operation.MULTIPLY_TOTAL));
        if (ModList.get().isLoaded("puffish_attributes")) {
            Attribute resistance = ForgeRegistries.ATTRIBUTES.getValue(RESISTANCE_ID);
            if (resistance != null) {
                modifiers.put(resistance, new AttributeModifier(RESISTANCE_UUID,
                        "sighing_shield_resistance", 0.15D,
                        AttributeModifier.Operation.MULTIPLY_TOTAL));
            }
        }
        return modifiers.build();
    }

    private boolean valid(SlotContext context, ItemStack stack) {
        return context != null && stack.is(this)
                && SLOT.equals(context.identifier()) && !context.cosmetic();
    }

    private static UUID uuid(String attributeId) {
        return UUID.nameUUIDFromBytes(("eternal_trinkets:sighing_shield/" + attributeId)
                .getBytes(StandardCharsets.UTF_8));
    }
}
