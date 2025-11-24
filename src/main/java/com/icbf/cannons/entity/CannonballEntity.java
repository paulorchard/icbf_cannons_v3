package com.icbf.cannons.entity;

import com.icbf.cannons.Config;
import com.icbf.cannons.init.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Cannonball projectile with custom physics (no drag).
 */
public class CannonballEntity extends ThrowableProjectile {
    
    // Debug tracking fields
    private boolean debugLogged = false;
    private double debugAngle = 0;
    private double debugVx = 0, debugVy = 0, debugVz = 0;
    private BlockPos debugOrigin = BlockPos.ZERO;
    private BlockPos debugTarget = BlockPos.ZERO;
    private double debugGravity = 0;
    private double debugHorizontalDist = 0;
    private double debugVerticalDist = 0;
    private double maxApexReached = Double.NEGATIVE_INFINITY; // Track highest Y position
    
    public CannonballEntity(EntityType<? extends CannonballEntity> type, Level level) {
        super(type, level);
    }

    public CannonballEntity(Level level, double x, double y, double z) {
        super(ModEntities.CANNONBALL.get(), level);
        this.setPos(x, y, z);
    }

    @Override
    protected void defineSynchedData() {
        // No synced data needed
    }

    /**
     * Set debug tracking data for this cannonball
     */
    public void setDebugData(double angle, double vx, double vy, double vz, BlockPos origin, BlockPos target, double gravity, double horizontalDist, double verticalDist) {
        this.debugAngle = angle;
        this.debugVx = vx;
        this.debugVy = vy;
        this.debugVz = vz;
        this.debugOrigin = origin;
        this.debugTarget = target;
        this.debugGravity = gravity;
        this.debugHorizontalDist = horizontalDist;
        this.debugVerticalDist = verticalDist;
    }

    @Override
    public void tick() {
        // Check for water contact BEFORE physics
        if (!this.level().isClientSide && this.isInWater()) {
            // Debug log water entry
            if (!debugLogged) {
                debugLogged = true;
                BlockPos currentPos = this.blockPosition();
                
                System.out.println("=== CANNONBALL DEBUG ===");
                System.out.println("Angle: " + Math.toDegrees(debugAngle) + "°");
                System.out.println("Initial velocity: vx=" + debugVx + " vy=" + debugVy + " vz=" + debugVz);
                System.out.println("Current velocity: " + this.getDeltaMovement());
                System.out.println("Origin block: " + debugOrigin);
                System.out.println("Target block: " + debugTarget);
                System.out.println("First contact: " + currentPos + " (Water)");
                System.out.println("========================");
            }
            
            // Explode on water contact
            this.level().explode(
                this,
                this.getX(),
                this.getY(),
                this.getZ(),
                (float) Config.explosionPower,
                Level.ExplosionInteraction.NONE
            );
            
            this.discard();
            return;
        }
        
        // Custom physics - NO DRAG
        // Store current position and velocity
        Vec3 oldPos = this.position();
        Vec3 velocity = this.getDeltaMovement();
        Vec3 newPos = oldPos.add(velocity);
        
        // Check for block collisions BEFORE moving
        net.minecraft.world.phys.HitResult hitResult = this.level().clip(
            new net.minecraft.world.level.ClipContext(
                oldPos,
                newPos,
                net.minecraft.world.level.ClipContext.Block.COLLIDER,
                net.minecraft.world.level.ClipContext.Fluid.NONE,
                this
            )
        );
        
        // If collision detected, stop at collision point and trigger impact
        if (hitResult.getType() != net.minecraft.world.phys.HitResult.Type.MISS) {
            this.setPos(hitResult.getLocation().x, hitResult.getLocation().y, hitResult.getLocation().z);
            this.onHit(hitResult);
            return; // Don't apply gravity or continue movement after impact
        }
        
        // No collision - move to new position
        this.setPos(newPos.x, newPos.y, newPos.z);
        
        // Apply gravity for NEXT tick
        this.setDeltaMovement(velocity.add(0, -Config.projectileGravity, 0));
        
        // Check entity collisions
        for (net.minecraft.world.entity.Entity entity : this.level().getEntities(this, this.getBoundingBox())) {
            if (entity != this.getOwner()) {
                this.onHitEntity(new EntityHitResult(entity));
                break;
            }
        }
        
        // Update bounding box
        this.checkInsideBlocks();
        
        // Age tracking
        if (this.tickCount > 200) { // 10 seconds max
            this.discard();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        
        if (!this.level().isClientSide) {
            // Create explosion at impact point
            this.level().explode(
                this,
                this.getX(),
                this.getY(),
                this.getZ(),
                (float) Config.explosionPower,
                Level.ExplosionInteraction.TNT
            );
            
            // Remove the projectile
            this.discard();
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        
        if (!this.level().isClientSide && !debugLogged) {
            debugLogged = true;
            
            // Current velocity at impact
            var currentVel = this.getDeltaMovement();
            double currentVx = currentVel.x;
            double currentVy = currentVel.y;
            double currentVz = currentVel.z;
            
            // Calculate actual distances
            BlockPos hitPos = result.getBlockPos();
            double dx = hitPos.getX() - debugOrigin.getX();
            double dy = hitPos.getY() - debugOrigin.getY();
            double dz = hitPos.getZ() - debugOrigin.getZ();
            double actualHorizontalDist = Math.sqrt(dx * dx + dz * dz);
            
            // Calculate initial velocity magnitude
            double initialVelMag = Math.sqrt(debugVx*debugVx + debugVy*debugVy + debugVz*debugVz);
            double currentVelMag = Math.sqrt(currentVx*currentVx + currentVy*currentVy + currentVz*currentVz);
            
            // Calculate theoretical apex using physics: apex_height = vy² / (2g)
            double theoreticalApexHeight = (debugVy * debugVy) / (2.0 * debugGravity);
            double theoreticalApexY = debugOrigin.getY() + theoreticalApexHeight;
            
            // Calculate theoretical apex distance: time_to_apex = vy / g, dist_at_apex = vx * time
            double timeToApex = debugVy / debugGravity;
            double horizontalVel = Math.sqrt(debugVx*debugVx + debugVz*debugVz);
            double theoreticalApexDist = horizontalVel * timeToApex;
            
            // Actual apex data
            double actualApexHeight = maxApexReached - debugOrigin.getY();
            
            System.out.println("\n" + "=".repeat(80));
            System.out.println("  CANNONBALL BALLISTIC DEBUG PANEL");
            System.out.println("=".repeat(80));
            
            System.out.println("\n[CONFIGURATION]");
            System.out.println("  Gravity:  " + String.format("%.4f", debugGravity) + " blocks/tick²");
            System.out.println("  Config velocity: " + String.format("%.2f", initialVelMag) + " blocks/tick");
            
            System.out.println("\n[FIRING SOLUTION]");
            System.out.println("  Launch angle:  " + String.format("%.2f", Math.toDegrees(debugAngle)) + "°");
            System.out.println("  Horizontal range: " + String.format("%.2f", debugHorizontalDist) + " blocks (target)");
            System.out.println("  Vertical drop:    " + String.format("%.2f", debugVerticalDist) + " blocks (dy)");
            
            System.out.println("\n[INITIAL VELOCITY COMPONENTS]");
            System.out.println("  vx: " + String.format("%+.6f", debugVx) + " blocks/tick");
            System.out.println("  vy: " + String.format("%+.6f", debugVy) + " blocks/tick");
            System.out.println("  vz: " + String.format("%+.6f", debugVz) + " blocks/tick");
            System.out.println("  Magnitude: " + String.format("%.6f", initialVelMag) + " blocks/tick");
            
            System.out.println("\n[TRAJECTORY PREDICTION]");
            System.out.println("  Theoretical apex height: " + String.format("%.2f", theoreticalApexHeight) + " blocks above origin");
            System.out.println("  Theoretical apex Y:      " + String.format("%.2f", theoreticalApexY) + " (world Y)");
            System.out.println("  Theoretical apex dist:   " + String.format("%.2f", theoreticalApexDist) + " blocks (should be ~50% of range)");
            System.out.println("  Time to apex:            " + String.format("%.2f", timeToApex) + " ticks");
            
            System.out.println("\n[ACTUAL FLIGHT DATA]");
            System.out.println("  Actual apex reached:  " + String.format("%.2f", maxApexReached) + " (world Y)");
            System.out.println("  Actual apex height:   " + String.format("%.2f", actualApexHeight) + " blocks above origin");
            System.out.println("  Apex accuracy:        " + String.format("%.2f", actualApexHeight - theoreticalApexHeight) + " blocks error");
            
            System.out.println("\n[IMPACT DATA]");
            System.out.println("  Origin:   " + debugOrigin + " (Y=" + debugOrigin.getY() + ")");
            System.out.println("  Target:   " + debugTarget + " (Y=" + debugTarget.getY() + ")");
            System.out.println("  Impact:   " + hitPos + " (Y=" + hitPos.getY() + ")");
            System.out.println("  Hit block: " + this.level().getBlockState(hitPos).getBlock().getName().getString());
            
            System.out.println("\n[DISTANCE VERIFICATION]");
            System.out.println("  Target horizontal: " + String.format("%.2f", debugHorizontalDist) + " blocks");
            System.out.println("  Actual horizontal: " + String.format("%.2f", actualHorizontalDist) + " blocks");
            System.out.println("  Range error:       " + String.format("%.2f", actualHorizontalDist - debugHorizontalDist) + " blocks (" + 
                String.format("%.1f", ((actualHorizontalDist - debugHorizontalDist) / debugHorizontalDist * 100)) + "% overshoot)");
            
            System.out.println("\n[VELOCITY AT IMPACT]");
            System.out.println("  vx: " + String.format("%+.6f", currentVx) + " (initial: " + String.format("%+.6f", debugVx) + ")");
            System.out.println("  vy: " + String.format("%+.6f", currentVy) + " (initial: " + String.format("%+.6f", debugVy) + ")");
            System.out.println("  vz: " + String.format("%+.6f", currentVz) + " (initial: " + String.format("%+.6f", debugVz) + ")");
            System.out.println("  Magnitude: " + String.format("%.6f", currentVelMag) + " blocks/tick");
            
            System.out.println("\n[PHYSICS VERIFICATION]");
            double vxChange = Math.abs(currentVx - debugVx);
            double vzChange = Math.abs(currentVz - debugVz);
            double horizontalVelLoss = Math.max(vxChange, vzChange);
            System.out.println("  Horizontal velocity preserved: " + (horizontalVelLoss < 0.0001 ? "YES ✓" : "NO ✗ (lost " + String.format("%.6f", horizontalVelLoss) + ")"));
            System.out.println("  Gravity effect per tick: -" + String.format("%.4f", debugGravity) + " blocks/tick²");
            System.out.println("  vy change: " + String.format("%.4f", debugVy - currentVy) + " blocks/tick (gravity accumulated)");
            
            System.out.println("\n" + "=".repeat(80) + "\n");
        }
        
        // Create explosion at impact point
        if (!this.level().isClientSide) {
            this.level().explode(
                this,
                this.getX(),
                this.getY(),
                this.getZ(),
                (float) Config.explosionPower,
                Config.enableBlockDamage ? Level.ExplosionInteraction.TNT : Level.ExplosionInteraction.NONE
            );
            
            // Remove the projectile
            this.discard();
        }
    }

    @Override
    public boolean shouldRender(double x, double y, double z) {
        // Always render cannonballs (visible to all players)
        return true;
    }
}
