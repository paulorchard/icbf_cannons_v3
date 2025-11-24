package com.icbf.cannons;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = IcbfCannons.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    // Cannon Behavior Settings
    private static final ForgeConfigSpec.DoubleValue EXPLOSION_POWER = BUILDER
            .comment("The explosion power of cannonballs (default: 2.0)")
            .defineInRange("explosionPower", 2.0, 0.0, 10.0);

    private static final ForgeConfigSpec.IntValue MAX_RANGE = BUILDER
            .comment("The maximum range of cannons in blocks (default: 200)")
            .defineInRange("maxRange", 200, 1, 500);

    private static final ForgeConfigSpec.IntValue COOLDOWN_TICKS = BUILDER
            .comment("The cooldown duration in ticks (20 ticks = 1 second, default: 100 = 5 seconds)")
            .defineInRange("cooldownTicks", 100, 0, 1200);

    // Targeting Settings
    private static final ForgeConfigSpec.IntValue BEACON_MAX_DISTANCE = BUILDER
            .comment("Maximum distance the targeting beacon can reach in blocks (default: 64)")
            .defineInRange("beaconMaxDistance", 64, 5, 200);

    private static final ForgeConfigSpec.IntValue BEACON_START_DISTANCE = BUILDER
            .comment("Starting distance of the targeting beacon in blocks (default: 5)")
            .defineInRange("beaconStartDistance", 5, 1, 50);

    private static final ForgeConfigSpec.IntValue BEACON_TOTAL_TICKS = BUILDER
            .comment("Total ticks for beacon to travel from start to max distance (default: 60 = 3 seconds)")
            .defineInRange("beaconTotalTicks", 60, 20, 200);

    // Projectile Physics Settings
    private static final ForgeConfigSpec.DoubleValue PROJECTILE_VELOCITY = BUILDER
            .comment("Base muzzle velocity in blocks/tick (default: 3.0, vanilla arrow ~3.0)")
            .defineInRange("projectileVelocity", 3.0, 0.5, 10.0);

    private static final ForgeConfigSpec.DoubleValue PROJECTILE_GRAVITY = BUILDER
            .comment("Gravity applied to cannonballs per tick (default: 0.03, lower than vanilla arrows for realistic cannon arcs)")
            .defineInRange("projectileGravity", 0.03, 0.01, 0.1);

    // Chain Firing Settings
    private static final ForgeConfigSpec.BooleanValue ENABLE_CHAIN_FIRING = BUILDER
            .comment("Enable chain firing of nearby cannons (default: true)")
            .define("enableChainFiring", true);

    private static final ForgeConfigSpec.IntValue CHAIN_FIRE_RADIUS = BUILDER
            .comment("The radius in blocks to search for connected cannons (default: 5)")
            .defineInRange("chainFireRadius", 5, 1, 20);

    private static final ForgeConfigSpec.IntValue CHAIN_BRANCH_LIMIT = BUILDER
            .comment("Maximum number of branches in chain firing to prevent exponential growth (default: 2)")
            .defineInRange("chainBranchLimit", 2, 0, 10);

    // Ammunition Settings
    private static final ForgeConfigSpec.BooleanValue REQUIRE_AMMUNITION = BUILDER
            .comment("Require cannonballs to fire cannons (default: true)")
            .define("requireAmmunition", true);

    private static final ForgeConfigSpec.BooleanValue CREATIVE_INFINITE_AMMO = BUILDER
            .comment("Creative mode players have infinite ammo (default: true)")
            .define("creativeInfiniteAmmo", true);

    // Damage Settings
    private static final ForgeConfigSpec.BooleanValue ENABLE_BLOCK_DAMAGE = BUILDER
            .comment("Allow cannons to damage blocks (default: true)")
            .define("enableBlockDamage", true);

    private static final ForgeConfigSpec.BooleanValue ENABLE_FRIENDLY_FIRE = BUILDER
            .comment("Allow cannons to damage players and entities (default: true)")
            .define("enableFriendlyFire", true);

    static final ForgeConfigSpec SPEC = BUILDER.build();

    // Public static getters for config values
    public static double explosionPower;
    public static int maxRange;
    public static int cooldownTicks;
    public static int beaconMaxDistance;
    public static int beaconStartDistance;
    public static int beaconTotalTicks;
    public static double projectileVelocity;
    public static double projectileGravity;
    public static boolean enableChainFiring;
    public static int chainFireRadius;
    public static int chainBranchLimit;
    public static boolean requireAmmunition;
    public static boolean creativeInfiniteAmmo;
    public static boolean enableBlockDamage;
    public static boolean enableFriendlyFire;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        explosionPower = EXPLOSION_POWER.get();
        maxRange = MAX_RANGE.get();
        cooldownTicks = COOLDOWN_TICKS.get();
        beaconMaxDistance = BEACON_MAX_DISTANCE.get();
        beaconStartDistance = BEACON_START_DISTANCE.get();
        beaconTotalTicks = BEACON_TOTAL_TICKS.get();
        projectileVelocity = PROJECTILE_VELOCITY.get();
        projectileGravity = PROJECTILE_GRAVITY.get();
        enableChainFiring = ENABLE_CHAIN_FIRING.get();
        chainFireRadius = CHAIN_FIRE_RADIUS.get();
        chainBranchLimit = CHAIN_BRANCH_LIMIT.get();
        requireAmmunition = REQUIRE_AMMUNITION.get();
        creativeInfiniteAmmo = CREATIVE_INFINITE_AMMO.get();
        enableBlockDamage = ENABLE_BLOCK_DAMAGE.get();
        enableFriendlyFire = ENABLE_FRIENDLY_FIRE.get();
    }
}
