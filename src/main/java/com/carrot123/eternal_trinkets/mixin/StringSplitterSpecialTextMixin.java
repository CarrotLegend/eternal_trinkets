package com.carrot123.eternal_trinkets.mixin;

import com.carrot123.eternal_trinkets.client.text.SpecialTextLayout;
import com.carrot123.eternal_trinkets.text.SpecialText;
import net.minecraft.client.StringSplitter;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.BiConsumer;

@Mixin(StringSplitter.class)
public abstract class StringSplitterSpecialTextMixin {
    @Shadow @Final private StringSplitter.WidthProvider widthProvider;

    @Inject(method = "stringWidth(Lnet/minecraft/util/FormattedCharSequence;)F", at = @At("HEAD"), cancellable = true)
    private void eternalTrinkets$visualWidth(FormattedCharSequence text, CallbackInfoReturnable<Float> cir) {
        SpecialText.Parsed parsed = SpecialText.parse(text);
        if (parsed.handled()) cir.setReturnValue(SpecialTextLayout.width(parsed.glyphs(), widthProvider));
    }

    @Inject(method = "stringWidth(Lnet/minecraft/network/chat/FormattedText;)F", at = @At("HEAD"), cancellable = true)
    private void eternalTrinkets$componentWidth(FormattedText text, CallbackInfoReturnable<Float> cir) {
        SpecialText.Parsed parsed = SpecialText.parse(text);
        if (parsed.handled()) cir.setReturnValue(SpecialTextLayout.width(parsed.glyphs(), widthProvider));
    }

    @Inject(method = "stringWidth(Ljava/lang/String;)F", at = @At("HEAD"), cancellable = true)
    private void eternalTrinkets$stringWidth(String text, CallbackInfoReturnable<Float> cir) {
        if (SpecialText.containsMarker(text)) cir.setReturnValue(SpecialTextLayout.width(SpecialText.parse(text).glyphs(), widthProvider));
    }

    @Inject(method = "splitLines(Lnet/minecraft/network/chat/FormattedText;ILnet/minecraft/network/chat/Style;Ljava/util/function/BiConsumer;)V",
            at = @At("HEAD"), cancellable = true)
    private void eternalTrinkets$split(FormattedText text, int width, Style parent,
                                      BiConsumer<FormattedText, Boolean> consumer, CallbackInfo ci) {
        SpecialText.Parsed parsed = SpecialText.parse(text, parent);
        if (!parsed.handled()) return;
        SpecialTextLayout.split(parsed.glyphs(), width, widthProvider, consumer);
        ci.cancel();
    }

    @Inject(method = "headByWidth", at = @At("HEAD"), cancellable = true)
    private void eternalTrinkets$head(FormattedText text, int width, Style parent, CallbackInfoReturnable<FormattedText> cir) {
        SpecialText.Parsed parsed = SpecialText.parse(text, parent);
        if (parsed.handled()) cir.setReturnValue(SpecialTextLayout.head(parsed.glyphs(), width, widthProvider));
    }

    @Inject(method = "componentStyleAtWidth(Lnet/minecraft/util/FormattedCharSequence;I)Lnet/minecraft/network/chat/Style;",
            at = @At("HEAD"), cancellable = true)
    private void eternalTrinkets$visualHit(FormattedCharSequence text, int x, CallbackInfoReturnable<Style> cir) {
        SpecialText.Parsed parsed = SpecialText.parse(text);
        if (parsed.handled()) cir.setReturnValue(SpecialTextLayout.styleAt(parsed.glyphs(), x, widthProvider));
    }

    @Inject(method = "componentStyleAtWidth(Lnet/minecraft/network/chat/FormattedText;I)Lnet/minecraft/network/chat/Style;",
            at = @At("HEAD"), cancellable = true)
    private void eternalTrinkets$componentHit(FormattedText text, int x, CallbackInfoReturnable<Style> cir) {
        SpecialText.Parsed parsed = SpecialText.parse(text);
        if (parsed.handled()) cir.setReturnValue(SpecialTextLayout.styleAt(parsed.glyphs(), x, widthProvider));
    }
}
