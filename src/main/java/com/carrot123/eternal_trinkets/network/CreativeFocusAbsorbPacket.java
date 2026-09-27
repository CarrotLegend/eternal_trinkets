package com.carrot123.eternal_trinkets.network;

import com.carrot123.eternal_trinkets.item.curio.GreedyFocusItem;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

public record CreativeFocusAbsorbPacket(int inventorySlot, ItemStack carried) {
    static void encode(CreativeFocusAbsorbPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.inventorySlot);
        buffer.writeItem(packet.carried);
    }

    static CreativeFocusAbsorbPacket decode(FriendlyByteBuf buffer) {
        return new CreativeFocusAbsorbPacket(buffer.readVarInt(), buffer.readItem());
    }

    static void handle(CreativeFocusAbsorbPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null && GreedyFocusItem.isEmptyFocus(packet.carried)
                    && GreedyFocusItem.absorbCreative(player, packet.inventorySlot)) {
                ModNetwork.sendCreativeFocusResult(player, new CreativeFocusResultPacket(
                        packet.inventorySlot,
                        player.getInventory().getItem(packet.inventorySlot).copy()));
            }
        });
        context.setPacketHandled(true);
    }
}
