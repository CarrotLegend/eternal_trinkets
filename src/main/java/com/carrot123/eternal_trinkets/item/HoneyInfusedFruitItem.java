package com.carrot123.eternal_trinkets.item;

import com.carrot123.eternal_trinkets.misc.ModEffects;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * 蜜浸果 — 食用后恢复 2 点生命值并获得 5s 蜜浸效果。
 * 食物属性与苹果完全一致（nutrition=4, saturationMod=0.3）。
 */
public class HoneyInfusedFruitItem extends Item {

    private static final FoodProperties FOOD = new FoodProperties.Builder()
            .nutrition(4)
            .saturationMod(0.3F)
            .alwaysEat()
            .build();

    public HoneyInfusedFruitItem() {
        super(new Item.Properties().food(FOOD));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide && entity instanceof Player player) {
            // 立刻恢复 2 点生命值（参考蜂蜜瓶）
            player.heal(2.0F);

            // 5s 蜜浸效果（100 tick，等级 I = amplifier 0）
            player.addEffect(new MobEffectInstance(
                    ModEffects.HONEY_INFUSED.get(), 100, 0,
                    false, false, true));
        }

        return super.finishUsingItem(stack, level, entity);
    }
}
