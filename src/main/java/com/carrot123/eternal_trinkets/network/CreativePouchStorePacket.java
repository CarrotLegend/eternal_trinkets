package com.carrot123.eternal_trinkets.network;

import com.carrot123.eternal_trinkets.item.EternalPotionPouchItem;
import com.carrot123.eternal_trinkets.item.ModItems;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record CreativePouchStorePacket(
        int inventorySlot, ItemStack potionStack) {

    static void encode(
            CreativePouchStorePacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.inventorySlot);
        buffer.writeItem(packet.potionStack);
    }

    static CreativePouchStorePacket decode(FriendlyByteBuf buffer) {
        return new CreativePouchStorePacket(
                buffer.readVarInt(), buffer.readItem());
    }

    static void handle(
            CreativePouchStorePacket packet,
            Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> handleOnServer(context.getSender(), packet));
        context.setPacketHandled(true);
    }

    private static void handleOnServer(
            ServerPlayer player, CreativePouchStorePacket packet) {
        if (player == null
                || !player.gameMode.isCreative()
                || packet.inventorySlot < 0
                || packet.inventorySlot >= player.getInventory().items.size()) {
            return;
        }

        ItemStack pouchStack =
                player.getInventory().getItem(packet.inventorySlot);
        if (!(pouchStack.getItem() instanceof EternalPotionPouchItem pouchItem)
                || !pouchStack.is(ModItems.ETERNAL_POTION_POUCH.get())) {
            return;
        }

        ItemStack potionStack = packet.potionStack.copyWithCount(1);
        boolean stored =
                pouchItem.storeCreativePotion(player, pouchStack, potionStack);
        ItemStack carriedResult =
                EternalPotionPouchItem.creativeCarriedResult(
                        packet.potionStack, stored);
        ModNetwork.sendCreativePouchResult(
                player,
                new CreativePouchResultPacket(
                        packet.inventorySlot,
                        pouchStack.copy(),
                        carriedResult));
    }
}
