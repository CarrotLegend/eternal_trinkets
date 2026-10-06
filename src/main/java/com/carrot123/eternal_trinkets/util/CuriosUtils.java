package com.carrot123.eternal_trinkets.util;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

/**
 * Curios 饰品工具方法。
 */
@SuppressWarnings("null")
public final class CuriosUtils {

    private CuriosUtils() {
        throw new UnsupportedOperationException("utility class");
    }

    /**
     * 检查穿戴者是否已在其他槽位穿戴了同类型饰品。
     * 防止同一饰品被重复装备到不同槽位。
     *
     * @param wearer 穿戴者
     * @param item   要检查的饰品物品
     * @return {@code true} 如果允许装备（未发现同类型），{@code false} 如果已有同类型
     */
    public static boolean noSameCurio(LivingEntity wearer, Item item) {
        LazyOptional<ICuriosItemHandler> handler = CuriosApi.getCuriosInventory(wearer);
        return handler.map(curios -> {
            for (var entry : curios.getCurios().entrySet()) {
                ICurioStacksHandler stacksHandler = entry.getValue();
                IDynamicStackHandler stacks = stacksHandler.getStacks();
                for (int i = 0; i < stacks.getSlots(); i++) {
                    ItemStack stack = stacks.getStackInSlot(i);
                    if (!stack.isEmpty() && stack.is(item)) {
                        return false;
                    }
                }
            }
            return true;
        }).orElse(true);
    }

    /**
     * 检查穿戴者是否正佩戴指定饰品（任意槽位）。
     *
     * @param wearer 穿戴者
     * @param item   要检查的饰品物品
     * @return {@code true} 如果已佩戴该饰品
     */
    public static boolean hasCurioEquipped(LivingEntity wearer, Item item) {
        if (wearer == null) {
            return false;
        }
        LazyOptional<ICuriosItemHandler> handler = CuriosApi.getCuriosInventory(wearer);
        return handler.map(curios -> {
            for (var entry : curios.getCurios().entrySet()) {
                IDynamicStackHandler stacks = entry.getValue().getStacks();
                for (int i = 0; i < stacks.getSlots(); i++) {
                    ItemStack stack = stacks.getStackInSlot(i);
                    if (!stack.isEmpty() && stack.is(item)) {
                        return true;
                    }
                }
            }
            return false;
        }).orElse(false);
    }

    public static boolean hasFunctionalCurio(LivingEntity wearer, Item item, String requiredSlot) {
        if (wearer == null || item == null || requiredSlot == null) {
            return false;
        }
        return CuriosApi.getCuriosInventory(wearer).map(curios ->
                curios.findCurios(item).stream().anyMatch(result ->
                        requiredSlot.equals(result.slotContext().identifier())
                                && !result.slotContext().cosmetic())).orElse(false);
    }
}
