package com.carrot123.eternal_trinkets.item.curio.curse;

import com.aizistral.enigmaticlegacy.api.items.ICursed;
import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import com.carrot123.eternal_trinkets.item.curio.BaseCurioItem;
import com.carrot123.eternal_trinkets.util.CuriosUtils;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;

public abstract class CursedCurioItem extends BaseCurioItem implements ICursed {
    private final String slot;

    protected CursedCurioItem(String slot) {
        super(Rarity.EPIC);
        this.slot = slot;
    }

    @Override
    public boolean canEquip(SlotContext context, ItemStack stack) {
        return context != null
                && slot.equals(context.identifier())
                && !context.cosmetic()
                && context.entity() instanceof Player player
                && SuperpositionHandler.isTheCursedOne(player)
                && CuriosUtils.noSameCurio(player, this);
    }

    @Override
    public boolean canEquipFromUse(SlotContext context, ItemStack stack) {
        return canEquip(context, stack);
    }

    public static boolean isActive(Player player, Item item, String slot) {
        return SuperpositionHandler.isTheCursedOne(player)
                && CuriosApi.getCuriosInventory(player).resolve()
                        .flatMap(handler -> handler.findFirstCurio(item))
                        .filter(result -> slot.equals(result.slotContext().identifier())
                                && !result.slotContext().cosmetic())
                        .isPresent();
    }

    @Override
    public List<Component> getAttributesTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.clear();
        return tooltips;
    }
}
