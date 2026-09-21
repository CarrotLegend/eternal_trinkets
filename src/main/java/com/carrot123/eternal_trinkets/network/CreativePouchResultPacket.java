package com.carrot123.eternal_trinkets.network;

import com.carrot123.eternal_trinkets.client.EternalPotionPouchClient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record CreativePouchResultPacket(
        int inventorySlot,
        ItemStack pouchStack,
        ItemStack carriedStack) {

    static void encode(
            CreativePouchResultPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.inventorySlot);
        buffer.writeItem(packet.pouchStack);
        buffer.writeItem(packet.carriedStack);
    }

    static CreativePouchResultPacket decode(FriendlyByteBuf buffer) {
        return new CreativePouchResultPacket(
                buffer.readVarInt(),
                buffer.readItem(),
                buffer.readItem());
    }

    static void handle(
            CreativePouchResultPacket packet,
            Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> EternalPotionPouchClient.applyResult(
                        packet.inventorySlot,
                        packet.pouchStack,
                        packet.carriedStack)));
        context.setPacketHandled(true);
    }
}
