# ICBF Cannons v3.0 - Development Plan

## Phase 1: Foundation (Week 1)

### Core Infrastructure
- [x] Project structure setup
- [ ] Main mod class (`IcbfCannons.java`)
- [ ] Creative tab registration
- [ ] Config system setup
- [ ] Basic networking infrastructure

### Resources
- [x] Copy cannon 3D model
- [x] Copy textures
- [x] META-INF/mods.toml
- [ ] Update version numbers to 3.0.0
- [ ] Add lang files (en_us.json)

## Phase 2: Block System (Week 2)

### Cannon Block
- [ ] CannonBlock class (multi-block controller)
- [ ] CannonPartBlock class (structure parts)
- [ ] CannonBarrierBlock class (collision prevention)
- [ ] BlockEntity for cooldown/state management
- [ ] Placement logic (CannonItem)
- [ ] Breaking logic (destroy all parts)
- [ ] Rotation/facing direction

### Models & Rendering
- [ ] Block state JSON files
- [ ] Model JSON files (reuse existing)
- [ ] Texture assignment
- [ ] Test in-game placement

## Phase 3: Projectile System (Week 3)

### Design Decision: Choose One
**Option A: Fast Naval Combat (v2.x style)**
- Direct explosion with travel delay
- 200 blocks/second timing
- Flame particle trails
- Good for ship combat

**Option B: Realistic Artillery (Swashbucklers style)**
- AbstractArrow projectile entity
- Parabolic trajectory
- Vanilla physics
- Good for ground combat

**Option C: Dual System (Recommended)**
- Different cannon types for different roles
- Naval cannons: Fast direct fire
- Mortars: Arcing projectiles
- Both available

### Implementation (after decision)
- [ ] Projectile entity class(es)
- [ ] Firing logic in CannonBlock
- [ ] Impact/explosion handling
- [ ] Particle effects
- [ ] Sound effects

## Phase 4: Ammunition System (Week 4)

### Items
- [ ] CannonballItem class
- [ ] Cannonball crafting recipe
- [ ] Inventory consumption logic
- [ ] Creative mode bypass
- [ ] Stack size configuration

### Optional Advanced Features
- [ ] Different ammo types (iron, explosive, chain shot)
- [ ] Ammo storage in cannon
- [ ] Reload mechanics
- [ ] Ammo count display

## Phase 5: Targeting System (Week 5)

### Spyglass Integration
- [ ] Client-side mouse input handler
- [ ] Raycast logic (blocks + entities)
- [ ] Target highlighting/feedback
- [ ] Network packet (client → server)
- [ ] Server-side validation

### Targeting Modes
- [ ] Manual aim (look direction)
- [ ] Spyglass precision targeting
- [ ] Auto-aim assistance (optional)
- [ ] Range indicators

## Phase 6: Chain Firing (Week 6)

### Multi-Cannon Coordination
- [ ] Cannon detection algorithm (BFS)
- [ ] Neighbor radius configuration
- [ ] Branch limit (prevent exponential)
- [ ] Sequential vs simultaneous fire
- [ ] Cooldown synchronization

### Feedback
- [ ] Fired cannon count message
- [ ] Muzzle flash effects
- [ ] Sound coordination
- [ ] Particle trails

## Phase 7: Swashbucklers Integration (Week 7)

### Ship Detection
- [ ] SwashbucklersShipHelper (reflection)
- [ ] Ship type enum (7 types)
- [ ] Cannon count per ship
- [ ] Player mounting detection

### Ship Cannons
- [ ] ShipCannonHelper (position calculation)
- [ ] Cone of fire validation
- [ ] Port/starboard selection
- [ ] Network packet for ship firing
- [ ] Client-side input (mounted controls)

### Optional: Ship Cannon Models
- [ ] Render cannons on ship entities
- [ ] Position synchronization
- [ ] Animation on fire

## Phase 8: Configuration & Balance (Week 8)

### Config Options
- [ ] Explosion power (default: 2.0)
- [ ] Max range (default: 200 blocks)
- [ ] Cooldown duration (default: 5 seconds)
- [ ] Chain radius (default: 5 blocks)
- [ ] Chain branch limit (default: 2)
- [ ] Ammo consumption (enable/disable)
- [ ] Friendly fire toggle
- [ ] Block damage toggle

### Testing
- [ ] Balance testing
- [ ] Multiplayer testing
- [ ] Performance profiling
- [ ] Mod compatibility testing

## Phase 9: Polish & Content (Week 9)

### Additional Features
- [ ] Multiple cannon sizes (small, medium, large)
- [ ] Decorative cannon variants
- [ ] Cannonball pile block (storage)
- [ ] Cannon upgrade system
- [ ] Achievement/advancement integration

### Sound & Effects
- [ ] Custom cannon fire sound
- [ ] Explosion sound variations
- [ ] Reload/loading sounds
- [ ] Enhanced particle effects
- [ ] Impact effects (water, dirt, stone)

### Documentation
- [ ] In-game guide book (Patchouli?)
- [ ] Recipe book integration
- [ ] Tooltip improvements
- [ ] Config file comments

## Phase 10: Release Preparation (Week 10)

### Testing & Bug Fixes
- [ ] Comprehensive bug testing
- [ ] Edge case handling
- [ ] Crash prevention
- [ ] Memory leak checks
- [ ] Chunk loading/unloading tests

### Documentation
- [ ] CurseForge page
- [ ] Modrinth page
- [ ] Changelog
- [ ] Installation guide
- [ ] Compatibility list

### Release
- [ ] Build final JAR
- [ ] Create GitHub release
- [ ] Upload to CurseForge
- [ ] Upload to Modrinth
- [ ] Announce release

## Key Design Decisions to Make

1. **Projectile System**: Fast direct fire vs realistic arc vs both?
2. **Ammunition**: Required vs optional vs disabled?
3. **Cannon Variants**: Single type vs multiple sizes/types?
4. **Swashbucklers**: Full integration vs optional compatibility?
5. **Multiplayer**: PvP enabled vs PvE only vs configurable?
6. **Block Damage**: Always vs never vs configurable?

## Architecture Principles

1. **Separation of Concerns**
   - Block logic separate from entity logic
   - Client code separate from server code
   - Networking in dedicated package
   - Utilities in shared package

2. **Configurability**
   - All gameplay values in config
   - Feature toggles for major systems
   - Balance knobs for server owners

3. **Compatibility**
   - Reflection for soft dependencies
   - Graceful degradation without Swashbucklers
   - No hard dependencies on other mods

4. **Performance**
   - Efficient projectile despawning
   - Lazy initialization
   - Chunk-aware entity spawning
   - Minimal network traffic

5. **Maintainability**
   - Clear code organization
   - Consistent naming conventions
   - Comprehensive comments
   - Modular design for easy updates

## Next Steps

1. Set up main mod class
2. Register creative tab
3. Create basic config
4. Copy over cannon block classes (cleaned up)
5. Get cannon placement working
6. **Then decide on projectile system**

## Questions to Answer

- Do you want ammunition required or optional?
- Should chain firing be default or require activation?
- One cannon type or multiple variants?
- Direct fire or arcing projectiles (or both)?
- How much Swashbucklers integration?
