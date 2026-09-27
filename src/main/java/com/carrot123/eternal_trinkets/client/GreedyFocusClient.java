package com.carrot123.eternal_trinkets.client;

import com.carrot123.eternal_trinkets.network.CreativeFocusAbsorbPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.simple.SimpleChannel;

@OnlyIn(Dist.CLIENT)
public final class GreedyFocusClient {
    private GreedyFocusClient() {
    }

    public static void request(SimpleChannel channel, int inventorySlot, ItemStack carried) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen instanceof CreativeModeInventoryScreen) {
            channel.sendToServer(new CreativeFocusAbsorbPacket(inventorySlot, carried.copyWithCount(1)));
        }
    }

    public static void applyResult(int inventorySlot, ItemStack stack) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || !(minecraft.screen instanceof CreativeModeInventoryScreen)
                || inventorySlot < 0 || inventorySlot >= minecraft.player.getInventory().items.size()) {
            return;
        }
        minecraft.player.getInventory().setItem(inventorySlot, stack.copy());
    }
}
