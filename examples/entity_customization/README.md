# Entity Customization Examples

This directory contains example files for creating custom entities with custom models and textures.

## Example 1: Simple Entity with Texture Only

**Configuration (entities.yml):**
```yaml
custom_warrior:
  id: "custom_warrior"
  visibleName: "§cCustom Warrior"
  entityType: ZOMBIE
  level: 5
  maxHealth: 100.0
  health: 100.0
  attack: 12.0
  defense: 8
  speed: 0.25
  experienceAfterDefeat: 50
  drops:
    - "adventurer_sword"
  isBoss: false
  isFriendly: false
  hasVisibleName: true
  texture.path: "custom_warrior.png"
```

**Steps:**
1. Create a 16x16 PNG texture file named `custom_warrior.png`
2. Place it in `/plugins/RPGCore/entities/textures/`
3. Add the configuration above to `entities.yml`
4. Restart the server
5. The resource pack will auto-generate a model for the texture

## Example 2: Entity with Custom Model

**Configuration (entities.yml):**
```yaml
fire_demon:
  id: "fire_demon"
  visibleName: "§6Fire Demon"
  entityType: BLAZE
  level: 20
  maxHealth: 500.0
  health: 500.0
  attack: 30.0
  defense: 15
  speed: 0.2
  experienceAfterDefeat: 200
  drops:
    - "flame_sword"
  isBoss: true
  isFriendly: false
  hasVisibleName: true
  texture.path: "fire_demon.png"
  model.path: "fire_demon.json"
```

**Model file (fire_demon.json):**
```json
{
  "parent": "minecraft:item/generated",
  "textures": {
    "layer0": "rpgcore:item/fire_demon"
  },
  "display": {
    "head": {
      "scale": [2, 2, 2]
    }
  }
}
```

**Steps:**
1. Create the texture file `fire_demon.png` (16x16 or 32x32)
2. Create the model file `fire_demon.json`
3. Place texture in `/plugins/RPGCore/entities/textures/`
4. Place model in `/plugins/RPGCore/entities/models/`
5. Add the configuration to `entities.yml`
6. Restart the server

## Example 3: Boss Entity with Advanced Model

**Configuration (entities.yml):**
```yaml
dragon_lord:
  id: "dragon_lord"
  visibleName: "§4Dragon Lord"
  entityType: ENDER_DRAGON
  level: 100
  maxHealth: 10000.0
  health: 10000.0
  attack: 100.0
  defense: 50
  speed: 0.15
  experienceAfterDefeat: 5000
  drops:
    - "dragon_sword"
    - "dragon_helmet"
  isBoss: true
  isFriendly: false
  hasVisibleName: true
  texture.path: "dragon_lord.png"
  model.path: "dragon_lord.json"
```

## Testing Your Custom Entity

Once configured, you can test your entity using:

```
/rpg summon
```

This will spawn a test entity. To spawn a specific entity programmatically:

```java
RPGEntity template = EntityDictionary.getEntity("fire_demon");
if (template != null) {
    RPGEntity instance = cloneEntity(template);
    instance.spawnIn(location);
}
```

## Tips for Creating Textures

1. Use 16x16 or 32x32 pixel textures for best results
2. PNG format with transparency support
3. Keep file sizes small for faster resource pack loading
4. Test textures in a resource pack editor first

## Tips for Creating Models

1. Start with simple "generated" models
2. Reference the texture using `rpgcore:item/<entity_id>`
3. Use Blockbench or similar tools for complex models
4. Test models in-game with `/reload` command

## Troubleshooting

- **Model not appearing:** Check server logs for errors, verify file paths
- **Invalid key error:** Ensure entity ID contains only lowercase letters, numbers, and underscores
- **Resource pack not loading:** Check that files exist in correct directories
- **Display entity visible but no model:** Verify the customModelKey was set correctly
