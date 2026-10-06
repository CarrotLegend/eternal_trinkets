package com.carrot123.eternal_trinkets.client;

import com.carrot123.eternal_trinkets.EternalTrinkets;
import com.carrot123.eternal_trinkets.event.AngelBlazeGuardEvents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderBlockScreenEffectEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = EternalTrinkets.MODID, value = Dist.CLIENT)
public final class AngelBlazeGuardClientEvents {
    private AngelBlazeGuardClientEvents() {
    }

    @SubscribeEvent
    public static void onOverlay(RenderBlockScreenEffectEvent event) {
        if (event.getOverlayType() == RenderBlockScreenEffectEvent.OverlayType.FIRE
                && AngelBlazeGuardEvents.active(event.getPlayer())) {
            event.setCanceled(true);
        }
    }
}
