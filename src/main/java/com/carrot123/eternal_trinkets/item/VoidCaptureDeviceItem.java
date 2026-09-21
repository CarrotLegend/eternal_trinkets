package com.carrot123.eternal_trinkets.item;

import java.util.List;

import javax.annotation.Nullable;

import com.carrot123.eternal_trinkets.client.VoidCaptureDeviceClient;
import com.carrot123.eternal_trinkets.util.CapturedMatter;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;

public final class VoidCaptureDeviceItem extends Item {

    public VoidCaptureDeviceItem() {
        super(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        HitResult hitResult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);
        if (hitResult.getType() != HitResult.Type.MISS) {
            return InteractionResultHolder.pass(stack);
        }

        CapturedMatter matter = selectMatter(level, player.getY());
        if (matter == CapturedMatter.NONE) {
            if (!level.isClientSide) {
                player.displayClientMessage(
                        Component.translatable("message.eternal_trinkets.void_capture_device.nothing"),
                        true);
            }
            return InteractionResultHolder.fail(stack);
        }

        if (!level.isClientSide) {
            matter.writeTo(stack);
            player.displayClientMessage(
                    Component.translatable("message.eternal_trinkets.void_capture_device.captured",
                            Component.translatable(matter.translationKey())),
                    true);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        // Preserve the target block's interaction; use() independently verifies MISS.
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player,
                                                   LivingEntity target, InteractionHand hand) {
        // Consume only this item's entity interaction so it cannot fall through to air capture.
        return InteractionResult.sidedSuccess(player.level().isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.eternal_trinkets.void_capture_device.intangible")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.eternal_trinkets.void_capture_device.no_tangible")
                .withStyle(ChatFormatting.DARK_GRAY));

        if (isShiftDown()) {
            addCaptureRules(tooltip);
        } else {
            tooltip.add(Component.translatable("tooltip.eternal_trinkets.void_capture_device.shift")
                    .withStyle(ChatFormatting.DARK_GRAY));
        }

        CapturedMatter matter = CapturedMatter.fromStack(stack);
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("tooltip.eternal_trinkets.void_capture_device.captured",
                        Component.translatable(matter.translationKey()))
                .withStyle(ChatFormatting.AQUA));
    }

    private static void addCaptureRules(List<Component> tooltip) {
        tooltip.add(Component.translatable("tooltip.eternal_trinkets.void_capture_device.rule.overworld",
                        Component.translatable(CapturedMatter.AIR.translationKey()))
                .withStyle(ChatFormatting.BLUE));
        tooltip.add(Component.translatable("tooltip.eternal_trinkets.void_capture_device.rule.end",
                        Component.translatable(CapturedMatter.AETHER.translationKey()))
                .withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.translatable("tooltip.eternal_trinkets.void_capture_device.rule.pure_void",
                        Component.translatable(CapturedMatter.PURE_VOID.translationKey()))
                .withStyle(ChatFormatting.DARK_PURPLE));
        tooltip.add(Component.translatable("tooltip.eternal_trinkets.void_capture_device.rule.thin_void",
                        Component.translatable(CapturedMatter.THIN_VOID.translationKey()))
                .withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.translatable("tooltip.eternal_trinkets.void_capture_device.rule.nether",
                        Component.translatable(CapturedMatter.HELL_BREATH.translationKey()))
                .withStyle(ChatFormatting.RED));
    }

    private static boolean isShiftDown() {
        return FMLEnvironment.dist == Dist.CLIENT && VoidCaptureDeviceClient.hasShiftDown();
    }

    private static CapturedMatter selectMatter(Level level, double playerY) {
        if (level.dimension() == Level.END) {
            if (playerY < 0.0D) {
                return CapturedMatter.PURE_VOID;
            }
            return CapturedMatter.AETHER;
        }
        if (playerY < -60.0D) {
            return CapturedMatter.THIN_VOID;
        }
        if (level.dimension() == Level.NETHER) {
            return CapturedMatter.HELL_BREATH;
        }
        if (level.dimension() == Level.OVERWORLD) {
            return CapturedMatter.AIR;
        }
        return CapturedMatter.NONE;
    }
}
