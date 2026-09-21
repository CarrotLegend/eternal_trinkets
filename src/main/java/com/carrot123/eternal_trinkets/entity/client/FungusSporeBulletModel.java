package com.carrot123.eternal_trinkets.entity.client;

import com.carrot123.eternal_trinkets.EternalTrinkets;
import com.carrot123.eternal_trinkets.entity.projectile.FungusSporeBullet;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class FungusSporeBulletModel extends GeoModel<FungusSporeBullet> {

    @Override
    public ResourceLocation getModelResource(FungusSporeBullet animatable) {
        return ResourceLocation.fromNamespaceAndPath(EternalTrinkets.MODID, "geo/fungus_spore_bullet.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(FungusSporeBullet animatable) {
        return ResourceLocation.fromNamespaceAndPath(EternalTrinkets.MODID, "textures/entity/fungus_spore_bullet.png");
    }

    @Override
    public ResourceLocation getAnimationResource(FungusSporeBullet animatable) {
        return ResourceLocation.fromNamespaceAndPath(EternalTrinkets.MODID, "animations/fungus_spore_bullet.animation.json");
    }
}
