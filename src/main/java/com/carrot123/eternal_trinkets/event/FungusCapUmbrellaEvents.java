package com.carrot123.eternal_trinkets.event;

import com.carrot123.eternal_trinkets.item.ModItems;
import com.carrot123.eternal_trinkets.misc.ModConfig;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class FungusCapUmbrellaEvents {

    /** 玩家主手是否持有菌盖伞。 */
    private static boolean isUmbrellaInMainHand(Player player) {
        return player.getMainHandItem().is(ModItems.FUNGUS_CAP_UMBRELLA.get());
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Player player = event.player;
        if (!isUmbrellaInMainHand(player)) {
            return;
        }
        if (player.getAbilities().flying || player.isFallFlying()
                || player.isInWater() || player.isInLava() || player.onGround()) {
            return;
        }

        Vec3 motion = player.getDeltaMovement();
        if (motion.y < 0.0) {
            double accel = ModConfig.FUNGUS_CAP_UMBRELLA_FALL_ACCELERATION.get(); 
            double maxFall = ModConfig.FUNGUS_CAP_UMBRELLA_MAX_FALL_SPEED.get();  
            double newY = motion.y + (0.08 - accel);
            if (newY > 0.0) {
                newY = 0.0; 
            }
            if (newY < -maxFall) {
                newY = -maxFall;
            }
            player.setDeltaMovement(motion.x, newY, motion.z);
            player.fallDistance = 0.0F; 
            player.hasImpulse = true;
        }
    }

    //免疫摔落伤害
    @SubscribeEvent
    public void onLivingFall(LivingFallEvent event) {
        if (event.getEntity() instanceof Player player && isUmbrellaInMainHand(player)) {
            event.setCanceled(true);
        }
    }

    //反弹来自上方的弹射物
    @SubscribeEvent
    public void onProjectileImpact(ProjectileImpactEvent event) {
        if (!(event.getRayTraceResult() instanceof EntityHitResult hit)) {
            return;
        }
        if (!(hit.getEntity() instanceof Player player) || !isUmbrellaInMainHand(player)) {
            return;
        }
        Projectile projectile = event.getProjectile();
        Vec3 velocity = projectile.getDeltaMovement();

        if (velocity.y >= 0.0) {
            return;
        }

        double damp = 0.6;
        Vec3 bounced = new Vec3(velocity.x * damp, -velocity.y * damp, velocity.z * damp);
        projectile.setDeltaMovement(bounced);
        projectile.hasImpulse = true;

        double horizontal = Math.sqrt(bounced.x * bounced.x + bounced.z * bounced.z);
        projectile.setYRot((float) (Mth.atan2(bounced.x, bounced.z) * (180.0 / Math.PI)));
        projectile.setXRot((float) (Mth.atan2(bounced.y, horizontal) * (180.0 / Math.PI)));
        projectile.yRotO = projectile.getYRot();
        projectile.xRotO = projectile.getXRot();

        event.setCanceled(true);
    }
}
