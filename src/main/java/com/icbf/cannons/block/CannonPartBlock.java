package com.icbf.cannons.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class CannonPartBlock extends Block {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public CannonPartBlock(Properties properties) {
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
        // Invisible - the controller block renders the full model
        return RenderShape.INVISIBLE;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        // Full block collision
        return Shapes.block();
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, net.minecraft.world.entity.player.Player player) {
        if (!level.isClientSide) {
            // Drop a single cannon item when any part is broken
            popResource(level, pos, new net.minecraft.world.item.ItemStack(com.icbf.cannons.init.ModItems.ICBF_CANNON.get()));
        }
        super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public boolean canBeReplaced(BlockState state, BlockPlaceContext useContext) {
        // Prevent blocks from being placed on top of or beside the cannon parts
        return false;
    }

    @Override
    public net.minecraft.world.InteractionResult use(BlockState state, Level level, BlockPos pos, net.minecraft.world.entity.player.Player player, net.minecraft.world.InteractionHand hand, net.minecraft.world.phys.BlockHitResult hit) {
        if (!level.isClientSide && player.isShiftKeyDown()) {
            // Sneak + right click to pick up the cannon
            if (!player.getAbilities().instabuild) {
                popResource(level, pos, new net.minecraft.world.item.ItemStack(com.icbf.cannons.init.ModItems.ICBF_CANNON.get()));
            }
            // Destroy the entire cannon structure
            destroyCannonStructure(level, pos, state.getValue(FACING));
            return net.minecraft.world.InteractionResult.SUCCESS;
        }
        return net.minecraft.world.InteractionResult.PASS;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            // Find and destroy the controller block, which will handle the rest
            destroyCannonStructure(level, pos, state.getValue(FACING));
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    private void destroyCannonStructure(Level level, BlockPos partPos, Direction facing) {
        // Search for the controller block in the multiblock structure
        // The controller is always at the bottom-back position
        // We need to check nearby positions to find it
        
        for (int y = -1; y <= 1; y++) {
            for (int x = -2; x <= 2; x++) {
                for (int z = -2; z <= 2; z++) {
                    BlockPos checkPos = partPos.offset(x, y, z);
                    BlockState checkState = level.getBlockState(checkPos);
                    if (checkState.getBlock() instanceof CannonBlock) {
                        // Found the controller, break it without dropping items
                        level.destroyBlock(checkPos, false);
                        return;
                    }
                }
            }
        }
    }
}
