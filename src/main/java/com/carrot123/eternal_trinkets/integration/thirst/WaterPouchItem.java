package com.carrot123.eternal_trinkets.integration.thirst;

import java.util.List;

import javax.annotation.Nullable;

import dev.ghen.thirst.foundation.common.capability.ModCapabilities;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class WaterPouchItem extends Item {

    private static final int THIRST_PER_SIP = 4;
    private static final int QUENCHED_PER_SIP = 2;
    private static final int BAR_COLOR = 0x3F76E4; // 水蓝
    private static final String TAG_WATER = "Water";

    private final int maxUses;

    public WaterPouchItem(int maxUses) {
        super(new Item.Properties().stacksTo(1));
        this.maxUses = maxUses;
    }

    private int getWater(ItemStack stack) {
        return stack.getOrCreateTag().getInt(TAG_WATER);
    }

    private void setWater(ItemStack stack, int value) {
        stack.getOrCreateTag().putInt(TAG_WATER, Mth.clamp(value, 0, maxUses));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        int water = getWater(stack);

        if (water < maxUses) {
            BlockHitResult blockHit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);
            if (blockHit.getType() == HitResult.Type.BLOCK) {
                BlockState state = level.getBlockState(blockHit.getBlockPos());
                if (state.is(Blocks.WATER_CAULDRON)) {
                    if (!level.isClientSide) {
                        int fillLevel = state.getValue(LayeredCauldronBlock.LEVEL);
                        if (fillLevel <= 1) {
                            level.setBlockAndUpdate(blockHit.getBlockPos(), Blocks.CAULDRON.defaultBlockState());
                        } else {
                            level.setBlockAndUpdate(blockHit.getBlockPos(),
                                    state.setValue(LayeredCauldronBlock.LEVEL, fillLevel - 1));
                        }
                        setWater(stack, water + 1);
                        playFillSound(level, player);
                    }
                    return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
                }
            }

            BlockHitResult srcHit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
            if (srcHit.getType() == HitResult.Type.BLOCK) {
                FluidState fluid = level.getFluidState(srcHit.getBlockPos());
                if (fluid.is(FluidTags.WATER) && fluid.isSource()) {
                    if (!level.isClientSide) {
                        setWater(stack, water + 1); 
                        playFillSound(level, player);
                    }
                    return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
                }
            }
        }

        if (water > 0) {
            return ItemUtils.startUsingInstantly(level, player, hand);
        }
        return InteractionResultHolder.pass(stack);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (entity instanceof Player player && !level.isClientSide) {
            int water = getWater(stack);
            if (water > 0) {
                setWater(stack, water - 1);
                player.getCapability(ModCapabilities.PLAYER_THIRST).ifPresent(cap ->
                        cap.drink(player, THIRST_PER_SIP, QUENCHED_PER_SIP));
            }
        }
        return stack;
    }

    private static void playFillSound(Level level, Player player) {
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BOTTLE_FILL, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 32;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(getWater(stack) * 13.0F / this.maxUses);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return BAR_COLOR;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                 List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.eternal_trinkets.water_pouch.stored",
                getWater(stack), this.maxUses).withStyle(ChatFormatting.AQUA));
    }
}
