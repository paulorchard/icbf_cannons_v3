package com.icbf.cannons.event;

import com.icbf.cannons.IcbfCannons;
import com.icbf.cannons.block.CannonBlock;
import com.icbf.cannons.network.ModMessages;
import com.icbf.cannons.network.StartTargetingPacket;
import com.icbf.cannons.network.StopTargetingPacket;
import net.minecraft.client.Minecraft;
import com.icbf.cannons.client.BeaconBeamRenderer;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.tags.FluidTags;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;

/**
 * Handles vanilla compass right-click to activate cannon targeting
 */
@Mod.EventBusSubscriber(modid = IcbfCannons.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class CompassTargetingHandler {
    
    @Nullable
    private static BlockPos currentTargetingCannon = null;
    private static boolean isHoldingRightClick = false;
    private static BlockPos currentLookedAtBlock = null;
    private static int beaconHoldTicks = 0; // grace ticks to avoid flicker when look ray briefly misses
    
    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        ItemStack stack = event.getItemStack();
        InteractionHand hand = event.getHand();
        Level level = event.getLevel();
        
        // Only handle compass on client side
        if (!level.isClientSide || !stack.is(Items.COMPASS)) {
            return;
        }
        
        // Only main hand to avoid double triggers
        if (hand != InteractionHand.MAIN_HAND) {
            return;
        }
        
        // Find nearest cannon within 20 blocks
        BlockPos playerPos = player.blockPosition();
        AABB searchBox = new AABB(playerPos).inflate(20);
        
        BlockPos nearestCannon = null;
        double nearestDistance = Double.MAX_VALUE;
        
        for (BlockPos pos : BlockPos.betweenClosed(
            (int) searchBox.minX, (int) searchBox.minY, (int) searchBox.minZ,
            (int) searchBox.maxX, (int) searchBox.maxY, (int) searchBox.maxZ)) {
            
            BlockState state = level.getBlockState(pos);
            if (state.getBlock() instanceof CannonBlock) {
                double distance = player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
                if (distance < nearestDistance) {
                    nearestDistance = distance;
                    nearestCannon = pos.immutable();
                }
            }
        }
        
        if (nearestCannon != null) {
            // Start targeting
            currentTargetingCannon = nearestCannon;
            isHoldingRightClick = true;
            ModMessages.sendToServer(new StartTargetingPacket(nearestCannon));
            // Do an immediate raytrace so the beacon appears without waiting for the next tick
            Minecraft mc = Minecraft.getInstance();
            if (mc != null && mc.getCameraEntity() != null) {
                HitResult hr0 = mc.getCameraEntity().pick(com.icbf.cannons.Config.beaconMaxDistance, 0.0F, true);
                if (hr0 != null && hr0.getType() == HitResult.Type.BLOCK) {
                    BlockHitResult bhr0 = (BlockHitResult) hr0;
                    currentLookedAtBlock = bhr0.getBlockPos();
                    beaconHoldTicks = 5; // hold for a few ticks to reduce flicker
                    BeaconBeamRenderer.setBeaconPosition(currentLookedAtBlock);
                }
            }
            player.displayClientMessage(Component.literal("Targeting cannon..."), true);
            // Cancel the event to prevent vanilla compass behavior
            event.setCanceled(true);
        } else {
            player.displayClientMessage(Component.literal("No cannons found within 20 blocks"), true);
            event.setCanceled(true);
        }
    }
    
    /**
     * Called each client tick to check if player released right-click
     */
    public static void checkReleaseButton() {
        Minecraft mc = Minecraft.getInstance();
        
        if (isHoldingRightClick && currentTargetingCannon != null) {
            // Check if player released right mouse button
            // While holding, do a raytrace from the player's POV to find the block being looked at
            HitResult hr = null;
            if (mc.getCameraEntity() != null) {
                hr = mc.getCameraEntity().pick(com.icbf.cannons.Config.beaconMaxDistance, 0.0F, true);
            }
            if (hr != null && hr.getType() == HitResult.Type.BLOCK) {
                BlockHitResult bhr = (BlockHitResult) hr;
                BlockPos hitPos = bhr.getBlockPos();
                // If we hit water, target the surface (block above water)
                if (mc.level != null && mc.level.getBlockState(hitPos).getFluidState().is(FluidTags.WATER)) {
                    currentLookedAtBlock = hitPos.above();
                } else {
                    currentLookedAtBlock = hitPos;
                }
                beaconHoldTicks = 5; // refresh hold ticks whenever we have a hit
                BeaconBeamRenderer.setBeaconPosition(currentLookedAtBlock);
            } else {
                // avoid flicker: only clear after grace period
                if (beaconHoldTicks > 0) {
                    beaconHoldTicks--;
                } else {
                    currentLookedAtBlock = null;
                    BeaconBeamRenderer.clearBeacon();
                }
            }

            if (!mc.options.keyUse.isDown()) {
                // Released! Only fire if we have a valid looked-at block
                if (currentLookedAtBlock != null) {
                    ModMessages.sendToServer(new StopTargetingPacket(currentTargetingCannon, currentLookedAtBlock));
                    if (mc.player != null) mc.player.displayClientMessage(Component.literal("FIRE!"), true);
                } else {
                    if (mc.player != null) mc.player.displayClientMessage(Component.literal("No valid target selected"), true);
                }

                isHoldingRightClick = false;
                currentTargetingCannon = null;
                currentLookedAtBlock = null;
                BeaconBeamRenderer.clearBeacon();
            }
        }
    }
}
