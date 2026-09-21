package com.carrot123.eternal_trinkets.entity.client;

import com.carrot123.eternal_trinkets.entity.projectile.FungusSporeBullet;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class FungusSporeBulletRenderer extends GeoEntityRenderer<FungusSporeBullet> {

    public FungusSporeBulletRenderer(net.minecraft.client.renderer.entity.EntityRendererProvider.Context context) {
        super(context, new FungusSporeBulletModel());
        this.shadowRadius = 0.25F;
    }
}
