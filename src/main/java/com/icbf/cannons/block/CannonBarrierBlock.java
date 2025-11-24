package com.icbf.cannons.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class CannonBarrierBlock extends Block {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public CannonBarrierBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection());
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        // Completely invisible
        return RenderShape.INVISIBLE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        // No collision - players and entities can walk through
        return Shapes.empty();
    }

    @Override
    public boolean canBeReplaced(BlockState state, BlockPlaceContext useContext) {
        // Prevent blocks from being placed on the barrier block
        return false;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            // Find and destroy the controller block
            destroyCannonStructure(level, pos, state.getValue(FACING));
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    private void destroyCannonStructure(Level level, BlockPos barrierPos, Direction facing) {
        // Barrier is at the top-back, controller is one below and one forward
        // Controller should be directly below and toward the barrel direction
        BlockPos controllerPos;
        
        switch (facing) {
            case NORTH:
                controllerPos = barrierPos.below().north(); // down and forward (north)
                break;
            case SOUTH:
                controllerPos = barrierPos.below().south();
                break;
            case EAST:
                controllerPos = barrierPos.below().east();
                break;
            case WEST:
                controllerPos = barrierPos.below().west();
                break;
            default:
                controllerPos = barrierPos.below();
                break;
        }
        
        BlockState checkState = level.getBlockState(controllerPos);
        
        if (checkState.getBlock() instanceof CannonBlock) {
            level.destroyBlock(controllerPos, true);
        } else {
            // Search nearby if not at expected position
            for (int x = -2; x <= 2; x++) {
                for (int z = -2; z <= 2; z++) {
                    BlockPos checkPos = barrierPos.offset(x, -1, z);
                    BlockState state = level.getBlockState(checkPos);
                    if (state.getBlock() instanceof CannonBlock) {
                        level.destroyBlock(checkPos, true);
                        return;
                    }
                }
            }
        }
    }
}
