package com.carrot123.eternal_trinkets.mixin;

import com.carrot123.eternal_trinkets.client.text.SpecialTextRenderer;
import com.carrot123.eternal_trinkets.text.SpecialText;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Core's Font entrypoints, extended to dispatch both registered span effects. */
@Mixin(Font.class)
public abstract class FontSpecialTextMixin {
    @Inject(method = "drawInBatch(Lnet/minecraft/util/FormattedCharSequence;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/gui/Font$DisplayMode;II)I",
            at = @At("HEAD"), cancellable = true)
    private void eternalTrinkets$draw(FormattedCharSequence text, float x, float y, int color, boolean shadow,
                                      Matrix4f matrix, MultiBufferSource buffer, Font.DisplayMode mode,
                                      int background, int light, CallbackInfoReturnable<Integer> cir) {
        if (SpecialTextRenderer.isDrawing()) return;
        SpecialText.Parsed parsed = SpecialText.parse(text);
        if (parsed.handled()) cir.setReturnValue(SpecialTextRenderer.draw((Font) (Object) this, parsed,
                x, y, color, shadow, matrix, buffer, mode, background, light, null));
    }

    @Inject(method = "drawInBatch(Ljava/lang/String;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/gui/Font$DisplayMode;IIZ)I",
            at = @At("HEAD"), cancellable = true)
    private void eternalTrinkets$string(String text, float x, float y, int color, boolean shadow,
                                        Matrix4f matrix, MultiBufferSource buffer, Font.DisplayMode mode,
                                        int background, int light, boolean bidi, CallbackInfoReturnable<Integer> cir) {
        if (SpecialTextRenderer.isDrawing()) return;
        if (SpecialText.containsMarker(text)) {
            Font font = (Font) (Object) this;
            FormattedCharSequence visual = bidi ? Component.literal(text).getVisualOrderText() : SpecialText.parse(text).sequence();
            cir.setReturnValue(font.drawInBatch(visual, x, y, color, shadow, matrix, buffer, mode, background, light));
        }
    }

    @Inject(method = "drawInBatch8xOutline", at = @At("HEAD"), cancellable = true)
    private void eternalTrinkets$outline(FormattedCharSequence text, float x, float y, int color, int outline,
                                         Matrix4f matrix, MultiBufferSource buffer, int light, CallbackInfo ci) {
        if (SpecialTextRenderer.isDrawing()) return;
        SpecialText.Parsed parsed = SpecialText.parse(text);
        if (!parsed.handled()) return;
        SpecialTextRenderer.draw((Font) (Object) this, parsed, x, y, color, false, matrix, buffer,
                Font.DisplayMode.NORMAL, 0, light, outline);
        ci.cancel();
    }

    @ModifyVariable(method = "getFontSet", at = @At("HEAD"), argsOnly = true)
    private ResourceLocation eternalTrinkets$originalFont(ResourceLocation font) {
        return SpecialText.originalFont(font);
    }
}
