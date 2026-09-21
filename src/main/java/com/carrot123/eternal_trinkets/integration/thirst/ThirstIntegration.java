package com.carrot123.eternal_trinkets.integration.thirst;

import com.carrot123.eternal_trinkets.EternalTrinkets;
import com.carrot123.eternal_trinkets.item.ModItems;

import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

/**
 * 通过 Thirst Was Taken API 注册蜜浸果的饮水值。
 * 等效于在 config/thirst/item-settings.toml 的 foods 列表中添加：
 * ["eternal_trinkets:honey_infused_fruit", 4, 1]
 */
@Mod.EventBusSubscriber(modid = EternalTrinkets.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ThirstIntegration {

    private ThirstIntegration() {
    }

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        if (!ModList.get().isLoaded("thirst")) {
            return;
        }

        event.enqueueWork(() -> {
            try {
                Class<?> helper = Class.forName(
                        "dev.ghen.thirst.foundation.common.capability.ThirstHelper");
                Item item = ModItems.HONEY_INFUSED_FRUIT.get();
                // addFood(Item item, int thirst, int quenched)
                // 苹果的饮水值: thirst=4 (2 droplets), quenched=1
                helper.getMethod("addFood", Item.class, int.class, int.class)
                        .invoke(null, item, 4, 1);
                EternalTrinkets.LOGGER.info("Registered honey_infused_fruit thirst values via Thirst Was Taken API.");
            } catch (Exception e) {
                EternalTrinkets.LOGGER.warn("Failed to register thirst values for honey_infused_fruit: {}", e.toString());
            }
        });
    }
}
