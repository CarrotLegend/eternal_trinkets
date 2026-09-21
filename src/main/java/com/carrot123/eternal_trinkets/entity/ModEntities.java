package com.carrot123.eternal_trinkets.entity;

import com.carrot123.eternal_trinkets.EternalTrinkets;
import com.carrot123.eternal_trinkets.entity.neutral.WarpedFungusSprite;
import com.carrot123.eternal_trinkets.entity.neutral.WarpedFungusUmbrella;
import com.carrot123.eternal_trinkets.entity.projectile.FungusSporeBullet;
import com.carrot123.eternal_trinkets.entity.vehicle.WarpedFungusCapBoat;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@SuppressWarnings("null")
public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, EternalTrinkets.MODID);

    public static final RegistryObject<EntityType<WarpedFungusSprite>> WARPED_FUNGUS_SPRITE =
            ENTITIES.register("warped_fungus_sprite",
                    () -> EntityType.Builder.of(WarpedFungusSprite::new, MobCategory.MONSTER)
                            .sized(0.8F, 1.8F)
                            .clientTrackingRange(8)
                            .build(ResourceLocation.fromNamespaceAndPath(EternalTrinkets.MODID, "warped_fungus_sprite").toString()));

    public static final RegistryObject<EntityType<WarpedFungusUmbrella>> WARPED_FUNGUS_UMBRELLA =
            ENTITIES.register("warped_fungus_umbrella",
                    () -> EntityType.Builder.of(WarpedFungusUmbrella::new, MobCategory.MONSTER)
                            .sized(1.0F, 1.8F)
                            .clientTrackingRange(8)
                            .build(ResourceLocation.fromNamespaceAndPath(EternalTrinkets.MODID, "warped_fungus_umbrella").toString()));

    public static final RegistryObject<EntityType<FungusSporeBullet>> FUNGUS_SPORE_BULLET =
            ENTITIES.register("fungus_spore_bullet",
                    () -> EntityType.Builder.<FungusSporeBullet>of(FungusSporeBullet::new, MobCategory.MISC)
                            .sized(0.25F, 0.25F)
                            .clientTrackingRange(4)
                            .updateInterval(10)
                            .build(ResourceLocation.fromNamespaceAndPath(EternalTrinkets.MODID, "fungus_spore_bullet").toString()));

    public static final RegistryObject<EntityType<WarpedFungusCapBoat>> WARPED_FUNGUS_CAP_BOAT =
            ENTITIES.register("warped_fungus_cap_boat",
                    () -> EntityType.Builder.<WarpedFungusCapBoat>of(WarpedFungusCapBoat::new, MobCategory.MISC)
                            .sized(1.375F, 0.5625F)
                            .clientTrackingRange(10)
                            .fireImmune()
                            .build(ResourceLocation.fromNamespaceAndPath(EternalTrinkets.MODID, "warped_fungus_cap_boat").toString()));

    private ModEntities() {
        throw new UnsupportedOperationException("utility class");
    }

    public static void register(IEventBus bus) {
        ENTITIES.register(bus);
    }
}
