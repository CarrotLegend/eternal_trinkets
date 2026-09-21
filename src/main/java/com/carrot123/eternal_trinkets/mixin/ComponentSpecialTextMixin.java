package com.carrot123.eternal_trinkets.mixin;

import com.carrot123.eternal_trinkets.text.SpecialText;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Optional;

/** Parse before language bidi, wrapping and narration, without altering saved Components. */
@Mixin(MutableComponent.class)
public abstract class ComponentSpecialTextMixin implements Component {
    // MutableComponent inherits these defaults. Add overrides on the concrete
    // class: Mixin 0.8.5 does not support injectors in interfaces.
    @Override
    public <T> Optional<T> visit(FormattedText.StyledContentConsumer<T> consumer, Style parent) {
        if (SpecialText.rawVisit()) return Component.super.visit(consumer, parent);
        SpecialText.Parsed parsed = SpecialText.component(this, parent);
        return parsed.handled() ? parsed.formatted().visit(consumer, Style.EMPTY) : Component.super.visit(consumer, parent);
    }

    @Override
    public <T> Optional<T> visit(FormattedText.ContentConsumer<T> consumer) {
        return SpecialText.rawVisit() ? Component.super.visit(consumer)
                : consumer.accept(SpecialText.plainComponent(this));
    }
}
