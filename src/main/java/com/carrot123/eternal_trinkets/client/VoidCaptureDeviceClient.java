package com.carrot123.eternal_trinkets.client;

import com.carrot123.eternal_trinkets.EternalTrinkets;
import com.carrot123.eternal_trinkets.util.CapturedMatter;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class VoidCaptureDeviceClient {

    public static final ResourceLocation CAPTURED_MATTER_PROPERTY =
            ResourceLocation.fromNamespaceAndPath(EternalTrinkets.MODID, "captured_matter");

    private VoidCaptureDeviceClient() {
    }

    public static boolean hasShiftDown() {
        return Screen.hasShiftDown();
    }

    public static void registerProperty(Item item) {
        ItemProperties.register(item, CAPTURED_MATTER_PROPERTY,
                (stack, level, entity, seed) -> CapturedMatter.fromStack(stack).predicateValue());
    }
}
