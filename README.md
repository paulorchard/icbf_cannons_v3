# ICBF Cannons v3.0

**A complete rewrite of the ICBF Cannons mod for Minecraft 1.20.1 Forge**

## Version 3.0 - What's New

This is a ground-up rewrite incorporating lessons learned from v1.x and v2.x:

### Planned Features

- **Multi-block Cannon Structures**: Realistic 3D cannon models
- **Projectile System**: Choose between instant-fire or realistic physics
- **Ammunition System**: Cannonball crafting and inventory management
- **Naval Combat**: Spyglass targeting and ship cannon integration
- **Swashbucklers Compatibility**: Full integration with Swashbucklers mod ships
- **Chain Firing**: Coordinate multiple cannons for devastating broadsides
- **Custom Creative Tab**: Organized cannon items and ammunition

### Design Goals

1. **Modular Architecture**: Clean separation of concerns
2. **Performance**: Efficient projectile handling and explosion systems
3. **Compatibility**: Work seamlessly with Swashbucklers and other mods
4. **Configurability**: Extensive config options for server owners
5. **Multiplayer Ready**: Robust networking and synchronization

## Project Structure

```
src/main/
├── java/com/icbf/cannons/
│   ├── IcbfCannons.java          # Main mod class
│   ├── block/                     # Cannon blocks
│   ├── entity/                    # Projectile entities
│   ├── item/                      # Cannon items, ammunition
│   ├── network/                   # Networking packets
│   ├── util/                      # Helper classes
│   └── config/                    # Configuration
└── resources/
    ├── assets/icbfcannons/
    │   ├── models/                # 3D models
    │   ├── textures/              # Textures
    │   ├── lang/                  # Translations
    │   └── blockstates/           # Block states
    └── META-INF/
        └── mods.toml              # Mod metadata
```

## Building

```bash
# Windows
.\gradlew build

# Linux/Mac
./gradlew build
```

Output JAR: `build/libs/icbf_cannons-3.0.0.jar`

## Development Setup

1. Clone the repository
2. Import as Gradle project in your IDE
3. Run `./gradlew genIntellijRuns` (IntelliJ) or `./gradlew genEclipseRuns` (Eclipse)
4. Use the generated run configurations

## Technical Details

- **Minecraft Version**: 1.20.1
- **Forge Version**: 47.4.10+
- **Java Version**: 17+
- **Mappings**: Official Mojang mappings

## Swashbucklers Integration

See [SWASHBUCKLERS_CANNON_ANALYSIS.md](SWASHBUCKLERS_CANNON_ANALYSIS.md) for detailed information about Swashbucklers' cannon systems and integration strategies.

## License

See [LICENSE.txt](LICENSE.txt)

## Credits

- Original ICBF Cannons concept and models
- Swashbucklers mod compatibility research
- Community feedback and testing

## Version History

### v3.0.0 (In Development)
- Complete rewrite from the ground up
- New modular architecture
- Enhanced projectile system
- Full Swashbucklers integration

### v2.0.0 (Previous)
- Ship cannon system
- Spyglass targeting
- Chain firing
- Direct explosion system

### v1.0.0 (Original)
- Basic cannon blocks
- Fireball projectiles
- Multi-block structures
