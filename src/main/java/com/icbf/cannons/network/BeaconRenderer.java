package com.icbf.cannons.network;

import net.minecraft.core.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

/**
 * Network-side wrapper for triggering client-only beacon rendering.
 * This class lives in the `network` package so packets can safely reference it
 * without directly importing client-only classes on the logical server.
 */
public final class BeaconRenderer {
    private BeaconRenderer() {}

    public static void setBeaconPosition(BlockPos pos) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> com.icbf.cannons.client.BeaconBeamRenderer.setBeaconPosition(pos));
    }

    public static void clearBeacon() {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> com.icbf.cannons.client.BeaconBeamRenderer.clearBeacon());
    }

    public static void preloadTexture() {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> com.icbf.cannons.client.BeaconBeamRenderer.preloadTexture());
    }
}
