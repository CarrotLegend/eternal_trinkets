package com.carrot123.eternal_trinkets.entity.neutral;

import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

import com.carrot123.eternal_trinkets.entity.projectile.FungusSporeBullet;
import com.carrot123.eternal_trinkets.misc.ModConfig;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.ResetUniversalAngerTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class WarpedFungusUmbrella extends Monster implements NeutralMob, GeoEntity {
    private static final EntityDataAccessor<Boolean> DATA_IS_ANGRY =
            SynchedEntityData.defineId(WarpedFungusUmbrella.class, EntityDataSerializers.BOOLEAN);

    private static final UniformInt PERSISTENT_ANGER_TIME = TimeUtil.rangeOfSeconds(20, 39);
    private int remainingPersistentAngerTime;
    @Nullable
    private UUID persistentAngerTarget;
    private static final int ALERT_RANGE_Y = 10;
    private static final UniformInt ALERT_INTERVAL = TimeUtil.rangeOfSeconds(4, 6);
    private int ticksUntilNextAlert;

    private int shootCooldown;
    private static final int SHOOT_COOLDOWN_TICKS = 60;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("animation.warped_fungus_umbrella.idle");
    private static final RawAnimation ATTACK_ANIM = RawAnimation.begin().thenPlay("animation.warped_fungus_umbrella.attack");

    public WarpedFungusUmbrella(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.applyConfiguredAttributes();
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new ShootSporeGoal(this));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 16.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class,
                10, true, false, this::isAngryAt));
        this.targetSelector.addGoal(3, new ResetUniversalAngerTargetGoal<>(this, true));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, ModConfig.WARPED_FUNGUS_UMBRELLA_MAX_HEALTH.getDefault())
                .add(Attributes.MOVEMENT_SPEED, 0.0)
                .add(Attributes.FOLLOW_RANGE, 16.0)
                .add(Attributes.ARMOR, ModConfig.WARPED_FUNGUS_UMBRELLA_ARMOR.getDefault())
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
    }

    private void applyConfiguredAttributes() {
        this.getAttribute(Attributes.MAX_HEALTH)
                .setBaseValue(ModConfig.WARPED_FUNGUS_UMBRELLA_MAX_HEALTH.get());
        this.getAttribute(Attributes.ARMOR)
                .setBaseValue(ModConfig.WARPED_FUNGUS_UMBRELLA_ARMOR.get());
        this.setHealth(this.getMaxHealth());
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_IS_ANGRY, false);
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void pushEntities() {
    }

    @Override
    public boolean canBeLeashed(Player player) {
        return false;
    }

    @Override
    public void playerTouch(Player player) {
        if (!this.level().isClientSide) {
            float damage = ModConfig.WARPED_FUNGUS_UMBRELLA_COLLISION_DAMAGE.get().floatValue();
            if (damage > 0 && this.isAngry()) {
                player.hurt(this.damageSources().mobAttack(this), damage);
            }
        }
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    public int getRemainingPersistentAngerTime() {
        return this.remainingPersistentAngerTime;
    }

    @Override
    public void setRemainingPersistentAngerTime(int time) {
        this.remainingPersistentAngerTime = time;
    }

    @Nullable
    @Override
    public UUID getPersistentAngerTarget() {
        return this.persistentAngerTarget;
    }

    @Override
    public void setPersistentAngerTarget(@Nullable UUID target) {
        this.persistentAngerTarget = target;
    }

    @Override
    public void startPersistentAngerTimer() {
        this.setRemainingPersistentAngerTime(PERSISTENT_ANGER_TIME.sample(this.random));
    }

    public boolean isAngry() {
        return this.entityData.get(DATA_IS_ANGRY);
    }

    public void setAngry(boolean angry) {
        this.entityData.set(DATA_IS_ANGRY, angry);
    }

    @Override
    protected void customServerAiStep() {
        this.updatePersistentAnger((ServerLevel) this.level(), true);
        if (this.getTarget() != null) {
            this.maybeAlertOthers();
        }
        if (this.isAngry()) {
            this.lastHurtByPlayerTime = this.tickCount;
        }
        if (this.shootCooldown > 0) {
            this.shootCooldown--;
        }
        super.customServerAiStep();
    }

    private void maybeAlertOthers() {
        if (this.ticksUntilNextAlert > 0) {
            this.ticksUntilNextAlert--;
        } else {
            if (this.getSensing().hasLineOfSight(this.getTarget())) {
                this.alertOthers();
            }
            this.ticksUntilNextAlert = ALERT_INTERVAL.sample(this.random);
        }
    }

    private void alertOthers() {
        double followRange = this.getAttributeValue(Attributes.FOLLOW_RANGE);
        AABB box = AABB.unitCubeFromLowerCorner(this.position())
                .inflate(followRange, ALERT_RANGE_Y, followRange);
        this.level()
                .getEntitiesOfClass(WarpedFungusUmbrella.class, box, EntitySelector.NO_SPECTATORS)
                .stream()
                .filter(u -> u != this)
                .filter(u -> u.getTarget() == null)
                .filter(u -> !u.isAlliedTo(this.getTarget()))
                .forEach(u -> u.setTarget(this.getTarget()));
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        if (this.getTarget() == null && target != null) {
            this.ticksUntilNextAlert = ALERT_INTERVAL.sample(this.random);
        }
        if (target instanceof Player) {
            this.setLastHurtByPlayer((Player) target);
        }
        super.setTarget(target);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        this.addPersistentAngerSaveData(tag);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.readPersistentAngerSaveData(this.level(), tag);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return null;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return null;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 5, this::predicate)
                .triggerableAnim("attack", ATTACK_ANIM));
    }

    private <T extends GeoAnimatable> PlayState predicate(
            software.bernie.geckolib.core.animation.AnimationState<T> state) {
        state.getController().setAnimation(IDLE_ANIM);
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public boolean isPreventingPlayerRest(Player player) {
        return this.isAngryAt(player);
    }

    static class ShootSporeGoal extends Goal {
        private final WarpedFungusUmbrella umbrella;
        private int attackDelayTicks;
        @Nullable
        private LivingEntity shootTarget; // 在 start() 时捕获目标，延迟后使用

        ShootSporeGoal(WarpedFungusUmbrella umbrella) {
            this.umbrella = umbrella;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return umbrella.getTarget() != null && umbrella.shootCooldown <= 0;
        }

        @Override
        public boolean canContinueToUse() {
            return this.attackDelayTicks > 0 && this.shootTarget != null;
        }

        @Override
        public void start() {
            umbrella.shootCooldown = SHOOT_COOLDOWN_TICKS;
            umbrella.swing(InteractionHand.MAIN_HAND);
            umbrella.triggerAnim("controller", "attack");
            umbrella.playSound(SoundEvents.SHULKER_SHOOT, 1.0F, 1.0F);
            this.shootTarget = umbrella.getTarget(); // 捕获目标
            this.attackDelayTicks = 10;
        }

        @Override
        public void tick() {
            if (this.attackDelayTicks > 0) {
                this.attackDelayTicks--;
                if (this.attackDelayTicks <= 0) {
                    performShoot();
                }
            }
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        private void performShoot() {
            if (this.shootTarget == null) return;

            FungusSporeBullet spore = new FungusSporeBullet(umbrella.level(), umbrella);
            double dx = shootTarget.getX() - umbrella.getX();
            double dy = shootTarget.getEyeY() - umbrella.getEyeY();
            double dz = shootTarget.getZ() - umbrella.getZ();
            spore.shoot(dx, dy, dz, 2.5F, 1.0F);
            umbrella.level().addFreshEntity(spore);
            this.shootTarget = null;
        }
    }

    // ── Static helpers ──────────────────────────────────────────

    public static void angerNearbyUmbrellas(Player player, Level level, BlockPos centerPos) {
        AABB box = new AABB(centerPos).inflate(16.0);
        List<WarpedFungusUmbrella> umbrellas = level.getEntitiesOfClass(WarpedFungusUmbrella.class, box);
        umbrellas.forEach(u -> {
            u.setAngry(true);
            u.setPersistentAngerTarget(player.getUUID());
            u.setRemainingPersistentAngerTime(PERSISTENT_ANGER_TIME.sample(u.random));
            u.setTarget(player);
        });
    }
}
