package cz.vitekform.rPGCore.blockdisplay;

import cz.vitekform.rPGCore.BlockDictionary;
import cz.vitekform.rPGCore.objects.RPGBlock;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Manages custom blocks using BARRIER blocks with BlockDisplay entities for visual representation.
 * Uses file-based storage for block locations since BARRIER blocks don't support PersistentDataContainer.
 */
public class CustomBlockManager {
    
    private final Logger logger;
    private final Plugin plugin;
    private final Map<Location, BlockDisplay> displayCache = new HashMap<>();
    private final Map<Location, CustomBlockData> blockData = new HashMap<>();
    private final File dataFile;
    
    /**
     * Internal class to store custom block data
     */
    private static class CustomBlockData {
        String blockId;
        UUID displayUUID;
        int customModel;
        
        CustomBlockData(String blockId, UUID displayUUID, int customModel) {
            this.blockId = blockId;
            this.displayUUID = displayUUID;
            this.customModel = customModel;
        }
    }
    
    public CustomBlockManager(Plugin plugin, Logger logger) {
        this.plugin = plugin;
        this.logger = logger;
        this.dataFile = new File(plugin.getDataFolder(), "custom_blocks.yml");
        loadBlockData();
    }
    
    /**
     * Places a custom block at the specified location.
     * Creates a BARRIER block and spawns a BlockDisplay entity for visual representation.
     * 
     * @param location The location to place the block
     * @param rpgBlock The RPG block definition
     * @return true if successfully placed
     */
    public boolean placeCustomBlock(Location location, RPGBlock rpgBlock) {
        Block block = location.getBlock();
        
        // Set the physical block to BARRIER
        block.setType(Material.BARRIER);
        
        // Spawn BlockDisplay entity for visual representation
        BlockDisplay display = spawnBlockDisplay(location, rpgBlock);
        if (display == null) {
            return false;
        }
        
        // Store block data in our map (not in PDC since BARRIER doesn't support it)
        CustomBlockData data = new CustomBlockData(
            rpgBlock.blockId,
            display.getUniqueId(),
            rpgBlock.customBlockModel
        );
        blockData.put(normalizeLocation(location), data);
        displayCache.put(normalizeLocation(location), display);
        
        // Save to disk
        saveBlockData();
        
        return true;
    }
    
    /**
     * Removes a custom block and its associated BlockDisplay entity.
     * 
     * @param location The location of the block to remove
     */
    public void removeCustomBlock(Location location) {
        Location normalized = normalizeLocation(location);
        Block block = location.getBlock();
        
        // Get block data
        CustomBlockData data = blockData.get(normalized);
        if (data != null && data.displayUUID != null) {
            // Remove the BlockDisplay entity
            World world = location.getWorld();
            if (world != null) {
                org.bukkit.entity.Entity entity = world.getEntity(data.displayUUID);
                if (entity instanceof BlockDisplay) {
                    entity.remove();
                }
            }
        }
        
        // Remove from caches
        displayCache.remove(normalized);
        blockData.remove(normalized);
        
        // Remove the barrier block
        block.setType(Material.AIR);
        
        // Save to disk
        saveBlockData();
    }
    
    /**
     * Spawns a BlockDisplay entity at the specified location for visual representation.
     * The display is centered at x+0.5, y, z+0.5 relative to the block.
     * 
     * @param location The block location
     * @param rpgBlock The RPG block definition
     * @return The spawned BlockDisplay entity, or null if failed
     */
    private BlockDisplay spawnBlockDisplay(Location location, RPGBlock rpgBlock) {
        World world = location.getWorld();
        if (world == null) {
            return null;
        }
        
        // Center the display at x+0.5, y, z+0.5
        Location displayLoc = location.clone().add(0.5, 0, 0.5);
        
        BlockDisplay display = world.spawn(displayLoc, BlockDisplay.class, entity -> {
            // Set the block data for rendering
            // For custom models, we use a note_block with specific properties
            // The resource pack will override this based on custom_block_model
            BlockData blockData = getDisplayBlockData(rpgBlock);
            entity.setBlock(blockData);
            
            // Set transformation (1x1x1 block centered)
            Transformation transformation = new Transformation(
                new Vector3f(-0.5f, 0, -0.5f), // Translation to center
                new AxisAngle4f(0, 0, 0, 1),    // Left rotation (none)
                new Vector3f(1, 1, 1),           // Scale (1x1x1)
                new AxisAngle4f(0, 0, 0, 1)     // Right rotation (none)
            );
            entity.setTransformation(transformation);
            
            // Set display properties
            entity.setBrightness(new Display.Brightness(15, 15)); // Full brightness
            entity.setViewRange(128.0f); // Visible from 128 blocks away
            
            // Make it persistent
            entity.setPersistent(true);
        });
        
        return display;
    }
    
    /**
     * Gets the BlockData to use for the BlockDisplay entity.
     * Uses note_block by default, which can be overridden by resource packs.
     * 
     * @param rpgBlock The RPG block definition
     * @return The BlockData for the display
     */
    private BlockData getDisplayBlockData(RPGBlock rpgBlock) {
        // Use note_block as the base for custom models
        // The resource pack will override this based on customBlockModel stored in PDC
        Material displayMaterial = Material.NOTE_BLOCK;
        return Bukkit.createBlockData(displayMaterial);
    }
    
    /**
     * Gets the RPGBlock from a block at the specified location.
     * 
     * @param block The block to check
     * @return The RPGBlock or null if not a custom block
     */
    public RPGBlock getRPGBlock(Block block) {
        if (block.getType() != Material.BARRIER) {
            return null;
        }
        
        Location normalized = normalizeLocation(block.getLocation());
        CustomBlockData data = blockData.get(normalized);
        if (data == null) {
            return null;
        }
        
        return BlockDictionary.blocks.get(data.blockId);
    }
    
    /**
     * Normalizes a location to ensure consistent map keys.
     * Rounds coordinates to block positions.
     */
    private Location normalizeLocation(Location loc) {
        return new Location(
            loc.getWorld(),
            loc.getBlockX(),
            loc.getBlockY(),
            loc.getBlockZ()
        );
    }
    
    /**
     * Loads block data from disk.
     */
    private void loadBlockData() {
        if (!dataFile.exists()) {
            return;
        }
        
        try {
            FileConfiguration config = YamlConfiguration.loadConfiguration(dataFile);
            
            for (String key : config.getKeys(false)) {
                String[] parts = key.split(",");
                if (parts.length != 4) {
                    continue;
                }
                
                try {
                    World world = Bukkit.getWorld(parts[0]);
                    if (world == null) {
                        continue;
                    }
                    
                    int x = Integer.parseInt(parts[1]);
                    int y = Integer.parseInt(parts[2]);
                    int z = Integer.parseInt(parts[3]);
                    Location location = new Location(world, x, y, z);
                    
                    String blockId = config.getString(key + ".blockId");
                    String uuidStr = config.getString(key + ".displayUUID");
                    int customModel = config.getInt(key + ".customModel", 0);
                    
                    if (blockId != null && uuidStr != null) {
                        UUID displayUUID = UUID.fromString(uuidStr);
                        CustomBlockData data = new CustomBlockData(blockId, displayUUID, customModel);
                        blockData.put(location, data);
                    }
                } catch (Exception e) {
                    logger.warning("Failed to load custom block data for key: " + key);
                }
            }
            
            logger.info("Loaded " + blockData.size() + " custom blocks from disk");
        } catch (Exception e) {
            logger.severe("Failed to load custom blocks data: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Saves block data to disk.
     */
    private void saveBlockData() {
        try {
            FileConfiguration config = new YamlConfiguration();
            
            for (Map.Entry<Location, CustomBlockData> entry : blockData.entrySet()) {
                Location loc = entry.getKey();
                CustomBlockData data = entry.getValue();
                
                String key = loc.getWorld().getName() + "," +
                            loc.getBlockX() + "," +
                            loc.getBlockY() + "," +
                            loc.getBlockZ();
                
                config.set(key + ".blockId", data.blockId);
                config.set(key + ".displayUUID", data.displayUUID.toString());
                config.set(key + ".customModel", data.customModel);
            }
            
            config.save(dataFile);
        } catch (IOException e) {
            logger.severe("Failed to save custom blocks data: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Gets the BlockDisplay UUID for a block location.
     */
    private UUID getDisplayUUID(Location location) {
        Location normalized = normalizeLocation(location);
        CustomBlockData data = blockData.get(normalized);
        return data != null ? data.displayUUID : null;
    }
    
    /**
     * Restores BlockDisplay entities for custom blocks in a chunk.
     * Called when a chunk is loaded.
     */
    public void restoreDisplaysInChunk(Chunk chunk) {
        World world = chunk.getWorld();
        int chunkX = chunk.getX();
        int chunkZ = chunk.getZ();
        
        int restored = 0;
        for (Map.Entry<Location, CustomBlockData> entry : blockData.entrySet()) {
            Location loc = entry.getKey();
            
            // Check if this location is in the loaded chunk
            if (loc.getWorld().equals(world) &&
                loc.getBlockX() >> 4 == chunkX &&
                loc.getBlockZ() >> 4 == chunkZ) {
                
                CustomBlockData data = entry.getValue();
                
                // Check if BlockDisplay still exists
                org.bukkit.entity.Entity entity = world.getEntity(data.displayUUID);
                if (entity instanceof BlockDisplay) {
                    displayCache.put(loc, (BlockDisplay) entity);
                } else {
                    // BlockDisplay is missing, respawn it
                    RPGBlock rpgBlock = BlockDictionary.blocks.get(data.blockId);
                    if (rpgBlock != null) {
                        BlockDisplay display = spawnBlockDisplay(loc, rpgBlock);
                        if (display != null) {
                            data.displayUUID = display.getUniqueId();
                            displayCache.put(loc, display);
                            restored++;
                        }
                    }
                }
            }
        }
        
        if (restored > 0) {
            logger.info("Restored " + restored + " BlockDisplay entities in chunk " + chunkX + "," + chunkZ);
            saveBlockData();
        }
    }
    
    /**
     * Cleans up BlockDisplay cache when a chunk unloads.
     */
    public void cleanupChunk(Chunk chunk) {
        World world = chunk.getWorld();
        int chunkX = chunk.getX();
        int chunkZ = chunk.getZ();
        
        displayCache.entrySet().removeIf(entry -> {
            Location loc = entry.getKey();
            return loc.getWorld().equals(world) &&
                   loc.getBlockX() >> 4 == chunkX &&
                   loc.getBlockZ() >> 4 == chunkZ;
        });
    }
}
