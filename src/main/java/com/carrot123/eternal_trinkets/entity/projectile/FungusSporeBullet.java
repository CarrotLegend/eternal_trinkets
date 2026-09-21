package com.carrot123.eternal_trinkets.entity.projectile;

import com.carrot123.eternal_trinkets.entity.ModEntities;
import com.carrot123.eternal_trinkets.misc.ModConfig;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

public class FungusSporeBullet extends ThrowableProjectile implements GeoEntity {

    private static final double SPEED = 1.0;
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public FungusSporeBullet(EntityType<? extends FungusSporeBullet> type, Level level) {
        super(type, level);
    }

    public FungusSporeBullet(Level level, LivingEntity shooter) {
        super(ModEntities.FUNGUS_SPORE_BULLET.get(), shooter, level);
    }

    @Override
    public void shootFromRotation(Entity shooter, float xRot, float yRot, float roll, float speed, float inaccuracy) {
        super.shootFromRotation(shooter, xRot, yRot, roll, (float) SPEED, inaccuracy);
    }

    @Override
    protected float getGravity() {
        return 0.05F;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            Vec3 delta = this.getDeltaMovement();
            for (int i = 0; i < 2; i++) {
                this.level().addParticle(ParticleTypes.MYCELIUM,
                        this.getX() + delta.x * i * 0.15,
                        this.getY() + delta.y * i * 0.15,
                        this.getZ() + delta.z * i * 0.15,
                        -delta.x * 0.1, -delta.y * 0.1, -delta.z * 0.1);
            }
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        Entity hit = result.getEntity();
        Entity owner = this.getOwner();
        float damage = ModConfig.WARPED_FUNGUS_UMBRELLA_SPORE_DAMAGE.get().floatValue();
        DamageSource source = this.damageSources().thrown(this, owner);
        hit.hurt(source, damage);
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level().isClientSide) {
            this.discard();
        }
    }

    @Override
    protected void defineSynchedData() {
    }

    // ── GeoEntity ──────────────────────────────────────────────

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
