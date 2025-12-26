# Testing Guide for RPGEntity Custom Models/Textures

This guide provides step-by-step instructions for testing the custom model and texture functionality for RPGEntity.

## Prerequisites

1. Minecraft Server running Paper 1.21+ (or compatible)
2. RPGCore plugin installed
3. Basic understanding of Minecraft resource packs
4. Image editing software (for creating textures)

## Test Plan

### Test 1: Basic Entity Loading

**Objective**: Verify entities load correctly from entities.yml

**Steps:**
1. Ensure `entities.yml` exists in `plugins/RPGCore/`
2. Start the server
3. Check server logs for:
   - "Loaded entity: bandit_tutorial_melee"
   - "Loaded X entities from entities.yml"

**Expected Result**: Entities load without errors

**Status**: [ ] Pass [ ] Fail

---

### Test 2: Entity Spawning (No Custom Model)

**Objective**: Verify basic entity spawning works

**Steps:**
1. Join the server as a player
2. Run `/rpg summon`
3. Observe the spawned entity

**Expected Result**:
- Entity spawns at player location
- Entity has correct name (visible above head)
- Entity behaves according to its type (Vindicator AI)

**Status**: [ ] Pass [ ] Fail

---

### Test 3: Texture-Only Custom Entity

**Objective**: Test entity with custom texture (auto-generated model)

**Setup:**
1. Create a 16x16 PNG texture file named `test_entity.png`
2. Place it in `plugins/RPGCore/entities/textures/`
3. Add to `entities.yml`:
```yaml
test_entity:
  id: "test_entity"
  visibleName: "§aTest Entity"
  entityType: ZOMBIE
  level: 1
  maxHealth: 20.0
  health: 20.0
  attack: 1.0
  defense: 0
  speed: 0.2
  experienceAfterDefeat: 5
  drops: []
  isBoss: false
  isFriendly: false
  hasVisibleName: true
  texture.path: "test_entity.png"
```

**Steps:**
1. Restart server
2. Check logs for "Added entity texture for test_entity"
3. Check logs for "Generated entity model for test_entity"
4. Accept resource pack when prompted
5. Update entities.yml to make test_entity the summon target
6. Run `/rpg summon`

**Expected Result**:
- Resource pack generates successfully
- Entity spawns with custom texture visible
- Base entity is invisible
- ItemDisplay entity is visible with custom texture

**Status**: [ ] Pass [ ] Fail

**Notes:**
_______________________________________

---

### Test 4: Full Custom Model Entity

**Objective**: Test entity with custom texture AND custom model

**Setup:**
1. Create texture: `custom_boss.png`
2. Create model file `custom_boss.json`:
```json
{
  "parent": "minecraft:item/generated",
  "textures": {
    "layer0": "rpgcore:item/custom_boss"
  },
  "display": {
    "head": {
      "scale": [2, 2, 2]
    }
  }
}
```
3. Place texture in `plugins/RPGCore/entities/textures/`
4. Place model in `plugins/RPGCore/entities/models/`
5. Add to entities.yml:
```yaml
custom_boss:
  id: "custom_boss"
  visibleName: "§cCustom Boss"
  entityType: ZOMBIE
  level: 10
  maxHealth: 200.0
  health: 200.0
  attack: 20.0
  defense: 10
  speed: 0.2
  experienceAfterDefeat: 100
  drops: []
  isBoss: true
  isFriendly: false
  hasVisibleName: true
  texture.path: "custom_boss.png"
  model.path: "custom_boss.json"
```

**Steps:**
1. Restart server
2. Check logs for texture and model additions
3. Accept resource pack
4. Spawn the entity
5. Verify model appears larger (2x scale)

**Expected Result**:
- Custom model applies correctly
- Model is scaled as specified
- Boss bar appears (isBoss: true)

**Status**: [ ] Pass [ ] Fail

---

### Test 5: Entity Behavior with Custom Model

**Objective**: Verify entity behavior is not affected by custom model

**Steps:**
1. Spawn entity with custom model
2. Test entity AI (should follow/attack player for hostile)
3. Attack the entity
4. Verify health decreases
5. Verify entity dies and drops loot

**Expected Result**:
- Entity AI works normally
- Combat works correctly
- Collision detection works
- Entity can be killed

**Status**: [ ] Pass [ ] Fail

---

### Test 6: Display Entity Cleanup

**Objective**: Ensure display entities are removed properly

**Steps:**
1. Spawn entity with custom model
2. Note the entity count: `/minecraft:entity list`
3. Kill the entity or use despawn
4. Check entity count again

**Expected Result**:
- Both base entity AND display entity are removed
- No orphaned entities remain

**Status**: [ ] Pass [ ] Fail

---

### Test 7: Multiple Entities

**Objective**: Test multiple custom entities at once

**Steps:**
1. Configure 3 different custom entities
2. Spawn all 3 in different locations
3. Verify each shows correct custom model

**Expected Result**:
- All entities display their respective custom models
- No model conflicts or errors

**Status**: [ ] Pass [ ] Fail

---

### Test 8: Error Handling - Missing Texture

**Objective**: Test graceful handling of missing texture file

**Setup:**
Configure entity with texture.path pointing to non-existent file

**Steps:**
1. Restart server
2. Check logs for warning message

**Expected Result**:
- Warning logged: "Entity texture file not found..."
- Server continues running
- Entity spawns without custom model

**Status**: [ ] Pass [ ] Fail

---

### Test 9: Error Handling - Invalid Model Key

**Objective**: Test handling of malformed customModelKey

**Setup:**
Manually set customModelKey to invalid format in code test

**Expected Result**:
- Error caught and logged
- Display entity cleaned up
- No server crash

**Status**: [ ] Pass [ ] Fail

---

### Test 10: Resource Pack Generation

**Objective**: Verify resource pack contains entity assets

**Steps:**
1. After server start, locate: `plugins/RPGCore/generated/resourcepack.zip`
2. Extract and examine contents
3. Verify structure:
   - `assets/rpgcore/textures/item/entity_name.png`
   - `assets/rpgcore/models/item/entity_name.json`
   - `assets/rpgcore/items/entity_name.json`

**Expected Result**:
- All entity assets are present
- File paths are correct
- Model references correct texture

**Status**: [ ] Pass [ ] Fail

---

### Test 11: Entity Cloning

**Objective**: Verify entity templates are cloned, not reused

**Steps:**
1. Spawn same entity type twice
2. Damage one entity
3. Verify other entity health unchanged

**Expected Result**:
- Each entity is independent instance
- Modifying one doesn't affect the other

**Status**: [ ] Pass [ ] Fail

---

### Test 12: Boss Entities with Custom Models

**Objective**: Test boss-specific features with custom models

**Steps:**
1. Spawn boss entity with custom model
2. Verify boss bar appears
3. Verify boss bar shows correct name
4. Damage boss, verify boss bar updates

**Expected Result**:
- Boss bar works correctly
- Custom model displays properly
- Boss-specific features function normally

**Status**: [ ] Pass [ ] Fail

---

## Performance Tests

### Test 13: Multiple Spawns Performance

**Objective**: Ensure performance is acceptable with many custom entities

**Steps:**
1. Spawn 10 entities with custom models
2. Monitor server TPS
3. Monitor memory usage

**Expected Result**:
- Server maintains good TPS (19-20)
- No memory leaks
- Reasonable CPU usage

**Status**: [ ] Pass [ ] Fail

---

## Integration Tests

### Test 14: Compatibility with Oraxen

**Objective**: Verify integration with Oraxen if present

**Steps:**
1. Install Oraxen plugin
2. Restart server with both plugins
3. Verify resource pack copied to Oraxen directory

**Expected Result**:
- Resource pack copied to `plugins/Oraxen/pack/uploads/`
- No conflicts between plugins

**Status**: [ ] Pass [ ] Fail [ ] N/A (Oraxen not present)

---

## Regression Tests

### Test 15: Existing Features Still Work

**Objective**: Ensure existing RPGCore features unaffected

**Steps:**
1. Test item system (custom items)
2. Test player system (RPGPlayer)
3. Test other entity features (drops, experience)

**Expected Result**:
- All existing features work as before
- No breaking changes

**Status**: [ ] Pass [ ] Fail

---

## Documentation Tests

### Test 16: Documentation Accuracy

**Objective**: Verify documentation matches implementation

**Steps:**
1. Follow ENTITY_CUSTOMIZATION.md guide
2. Follow examples in examples/ directory
3. Try each example configuration

**Expected Result**:
- All documentation is accurate
- Examples work as described
- No missing steps or information

**Status**: [ ] Pass [ ] Fail

---

## Test Summary

Total Tests: 16
Passed: ___
Failed: ___
N/A: ___

## Issues Found

(List any issues discovered during testing)

1. 
2. 
3. 

## Recommendations

(List any recommendations for improvements)

1. 
2. 
3. 

---

## Tested By

Name: _________________
Date: _________________
Server Version: _________________
Plugin Version: _________________
