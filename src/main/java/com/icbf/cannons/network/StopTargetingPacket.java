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
                    // Create a FireOrder and hand it to the primary cannon. The primary will
                    // propagate to neighbors according to configured branching rules.
                    java.util.UUID origin = java.util.UUID.randomUUID();
                    // If targetPos is BlockPos.ZERO treat as null/no-target
                    net.minecraft.core.BlockPos tgt = (targetPos.equals(net.minecraft.core.BlockPos.ZERO) ? null : targetPos);
                    com.icbf.cannons.fire.FireOrder order = new com.icbf.cannons.fire.FireOrder(origin, tgt, 100, 0, 4, 0);
                    // Attach initiator player UUID so they receive guaranteed impact feedback
                    if (player != null) order.initiatorPlayer = player.getUUID();
                    cannonBE.receiveFireOrder(order);
                }
            }
        });
        return true;
    }
}
