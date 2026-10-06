package com.carrot123.eternal_trinkets.item.curio;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import top.theillusivec4.curios.api.SlotContext;

public final class JadeBraceletItem extends BaseCurioItem {
    public static final String SLOT = "bracelet";
    private static final Multimap<Attribute, AttributeModifier> MODIFIERS =
            ImmutableMultimap.<Attribute, AttributeModifier>builder()
                    .put(Attributes.LUCK, modifier("minecraft:generic.luck"))
                    .put(Attributes.ARMOR, modifier("minecraft:generic.armor"))
                    .put(Attributes.ATTACK_DAMAGE, modifier("minecraft:generic.attack_damage"))
                    .build();

    public JadeBraceletItem() {
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
        return valid(context, stack) ? MODIFIERS : ImmutableMultimap.of();
    }

    private boolean valid(SlotContext context, ItemStack stack) {
        return context != null && stack.is(this)
                && SLOT.equals(context.identifier()) && !context.cosmetic();
    }

    private static AttributeModifier modifier(String attributeId) {
        UUID id = UUID.nameUUIDFromBytes(("eternal_trinkets:jade_bracelet/" + attributeId)
                .getBytes(StandardCharsets.UTF_8));
        return new AttributeModifier(id, "jade_bracelet_" + attributeId,
                2.0D, AttributeModifier.Operation.ADDITION);
    }
}
