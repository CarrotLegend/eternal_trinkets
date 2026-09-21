package com.carrot123.eternal_trinkets.util;

import com.google.common.collect.Multimap;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;

/** Recomputes one already-equipped Curio after its Yin-Yang partner changes. */
public final class YinYangAttributeRefresh {

    private YinYangAttributeRefresh() {
        throw new UnsupportedOperationException("utility class");
    }

    public static void refreshEquipped(LivingEntity wearer, Item item) {
        if (wearer == null || wearer.level().isClientSide) {
            return;
        }
        CuriosApi.getCuriosInventory(wearer).ifPresent(handler ->
                handler.findFirstCurio(item).ifPresent(result -> {
                    SlotContext context = result.slotContext();
                    Multimap<Attribute, AttributeModifier> modifiers =
                            CuriosApi.getAttributeModifiers(
                                    context,
                                    CuriosApi.getSlotUuid(context),
                                    result.stack());
                    wearer.getAttributes().removeAttributeModifiers(modifiers);
                    wearer.getAttributes().addTransientAttributeModifiers(modifiers);
                }));
    }
}
