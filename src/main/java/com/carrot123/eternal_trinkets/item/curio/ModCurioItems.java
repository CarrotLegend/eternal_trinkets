package com.carrot123.eternal_trinkets.item.curio;

import com.carrot123.eternal_trinkets.EternalTrinkets;
import com.carrot123.eternal_trinkets.item.curio.combat.GrumpyPufferfishItem;
import com.carrot123.eternal_trinkets.item.curio.combat.StarlightStingItem;
import com.carrot123.eternal_trinkets.item.curio.defense.JiaoBaiItem;
import com.carrot123.eternal_trinkets.item.curio.defense.TianYiItem;
import com.carrot123.eternal_trinkets.item.curio.defense.YinXuanItem;
import com.carrot123.eternal_trinkets.item.curio.health.FungalHeartItem;
import com.carrot123.eternal_trinkets.item.curio.health.GoldenHoneyMedicineItem;
import com.carrot123.eternal_trinkets.item.curio.luck.LuckyCloverItem;

import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModCurioItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, EternalTrinkets.MODID);

    public static final RegistryObject<Item> FUNGAL_HEART =
            ITEMS.register("fungal_heart", FungalHeartItem::new);

    public static final RegistryObject<Item> GOLDEN_HONEY_MEDICINE =
            ITEMS.register("golden_honey_medicine", GoldenHoneyMedicineItem::new);

    public static final RegistryObject<Item> LUCKY_CLOVER =
            ITEMS.register("lucky_clover", LuckyCloverItem::new);

    public static final RegistryObject<Item> GRUMPY_PUFFERFISH =
            ITEMS.register("grumpy_pufferfish", GrumpyPufferfishItem::new);

    public static final RegistryObject<Item> STARLIGHT_STING =
            ITEMS.register("starlight_sting", StarlightStingItem::new);

    public static final RegistryObject<Item> YIN_XUAN =
            ITEMS.register("yin_xuan", YinXuanItem::new);

    public static final RegistryObject<Item> JIAO_BAI =
            ITEMS.register("jiao_bai", JiaoBaiItem::new);

    public static final RegistryObject<Item> TIAN_YI =
            ITEMS.register("tian_yi", TianYiItem::new);

    public static final RegistryObject<Item> CRYSTAL_NECKLACE =
            ITEMS.register("crystal_necklace", () -> new OptionalAttributeCurioItem(OptionalAttributeCurioItem.Kind.CRYSTAL_NECKLACE));
    public static final RegistryObject<Item> COUNTER_EYE =
            ITEMS.register("counter_eye", () -> new OptionalAttributeCurioItem(OptionalAttributeCurioItem.Kind.COUNTER_EYE));
    public static final RegistryObject<Item> PRECISION_EYE =
            ITEMS.register("precision_eye", () -> new OptionalAttributeCurioItem(OptionalAttributeCurioItem.Kind.PRECISION_EYE));
    public static final RegistryObject<Item> NOVICE_MAGE_HAT =
            ITEMS.register("novice_mage_hat", () -> new OptionalAttributeCurioItem(OptionalAttributeCurioItem.Kind.NOVICE_MAGE_HAT));
    public static final RegistryObject<Item> GREEDY_FOCUS =
            ITEMS.register("greedy_focus", GreedyFocusItem::new);
    public static final RegistryObject<Item> FOREST_BRACELET =
            ITEMS.register("forest_bracelet", () -> new OptionalAttributeCurioItem(OptionalAttributeCurioItem.Kind.FOREST_BRACELET));

    public static final RegistryObject<Item> FOREST_CROWN =
            ITEMS.register("forest_crown", ForestCrownItem::new);

    public static final RegistryObject<Item> ELF_BOOTS =
            ITEMS.register("elf_boots", ElfBootsItem::new);

    private ModCurioItems() {
        throw new UnsupportedOperationException("utility class");
    }
}
