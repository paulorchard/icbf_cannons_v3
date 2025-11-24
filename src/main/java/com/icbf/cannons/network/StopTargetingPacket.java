package com.icbf.cannons.network;

import com.icbf.cannons.blockentity.CannonBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Sent from client to server when player releases right-click to stop targeting and fire cannon
 */
public class StopTargetingPacket {
    private final BlockPos cannonPos;
    private final BlockPos targetPos; // block the player was looking at

    public StopTargetingPacket(BlockPos cannonPos, BlockPos targetPos) {
        this.cannonPos = cannonPos;
        this.targetPos = targetPos;
    }

    public StopTargetingPacket(FriendlyByteBuf buf) {
        this.cannonPos = buf.readBlockPos();
        this.targetPos = buf.readBlockPos();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBlockPos(cannonPos);
        buf.writeBlockPos(targetPos == null ? BlockPos.ZERO : targetPos);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player != null) {
                BlockEntity be = player.level().getBlockEntity(cannonPos);
                if (be instanceof CannonBlockEntity cannonBE) {
                    // Fire using provided targetPos (server authoritative check will be applied)
                    cannonBE.fireAtTarget(player, targetPos);
                }
            }
        });
        return true;
    }
}
