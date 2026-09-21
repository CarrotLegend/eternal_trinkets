package com.carrot123.eternal_trinkets.client.curio;

import com.carrot123.eternal_trinkets.EternalTrinkets;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

@SuppressWarnings("null")
public class GrumpyPufferfishCurioModel extends Model {

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(EternalTrinkets.MODID, "grumpy_pufferfish_curio"), "main");

    private final ModelPart bb_main;

    public GrumpyPufferfishCurioModel(ModelPart root) {
        super(RenderType::entityCutoutNoCull);
        this.bb_main = root.getChild("bb_main");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition bb_main = partdefinition.addOrReplaceChild("bb_main", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-2.0F, -4.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(5, 10).addBox(-2.5F, -2.0F, -2.5F, 5.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(5, 11).addBox(-2.5F, -2.0F, 1.5F, 5.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        bb_main.addOrReplaceChild("spike6_r1", CubeListBuilder.create()
                .texOffs(5, 9).addBox(-2.5F, 0.0F, -0.5F, 5.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, -4.0F, 2.0F, -2.3562F, 0.0F, 0.0F));

        bb_main.addOrReplaceChild("spike7_r1", CubeListBuilder.create()
                .texOffs(5, 12).addBox(-2.5F, 0.0F, -0.5F, 5.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 0.0F, 2.0F, -0.7854F, 0.0F, 0.0F));

        bb_main.addOrReplaceChild("spike5_r1", CubeListBuilder.create()
                .texOffs(5, 8).addBox(-2.5F, 0.0F, -0.5F, 5.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, -4.0F, -2.0F, -0.7854F, 0.0F, 0.0F));

        bb_main.addOrReplaceChild("spike2_r1", CubeListBuilder.create()
                .texOffs(-5, 8).addBox(-1.0F, 0.0F, -2.5F, 1.0F, 0.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(-3, 8).addBox(3.0F, 0.0F, -2.5F, 1.0F, 0.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-1.5F, -2.0F, 0.0F, 1.5708F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 16, 16);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer,
                               int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
        bb_main.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
