package com.carrot123.eternal_trinkets.item;

import com.carrot123.eternal_trinkets.EternalTrinkets;
import com.carrot123.eternal_trinkets.block.ModBlocks;
import com.carrot123.eternal_trinkets.entity.ModEntities;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@SuppressWarnings("null")
public final class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, EternalTrinkets.MODID);

    public static final RegistryObject<Item> WARPED_CORE = ITEMS.register("warped_core", () -> new BlockItem(ModBlocks.WARPED_CORE.get(), new Item.Properties().rarity(Rarity.RARE)));
    public static final RegistryObject<Item> WARPED_FUNGUS_SPRITE_SPAWN_EGG = ITEMS.register("warped_fungus_sprite_spawn_egg", () -> new ForgeSpawnEggItem(ModEntities.WARPED_FUNGUS_SPRITE, 0x562C3E, 0x16615B, new Item.Properties()));
    public static final RegistryObject<Item> WARPED_FUNGUS_UMBRELLA_SPAWN_EGG = ITEMS.register("warped_fungus_umbrella_spawn_egg", () -> new ForgeSpawnEggItem(ModEntities.WARPED_FUNGUS_UMBRELLA, 0x318D8C, 0x3DA9A6, new Item.Properties()));
    public static final RegistryObject<Item> WARPED_FUNGUS_CAP_BOAT = ITEMS.register("warped_fungus_cap_boat", () -> new com.carrot123.eternal_trinkets.item.WarpedFungusCapBoatItem( new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> FUNGUS_CAP_UMBRELLA = ITEMS.register("fungus_cap_umbrella", () -> new FungusCapUmbrellaItem(new Item.Properties().rarity(Rarity.UNCOMMON).stacksTo(1)));
    public static final RegistryObject<Item> WARPED_FUNGUS_CAP = ITEMS.register("warped_fungus_cap", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> HONEY_INFUSED_FRUIT = ITEMS.register("honey_infused_fruit", HoneyInfusedFruitItem::new);
    public static final RegistryObject<Item> EXQUISITE_CORE = ITEMS.register("exquisite_core", () -> new Item(new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> PURPUR_INGOT = ITEMS.register("purpur_ingot", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> CALAMITY_QUARTZ = ITEMS.register("calamity_quartz", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> FORTUNE_QUARTZ = ITEMS.register("fortune_quartz", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> SPECIAL_GLASS_BOTTLE = ITEMS.register("special_glass_bottle", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> AIR_ESSENCE = ITEMS.register("air_essence", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> PRISON_BREATH = ITEMS.register("prison_breath", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> THIN_VOID = ITEMS.register("thin_void", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> PURE_VOID = ITEMS.register("pure_void", () -> new Item(new Item.Properties().rarity(Rarity.RARE)));
    public static final RegistryObject<Item> AETHER = ITEMS.register("aether", () -> new Item(new Item.Properties().rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> VOID_CAPTURE_DEVICE =
            ITEMS.register("void_capture_device", VoidCaptureDeviceItem::new);
    public static final RegistryObject<Item> ETERNAL_POTION_POUCH =
            ITEMS.register("eternal_potion_pouch", EternalPotionPouchItem::new);

    private ModItems() {
        throw new UnsupportedOperationException("utility class");
    }

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}
