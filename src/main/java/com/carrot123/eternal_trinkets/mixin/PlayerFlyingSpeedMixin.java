package com.carrot123.eternal_trinkets.mixin;

import com.carrot123.eternal_trinkets.event.AngelBlazeGuardEvents;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerFlyingSpeedMixin {
    @Inject(method = "getFlyingSpeed", at = @At("RETURN"), cancellable = true)
    private void eternalTrinkets$flightSpeed(CallbackInfoReturnable<Float> cir) {
        Player player = (Player) (Object) this;
        if (player.getAbilities().flying && AngelBlazeGuardEvents.active(player)) {
            cir.setReturnValue(cir.getReturnValue() * 1.5F);
        }
    }
}
