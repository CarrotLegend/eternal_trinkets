package com.carrot123.eternal_trinkets.client;

import com.carrot123.eternal_trinkets.EternalTrinkets;
import com.carrot123.eternal_trinkets.item.ModRarity;

import net.minecraft.Util;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;


/**
 * Rarity controls ONLY the existing background and decorative border.
 * Name effects are parsed independently by the shared special-text pipeline.
 */
@Mod.EventBusSubscriber(modid = EternalTrinkets.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class YinYangRenderHandler {

    private static final int BACKGROUND_START = 0xA0202020;
    private static final int BACKGROUND_END = 0xA0303030;

    private static final long ANIMATION_PERIOD_NANOS = 2_000_000_000L;
    private static final double TWO_PI = Math.PI * 2.0;

    private YinYangRenderHandler() {
    }

    @SubscribeEvent
    public static void onTooltipPre(RenderTooltipEvent.Pre event) {
        YinYangBorderRenderer.clearPrepared();
        if (isYinYang(event.getItemStack())) {
            YinYangBorderRenderer.adjustTooltipPosition(event);
        }
    }

    @SubscribeEvent
    public static void onTooltipColor(RenderTooltipEvent.Color event) {
        ItemStack stack = event.getItemStack();
        if (!isYinYang(stack)) {
            YinYangBorderRenderer.clearPrepared();
            return;
        }

        event.setBackgroundStart(BACKGROUND_START);
        event.setBackgroundEnd(BACKGROUND_END);

        double phase = animationPhase();
        int gray = (int) Math.round(((Math.sin(phase) + 1.0) * 0.5) * 255.0);
        int border = 0xFF000000 | gray << 16 | gray << 8 | gray;
        event.setBorderStart(border);
        event.setBorderEnd(border);
        YinYangBorderRenderer.prepare(event);
    }

    private static boolean isYinYang(ItemStack stack) {
        return !stack.isEmpty() && stack.getRarity() == ModRarity.YIN_YANG;
    }

    private static double animationPhase() {
        long elapsed = Math.floorMod(Util.getNanos(), ANIMATION_PERIOD_NANOS);
        return elapsed * TWO_PI / ANIMATION_PERIOD_NANOS;
    }

}
