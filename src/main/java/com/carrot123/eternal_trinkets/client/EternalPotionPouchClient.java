package com.carrot123.eternal_trinkets.client;

import com.carrot123.eternal_trinkets.network.CreativePouchStorePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.simple.SimpleChannel;

@OnlyIn(Dist.CLIENT)
public final class EternalPotionPouchClient {
    private EternalPotionPouchClient() {
    }

    public static void requestStore(
            SimpleChannel channel,
            int inventorySlot,
            ItemStack potionStack) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen instanceof CreativeModeInventoryScreen) {
            channel.sendToServer(new CreativePouchStorePacket(
                    inventorySlot, potionStack.copy()));
        }
    }

    public static void applyResult(
            int inventorySlot,
            ItemStack pouchStack,
            ItemStack carriedStack) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null
                || !(minecraft.screen instanceof CreativeModeInventoryScreen)
                || inventorySlot < 0
                || inventorySlot >= minecraft.player.getInventory().items.size()) {
            return;
        }

        minecraft.player.getInventory().setItem(
                inventorySlot, pouchStack.copy());
        minecraft.player.inventoryMenu.setCarried(carriedStack.copy());
    }
}
