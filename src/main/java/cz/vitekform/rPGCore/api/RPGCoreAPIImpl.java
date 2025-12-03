package cz.vitekform.rPGCore.api;

import cz.vitekform.rPGCore.ItemDictionary;
import cz.vitekform.rPGCore.ItemLoader;
import cz.vitekform.rPGCore.RPGCore;
import cz.vitekform.rPGCore.objects.RPGEntity;
import cz.vitekform.rPGCore.objects.RPGItem;
import cz.vitekform.rPGCore.objects.RPGPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

/**
 * Implementation of the RPGCoreAPI interface.
 * This class provides concrete implementations for all public API methods.
 */
public class RPGCoreAPIImpl implements RPGCoreAPI {
    
    private final JavaPlugin plugin;
    
    public RPGCoreAPIImpl(JavaPlugin plugin) {
        this.plugin = plugin;
    }
    
    // ========== Item Registry Methods ==========
    
    @Override
    public RPGItem getItem(String itemId) {
        return ItemDictionary.getItem(itemId);
    }
    
    @Override
    public RPGItem getItem(UUID uuid) {
        return ItemDictionary.getItem(uuid);
    }
    
    @Override
    public boolean registerItem(String itemId, RPGItem item) {
        if (ItemDictionary.items.containsKey(itemId)) {
            return false;
        }
        
        // Generate UUID for the item
        UUID itemUUID = UUID.nameUUIDFromBytes(itemId.getBytes(StandardCharsets.UTF_8));
        
        // Register in both maps
        ItemDictionary.items.put(itemId, item);
        ItemDictionary.itemRegistry.put(itemUUID, item);
        
        return true;
    }
    
    @Override
    public Map<String, RPGItem> getItemRegistry() {
        return Collections.unmodifiableMap(ItemDictionary.items);
    }
    
    @Override
    public Map<UUID, RPGItem> getItemRegistryByUUID() {
        return Collections.unmodifiableMap(ItemDictionary.itemRegistry);
    }
    
    // ========== Entity Registry Methods ==========
    
    @Override
    public RPGEntity getEntity(UUID uuid) {
        return RPGCore.entityStorage.get(uuid);
    }
    
    @Override
    public void registerEntity(UUID uuid, RPGEntity entity) {
        RPGCore.entityStorage.put(uuid, entity);
    }
    
    @Override
    public RPGEntity removeEntity(UUID uuid) {
        return RPGCore.entityStorage.remove(uuid);
    }
    
    @Override
    public Map<UUID, RPGEntity> getEntityRegistry() {
        return Collections.unmodifiableMap(RPGCore.entityStorage);
    }
    
    // ========== Player Methods ==========
    
    @Override
    public RPGPlayer getRPGPlayer(UUID uuid) {
        return RPGCore.playerStorage.get(uuid);
    }
    
    @Override
    public RPGPlayer getRPGPlayer(Player player) {
        return RPGCore.playerStorage.get(player.getUniqueId());
    }
    
    @Override
    public void registerRPGPlayer(UUID uuid, RPGPlayer rpgPlayer) {
        RPGCore.playerStorage.put(uuid, rpgPlayer);
    }
    
    @Override
    public RPGPlayer removeRPGPlayer(UUID uuid) {
        return RPGCore.playerStorage.remove(uuid);
    }
    
    @Override
    public Map<UUID, RPGPlayer> getPlayerRegistry() {
        return Collections.unmodifiableMap(RPGCore.playerStorage);
    }
    
    // ========== Utility Methods ==========
    
    @Override
    public void reloadItems() {
        // Clear existing items
        ItemDictionary.items.clear();
        ItemDictionary.itemRegistry.clear();
        
        // Reload items from configuration
        ItemLoader itemLoader = new ItemLoader(plugin);
        itemLoader.loadItems();
    }
    
    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }
}
