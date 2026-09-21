package com.carrot123.eternal_trinkets.entity.client;

import com.carrot123.eternal_trinkets.EternalTrinkets;
import com.carrot123.eternal_trinkets.entity.neutral.WarpedFungusSprite;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class WarpedFungusSpriteRenderer extends GeoEntityRenderer<WarpedFungusSprite> {

    private static final ResourceLocation GLOW_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(EternalTrinkets.MODID, "textures/entity/warped_fungus_sprite_glow.png");

    public WarpedFungusSpriteRenderer(net.minecraft.client.renderer.entity.EntityRendererProvider.Context context) {
        super(context, new WarpedFungusSpriteModel());
        this.shadowRadius = 0.5F;
        this.addRenderLayer(new GlowLayer(this));
    }

    private static class GlowLayer extends GeoRenderLayer<WarpedFungusSprite> {

        public GlowLayer(GeoEntityRenderer<WarpedFungusSprite> renderer) {
            super(renderer);
        }

        @Override
        @SuppressWarnings("null")
        public void render(PoseStack poseStack, WarpedFungusSprite animatable, BakedGeoModel bakedModel,
                           @Nullable RenderType renderType, MultiBufferSource bufferSource,
                           @Nullable VertexConsumer buffer, float partialTick,
                           int packedLight, int packedOverlay) {
            RenderType emissiveType = RenderType.eyes(GLOW_TEXTURE);
            this.getRenderer().reRender(
                    this.getRenderer().getGeoModel().getBakedModel(
                            this.getRenderer().getGeoModel().getModelResource(animatable)),
                    poseStack, bufferSource, animatable, emissiveType,
                    bufferSource.getBuffer(emissiveType),
                    partialTick,
                    0xF00000,
                    OverlayTexture.NO_OVERLAY,
                    1.0F, 1.0F, 1.0F, 1.0F
            );
        }
    }
}
