package com.carrot123.eternal_trinkets.network;

import com.carrot123.eternal_trinkets.EternalTrinkets;
import com.carrot123.eternal_trinkets.client.EternalPotionPouchClient;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;

public final class ModNetwork {
    private static final String PROTOCOL_VERSION = "1";
    private static final SimpleChannel CHANNEL =
            NetworkRegistry.newSimpleChannel(
                    ResourceLocation.fromNamespaceAndPath(
                            EternalTrinkets.MODID, "main"),
                    () -> PROTOCOL_VERSION,
                    PROTOCOL_VERSION::equals,
                    PROTOCOL_VERSION::equals);

    private ModNetwork() {
    }

    public static void register() {
        int id = 0;
        CHANNEL.registerMessage(
                id++,
                CreativePouchStorePacket.class,
                CreativePouchStorePacket::encode,
                CreativePouchStorePacket::decode,
                CreativePouchStorePacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(
                id,
                CreativePouchResultPacket.class,
                CreativePouchResultPacket::encode,
                CreativePouchResultPacket::decode,
                CreativePouchResultPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }

    public static void requestCreativePouchStore(
            int inventorySlot, ItemStack potionStack) {
        DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> EternalPotionPouchClient.requestStore(
                        CHANNEL, inventorySlot, potionStack));
    }

    static void sendCreativePouchResult(
            net.minecraft.server.level.ServerPlayer player,
            CreativePouchResultPacket packet) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }
}
