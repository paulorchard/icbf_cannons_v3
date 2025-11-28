package com.icbf.cannons.util;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Minimal fallback raycast helper.
 *
 * If Valkyrien Skies is present a mod-provided helper may be used instead.
 * This implementation performs a vanilla clip() raytrace and returns a
 * BlockHitResult when a block was hit, otherwise null.
 */
public final class VSShipRaycastHelper {

    private VSShipRaycastHelper() {
    }

    public static BlockHitResult raycastWithShips(Entity cameraEntity, double maxDistance, float partialTicks, boolean includeFluids) {
        if (cameraEntity == null || cameraEntity.level() == null) return null;

        Vec3 eye = cameraEntity.getEyePosition(partialTicks);
        Vec3 look = cameraEntity.getViewVector(partialTicks);
        Vec3 end = eye.add(look.scale(maxDistance));

        ClipContext.Fluid fluid = includeFluids ? ClipContext.Fluid.ANY : ClipContext.Fluid.NONE;
        HitResult hr = cameraEntity.level().clip(new ClipContext(eye, end, ClipContext.Block.OUTLINE, fluid, cameraEntity));
        if (hr instanceof BlockHitResult) {
            return (BlockHitResult) hr;
        }
        return null;
    }
}
