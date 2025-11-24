package com.icbf.cannons.init;

import com.icbf.cannons.IcbfCannons;
import com.icbf.cannons.block.CannonBarrierBlock;
import com.icbf.cannons.block.CannonBlock;
import com.icbf.cannons.block.CannonPartBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, IcbfCannons.MOD_ID);

    // Cannon Controller Block (bottom-back position, renders the full model)
    public static final RegistryObject<Block> CANNON_CONTROLLER = BLOCKS.register("cannon_controller",
            () -> new CannonBlock(BlockBehaviour.Properties.of()
                    .strength(3.0f, 6.0f)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops() // Requires iron pickaxe or better
                    .noOcclusion()));

    // Cannon Part Block (invisible structure blocks)
    public static final RegistryObject<Block> CANNON_PART = BLOCKS.register("cannon_part",
            () -> new CannonPartBlock(BlockBehaviour.Properties.of()
                    .strength(3.0f, 6.0f)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops() // Requires iron pickaxe or better
                    .noOcclusion()));

    // Cannon Barrier Block (invisible collision block - unbreakable directly)
    public static final RegistryObject<Block> CANNON_BARRIER = BLOCKS.register("cannon_barrier",
            () -> new CannonBarrierBlock(BlockBehaviour.Properties.of()
                    .strength(-1.0f, 3600000.0f) // Unbreakable like bedrock
                    .sound(SoundType.METAL)
                    .noOcclusion()));

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
