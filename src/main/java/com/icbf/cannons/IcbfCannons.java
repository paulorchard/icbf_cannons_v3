package com.icbf.cannons;

import com.icbf.cannons.init.ModBlockEntities;
import com.icbf.cannons.init.ModBlocks;
import com.icbf.cannons.init.ModEntities;
import com.icbf.cannons.init.ModItems;
import com.icbf.cannons.network.ModMessages;
import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

@Mod(IcbfCannons.MOD_ID)
public class IcbfCannons {
    public static final String MOD_ID = "icbfcannons";
    public static final Logger LOGGER = LogUtils.getLogger();

    // Creative Tab Registration
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MOD_ID);

    public static final RegistryObject<CreativeModeTab> ISLANDCRAFT_TAB = CREATIVE_MODE_TABS.register("islandcraft",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.icbfcannons.islandcraft"))
                    .icon(() -> new ItemStack(ModItems.ICBF_CANNON.get()))
                    .displayItems((parameters, output) -> {
                        // Add items to creative tab
                        output.accept(ModItems.ICBF_CANNON.get());
                        output.accept(net.minecraft.world.item.Items.COMPASS);
                    })
                    .build());

    public IcbfCannons() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register creative tab
        CREATIVE_MODE_TABS.register(modEventBus);

        // Register items, blocks, entities, and block entities
        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModEntities.register(modEventBus);
        ModBlockEntities.register(modEventBus);

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        // Register the item to a creative tab
        modEventBus.addListener(this::addCreative);

        // Register our mod's ForgeConfigSpec so that Forge can create and load the config file for us
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        // Some common setup code
        LOGGER.info("ICBF Cannons v3.0 - Common setup");
        
        // Register network messages
        event.enqueueWork(() -> {
            ModMessages.register();
        });
    }

    // Add the mod items to the creative tab
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        // Items are already added via the creative tab builder above
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        LOGGER.info("ICBF Cannons v3.0 - Server starting");
    }
    
    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        // Cannon test command removed - targeting now uses player POV raytrace
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            // Some client setup code
            LOGGER.info("ICBF Cannons v3.0 - Client setup");
            // Preload the beacon beam texture to reduce first-frame hitch
            com.icbf.cannons.client.BeaconBeamRenderer.preloadTexture();
        }
        
        @SubscribeEvent
        public static void registerRenderers(net.minecraftforge.client.event.EntityRenderersEvent.RegisterRenderers event) {
            // Register custom cannonball renderer
            event.registerEntityRenderer(ModEntities.CANNONBALL.get(), 
                com.icbf.cannons.client.renderer.CannonballRenderer::new);
        }
    }
    
    // Client tick handler for compass release detection
    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static class ClientForgeEvents {
        @SubscribeEvent
        public static void onClientTick(net.minecraftforge.event.TickEvent.ClientTickEvent event) {
            if (event.phase == net.minecraftforge.event.TickEvent.Phase.END) {
                com.icbf.cannons.event.CompassTargetingHandler.checkReleaseButton();
            }
        }
    }
}
