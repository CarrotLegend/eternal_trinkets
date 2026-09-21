package com.carrot123.eternal_trinkets.entity.vehicle;

import com.carrot123.eternal_trinkets.item.ModItems;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WaterlilyBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class WarpedFungusCapBoat extends Boat {

    public WarpedFungusCapBoat(EntityType<? extends Boat> type, Level level) {
        super(type, level);
        this.blocksBuilding = true;
    }

    public WarpedFungusCapBoat(Level level, double x, double y, double z) {
        this(com.carrot123.eternal_trinkets.entity.ModEntities.WARPED_FUNGUS_CAP_BOAT.get(), level);
        this.setPos(x, y, z);
        this.xo = x;
        this.yo = y;
        this.zo = z;
    }

    @Override
    public boolean fireImmune() { return true; }

    @Override
    public Item getDropItem() { return ModItems.WARPED_FUNGUS_CAP_BOAT.get(); }

    @Override
    public boolean canBoatInFluid(FluidState state) {
        return state.supportsBoating(this) || state.is(FluidTags.LAVA);
    }

    @Override
    public boolean shouldUpdateFluidWhileRiding(FluidState state, Entity rider) {
        return !state.is(FluidTags.LAVA);
    }

    @Override
    public float getBlockSpeedFactor() {
        if (isOverSoulSand()) return 1.0F;
        return super.getBlockSpeedFactor();
    }

    @Override
    public float getGroundFriction() {
        AABB aabb = this.getBoundingBox();
        AABB aabb1 = new AABB(aabb.minX, aabb.minY - 0.001, aabb.minZ, aabb.maxX, aabb.minY, aabb.maxZ);
        int i = Mth.floor(aabb1.minX) - 1;
        int j = Mth.ceil(aabb1.maxX) + 1;
        int k = Mth.floor(aabb1.minY) - 1;
        int l = Mth.ceil(aabb1.maxY) + 1;
        int i1 = Mth.floor(aabb1.minZ) - 1;
        int j1 = Mth.ceil(aabb1.maxZ) + 1;
        VoxelShape voxelshape = Shapes.create(aabb1);
        float f = 0.0F;
        int k1 = 0;
        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

        for (int l1 = i; l1 < j; l1++) {
            for (int i2 = i1; i2 < j1; i2++) {
                int j2 = (l1 != i && l1 != j - 1 ? 0 : 1) + (i2 != i1 && i2 != j1 - 1 ? 0 : 1);
                if (j2 != 2) {
                    for (int k2 = k; k2 < l; k2++) {
                        if (j2 <= 0 || k2 != k && k2 != l - 1) {
                            blockpos$mutableblockpos.set(l1, k2, i2);
                            BlockState blockstate = this.level().getBlockState(blockpos$mutableblockpos);
                            if (!(blockstate.getBlock() instanceof WaterlilyBlock)
                                    && Shapes.joinIsNotEmpty(
                                            blockstate.getCollisionShape(this.level(), blockpos$mutableblockpos).move(l1, k2, i2),
                                            voxelshape, BooleanOp.AND)) {
                                if (blockstate.is(Blocks.SOUL_SAND)) {
                                    f += Blocks.ICE.getFriction();
                                } else {
                                    f += blockstate.getBlock().getFriction();
                                }
                                k1++;
                            }
                        }
                    }
                }
            }
        }

        return f / k1;
    }

    private boolean isOverSoulSand() {
        var box = this.getBoundingBox();
        int minX = Mth.floor(box.minX);
        int maxX = Mth.ceil(box.maxX);
        int minZ = Mth.floor(box.minZ);
        int maxZ = Mth.ceil(box.maxZ);
        int y = Mth.floor(box.minY - 0.01);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = minX; x < maxX; x++) {
            for (int z = minZ; z < maxZ; z++) {
                pos.set(x, y, z);
                if (this.level().getBlockState(pos).is(Blocks.SOUL_SAND)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected void removePassenger(Entity passenger) {
        super.removePassenger(passenger);
        if (!this.level().isClientSide && passenger instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 100, 0, false, false));
        }
    }

    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity rider) {
        return new Vec3(this.getX(), this.getBoundingBox().maxY, this.getZ());
    }
}
