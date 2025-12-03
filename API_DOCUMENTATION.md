# RPGCore Public API

This document describes how to use the RPGCore public API from your plugin.

## Overview

RPGCore provides a comprehensive public API that allows other plugins to:
- Register and retrieve custom RPG items
- Access and manage RPG entities
- Interact with RPG player data
- Reload items dynamically

## Getting Started

### 1. Add RPGCore as a Dependency

Add RPGCore to your plugin's dependencies in `plugin.yml`:

```yaml
depend: [RPGCore]
```

Or if it's optional:

```yaml
softdepend: [RPGCore]
```

### 2. Access the API

Use the `RPGCoreAPIProvider` class to get the API instance:

```java
import cz.vitekform.rPGCore.api.RPGCoreAPI;
import cz.vitekform.rPGCore.api.RPGCoreAPIProvider;

public class YourPlugin extends JavaPlugin {
    
    private RPGCoreAPI rpgCoreAPI;
    
    @Override
    public void onEnable() {
        // Check if RPGCore is available
        if (!RPGCoreAPIProvider.isAPIAvailable()) {
            getLogger().severe("RPGCore is not available!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        // Get the API instance
        rpgCoreAPI = RPGCoreAPIProvider.getAPI();
        getLogger().info("Successfully hooked into RPGCore!");
    }
}
```

## API Usage Examples

### Working with Items

#### Get an Item by ID

```java
RPGItem sword = rpgCoreAPI.getItem("adventurer_sword");
if (sword != null) {
    // Use the item
    ItemStack itemStack = sword.build();
    player.getInventory().addItem(itemStack);
}
```

#### Get an Item by UUID

```java
UUID itemUUID = // ... your item UUID
RPGItem item = rpgCoreAPI.getItem(itemUUID);
```

#### Register a Custom Item

```java
import cz.vitekform.rPGCore.objects.RPGItem;
import cz.vitekform.rPGCore.objects.RPGClass;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;

// Create a new RPG item
RPGItem customItem = new RPGItem();
customItem.itemName = Component.text("Custom Sword").color(NamedTextColor.GOLD);
customItem.material = Material.DIAMOND_SWORD;
customItem.attack = 50.0;
customItem.reqLevel = 10;
customItem.reqClass = RPGClass.WARRIOR;

// Register the item
boolean success = rpgCoreAPI.registerItem("custom_sword", customItem);
if (success) {
    getLogger().info("Custom item registered successfully!");
} else {
    getLogger().warning("Item ID already exists!");
}
```

#### Get All Items

```java
Map<String, RPGItem> allItems = rpgCoreAPI.getItemRegistry();
for (Map.Entry<String, RPGItem> entry : allItems.entrySet()) {
    String itemId = entry.getKey();
    RPGItem item = entry.getValue();
    // Process items...
}
```

### Working with Entities

#### Get an Entity

```java
UUID entityUUID = // ... entity UUID from event or other source
RPGEntity entity = rpgCoreAPI.getEntity(entityUUID);
if (entity != null) {
    getLogger().info("Entity name: " + entity.visibleName);
    getLogger().info("Entity level: " + entity.level);
}
```

#### Register a Custom Entity

```java
import cz.vitekform.rPGCore.objects.RPGEntity;
import org.bukkit.entity.EntityType;

// Create a new RPG entity
RPGEntity customEntity = new RPGEntity();
customEntity.visibleName = "Custom Boss";
customEntity.level = 50;
customEntity.maxHealth = 1000.0;
customEntity.health = 1000.0;
customEntity.attack = 75.0;
customEntity.defense = 30;
customEntity.isBoss = true;
customEntity.hasVisibleName = true;
customEntity.entityType = EntityType.ZOMBIE;

// Spawn the entity in the world
Location spawnLocation = // ... your location
Entity spawnedEntity = customEntity.spawnIn(spawnLocation);

// Register in the system (happens automatically in spawnIn, but can also be done manually)
rpgCoreAPI.registerEntity(spawnedEntity.getUniqueId(), customEntity);
```

#### Get All Entities

```java
Map<UUID, RPGEntity> allEntities = rpgCoreAPI.getEntityRegistry();
for (Map.Entry<UUID, RPGEntity> entry : allEntities.entrySet()) {
    UUID uuid = entry.getKey();
    RPGEntity entity = entry.getValue();
    // Process entities...
}
```

### Working with Players

#### Get an RPG Player

```java
import cz.vitekform.rPGCore.objects.RPGPlayer;

// Get by UUID
RPGPlayer rpgPlayer = rpgCoreAPI.getRPGPlayer(playerUUID);

// Or get by Bukkit Player object
Player bukkitPlayer = // ... your player
RPGPlayer rpgPlayer = rpgCoreAPI.getRPGPlayer(bukkitPlayer);

if (rpgPlayer != null) {
    getLogger().info("Player level: " + rpgPlayer.level);
    getLogger().info("Player class: " + rpgPlayer.rpgClass);
}
```

#### Access Player Stats

```java
RPGPlayer rpgPlayer = rpgCoreAPI.getRPGPlayer(player);
if (rpgPlayer != null) {
    double health = rpgPlayer.health;
    int mana = rpgPlayer.mana;
    int level = rpgPlayer.level;
    
    // Modify player stats
    rpgPlayer.addExperience(100);
    rpgPlayer.giveItem(customItem);
}
```

#### Get All Players

```java
Map<UUID, RPGPlayer> allPlayers = rpgCoreAPI.getPlayerRegistry();
for (Map.Entry<UUID, RPGPlayer> entry : allPlayers.entrySet()) {
    UUID uuid = entry.getKey();
    RPGPlayer player = entry.getValue();
    // Process players...
}
```

### Utility Methods

#### Reload Items

```java
// Reload all items from the configuration file
rpgCoreAPI.reloadItems();
getLogger().info("Items reloaded from configuration!");
```

#### Get Plugin Version

```java
String version = rpgCoreAPI.getVersion();
getLogger().info("RPGCore version: " + version);
```

## API Reference

### RPGCoreAPI Interface

All methods available in the API:

#### Item Methods
- `RPGItem getItem(String itemId)` - Get item by string ID
- `RPGItem getItem(UUID uuid)` - Get item by UUID
- `boolean registerItem(String itemId, RPGItem item)` - Register a new item
- `Map<String, RPGItem> getItemRegistry()` - Get all items by string ID
- `Map<UUID, RPGItem> getItemRegistryByUUID()` - Get all items by UUID

#### Entity Methods
- `RPGEntity getEntity(UUID uuid)` - Get entity by UUID
- `void registerEntity(UUID uuid, RPGEntity entity)` - Register an entity
- `RPGEntity removeEntity(UUID uuid)` - Remove an entity
- `Map<UUID, RPGEntity> getEntityRegistry()` - Get all entities

#### Player Methods
- `RPGPlayer getRPGPlayer(UUID uuid)` - Get player by UUID
- `RPGPlayer getRPGPlayer(Player player)` - Get player by Bukkit Player
- `void registerRPGPlayer(UUID uuid, RPGPlayer rpgPlayer)` - Register a player
- `RPGPlayer removeRPGPlayer(UUID uuid)` - Remove a player
- `Map<UUID, RPGPlayer> getPlayerRegistry()` - Get all players

#### Utility Methods
- `void reloadItems()` - Reload items from configuration
- `String getVersion()` - Get plugin version

## Thread Safety

The API provides unmodifiable views of the registries to prevent external modification. All modifications should be done through the provided API methods.

## Support

For issues, feature requests, or questions about the API, please visit the RPGCore repository.
