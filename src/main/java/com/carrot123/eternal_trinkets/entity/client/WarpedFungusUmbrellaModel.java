package com.carrot123.eternal_trinkets.entity.client;

import com.carrot123.eternal_trinkets.EternalTrinkets;
import com.carrot123.eternal_trinkets.entity.neutral.WarpedFungusUmbrella;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class WarpedFungusUmbrellaModel extends GeoModel<WarpedFungusUmbrella> {

    @Override
    public ResourceLocation getModelResource(WarpedFungusUmbrella animatable) {
        return ResourceLocation.fromNamespaceAndPath(EternalTrinkets.MODID, "geo/warped_fungus_umbrella.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(WarpedFungusUmbrella animatable) {
        return ResourceLocation.fromNamespaceAndPath(EternalTrinkets.MODID, "textures/entity/warped_fungus_umbrella.png");
    }

    @Override
    public ResourceLocation getAnimationResource(WarpedFungusUmbrella animatable) {
        return ResourceLocation.fromNamespaceAndPath(EternalTrinkets.MODID, "animations/warped_fungus_umbrella.animation.json");
    }
}
