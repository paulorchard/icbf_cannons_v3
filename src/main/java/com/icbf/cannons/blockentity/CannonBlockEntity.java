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

public class CannonBlockEntity extends BlockEntity {
    private int cooldown = 0;
    
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
            
            // No server-side beacon advancement; targeting is client-side raytrace
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

        double tX = targetBlock.getX() + 0.5;
        double tY = targetBlock.getY() + 0.5;
        double tZ = targetBlock.getZ() + 0.5;

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

        level.addFreshEntity(cannonball);
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
