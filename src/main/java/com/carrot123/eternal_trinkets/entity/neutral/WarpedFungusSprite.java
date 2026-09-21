package com.carrot123.eternal_trinkets.entity.neutral;

import java.util.List;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import com.carrot123.eternal_trinkets.misc.ModConfig;
import com.carrot123.eternal_trinkets.misc.ModSounds;
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
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
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

public class WarpedFungusSprite extends Monster implements NeutralMob, GeoEntity {
    private static final EntityDataAccessor<Boolean> DATA_IS_ANGRY =
            SynchedEntityData.defineId(WarpedFungusSprite.class, EntityDataSerializers.BOOLEAN);

    private static final UUID SPEED_MODIFIER_ATTACKING_UUID =
            UUID.fromString("49455A49-7EC5-45BA-B886-3B90B23A1719");
    private static final AttributeModifier SPEED_MODIFIER_ATTACKING =
            new AttributeModifier(SPEED_MODIFIER_ATTACKING_UUID, "Attacking speed boost",
                    0.05, AttributeModifier.Operation.ADDITION);

    private static final UniformInt FIRST_ANGER_SOUND_DELAY = TimeUtil.rangeOfSeconds(0, 1);
    private int playFirstAngerSoundIn;
    private static final UniformInt PERSISTENT_ANGER_TIME = TimeUtil.rangeOfSeconds(20, 39);
    private int remainingPersistentAngerTime;
    @Nullable
    private UUID persistentAngerTarget;
    private static final int ALERT_RANGE_Y = 10;
    private static final UniformInt ALERT_INTERVAL = TimeUtil.rangeOfSeconds(4, 6);
    private int ticksUntilNextAlert;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    // 延迟伤害系统
    private int delayedDamageTicks = 0;
    @Nullable
    private net.minecraft.world.entity.Entity pendingDamageTarget = null;

    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("animation.warped_fungus_sprite.idle");
    private static final RawAnimation WALK_ANIM = RawAnimation.begin().thenLoop("animation.warped_fungus_sprite.walk");
    private static final RawAnimation ATTACK_ANIM = RawAnimation.begin().thenPlay("animation.warped_fungus_sprite.attack");

    private static final SoundEvent[] AMBIENT_SOUNDS = {
            ModSounds.WARPED_FUNGUS_SPRITE_AMBIENT_1.get(),
            ModSounds.WARPED_FUNGUS_SPRITE_AMBIENT_2.get(),
            ModSounds.WARPED_FUNGUS_SPRITE_AMBIENT_3.get()
    };

    private static final SoundEvent[] STEP_SOUNDS = {
            ModSounds.WARPED_FUNGUS_SPRITE_STEP_1.get(),
            ModSounds.WARPED_FUNGUS_SPRITE_STEP_2.get(),
            ModSounds.WARPED_FUNGUS_SPRITE_STEP_3.get()
    };

    public WarpedFungusSprite(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.applyConfiguredAttributes();
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class,
                10, true, false, this::isAngryAt));
        this.targetSelector.addGoal(3, new ResetUniversalAngerTargetGoal<>(this, true));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, ModConfig.WARPED_FUNGUS_SPRITE_MAX_HEALTH.getDefault())
                .add(Attributes.MOVEMENT_SPEED, 0.2)
                .add(Attributes.ATTACK_DAMAGE, ModConfig.WARPED_FUNGUS_SPRITE_ATTACK_DAMAGE.getDefault())
                .add(Attributes.FOLLOW_RANGE, 35.0)
                .add(Attributes.ARMOR, ModConfig.WARPED_FUNGUS_SPRITE_ARMOR.getDefault());
    }

    private void applyConfiguredAttributes() {
        this.getAttribute(Attributes.MAX_HEALTH)
                .setBaseValue(ModConfig.WARPED_FUNGUS_SPRITE_MAX_HEALTH.get());
        this.getAttribute(Attributes.ATTACK_DAMAGE)
                .setBaseValue(ModConfig.WARPED_FUNGUS_SPRITE_ATTACK_DAMAGE.get());
        this.getAttribute(Attributes.ARMOR)
                .setBaseValue(ModConfig.WARPED_FUNGUS_SPRITE_ARMOR.get());
        this.setHealth(this.getMaxHealth());
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_IS_ANGRY, false);
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
        AttributeInstance movementSpeed = this.getAttribute(Attributes.MOVEMENT_SPEED);
        if (this.isAngry()) {
            if (!movementSpeed.hasModifier(SPEED_MODIFIER_ATTACKING)) {
                movementSpeed.addTransientModifier(SPEED_MODIFIER_ATTACKING);
            }
            this.maybePlayFirstAngerSound();
        } else if (movementSpeed.hasModifier(SPEED_MODIFIER_ATTACKING)) {
            movementSpeed.removeModifier(SPEED_MODIFIER_ATTACKING);
        }
        this.updatePersistentAnger((ServerLevel)this.level(), true);
        if (this.getTarget() != null) {
            this.maybeAlertOthers();
        }
        if (this.isAngry()) {
            this.lastHurtByPlayerTime = this.tickCount;
        }
        super.customServerAiStep();
    }

    private void maybePlayFirstAngerSound() {
        if (this.playFirstAngerSoundIn > 0) {
            this.playFirstAngerSoundIn--;
            if (this.playFirstAngerSoundIn == 0) {
                this.playAngerSound();
            }
        }
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
                .getEntitiesOfClass(WarpedFungusSprite.class, box, EntitySelector.NO_SPECTATORS)
                .stream()
                .filter(sprite -> sprite != this)
                .filter(sprite -> sprite.getTarget() == null)
                .filter(sprite -> !sprite.isAlliedTo(this.getTarget()))
                .forEach(sprite -> sprite.setTarget(this.getTarget()));
    }

    private void playAngerSound() {
        this.playSound(ModSounds.WARPED_FUNGUS_SPRITE_ANGRY.get(),
                this.getSoundVolume() * 2.0F, this.getVoicePitch() * 1.8F);
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        if (this.getTarget() == null && target != null) {
            this.playFirstAngerSoundIn = FIRST_ANGER_SOUND_DELAY.sample(this.random);
            this.ticksUntilNextAlert = ALERT_INTERVAL.sample(this.random);
        }
        if (target instanceof Player player) {
            this.setLastHurtByPlayer(player);
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
        return AMBIENT_SOUNDS[this.random.nextInt(AMBIENT_SOUNDS.length)];
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.WARPED_FUNGUS_SPRITE_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.WARPED_FUNGUS_SPRITE_DIE.get();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(STEP_SOUNDS[this.random.nextInt(STEP_SOUNDS.length)], 0.15F, 1.0F);
    }

    @Override
    public boolean doHurtTarget(net.minecraft.world.entity.Entity target) {
        // 不立即造成伤害，而是在攻击动画播放 10 tick 后再造成伤害
        this.triggerAnim("controller", "attack");
        this.pendingDamageTarget = target;
        this.delayedDamageTicks = 10;
        return false; // 表示尚未造成伤害
    }

    @Override
    public void tick() {
        super.tick();
        // 处理延迟伤害
        if (!this.level().isClientSide && this.delayedDamageTicks > 0 && this.pendingDamageTarget != null) {
            this.delayedDamageTicks--;
            if (this.delayedDamageTicks <= 0) {
                this.doHurtTargetNow(this.pendingDamageTarget);
                this.pendingDamageTarget = null;
            }
        }
    }

    private void doHurtTargetNow(net.minecraft.world.entity.Entity target) {
        super.doHurtTarget(target);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 5, this::predicate)
                .triggerableAnim("attack", ATTACK_ANIM));
    }

    private <T extends GeoAnimatable> PlayState predicate(
            software.bernie.geckolib.core.animation.AnimationState<T> state) {
        // 攻击动画由 triggerAnim("controller","attack") 触发（见 doHurtTarget），
        // 触发中的动画会自动覆盖这里设置的 walk/idle，无需在此处理 swingTime。
        if (state.isMoving()) {
            state.getController().setAnimation(WALK_ANIM);
            return PlayState.CONTINUE;
        }
        state.getController().setAnimation(IDLE_ANIM);
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    public static void angerNearbySprites(Player player, Level level, BlockPos centerPos) {
        AABB box = new AABB(centerPos).inflate(16.0);
        List<WarpedFungusSprite> sprites = level.getEntitiesOfClass(WarpedFungusSprite.class, box);
        sprites.forEach(sprite -> {
            sprite.setAngry(true);
            setAngerTarget(sprite, player);
            sprite.setTarget(player);
        });
    }

    private static void setAngerTarget(WarpedFungusSprite sprite, LivingEntity target) {
        sprite.setAngry(true);
        sprite.setPersistentAngerTarget(target.getUUID());
        sprite.setRemainingPersistentAngerTime(PERSISTENT_ANGER_TIME.sample(sprite.random));
        sprite.setTarget(target);
    }

    @Override
    public boolean isPreventingPlayerRest(Player player) {
        return this.isAngryAt(player);
    }
}
