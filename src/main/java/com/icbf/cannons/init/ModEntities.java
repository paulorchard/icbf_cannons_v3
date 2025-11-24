package com.icbf.cannons.init;

import com.icbf.cannons.IcbfCannons;
import com.icbf.cannons.entity.CannonballEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, IcbfCannons.MOD_ID);

    public static final RegistryObject<EntityType<CannonballEntity>> CANNONBALL = ENTITIES.register("cannonball",
            () -> EntityType.Builder.<CannonballEntity>of(CannonballEntity::new, MobCategory.MISC)
                    .sized(0.5f, 0.5f)
                    .clientTrackingRange(128)
                    .updateInterval(1)
                    .build("cannonball"));

    public static void register(IEventBus eventBus) {
        ENTITIES.register(eventBus);
    }
}
