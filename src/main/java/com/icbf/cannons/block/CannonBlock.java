package com.icbf.cannons.block;

import com.icbf.cannons.blockentity.CannonBlockEntity;
import com.icbf.cannons.init.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class CannonBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public CannonBlock(Properties properties) {
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
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        // Full block collision for the controller
        return Shapes.block();
    }

    @Override
    public boolean canBeReplaced(BlockState state, BlockPlaceContext useContext) {
        // Prevent blocks from being placed on top of or beside the cannon controller
        return false;
    }

    @Override
    public net.minecraft.world.InteractionResult use(BlockState state, Level level, BlockPos pos, net.minecraft.world.entity.player.Player player, net.minecraft.world.InteractionHand hand, net.minecraft.world.phys.BlockHitResult hit) {
        if (!level.isClientSide && player.isShiftKeyDown()) {
            // Sneak + right click to pick up the cannon
            if (!player.getAbilities().instabuild) {
                popResource(level, pos, new net.minecraft.world.item.ItemStack(com.icbf.cannons.init.ModItems.ICBF_CANNON.get()));
            }
            // Destroy the multiblock without dropping items again
            destroyMultiblock(level, pos, state.getValue(FACING));
            level.removeBlock(pos, false);
            return net.minecraft.world.InteractionResult.SUCCESS;
        }
        return net.minecraft.world.InteractionResult.PASS;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CannonBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, ModBlockEntities.CANNON_BLOCK_ENTITY.get(), CannonBlockEntity::tick);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        // Show flame particles at the front (muzzle) of the cannon
        Direction facing = state.getValue(FACING);
        
        // Get the front position (barrel end)
        BlockPos frontPos = pos.relative(facing);
        
        // Spawn particles at the front face
        double x = frontPos.getX() + 0.5;
        double y = frontPos.getY() + 0.5;
        double z = frontPos.getZ() + 0.5;
        
        // Add some randomness
        x += (random.nextDouble() - 0.5) * 0.3;
        y += (random.nextDouble() - 0.5) * 0.3;
        z += (random.nextDouble() - 0.5) * 0.3;
        
        level.addParticle(ParticleTypes.FLAME, x, y, z, 0.0, 0.0, 0.0);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            // Destroy the entire multiblock structure without dropping items
            destroyMultiblock(level, pos, state.getValue(FACING));
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    private void destroyMultiblock(Level level, BlockPos controllerPos, Direction facing) {
        // Controller is at bottom-back (y0, z2 in local coords)
        // Get all 6 positions based on facing direction
        BlockPos[] positions = getMultiblockPositions(controllerPos, facing);
        
        for (BlockPos blockPos : positions) {
            if (!blockPos.equals(controllerPos)) {
                BlockState state = level.getBlockState(blockPos);
                if (state.getBlock() instanceof CannonPartBlock || state.getBlock() instanceof CannonBarrierBlock) {
                    level.removeBlock(blockPos, false);
                }
            }
        }
    }

    public static BlockPos[] getMultiblockPositions(BlockPos controllerPos, Direction facing) {
        // Controller is at the MIDDLE position (x1,y0 in grid)
        // Local coordinates: controller = (0, 0, 1) - the center block
        // We need to calculate positions for:
        // y0: z0 (front), z1 (controller/middle), z2 (back), left barrier, right barrier
        // y1: z0, z1, z2 (upper back is barrier)
        
        BlockPos[] positions = new BlockPos[8];
        
        // Transform local coordinates based on facing direction
        switch (facing) {
            case NORTH: // Barrel points north (Z-)
                positions[0] = controllerPos.north();            // y0,z0 - front barrel
                positions[1] = controllerPos;                    // y0,z1 - controller (middle)
                positions[2] = controllerPos.south();            // y0,z2 - back
                positions[3] = controllerPos.above().north();    // y1,z0 - upper front
                positions[4] = controllerPos.above();            // y1,z1 - upper middle
                positions[5] = controllerPos.above().south();    // y1,z2 - upper back (barrier)
                positions[6] = controllerPos.west();             // left barrier (x1,y0)
                positions[7] = controllerPos.east();             // right barrier (x1,y0)
                break;
            case SOUTH: // Barrel points south (Z+)
                positions[0] = controllerPos.south();
                positions[1] = controllerPos;
                positions[2] = controllerPos.north();
                positions[3] = controllerPos.above().south();
                positions[4] = controllerPos.above();
                positions[5] = controllerPos.above().north();
                positions[6] = controllerPos.east();             // left barrier (x1,y0)
                positions[7] = controllerPos.west();             // right barrier (x1,y0)
                break;
            case EAST: // Barrel points east (X+)
                positions[0] = controllerPos.east();
                positions[1] = controllerPos;
                positions[2] = controllerPos.west();
                positions[3] = controllerPos.above().east();
                positions[4] = controllerPos.above();
                positions[5] = controllerPos.above().west();
                positions[6] = controllerPos.north();            // left barrier (x1,y0)
                positions[7] = controllerPos.south();            // right barrier (x1,y0)
                break;
            case WEST: // Barrel points west (X-)
                positions[0] = controllerPos.west();
                positions[1] = controllerPos;
                positions[2] = controllerPos.east();
                positions[3] = controllerPos.above().west();
                positions[4] = controllerPos.above();
                positions[5] = controllerPos.above().east();
                positions[6] = controllerPos.south();            // left barrier (x1,y0)
                positions[7] = controllerPos.north();            // right barrier (x1,y0)
                break;
            default:
                // Shouldn't happen with HORIZONTAL_FACING, but default to NORTH
                positions[0] = controllerPos.north();
                positions[1] = controllerPos;
                positions[2] = controllerPos.south();
                positions[3] = controllerPos.above().north();
                positions[4] = controllerPos.above();
                positions[5] = controllerPos.above().south();
                break;
        }
        
        return positions;
    }
}
