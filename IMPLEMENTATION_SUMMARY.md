# RPGEntity Custom Model/Texture Implementation - Summary

## Overview
This implementation adds custom model and texture support to RPGEntity, mirroring the existing functionality available for RPGItem. Entities can now have custom visual appearances using resource packs while maintaining their base entity behavior.

## What Was Implemented

### 1. RPGEntity Class Enhancements
**File:** `src/main/java/cz/vitekform/rPGCore/objects/RPGEntity.java`

**Added Fields:**
- `modelPath` - Path to custom model JSON file
- `texturePath` - Path to custom texture PNG file
- `customModelKey` - Resource pack key for the custom model
- `displayEntityUUID` - UUID of the ItemDisplay entity showing the custom model

**New Method:**
- `applyCustomModel(LivingEntity entity)` - Creates an ItemDisplay entity to show the custom model
  - Spawns ItemDisplay entity riding on the base entity
  - Makes base entity invisible
  - Handles errors gracefully with fallback

**Updated Methods:**
- `spawnIn(Location)` - Now applies custom model if customModelKey is set
- `despawn()` - Cleans up the display entity when entity is removed

### 2. Entity Configuration Loading
**File:** `src/main/java/cz/vitekform/rPGCore/EntityLoader.java` (NEW)

This class loads entities from `entities.yml` similar to how ItemLoader works:
- Reads entity configurations from YAML
- Validates required fields (id, visibleName, entityType)
- Loads optional custom model/texture paths
- Populates EntityDictionary with loaded entities

### 3. EntityDictionary Updates
**File:** `src/main/java/cz/vitekform/rPGCore/EntityDictionary.java`

**Added:**
- `entities` Map - Stores loaded entities by ID
- `getEntity(String id)` - Retrieves an entity template by ID

### 4. Resource Pack Generation
**File:** `src/main/java/cz/vitekform/rPGCore/pluginUtils/ResourcePackGenerator.java`

**Added Support For:**
- Entity texture and model directories
- Collection of entities with custom assets
- Processing entity textures and models
- Auto-generation of simple models for texture-only entities
- Proper resource pack structure for entity assets

**New Methods:**
- `collectCustomAssetEntities()` - Finds entities needing custom assets
- `processEntity()` - Processes an entity's custom assets
- `addEntityTexture()` - Adds entity texture to resource pack
- `addCustomEntityModel()` - Adds custom entity model to resource pack
- `generateEntityModel()` - Auto-generates simple model for texture

### 5. Main Plugin Integration
**File:** `src/main/java/cz/vitekform/rPGCore/RPGCore.java`

**Changes:**
- Added EntityLoader initialization during plugin startup
- Entities are loaded before resource pack generation
- Updated SUMMON command to use loaded entities from config
- Added `cloneEntity()` helper method to create entity instances

### 6. Configuration File
**File:** `src/main/resources/entities.yml` (NEW)

Provides configuration structure for defining custom entities:
```yaml
entities:
  entity_id:
    id: "entity_id"
    visibleName: "Display Name"
    entityType: ZOMBIE
    # ... stats ...
    texture.path: "texture.png"
    model.path: "model.json"
```

### 7. Documentation
**Files:**
- `ENTITY_CUSTOMIZATION.md` - Complete guide for creating custom entities
- `examples/entity_customization/README.md` - Step-by-step examples
- `examples/entity_customization/entities_example.yml` - Sample configurations
- `examples/entity_customization/fire_demon_example.json` - Sample model file

## How It Works

### At Server Startup:
1. ItemLoader loads custom items from items.yml
2. **EntityLoader loads custom entities from entities.yml** (NEW)
3. ResourcePackGenerator processes both items and entities
4. Custom textures/models are packaged into a resource pack
5. Resource pack is uploaded or copied to distribution location

### When Spawning an Entity:
1. Entity is spawned at specified location
2. Base entity attributes are applied (health, name, etc.)
3. If `customModelKey` is set:
   - ItemDisplay entity is created at entity's location
   - Custom model item is applied to ItemDisplay
   - ItemDisplay rides on base entity
   - Base entity is made invisible
4. Entity is registered in storage

### When Entity is Despawned:
1. Display entity (if any) is removed
2. Boss bar (if any) is hidden from players
3. Base entity is removed from world and storage

## Technical Details

### Custom Model Display System
- Uses Minecraft's ItemDisplay entity feature (1.19.4+)
- ItemDisplay rides as passenger on base entity
- Base entity provides collision, AI, and behavior
- Display entity provides visual appearance
- Billboard mode set to VERTICAL for player-facing display

### Resource Pack Structure
Entity assets are added to resource pack at:
```
assets/rpgcore/
├── textures/item/
│   └── entity_name.png
├── models/item/
│   └── entity_name.json
└── items/
    └── entity_name.json (item definition)
```

### Entity Cloning
Template entities from EntityDictionary are cloned when spawned to prevent modification of templates. Each spawned entity is an independent instance.

## Benefits

1. **Visual Customization**: Entities can have unique appearances beyond vanilla Minecraft entities
2. **Flexibility**: Supports both simple texture-only entities and complex custom models
3. **Consistency**: Mirrors the proven RPGItem system architecture
4. **Maintainability**: Centralized configuration in entities.yml
5. **Performance**: Resource pack generated once at startup
6. **Safety**: Robust error handling prevents crashes from invalid configurations

## Usage Example

```yaml
# entities.yml
entities:
  fire_boss:
    id: "fire_boss"
    visibleName: "§cFire Lord"
    entityType: BLAZE
    level: 50
    maxHealth: 1000.0
    attack: 50.0
    texture.path: "fire_boss.png"
    model.path: "fire_boss.json"
```

```java
// Spawning in code
RPGEntity template = EntityDictionary.getEntity("fire_boss");
RPGEntity instance = RPGCore.cloneEntity(template);
instance.spawnIn(location);
```

## Security
- CodeQL security scan: **0 alerts**
- Proper input validation for file paths
- Exception handling for invalid configurations
- No exposure of sensitive data

## Future Enhancements (Optional)
- Animation support for custom models
- Multiple model variations per entity
- Model scaling configuration
- Custom hit boxes to match visual size
- Model rotation/orientation options

## Files Changed/Added

**Modified:**
- src/main/java/cz/vitekform/rPGCore/objects/RPGEntity.java
- src/main/java/cz/vitekform/rPGCore/EntityDictionary.java
- src/main/java/cz/vitekform/rPGCore/RPGCore.java
- src/main/java/cz/vitekform/rPGCore/pluginUtils/ResourcePackGenerator.java

**Added:**
- src/main/java/cz/vitekform/rPGCore/EntityLoader.java
- src/main/resources/entities.yml
- ENTITY_CUSTOMIZATION.md
- examples/entity_customization/README.md
- examples/entity_customization/entities_example.yml
- examples/entity_customization/fire_demon_example.json

## Testing Recommendations

1. Create a simple entity with texture only
2. Test spawning with `/rpg summon`
3. Verify custom model appears correctly
4. Test entity behavior (AI, collision, attacks)
5. Verify display entity cleanup on despawn
6. Test with invalid configurations to ensure error handling
7. Test resource pack generation and distribution
