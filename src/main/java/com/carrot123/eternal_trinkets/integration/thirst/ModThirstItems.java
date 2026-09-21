package com.carrot123.eternal_trinkets.integration.thirst;

import com.carrot123.eternal_trinkets.EternalTrinkets;

import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModThirstItems {

    public static final String THIRST_MODID = "thirst";

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, EternalTrinkets.MODID);

    public static RegistryObject<Item> WATER_POUCH;
    public static RegistryObject<Item> IMPROVED_WATER_POUCH;

    private ModThirstItems() {
        throw new UnsupportedOperationException("utility class");
    }

    public static boolean isThirstLoaded() {
        return ModList.get().isLoaded(THIRST_MODID);
    }

    public static void register(IEventBus bus) {
        if (isThirstLoaded()) {
            WATER_POUCH = ITEMS.register("water_pouch", () -> new WaterPouchItem(9));
            IMPROVED_WATER_POUCH = ITEMS.register("improved_water_pouch", () -> new WaterPouchItem(24));
            EternalTrinkets.LOGGER.info("Thirst Was Taken detected — registered water pouch items.");
        }
        ITEMS.register(bus);
    }
}
