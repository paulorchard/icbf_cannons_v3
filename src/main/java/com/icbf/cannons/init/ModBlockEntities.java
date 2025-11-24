package com.icbf.cannons.init;

import com.icbf.cannons.IcbfCannons;
import com.icbf.cannons.blockentity.CannonBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, IcbfCannons.MOD_ID);

    public static final RegistryObject<BlockEntityType<CannonBlockEntity>> CANNON_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("cannon_block_entity", () ->
                    BlockEntityType.Builder.of(CannonBlockEntity::new, 
                            ModBlocks.CANNON_CONTROLLER.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
