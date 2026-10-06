package com.carrot123.eternal_trinkets.item.curio;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import java.util.UUID;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import top.theillusivec4.curios.api.SlotContext;

public final class ArcherAimingScopeItem extends BaseCurioItem {
    public static final String SLOT = "hands";

    public ArcherAimingScopeItem() {
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
        return ImmutableMultimap.of();
    }

    private boolean valid(SlotContext context, ItemStack stack) {
        return context != null && stack.is(this)
                && SLOT.equals(context.identifier()) && !context.cosmetic();
    }
}
