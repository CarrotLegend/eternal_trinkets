package com.carrot123.eternal_trinkets.item;

import java.util.List;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

public class FungusCapUmbrellaItem extends Item implements GeoItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public FungusCapUmbrellaItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private com.carrot123.eternal_trinkets.client.item.FungusCapUmbrellaRenderer renderer;
            private final HumanoidModel.ArmPose umbrellaPose =
                    HumanoidModel.ArmPose.create(
                            "eternal_trinkets_umbrella",
                            false,
                            (model, entity, arm) -> {
                                ModelPart armPart = arm == HumanoidArm.RIGHT
                                        ? model.rightArm
                                        : model.leftArm;
                                armPart.xRot = (float) Math.toRadians(-140);
                                armPart.yRot = 0.0F;
                                armPart.zRot = arm == HumanoidArm.RIGHT
                                        ? -0.35F
                                        : 0.35F;
                            });

            @Override
            public net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null) {
                    this.renderer = new com.carrot123.eternal_trinkets.client.item.FungusCapUmbrellaRenderer();
                }
                return this.renderer;
            }

            @Override
            public HumanoidModel.ArmPose getArmPose(LivingEntity entity, net.minecraft.world.InteractionHand hand, ItemStack stack) {
                return this.umbrellaPose;
            }
        });
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                 List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.eternal_trinkets.fungus_cap_umbrella.deflect"));
        tooltip.add(Component.translatable("tooltip.eternal_trinkets.fungus_cap_umbrella.safe_landing"));
    }
}
