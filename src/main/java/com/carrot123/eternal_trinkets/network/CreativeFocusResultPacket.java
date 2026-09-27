package com.carrot123.eternal_trinkets.network;

import com.carrot123.eternal_trinkets.client.GreedyFocusClient;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public record CreativeFocusResultPacket(int inventorySlot, ItemStack stack) {
    static void encode(CreativeFocusResultPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.inventorySlot);
        buffer.writeItem(packet.stack);
    }

    static CreativeFocusResultPacket decode(FriendlyByteBuf buffer) {
        return new CreativeFocusResultPacket(buffer.readVarInt(), buffer.readItem());
    }

    static void handle(CreativeFocusResultPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> GreedyFocusClient.applyResult(packet.inventorySlot, packet.stack)));
        context.setPacketHandled(true);
    }
}
