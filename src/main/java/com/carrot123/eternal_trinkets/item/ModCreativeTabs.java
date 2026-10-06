package com.carrot123.eternal_trinkets.item;

import com.carrot123.eternal_trinkets.EternalTrinkets;
import com.carrot123.eternal_trinkets.integration.thirst.ModThirstItems;
import com.carrot123.eternal_trinkets.item.curio.ModCurioItems;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

@SuppressWarnings("null")
public final class ModCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(
                    Registries.CREATIVE_MODE_TAB,
                    EternalTrinkets.MODID);

    public static final RegistryObject<CreativeModeTab> ETERNAL_TRINKETS_TAB =
            CREATIVE_MODE_TABS.register(
                    "eternal_trinkets",
                    () -> CreativeModeTab.builder()
                            .title(Component.translatable("itemGroup.eternal_trinkets"))
                            .icon(() -> new ItemStack(ModItems.WARPED_CORE.get()))
                            .displayItems((params, output) -> {
                                output.accept(ModCurioItems.FUNGAL_HEART.get());
                                output.accept(ModCurioItems.GOLDEN_HONEY_MEDICINE.get());
                                output.accept(ModCurioItems.LUCKY_CLOVER.get());
                                output.accept(ModCurioItems.GRUMPY_PUFFERFISH.get());
                                output.accept(ModCurioItems.STARLIGHT_STING.get());
                                output.accept(ModCurioItems.YIN_XUAN.get());
                                output.accept(ModCurioItems.JIAO_BAI.get());
                                output.accept(ModCurioItems.TIAN_YI.get());
                                output.accept(ModCurioItems.CRYSTAL_NECKLACE.get());
                                output.accept(ModCurioItems.COUNTER_EYE.get());
                                output.accept(ModCurioItems.PRECISION_EYE.get());
                                output.accept(ModCurioItems.NOVICE_MAGE_HAT.get());
                                output.accept(ModCurioItems.GREEDY_FOCUS.get());
                                output.accept(ModCurioItems.FOREST_BRACELET.get());
                                output.accept(ModCurioItems.FOREST_CROWN.get());
                                output.accept(ModCurioItems.ELF_BOOTS.get());
                                output.accept(ModCurioItems.EXTINCTION_STONE.get());
                                output.accept(ModCurioItems.HELL_EYE.get());
                                output.accept(ModCurioItems.ANGEL_BLAZE_GUARD.get());
                                output.accept(ModCurioItems.HATRED_DIARY.get());

                                output.accept(ModItems.FUNGUS_CAP_UMBRELLA.get());
                                output.accept(ModItems.WARPED_FUNGUS_CAP_BOAT.get());
                                output.accept(ModItems.WARPED_FUNGUS_CAP.get());
                                output.accept(ModItems.HONEY_INFUSED_FRUIT.get());
                                output.accept(ModItems.WARPED_CORE.get());
                                output.accept(ModItems.EXQUISITE_CORE.get());
                                output.accept(ModItems.PURPUR_INGOT.get());
                                output.accept(ModItems.CALAMITY_QUARTZ.get());
                                output.accept(ModItems.FORTUNE_QUARTZ.get());
                                output.accept(ModItems.SPECIAL_GLASS_BOTTLE.get());
                                output.accept(ModItems.AIR_ESSENCE.get());
                                output.accept(ModItems.PRISON_BREATH.get());
                                output.accept(ModItems.THIN_VOID.get());
                                output.accept(ModItems.PURE_VOID.get());
                                output.accept(ModItems.AETHER.get());
                                output.accept(ModItems.VOID_CAPTURE_DEVICE.get());
                                output.accept(ModItems.ETERNAL_POTION_POUCH.get());
                                output.accept(ModItems.WARPED_FUNGUS_SPRITE_SPAWN_EGG.get());
                                output.accept(ModItems.WARPED_FUNGUS_UMBRELLA_SPAWN_EGG.get());

                                if (ModThirstItems.WATER_POUCH != null) {
                                    output.accept(ModThirstItems.WATER_POUCH.get());
                                    output.accept(ModThirstItems.IMPROVED_WATER_POUCH.get());
                                }
                            })
                            .build());

    private ModCreativeTabs() {
        throw new UnsupportedOperationException("utility class");
    }

    public static void register(IEventBus bus) {
        CREATIVE_MODE_TABS.register(bus);
    }
}
