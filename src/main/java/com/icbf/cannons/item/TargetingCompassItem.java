package com.icbf.cannons.item;

import com.icbf.cannons.block.CannonBlock;
import com.icbf.cannons.network.ModMessages;
import com.icbf.cannons.network.StartTargetingPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Targeting compass used to aim cannons.
 * Right-click to find nearby cannons within 20 blocks and start targeting.
 */
public class TargetingCompassItem extends Item {

    public TargetingCompassItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide) {
            // Find nearby cannons (20 block radius)
            BlockPos playerPos = player.blockPosition();
            AABB searchBox = new AABB(playerPos).inflate(20);
            
            BlockPos nearestCannon = null;
            double nearestDistance = Double.MAX_VALUE;

            // Search for cannon controllers
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
                // Send packet to server to start targeting
                ModMessages.sendToServer(new StartTargetingPacket(nearestCannon));
                player.displayClientMessage(Component.literal("Targeting cannon..."), true);
                return InteractionResultHolder.success(stack);
            } else {
                player.displayClientMessage(Component.literal("No cannons found within 20 blocks"), true);
                return InteractionResultHolder.fail(stack);
            }
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
