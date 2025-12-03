package cz.vitekform.rPGCore.api;

import cz.vitekform.rPGCore.objects.RPGEntity;
import cz.vitekform.rPGCore.objects.RPGItem;
import cz.vitekform.rPGCore.objects.RPGPlayer;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

/**
 * Public API for RPGCore plugin.
 * This interface provides access to all core functionality that other plugins might need.
 */
public interface RPGCoreAPI {
    
    // ========== Item Registry Methods ==========
    
    /**
     * Get an item by its string identifier from the item registry.
     * @param itemId The item identifier (e.g., "adventurer_sword")
     * @return The RPGItem or null if not found
     */
    RPGItem getItem(String itemId);
    
    /**
     * Get an item by its UUID from the item registry.
     * @param uuid The item UUID
     * @return The RPGItem or null if not found
     */
    RPGItem getItem(UUID uuid);
    
    /**
     * Register a new item in the item registry.
     * @param itemId The unique identifier for the item
     * @param item The RPGItem to register
     * @return true if the item was registered successfully, false if the ID already exists
     */
    boolean registerItem(String itemId, RPGItem item);
    
    /**
     * Get all registered items as a map of item IDs to RPGItems.
     * @return Unmodifiable map of all registered items
     */
    Map<String, RPGItem> getItemRegistry();
    
    /**
     * Get all registered items by UUID.
     * @return Unmodifiable map of UUIDs to RPGItems
     */
    Map<UUID, RPGItem> getItemRegistryByUUID();
    
    // ========== Entity Registry Methods ==========
    
    /**
     * Get an RPGEntity by its UUID.
     * @param uuid The entity UUID
     * @return The RPGEntity or null if not found
     */
    RPGEntity getEntity(UUID uuid);
    
    /**
     * Register a new entity in the entity storage.
     * @param uuid The unique identifier for the entity
     * @param entity The RPGEntity to register
     */
    void registerEntity(UUID uuid, RPGEntity entity);
    
    /**
     * Remove an entity from the entity storage.
     * @param uuid The entity UUID to remove
     * @return The removed RPGEntity or null if not found
     */
    RPGEntity removeEntity(UUID uuid);
    
    /**
     * Get all registered entities.
     * @return Unmodifiable map of all registered entities
     */
    Map<UUID, RPGEntity> getEntityRegistry();
    
    // ========== Player Methods ==========
    
    /**
     * Get an RPGPlayer by their UUID.
     * @param uuid The player UUID
     * @return The RPGPlayer or null if not found
     */
    RPGPlayer getRPGPlayer(UUID uuid);
    
    /**
     * Get an RPGPlayer by the Bukkit Player object.
     * @param player The Bukkit Player
     * @return The RPGPlayer or null if not found
     */
    RPGPlayer getRPGPlayer(Player player);
    
    /**
     * Register a new RPGPlayer.
     * @param uuid The player UUID
     * @param rpgPlayer The RPGPlayer to register
     */
    void registerRPGPlayer(UUID uuid, RPGPlayer rpgPlayer);
    
    /**
     * Remove an RPGPlayer from storage.
     * @param uuid The player UUID to remove
     * @return The removed RPGPlayer or null if not found
     */
    RPGPlayer removeRPGPlayer(UUID uuid);
    
    /**
     * Get all registered RPG players.
     * @return Unmodifiable map of all registered players
     */
    Map<UUID, RPGPlayer> getPlayerRegistry();
    
    // ========== Utility Methods ==========
    
    /**
     * Reload items from the items.yml configuration file.
     * This will clear and reload all items from the configuration.
     */
    void reloadItems();
    
    /**
     * Get the version of the RPGCore plugin.
     * @return The plugin version string
     */
    String getVersion();
}
