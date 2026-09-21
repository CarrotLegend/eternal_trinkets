package com.carrot123.eternal_trinkets.client.item;

import com.carrot123.eternal_trinkets.EternalTrinkets;
import com.carrot123.eternal_trinkets.item.FungusCapUmbrellaItem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.renderer.GeoItemRenderer;

@SuppressWarnings("null")
public class FungusCapUmbrellaRenderer extends GeoItemRenderer<FungusCapUmbrellaItem> {

    public static final ResourceLocation GUI_MODEL =
            ResourceLocation.fromNamespaceAndPath(EternalTrinkets.MODID, "item/fungus_cap_umbrella_gui");

    public FungusCapUmbrellaRenderer() {
        super(new FungusCapUmbrellaModel());
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack poseStack,
                             MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (context == ItemDisplayContext.GUI
                || context == ItemDisplayContext.FIXED
                || context == ItemDisplayContext.GROUND) {
            Minecraft mc = Minecraft.getInstance();
            BakedModel guiModel = mc.getModelManager().getModel(GUI_MODEL);
            poseStack.pushPose();
            poseStack.translate(0.5F, 0.5F, 0.5F);
            mc.getItemRenderer().render(stack, context, false, poseStack, buffer,
                    packedLight, packedOverlay, guiModel);
            poseStack.popPose();
        } else {
            super.renderByItem(stack, context, poseStack, buffer, packedLight, packedOverlay);
        }
    }

    @Override
    public RenderType getRenderType(FungusCapUmbrellaItem animatable, ResourceLocation texture,
                                    MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucent(texture);
    }
}
