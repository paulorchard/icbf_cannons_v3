# Swashbucklers Handcannon and Mortar Analysis

## Entity Classes

### HandCannonEntity
- **Base Class**: AbstractArrow (extends Minecraft's arrow projectile)
- **Interface**: ItemSupplier
- **Projectile Type**: Cannonball item
- **Key Characteristics**:
  - Uses vanilla AbstractArrow physics (gravity, trajectory)
  - On entity hit: Reduces target's armor by 1 point
  - On block hit: Destroys the block (if not waterlogged/liquid)
  - Returns cannonball item when picked up

### HandMortarEntity  
- **Base Class**: AbstractArrow
- **Interface**: ItemSupplier
- **Projectile Type**: Mortar ball item
- **Key Characteristics**:
  - Uses vanilla AbstractArrow physics
  - On block hit: Creates 3.0 power explosion (TNT interaction mode)
  - On entity hit: Similar explosion at impact location
  - More destructive than handcannon

## Firing Mechanics

### HandCannonItem
**Firing Process**:
1. Player starts using item (right-click hold)
2. On release: Checks if player has cannonball ammunition
3. Searches player inventory for cannonball items
4. If found and not in creative:
   - Consumes 1 cannonball from inventory
   - Creates HandCannonEntity projectile
5. Uses `shoot()` method with parameters:
   - Speed: 2.5f
   - Inaccuracy: 3.0d
   - Use time affects final velocity

**Key Code Patterns**:
- `ProjectileWeaponItem.m_43010_()` - Gets ammunition from player
- Checks inventory manually for cannonball items
- Not creative mode: removes 1 item from stack
- 72000 tick maximum use duration

### HandMortarItem
**Similar to HandCannonItem**:
- Uses mortar ball ammunition instead
- Same firing mechanism (right-click hold and release)
- Same ammo consumption logic

## Impact Effects

### HandCannon Impact
**Block Hit**:
`
if (block is not liquid/waterlogged) {
    Block.dropResources(blockState, level, pos, null);
    level.destroyBlock(pos, false);
}
`
- Breaks single block
- Drops block resources
- No explosion

**Entity Hit**:
- Reduces entity armor by 1
- Calls `HandCannonProjectileHitsLivingEntityProcedure`

### HandMortar Impact
**Block Hit**:
`
if (!level.isClientSide) {
    level.explode(null, x, y, z, 3.0f, Level.ExplosionInteraction.TNT);
}
`
- 3.0 explosion power (TNT = 4.0 for reference)
- TNT interaction mode (breaks blocks)
- Server-side only

**Entity Hit**:
- Same 3.0 explosion at entity position

## Network/Ship Cannon Messages

### Found Classes:
- `CannonLeftMessage` - Controls left-side ship cannons
- `CannonRightMessage` - Controls right-side ship cannons  
- `FireMortarKeyMessage` - Controls mortar firing

These appear to be for ship-mounted cannons (separate from handheld)

## Ammunition System

### Items:
- **Cannonball**: Used by handcannon
- **Mortar Ball**: Used by mortar

### Consumption:
- Searches player inventory for matching ammo item
- Creative mode: No consumption
- Survival: Removes 1 from stack

## Key Differences from ICBF Cannons

1. **Projectile Type**: AbstractArrow vs. LargeFireball
   - Arrows have built-in gravity
   - Arrows stick in blocks (can be picked up)
   - Different rendering

2. **Ammunition**: Required item consumption vs. none
   - Must have cannonball/mortar ball items
   - Consumed on each shot

3. **Impact Behavior**:
   - Handcannon: Single block destruction
   - Mortar: 3.0 explosion
   - vs ICBF: 2.0 explosion (current)

4. **Firing Method**: Hold and release vs. direct click
   - Right-click and hold to aim
   - Release to fire
   - vs ICBF: Left-click to fire instantly

5. **Physics**: Vanilla arrow gravity vs. custom distance-based
   - Swashbucklers uses standard arrow arc
   - ICBF uses straight line until 200 blocks, then drops

## Ship Cannon Tags

Found entity type tags:
- `forge:front_cannon` - Ships with front cannons
- `forge:front_mortar` - Ships with front mortars  
- `forge:side_cannons` - Ships with broadside cannons

## Integration Recommendations

### Option 1: Add Handcannon/Mortar as New Items
- Create separate items using AbstractArrow projectiles
- Implement ammo consumption system
- Keep existing ICBF cannons unchanged

### Option 2: Hybrid Approach
- Keep current instant-fire spyglass system
- Add optional ammo consumption (config?)
- Use AbstractArrow for some cannon types

### Option 3: Two Firing Modes
- Mode 1: Current direct explosion (200 blocks/sec)
- Mode 2: Projectile mode with vanilla physics
- Config or different cannon types

## Technical Details

### AbstractArrow Benefits:
- Built-in gravity and physics
- Entity collision detection
- Can be picked up
- Renders as arrow (customizable)
- Sticks in blocks

### AbstractArrow Drawbacks:
- Parabolic arc (not straight line)
- Slower than current system
- More entities to track
- Different feel than naval cannons

### LargeFireball Benefits (current):
- Fast, straight trajectory
- Visual fireball effect
- Existing custom gravity system
- Naval cannon feel

## Ship Cannon Procedures

The ship cannons appear to use different procedures than handheld:
- `MortarFireProcedure` - Ship mortar firing
- Separate from handheld item classes
- Likely triggered by key messages

