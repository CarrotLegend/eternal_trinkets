package com.carrot123.eternal_trinkets.entity.client;

import com.carrot123.eternal_trinkets.EternalTrinkets;
import com.carrot123.eternal_trinkets.entity.neutral.WarpedFungusSprite;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class WarpedFungusSpriteModel extends GeoModel<WarpedFungusSprite> {

    @Override
    public ResourceLocation getModelResource(WarpedFungusSprite animatable) {
        return ResourceLocation.fromNamespaceAndPath(EternalTrinkets.MODID, "geo/warped_fungus_sprite.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(WarpedFungusSprite animatable) {
        return ResourceLocation.fromNamespaceAndPath(EternalTrinkets.MODID, "textures/entity/warped_fungus_sprite.png");
    }

    @Override
    public ResourceLocation getAnimationResource(WarpedFungusSprite animatable) {
        return ResourceLocation.fromNamespaceAndPath(EternalTrinkets.MODID, "animations/warped_fungus_sprite.animation.json");
    }
}
