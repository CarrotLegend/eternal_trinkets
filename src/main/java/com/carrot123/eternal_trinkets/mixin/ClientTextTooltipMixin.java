package com.carrot123.eternal_trinkets.mixin;

import com.carrot123.eternal_trinkets.client.YinYangBorderRenderer;
import com.carrot123.eternal_trinkets.client.text.SpecialTextLayout;
import com.carrot123.eternal_trinkets.text.SpecialText;
import com.carrot123.eternal_trinkets.text.SpecialTextFormat;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTextTooltip;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.FormattedCharSequence;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientTextTooltip.class)
public abstract class ClientTextTooltipMixin {
    @Shadow @Final private FormattedCharSequence text;

    @Inject(method = "getHeight", at = @At("HEAD"), cancellable = true)
    private void eternalTrinkets$height(CallbackInfoReturnable<Integer> cir) {
        if (SpecialText.parse(text).has(SpecialTextFormat.YINYANG)) cir.setReturnValue(SpecialTextLayout.LINE_HEIGHT);
    }

    @ModifyVariable(method = "renderText", at = @At("HEAD"), argsOnly = true, ordinal = 1)
    private int eternalTrinkets$topPadding(int y) {
        return y + (SpecialText.parse(text).has(SpecialTextFormat.YINYANG) ? SpecialTextLayout.VERTICAL_PADDING : 0);
    }

    @Inject(method = "renderText", at = @At("HEAD"))
    private void eternalTrinkets$border(Font font, int x, int y, Matrix4f matrix,
                                        MultiBufferSource.BufferSource buffer, CallbackInfo ci) {
        // Prepared exclusively by the rarity border handler; consumed once at
        // precisely the former title-component hook (after background, before text).
        YinYangBorderRenderer.renderPrepared();
    }
}
