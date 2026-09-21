package com.carrot123.eternal_trinkets.entity.client;

import com.carrot123.eternal_trinkets.EternalTrinkets;
import com.mojang.datafixers.util.Pair;

import net.minecraft.client.model.BoatModel;
import net.minecraft.client.model.ListModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.BoatRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.vehicle.Boat;

@SuppressWarnings("null")
public class WarpedFungusCapBoatRenderer extends BoatRenderer {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(EternalTrinkets.MODID, "textures/model/boat.png");

    private final Pair<ResourceLocation, ListModel<Boat>> modelWithTexture;

    public WarpedFungusCapBoatRenderer(EntityRendererProvider.Context context) {
        super(context, false);
        BoatModel model = new BoatModel(context.bakeLayer(ModelLayers.createBoatModelName(Boat.Type.OAK)));
        this.modelWithTexture = Pair.of(TEXTURE, model);
    }

    @Override
    public Pair<ResourceLocation, ListModel<Boat>> getModelWithLocation(Boat boat) {
        return this.modelWithTexture;
    }
}
