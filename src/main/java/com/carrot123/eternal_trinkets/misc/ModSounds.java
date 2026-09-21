package com.carrot123.eternal_trinkets.misc;

import com.carrot123.eternal_trinkets.EternalTrinkets;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, EternalTrinkets.MODID);

    public static final RegistryObject<SoundEvent> WARPED_FUNGUS_SPRITE_DIE =
            SOUNDS.register("warped_fungus_sprite_die",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(EternalTrinkets.MODID, "warped_fungus_sprite_die")));

    public static final RegistryObject<SoundEvent> WARPED_FUNGUS_SPRITE_ANGRY =
            SOUNDS.register("warped_fungus_sprite_angry",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(EternalTrinkets.MODID, "warped_fungus_sprite_angry")));

    public static final RegistryObject<SoundEvent> WARPED_FUNGUS_SPRITE_HURT =
            SOUNDS.register("warped_fungus_sprite_hurt",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(EternalTrinkets.MODID, "warped_fungus_sprite_hurt")));

    public static final RegistryObject<SoundEvent> WARPED_FUNGUS_SPRITE_AMBIENT_1 =
            SOUNDS.register("warped_fungus_sprite_ambient_1",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(EternalTrinkets.MODID, "warped_fungus_sprite_ambient_1")));

    public static final RegistryObject<SoundEvent> WARPED_FUNGUS_SPRITE_AMBIENT_2 =
            SOUNDS.register("warped_fungus_sprite_ambient_2",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(EternalTrinkets.MODID, "warped_fungus_sprite_ambient_2")));

    public static final RegistryObject<SoundEvent> WARPED_FUNGUS_SPRITE_AMBIENT_3 =
            SOUNDS.register("warped_fungus_sprite_ambient_3",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(EternalTrinkets.MODID, "warped_fungus_sprite_ambient_3")));

    public static final RegistryObject<SoundEvent> WARPED_FUNGUS_SPRITE_STEP_1 =
            SOUNDS.register("warped_fungus_sprite_step_1",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(EternalTrinkets.MODID, "warped_fungus_sprite_step_1")));

    public static final RegistryObject<SoundEvent> WARPED_FUNGUS_SPRITE_STEP_2 =
            SOUNDS.register("warped_fungus_sprite_step_2",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(EternalTrinkets.MODID, "warped_fungus_sprite_step_2")));

    public static final RegistryObject<SoundEvent> WARPED_FUNGUS_SPRITE_STEP_3 =
            SOUNDS.register("warped_fungus_sprite_step_3",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(EternalTrinkets.MODID, "warped_fungus_sprite_step_3")));

    private ModSounds() {
        throw new UnsupportedOperationException("utility class");
    }

    public static void register(IEventBus bus) {
        SOUNDS.register(bus);
    }
}
