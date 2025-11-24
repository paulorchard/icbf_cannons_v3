# ICBF Cannons Configuration Guide

## Targeting Beacon Settings

### beaconMaxDistance (default: 64)
- Maximum distance the targeting beacon can travel
- Range: 5-200 blocks
- Higher values let you target further away

### beaconStartDistance (default: 5)
- Starting distance from the cannon where beacon begins
- Range: 1-50 blocks
- Prevents beacon from appearing inside cannon model

### beaconTotalTicks (default: 60)
- Total time for beacon to travel from start to max distance
- Range: 20-200 ticks (1 second = 20 ticks)
- **Beacon speed automatically adjusts based on beaconMaxDistance**
- Example: 60 ticks with 64 max distance = ~1 block/tick
- Example: 60 ticks with 120 max distance = ~2 blocks/tick

## Projectile Physics Settings (Physics-Based Trajectory)

The mod now uses **real ballistic physics** to calculate cannonball trajectory!

### projectileVelocity (default: 3.0)
- Base muzzle velocity in blocks/tick
- Range: 0.5-10.0
- Vanilla arrow velocity ≈ 3.0
- Higher = longer range, flatter trajectory
- **This determines maximum effective range**

### projectileGravity (default: 0.05)
- Gravity applied per tick (same as vanilla arrows)
- Range: 0.01-0.1
- Lower = flatter arcs
- **Trajectory is automatically calculated using physics equations**

## How Physics Work

The mod uses projectile motion equations to calculate the perfect launch angle:

```
Launch Angle (θ) = arctan((v² - √(v⁴ - g(gx² + 2yv²))) / (gx))

Where:
- v = projectileVelocity
- g = projectileGravity  
- x = horizontal distance to target
- y = vertical distance to target
```

**If target is out of range**, the cannon fires at 45° for maximum distance.

## Maximum Range Formula

Maximum horizontal range = **v² / g**

Examples:
- velocity=3.0, gravity=0.05: Max range = 180 blocks
- velocity=4.0, gravity=0.05: Max range = 320 blocks
- velocity=3.0, gravity=0.03: Max range = 300 blocks

## Recommended Configurations

### Standard Cannon (Default)
```
projectileVelocity = 3.0
projectileGravity = 0.05
beaconMaxDistance = 64
beaconTotalTicks = 60
Max Range: ~180 blocks
```

### Heavy Artillery (Long Range)
```
projectileVelocity = 4.5
projectileGravity = 0.05
beaconMaxDistance = 120
beaconTotalTicks = 80
Max Range: ~405 blocks
```

### Naval Cannon (Flat Trajectory)
```
projectileVelocity = 3.5
projectileGravity = 0.03
beaconMaxDistance = 80
beaconTotalTicks = 60
Max Range: ~408 blocks
```

### Mortar (High Arc)
```
projectileVelocity = 2.5
projectileGravity = 0.08
beaconMaxDistance = 50
beaconTotalTicks = 40
Max Range: ~78 blocks
```

## Beacon Beam

The targeting beacon uses a **fake beacon beam** (red color) that:
- Only visible to the player targeting
- No chunk distance limits (renders at any distance)
- **Speed automatically scales with max distance**
- Automatically clears when you release right-click

## Config File Location

After first launch: `config/icbfcannons-common.toml`

## Tips

1. **Set beaconMaxDistance ≤ calculated max range** for best results
2. Beacon speed adapts: longer range = faster beacon movement
3. Physics ensures cannonballs always hit the beacon target (if in range)
4. Out-of-range targets get 45° angle for maximum distance attempt
