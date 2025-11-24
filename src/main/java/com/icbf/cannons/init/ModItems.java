package com.icbf.cannons.init;

import com.icbf.cannons.IcbfCannons;
import com.icbf.cannons.item.CannonItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, IcbfCannons.MOD_ID);

    // Cannon Item (places the multiblock structure)
    public static final RegistryObject<Item> ICBF_CANNON = ITEMS.register("icbf_cannon",
            () -> new CannonItem(new Item.Properties().stacksTo(1)));

    // Cannonball Item
    public static final RegistryObject<Item> CANNONBALL = ITEMS.register("cannonball",
            () -> new Item(new Item.Properties()));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
