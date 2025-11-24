package com.icbf.cannons.network;

import com.icbf.cannons.blockentity.CannonBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Sent from client to server when player right-clicks with compass to start targeting a cannon
 */
public class StartTargetingPacket {
    private final BlockPos cannonPos;

    public StartTargetingPacket(BlockPos cannonPos) {
        this.cannonPos = cannonPos;
    }

    public StartTargetingPacket(FriendlyByteBuf buf) {
        this.cannonPos = buf.readBlockPos();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBlockPos(cannonPos);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player != null) {
                BlockEntity be = player.level().getBlockEntity(cannonPos);
                if (be instanceof CannonBlockEntity cannonBE) {
                    cannonBE.startTargeting(player.getUUID());
                }
            }
        });
        return true;
    }
}
