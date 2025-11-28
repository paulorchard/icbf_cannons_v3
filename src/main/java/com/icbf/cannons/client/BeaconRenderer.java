package com.icbf.cannons.client;

import net.minecraft.core.BlockPos;

/**
 * Compatibility wrapper for the beacon renderer used by network packets.
 * Some code expects a `BeaconRenderer` class with `setBeaconPosition`/`clearBeacon`.
 * Delegate to `BeaconBeamRenderer` which implements the actual rendering logic.
 */
public final class BeaconRenderer {
    private BeaconRenderer() {}

    public static void setBeaconPosition(BlockPos pos) {
        BeaconBeamRenderer.setBeaconPosition(pos);
    }

    public static void clearBeacon() {
        BeaconBeamRenderer.clearBeacon();
    }

    public static void preloadTexture() {
        BeaconBeamRenderer.preloadTexture();
    }
}
