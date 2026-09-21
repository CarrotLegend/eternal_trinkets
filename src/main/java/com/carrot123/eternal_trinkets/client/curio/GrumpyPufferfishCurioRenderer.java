package com.carrot123.eternal_trinkets.client.curio;

import com.carrot123.eternal_trinkets.EternalTrinkets;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

@SuppressWarnings("null")
public class GrumpyPufferfishCurioRenderer implements ICurioRenderer {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(EternalTrinkets.MODID, "textures/entity/curio/grumpy_pufferfish.png");

    private final GrumpyPufferfishCurioModel model;

    public GrumpyPufferfishCurioRenderer() {
        this.model = new GrumpyPufferfishCurioModel(
                Minecraft.getInstance().getEntityModels().bakeLayer(GrumpyPufferfishCurioModel.LAYER));
    }

    @Override
    public <T extends LivingEntity, M extends EntityModel<T>> void render(
            ItemStack stack, SlotContext slotContext, PoseStack poseStack,
            RenderLayerParent<T, M> renderLayerParent, MultiBufferSource buffer, int light,
            float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks,
            float netHeadYaw, float headPitch) {

        poseStack.pushPose();

        if (renderLayerParent.getModel() instanceof HumanoidModel<?> humanoid) {
            humanoid.rightArm.translateAndRotate(poseStack);
        }

        poseStack.translate(-0.09D, 0.70D, 0.0D);
        poseStack.scale(1.3F, 1.3F, 1.3F);

        VertexConsumer vc = buffer.getBuffer(this.model.renderType(TEXTURE));
        this.model.renderToBuffer(poseStack, vc, light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);

        poseStack.popPose();
    }
}
