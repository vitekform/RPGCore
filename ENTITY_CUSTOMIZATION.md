# Custom Entity Models and Textures Guide

This guide explains how to create and use custom models and textures for RPGCore entities.

## Overview

RPGCore supports custom models and textures for entities through resource packs. Entities can have:
- Custom textures (PNG images)
- Custom models (JSON model files)

When an entity with a custom model is spawned, it uses an ItemDisplay entity to show the custom model while the base entity is made invisible.

## Directory Structure

Place your custom assets in the following directories:

```
plugins/RPGCore/
├── entities/
│   ├── textures/
│   │   └── your_texture.png
│   └── models/
│       └── your_model.json
```

## Configuration in entities.yml

Add custom model/texture properties to your entity configuration:

```yaml
entities:
  custom_bandit:
    id: "custom_bandit"
    visibleName: "Custom Bandit"
    entityType: VINDICATOR
    level: 5
    maxHealth: 50.0
    health: 50.0
    attack: 10.0
    defense: 5
    speed: 0.25
    experienceAfterDefeat: 25
    drops:
      - "adventurer_sword"
    isBoss: false
    isFriendly: false
    hasVisibleName: true
    # Custom model and texture
    texture.path: "custom_bandit.png"
    model.path: "custom_bandit.json"
```

## Creating Custom Textures

1. Create a PNG image (recommended size: 16x16 or 32x32 pixels)
2. Save it in `plugins/RPGCore/entities/textures/`
3. Reference it in entities.yml using `texture.path`

## Creating Custom Models

You can create custom models in two ways:

### Option 1: Use texture.path only (Auto-generated model)
If you only specify `texture.path`, RPGCore will automatically generate a simple "generated" model that displays your texture.

### Option 2: Provide a custom model file
Create a JSON model file and place it in `plugins/RPGCore/entities/models/`. Reference it using `model.path`.

Example model file (`custom_bandit.json`):
```json
{
  "parent": "minecraft:item/generated",
  "textures": {
    "layer0": "rpgcore:item/custom_bandit"
  }
}
```

## How It Works

1. When you start the server, RPGCore loads entities from `entities.yml`
2. The ResourcePackGenerator creates a resource pack including all custom entity assets
3. The resource pack is automatically generated in `plugins/RPGCore/generated/`
4. When an entity with custom assets is spawned:
   - An ItemDisplay entity is created
   - The custom model is applied to the ItemDisplay
   - The ItemDisplay rides on top of the base entity
   - The base entity is made invisible

## Testing Your Custom Entity

1. Place your texture and/or model files in the appropriate directories
2. Configure your entity in `entities.yml`
3. Restart the server or reload the plugin
4. Use `/rpg summon` to spawn the entity (if configured as the default test entity)
5. Or spawn it programmatically using the EntityDictionary

## Example: Complete Custom Entity

Here's a complete example of a custom boss entity:

**entities.yml:**
```yaml
  flame_lord:
    id: "flame_lord"
    visibleName: "§cFlame Lord"
    entityType: BLAZE
    level: 50
    maxHealth: 1000.0
    health: 1000.0
    attack: 50.0
    defense: 30
    speed: 0.15
    experienceAfterDefeat: 1000
    drops:
      - "flame_sword"
      - "flame_armor"
    isBoss: true
    isFriendly: false
    hasVisibleName: true
    texture.path: "flame_lord.png"
    model.path: "flame_lord.json"
```

**File locations:**
- Texture: `plugins/RPGCore/entities/textures/flame_lord.png`
- Model: `plugins/RPGCore/entities/models/flame_lord.json`

## Notes

- Custom models are displayed using Minecraft's ItemDisplay entity feature
- The resource pack is automatically uploaded to catbox.moe (if Oraxen is not present) or copied to Oraxen's upload directory
- Players must accept the resource pack to see custom models
- The base entity type (entityType) is still used for entity behavior, AI, and collision
