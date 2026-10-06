package com.carrot123.eternal_trinkets.event;

import com.carrot123.eternal_trinkets.item.curio.AngelBlazeGuardItem;
import com.carrot123.eternal_trinkets.item.curio.ModCurioItems;
import com.carrot123.eternal_trinkets.util.CuriosUtils;
import com.carrot123.eternal_trinkets.util.PlayerDamageAttribution;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class AngelBlazeGuardEvents {
    private static final String OWNED_TAG = "EternalTrinketsAngelFlightOwned";

    public static boolean active(Player player) {
        return CuriosUtils.hasFunctionalCurio(player, ModCurioItems.ANGEL_BLAZE_GUARD.get(),
                AngelBlazeGuardItem.SLOT);
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) {
            return;
        }
        boolean equipped = active(player);
        if (equipped) {
            if (player.isOnFire()) {
                player.clearFire();
            }
            if (!player.getAbilities().mayfly) {
                player.getAbilities().mayfly = true;
                ownedData(player).putBoolean(OWNED_TAG, true);
                player.onUpdateAbilities();
            }
        } else if (player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG)
                .getBoolean(OWNED_TAG)) {
            if (player.isCreative() || player.isSpectator()) {
                ownedData(player).remove(OWNED_TAG);
            } else {
                player.getAbilities().mayfly = false;
                player.getAbilities().flying = false;
                ownedData(player).remove(OWNED_TAG);
                player.onUpdateAbilities();
            }
        }
    }

    @SubscribeEvent
    public void onClone(PlayerEvent.Clone event) {
        CompoundTag previous = event.getOriginal().getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        if (previous.contains(OWNED_TAG)) {
            ownedData(event.getEntity()).putBoolean(OWNED_TAG, previous.getBoolean(OWNED_TAG));
        }
    }

    @SubscribeEvent
    public void onFireDamage(LivingAttackEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && active(player)
                && event.getSource().is(DamageTypeTags.IS_FIRE)) {
            player.clearFire();
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onFall(LivingFallEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && active(player)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onUndeadHurt(LivingHurtEvent event) {
        if (event.getEntity().level().isClientSide
                || event.getEntity().getMobType() != MobType.UNDEAD
                || event.getAmount() <= 0 || !Float.isFinite(event.getAmount())) {
            return;
        }
        ServerPlayer attacker = PlayerDamageAttribution.resolve(event.getSource());
        if (attacker != null && attacker != event.getEntity() && active(attacker)) {
            float amount = event.getAmount() * 1.25F;
            if (Float.isFinite(amount)) {
                event.setAmount(amount);
            }
        }
    }

    private static CompoundTag ownedData(Player player) {
        CompoundTag persistent = player.getPersistentData();
        if (!persistent.contains(Player.PERSISTED_NBT_TAG)) {
            persistent.put(Player.PERSISTED_NBT_TAG, new CompoundTag());
        }
        return persistent.getCompound(Player.PERSISTED_NBT_TAG);
    }
}
