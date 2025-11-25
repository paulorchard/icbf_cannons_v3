package com.icbf.cannons.blockentity;

import com.icbf.cannons.Config;
import com.icbf.cannons.block.CannonBlock;
import com.icbf.cannons.init.ModBlockEntities;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import java.util.ArrayList;
import java.util.List;
import java.util.Comparator;
import java.util.Set;
import com.icbf.cannons.fire.FireOrder;

public class CannonBlockEntity extends BlockEntity {
    private int cooldown = 0;
    // pending orders scheduled to forward later
    private final List<PendingOrder> pendingOrders = new ArrayList<>();
    
    // Targeting system: replaced by client POV raytrace. Server accepts final target on release.

    public CannonBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CANNON_BLOCK_ENTITY.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, CannonBlockEntity blockEntity) {
        if (!level.isClientSide) {
            // Cooldown system
            if (blockEntity.cooldown > 0) {
                blockEntity.cooldown--;
                blockEntity.setChanged();
            }
            
            // Process any pending fire orders whose delay has elapsed
            long gameTime = level.getGameTime();
            if (!blockEntity.pendingOrders.isEmpty()) {
                List<PendingOrder> toProcess = new ArrayList<>();
                for (PendingOrder p : blockEntity.pendingOrders) {
                    if (p.executeAt <= gameTime) toProcess.add(p);
                }
                blockEntity.pendingOrders.removeAll(toProcess);
                for (PendingOrder p : toProcess) {
                    blockEntity.forwardToNeighbors(p.order);
                }
            }
            // No server-side beacon advancement; targeting is client-side raytrace
        }
    }

    private static class PendingOrder {
        public final FireOrder order;
        public final long executeAt;
        public PendingOrder(FireOrder order, long executeAt) {
            this.order = order;
            this.executeAt = executeAt;
        }
    }

    /**
     * Receive a propagated fire order. This runs on the server thread.
     */
    public void receiveFireOrder(FireOrder order) {
        if (this.level == null || this.level.isClientSide) return;

        // Prevent processing the same cannon twice for the same order
        synchronized (order) {
            if (order.visited.contains(this.worldPosition)) return;
            order.visited.add(this.worldPosition);
            // Consume budget when cannon receives coordinates (as requested)
            order.remainingBudget = Math.max(0, order.remainingBudget - 1);
        }

        // Immediate firing check (does not wait for forwarding delay)
        if (order.target != null && !this.isOnCooldown()) {
            // Use null player - fireAtTarget checks for null when sending messages
            this.fireAtTarget(null, order.target);
        }

        // If no remaining budget, stop propagation
        if (order.remainingBudget <= 0) return;

        // Schedule forwarding after configured delay
        long executeAt = this.level.getGameTime() + order.delayTicks;
        this.pendingOrders.add(new PendingOrder(order, executeAt));
        this.setChanged();
    }

    private void forwardToNeighbors(FireOrder order) {
        if (this.level == null || this.level.isClientSide) return;

        int radius = 5;
        // Determine branch limit for forwarded orders: primary's forwarding uses 4 already,
        // subsequent forwards should use 3 as per spec. We'll set forwarded branchLimit=3.
        int forwardedBranch = 3;

        // Find neighboring cannons within radius, excluding visited
        List<CannonBlockEntity> neighbors = new ArrayList<>();
        BlockPos originPos = this.worldPosition;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    BlockPos p = originPos.offset(dx, dy, dz);
                    if (p.equals(originPos)) continue;
                    if (order.visited.contains(p)) continue;
                    BlockEntity be = this.level.getBlockEntity(p);
                    if (be instanceof CannonBlockEntity cbe) {
                        double distSq = p.distSqr(originPos);
                        neighbors.add(cbe);
                    }
                }
            }
        }

        // Sort neighbors by distance ascending and limit by branchLimit
        neighbors.sort(Comparator.comparingDouble(n -> n.worldPosition.distSqr(this.worldPosition)));
        int limit = Math.min(order.branchLimit, neighbors.size());
        for (int i = 0; i < limit; i++) {
            CannonBlockEntity nb = neighbors.get(i);
            // Create a forwarded copy - same visited set (copied in copyForForward)
            FireOrder fwd = order.copyForForward(forwardedBranch, 2);
            // Decrement remainingBudget preemptively so total recipients cannot exceed the budget
            synchronized (fwd) {
                fwd.remainingBudget = Math.max(0, order.remainingBudget);
            }
            // Deliver directly (runs on server thread)
            nb.receiveFireOrder(fwd);
        }
    }
    
    /**
     * Start the targeting sequence for this cannon
     */
    public void startTargeting(UUID playerUUID) {
        if (this.cooldown > 0) {
            return; // Cannon is on cooldown
        }
        // No server-side state required for POV targeting. Keep method for backward compatibility.
    }
    // findSurfaceBelow is no longer required for server-side beacon. Keep if needed later.
    
    /**
     * Old server-side beacon firing logic removed. Use public fireAtTarget(ServerPlayer, BlockPos).
     */
    
    // stopTargeting removed - client handles release visuals, server receives final target via packet

    /**
     * Public entry to fire the cannon at a specific block position (called from network handler).
     * Performs server-side cone check and then computes ballistic trajectory to hit the target block.
     */
    public void fireAtTarget(ServerPlayer player, BlockPos targetBlock) {
        if (this.level == null || this.level.isClientSide) return;

        Level level = this.level;
        BlockState state = this.getBlockState();

        Direction facing = state.getValue(CannonBlock.FACING);

        // x0,y1 is the upper front barrel block
        BlockPos[] positions = CannonBlock.getMultiblockPositions(this.worldPosition, facing);
        BlockPos frontBarrelPos = positions[3];
        BlockPos spawnPos = frontBarrelPos.relative(facing);

        double startX = spawnPos.getX() + 0.5;
        double startY = spawnPos.getY() + 0.5;
        double startZ = spawnPos.getZ() + 0.5;

        // Determine target center coordinates
        if (targetBlock == null) {
            if (player != null) {
                player.displayClientMessage(Component.literal("No target selected"), true);
            }
            return;
        }

        // If target is water, prefer the surface (block above water)
        if (this.level != null && this.level.getFluidState(targetBlock).is(FluidTags.WATER)) {
            targetBlock = targetBlock.above();
        }

        double tX = targetBlock.getX() + 0.5;
        double tY = targetBlock.getY() + 0.5;
        double tZ = targetBlock.getZ() + 0.5;

        // Perform a server-side rayclip from the muzzle to the target to detect any intervening water
        if (this.level != null) {
            Vec3 startVec = new Vec3(startX, startY, startZ);
            Vec3 targetVec = new Vec3(tX, tY, tZ);
            HitResult hr = this.level.clip(new ClipContext(startVec, targetVec, ClipContext.Block.COLLIDER, ClipContext.Fluid.ANY, null));
            if (hr != null && hr.getType() == HitResult.Type.BLOCK) {
                BlockHitResult bhr = (BlockHitResult) hr;
                BlockPos hitPos = bhr.getBlockPos();
                if (this.level.getFluidState(hitPos).is(FluidTags.WATER)) {
                    // Aim at the water surface instead
                    targetBlock = hitPos.above();
                    tX = targetBlock.getX() + 0.5;
                    tY = targetBlock.getY() + 0.5;
                    tZ = targetBlock.getZ() + 0.5;
                }
            }
        }

        // Cone check: ensure horizontal angle between cannon facing and target is within allowed cone
        double dirX = facing.getStepX();
        double dirZ = facing.getStepZ();
        double toTX = tX - startX;
        double toTZ = tZ - startZ;
        double horizLen = Math.sqrt(toTX * toTX + toTZ * toTZ);
        if (horizLen > 0.0001) {
            double dot = (dirX * toTX + dirZ * toTZ) / (Math.sqrt(dirX * dirX + dirZ * dirZ) * horizLen);
            double angle = Math.acos(Math.max(-1.0, Math.min(1.0, dot)));
            double maxAngle = Math.toRadians(25.0);
            if (angle > maxAngle) {
                if (player != null) {
                    player.displayClientMessage(Component.literal("Target outside cannon cone (25°)"), true);
                }
                return;
            }
        }

        // Build and launch projectile using previous firing logic
        // Compute deltas
        double dx = tX - startX;
        double dy = tY - startY;
        double dz = tZ - startZ;
        double horizontalDist = Math.sqrt(dx * dx + dz * dz);

        // Ballistic calculation
        double gravity = Config.projectileGravity;
        double velocity = Config.projectileVelocity;
        double v2 = velocity * velocity;
        double v4 = v2 * v2;
        double gx = gravity * horizontalDist;
        double discriminant = v4 - gravity * (gravity * horizontalDist * horizontalDist + 2 * dy * v2);

        double vx, vy, vz;
        double angle;

        if (discriminant < 0) {
            angle = Math.PI / 4.0;
            if (player != null) player.displayClientMessage(Component.literal("Target out of range; firing at max arc"), true);
        } else {
            double sqrtDiscriminant = Math.sqrt(discriminant);
            double tanAngle1 = (v2 + sqrtDiscriminant) / gx;
            double tanAngle2 = (v2 - sqrtDiscriminant) / gx;
            angle = Math.atan(tanAngle2);
        }

        double horizontalVelocity = velocity * Math.cos(angle);
        vy = velocity * Math.sin(angle);
        if (horizontalDist > 0.0001) {
            vx = (dx / horizontalDist) * horizontalVelocity;
            vz = (dz / horizontalDist) * horizontalVelocity;
        } else {
            vx = 0; vz = 0;
        }

        com.icbf.cannons.entity.CannonballEntity cannonball = new com.icbf.cannons.entity.CannonballEntity(level, startX, startY, startZ);
        cannonball.setDeltaMovement(vx, vy, vz);
        cannonball.setDebugData(angle, vx, vy, vz, spawnPos, targetBlock, gravity, horizontalDist, dy);
        // Set the owner if player provided so we can send guaranteed impact effects back to them
        if (player != null) {
            try {
                cannonball.setOwner(player);
            } catch (Exception ignored) {}
        }

        level.addFreshEntity(cannonball);

        // Play muzzle sound and spawn smoke/flame particles along the barrel up to 2 blocks
        try {
            if (level instanceof net.minecraft.server.level.ServerLevel slevel) {
                slevel.playSound(null, spawnPos.getX(), spawnPos.getY(), spawnPos.getZ(), net.minecraft.sounds.SoundEvents.GENERIC_EXPLODE, net.minecraft.sounds.SoundSource.BLOCKS, 1.2F, 0.9F + (float)(Math.random() * 0.2));
                // Emit particles along barrel direction to show fire exiting the muzzle
                int steps = 4; // 4 steps of 0.5 = 2 blocks
                double stepSize = 0.5;
                double dirStepX = facing.getStepX();
                double dirStepY = facing.getStepY();
                double dirStepZ = facing.getStepZ();
                for (int i = 0; i < steps; i++) {
                    double t = (i + 1) * stepSize; // start slightly outside the muzzle
                    double px = startX + dirStepX * t;
                    double py = startY + dirStepY * t;
                    double pz = startZ + dirStepZ * t;
                    // smoke and flame
                    slevel.sendParticles(net.minecraft.core.particles.ParticleTypes.SMOKE, px, py, pz, 2, 0.05, 0.05, 0.05, 0.01);
                    slevel.sendParticles(net.minecraft.core.particles.ParticleTypes.FLAME, px, py, pz, 1, 0.02, 0.02, 0.02, 0.01);
                }
                // Ring of large smoke puffs at the muzzle to simulate blast (TNT-like grey puffs)
                java.util.Random rnd = new java.util.Random();
                int ringCount = 20;
                double baseRadius = 0.4; // radius around muzzle
                for (int i = 0; i < ringCount; i++) {
                    double ang = (i / (double)ringCount) * Math.PI * 2.0 + (rnd.nextDouble() - 0.5) * 0.1;
                    double r = baseRadius + rnd.nextDouble() * 0.4;
                    double px = startX + Math.cos(ang) * r;
                    double py = startY + 0.1 + rnd.nextDouble() * 0.2;
                    double pz = startZ + Math.sin(ang) * r;
                    double svx = Math.cos(ang) * (0.02 + rnd.nextDouble() * 0.02);
                    double svy = 0.04 + rnd.nextDouble() * 0.04;
                    double svz = Math.sin(ang) * (0.02 + rnd.nextDouble() * 0.02);
                    slevel.sendParticles(net.minecraft.core.particles.ParticleTypes.LARGE_SMOKE, px, py, pz, 1, svx, svy, svz, 0.0);
                }
            }
        } catch (Exception ignored) {}
        this.setCooldown(Config.cooldownTicks);
        this.setChanged();
    }

    public boolean isOnCooldown() {
        return cooldown > 0;
    }

    public void setCooldown(int ticks) {
        this.cooldown = ticks;
        this.setChanged();
    }

    public int getCooldown() {
        return cooldown;
    }
    
    // testRange removed

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("Cooldown", cooldown);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        cooldown = tag.getInt("Cooldown");
    }
}
