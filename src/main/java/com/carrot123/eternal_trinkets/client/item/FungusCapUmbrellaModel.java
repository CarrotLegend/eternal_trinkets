package com.carrot123.eternal_trinkets.client.item;

import com.carrot123.eternal_trinkets.EternalTrinkets;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class FungusCapUmbrellaModel extends GeoModel<com.carrot123.eternal_trinkets.item.FungusCapUmbrellaItem> {

    @Override
    public ResourceLocation getModelResource(com.carrot123.eternal_trinkets.item.FungusCapUmbrellaItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(EternalTrinkets.MODID, "geo/fungus_cap_umbrella.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(com.carrot123.eternal_trinkets.item.FungusCapUmbrellaItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(EternalTrinkets.MODID, "textures/model/item/fungus_cap_umbrella.png");
    }

    @Override
    public ResourceLocation getAnimationResource(com.carrot123.eternal_trinkets.item.FungusCapUmbrellaItem animatable) {
        return null;
    }
}
