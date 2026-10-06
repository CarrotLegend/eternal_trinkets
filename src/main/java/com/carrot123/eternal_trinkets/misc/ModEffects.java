package com.carrot123.eternal_trinkets.misc;

import com.carrot123.eternal_trinkets.EternalTrinkets;
import com.carrot123.eternal_trinkets.effect.HoneyInfusedEffect;
import com.carrot123.eternal_trinkets.effect.YinYangDissonanceEffect;
import com.carrot123.eternal_trinkets.effect.RevengeTargetEffect;

import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModEffects {

    public static final DeferredRegister<MobEffect> EFFECTS =
            DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, EternalTrinkets.MODID);

    public static final RegistryObject<MobEffect> HONEY_INFUSED =
            EFFECTS.register("honey_infused", HoneyInfusedEffect::new);

    public static final RegistryObject<MobEffect> YIN_YANG_DISSONANCE =
            EFFECTS.register("yin_yang_dissonance", YinYangDissonanceEffect::new);

    public static final RegistryObject<MobEffect> REVENGE_TARGET =
            EFFECTS.register("revenge_target", RevengeTargetEffect::new);

    private ModEffects() {
        throw new UnsupportedOperationException("utility class");
    }

    public static void register(IEventBus bus) {
        EFFECTS.register(bus);
    }
}
