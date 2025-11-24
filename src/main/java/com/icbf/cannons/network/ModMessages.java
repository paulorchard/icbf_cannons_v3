package com.icbf.cannons.network;

import com.icbf.cannons.IcbfCannons;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModMessages {
    public static SimpleChannel INSTANCE;
    private static int packetId = 0;

    private static int id() {
        return packetId++;
    }

    public static void register() {
        SimpleChannel net = NetworkRegistry.ChannelBuilder
                .named(ResourceLocation.fromNamespaceAndPath(IcbfCannons.MOD_ID, "messages"))
                .networkProtocolVersion(() -> "1.0")
                .clientAcceptedVersions(s -> true)
                .serverAcceptedVersions(s -> true)
                .simpleChannel();

        INSTANCE = net;

        // Register targeting packets
        net.messageBuilder(StartTargetingPacket.class, id())
            .decoder(StartTargetingPacket::new)
            .encoder(StartTargetingPacket::toBytes)
            .consumerMainThread(StartTargetingPacket::handle)
            .add();

        net.messageBuilder(StopTargetingPacket.class, id())
            .decoder(StopTargetingPacket::new)
            .encoder(StopTargetingPacket::toBytes)
            .consumerMainThread(StopTargetingPacket::handle)
            .add();

        // Legacy beacon packets removed (client now raytraces locally).

        IcbfCannons.LOGGER.info("Registering network messages for " + IcbfCannons.MOD_ID);
    }

    public static <MSG> void sendToServer(MSG message) {
        INSTANCE.sendToServer(message);
    }

    public static <MSG> void sendToPlayer(MSG message, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
    }

    public static <MSG> void sendToAllPlayers(MSG message) {
        INSTANCE.send(PacketDistributor.ALL.noArg(), message);
    }
}
