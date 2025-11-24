package com.icbf.cannons.item;

import com.icbf.cannons.block.CannonBarrierBlock;
import com.icbf.cannons.block.CannonBlock;
import com.icbf.cannons.block.CannonPartBlock;
import com.icbf.cannons.init.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class CannonItem extends BlockItem {

    public CannonItem(Properties properties) {
        super(ModBlocks.CANNON_CONTROLLER.get(), properties);
    }

    @Override
    public InteractionResult place(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos clickedPos = context.getClickedPos();
        Direction playerFacing = context.getHorizontalDirection();
        
        // The cannon should face away from the player (same direction player is looking)
        Direction cannonFacing = playerFacing;
        
        // The clicked position should be the BACK of the cannon (x2,y0)
        // Controller is at the middle (x1,y0), so it's 1 block FORWARD from clicked position
        BlockPos controllerPos = clickedPos.relative(cannonFacing);
        
        // Check if all 6 positions are available
        if (!canPlaceCannon(level, controllerPos, cannonFacing)) {
            return InteractionResult.FAIL;
        }
        
        // Place the multiblock structure
        if (!level.isClientSide) {
            placeCannonStructure(level, controllerPos, cannonFacing);
            
            // Consume the item from inventory (except in creative mode)
            if (context.getPlayer() != null && !context.getPlayer().getAbilities().instabuild) {
                context.getItemInHand().shrink(1);
            }
        }
        
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private boolean canPlaceCannon(Level level, BlockPos controllerPos, Direction facing) {
        BlockPos[] positions = CannonBlock.getMultiblockPositions(controllerPos, facing);
        
        for (BlockPos pos : positions) {
            BlockState state = level.getBlockState(pos);
            // Check if the position is replaceable (air, grass, flowers, etc.)
            if (!state.canBeReplaced()) {
                return false;
            }
            
            // Check if there's enough vertical space
            if (!level.getBlockState(pos.above()).canBeReplaced() && pos.getY() < controllerPos.getY() + 1) {
                // Additional blocks above might block placement
            }
        }
        
        return true;
    }

    private void placeCannonStructure(Level level, BlockPos controllerPos, Direction facing) {
        BlockPos[] positions = CannonBlock.getMultiblockPositions(controllerPos, facing);
        
        // Position indices:
        // 0: y0,z0 - front barrel (CannonPart)
        // 1: y0,z1 - controller/middle (CannonBlock) - MODEL CENTER
        // 2: y0,z2 - back (CannonPart)
        // 3: y1,z0 - upper front (CannonPart)
        // 4: y1,z1 - upper middle (CannonPart)
        // 5: y1,z2 - upper back barrier (CannonBarrier)
        
        // Place controller at middle position (index 1)
        BlockState controllerState = ModBlocks.CANNON_CONTROLLER.get()
                .defaultBlockState()
                .setValue(CannonBlock.FACING, facing);
        level.setBlock(positions[1], controllerState, 3);
        
        // Place part blocks
        BlockState partState = ModBlocks.CANNON_PART.get()
                .defaultBlockState()
                .setValue(CannonPartBlock.FACING, facing);
        
        level.setBlock(positions[0], partState, 3); // front barrel
        level.setBlock(positions[2], partState, 3); // back
        level.setBlock(positions[3], partState, 3); // upper front
        level.setBlock(positions[4], partState, 3); // upper middle
        
        // Place barrier blocks at upper back and left/right sides
        BlockState barrierState = ModBlocks.CANNON_BARRIER.get()
                .defaultBlockState()
                .setValue(CannonBarrierBlock.FACING, facing);
        level.setBlock(positions[5], barrierState, 3); // upper back
        level.setBlock(positions[6], barrierState, 3); // left side
        level.setBlock(positions[7], barrierState, 3); // right side
    }
}
