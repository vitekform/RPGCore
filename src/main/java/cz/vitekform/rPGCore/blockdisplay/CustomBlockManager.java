package cz.vitekform.rPGCore.blockdisplay;

import cz.vitekform.rPGCore.BlockDictionary;
import cz.vitekform.rPGCore.objects.RPGBlock;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Manages custom blocks using BARRIER blocks with BlockDisplay entities for visual representation.
 */
public class CustomBlockManager {
    
    private static final NamespacedKey BLOCK_ID_KEY = new NamespacedKey("rpgcore", "rpg_block_id");
    private static final NamespacedKey DISPLAY_UUID_KEY = new NamespacedKey("rpgcore", "display_uuid");
    private static final NamespacedKey CUSTOM_MODEL_KEY = new NamespacedKey("rpgcore", "custom_block_model");
    
    private final Logger logger;
    private final Map<Location, BlockDisplay> displayCache = new HashMap<>();
    
    public CustomBlockManager(Logger logger) {
        this.logger = logger;
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
        
        // Store block data in PDC
        org.bukkit.block.BlockState state = block.getState();
        if (!(state instanceof org.bukkit.block.TileState)) {
            logger.warning("BARRIER block at " + location + " is not a TileState!");
            return false;
        }
        
        org.bukkit.block.TileState tileState = (org.bukkit.block.TileState) state;
        PersistentDataContainer pdc = tileState.getPersistentDataContainer();
        
        // Store block ID and custom model
        pdc.set(BLOCK_ID_KEY, PersistentDataType.STRING, rpgBlock.blockId);
        if (rpgBlock.customBlockModel > 0) {
            pdc.set(CUSTOM_MODEL_KEY, PersistentDataType.INTEGER, rpgBlock.customBlockModel);
        }
        
        // Spawn BlockDisplay entity for visual representation
        BlockDisplay display = spawnBlockDisplay(location, rpgBlock);
        if (display != null) {
            pdc.set(DISPLAY_UUID_KEY, PersistentDataType.STRING, display.getUniqueId().toString());
            displayCache.put(location, display);
        }
        
        tileState.update(true, false);
        return true;
    }
    
    /**
     * Removes a custom block and its associated BlockDisplay entity.
     * 
     * @param location The location of the block to remove
     */
    public void removeCustomBlock(Location location) {
        Block block = location.getBlock();
        
        // Get BlockDisplay UUID from PDC
        UUID displayUUID = getDisplayUUID(block);
        if (displayUUID != null) {
            // Remove the BlockDisplay entity
            World world = location.getWorld();
            if (world != null) {
                org.bukkit.entity.Entity entity = world.getEntity(displayUUID);
                if (entity instanceof BlockDisplay) {
                    entity.remove();
                }
            }
        }
        
        // Remove from cache
        displayCache.remove(location);
        
        // Remove the barrier block
        block.setType(Material.AIR);
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
        
        org.bukkit.block.BlockState state = block.getState();
        if (!(state instanceof org.bukkit.block.TileState)) {
            return null;
        }
        
        org.bukkit.block.TileState tileState = (org.bukkit.block.TileState) state;
        PersistentDataContainer pdc = tileState.getPersistentDataContainer();
        
        if (!pdc.has(BLOCK_ID_KEY, PersistentDataType.STRING)) {
            return null;
        }
        
        String blockId = pdc.get(BLOCK_ID_KEY, PersistentDataType.STRING);
        return BlockDictionary.getBlock(blockId);
    }
    
    /**
     * Gets the BlockDisplay UUID from a block's PDC.
     * 
     * @param block The block
     * @return The UUID or null if not found
     */
    private UUID getDisplayUUID(Block block) {
        org.bukkit.block.BlockState state = block.getState();
        if (!(state instanceof org.bukkit.block.TileState)) {
            return null;
        }
        
        org.bukkit.block.TileState tileState = (org.bukkit.block.TileState) state;
        PersistentDataContainer pdc = tileState.getPersistentDataContainer();
        
        if (!pdc.has(DISPLAY_UUID_KEY, PersistentDataType.STRING)) {
            return null;
        }
        
        String uuidStr = pdc.get(DISPLAY_UUID_KEY, PersistentDataType.STRING);
        try {
            return UUID.fromString(uuidStr);
        } catch (IllegalArgumentException e) {
            logger.warning("Invalid UUID in block PDC: " + uuidStr);
            return null;
        }
    }
    
    /**
     * Restores BlockDisplay entities for custom blocks in a chunk.
     * Called when a chunk loads.
     * 
     * @param chunk The chunk that was loaded
     */
    public void restoreDisplaysInChunk(org.bukkit.Chunk chunk) {
        World world = chunk.getWorld();
        int chunkX = chunk.getX() * 16;
        int chunkZ = chunk.getZ() * 16;
        
        // Scan all blocks in the chunk for custom blocks
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = world.getMinHeight(); y < world.getMaxHeight(); y++) {
                    Block block = world.getBlockAt(chunkX + x, y, chunkZ + z);
                    if (block.getType() == Material.BARRIER) {
                        RPGBlock rpgBlock = getRPGBlock(block);
                        if (rpgBlock != null) {
                            // Check if BlockDisplay exists
                            UUID displayUUID = getDisplayUUID(block);
                            if (displayUUID != null) {
                                org.bukkit.entity.Entity entity = world.getEntity(displayUUID);
                                if (entity == null || !(entity instanceof BlockDisplay)) {
                                    // Display is missing, respawn it
                                    Location location = block.getLocation();
                                    BlockDisplay display = spawnBlockDisplay(location, rpgBlock);
                                    if (display != null) {
                                        // Update PDC with new UUID
                                        org.bukkit.block.BlockState state = block.getState();
                                        if (state instanceof org.bukkit.block.TileState) {
                                            org.bukkit.block.TileState tileState = (org.bukkit.block.TileState) state;
                                            PersistentDataContainer pdc = tileState.getPersistentDataContainer();
                                            pdc.set(DISPLAY_UUID_KEY, PersistentDataType.STRING, display.getUniqueId().toString());
                                            tileState.update(true, false);
                                        }
                                        displayCache.put(location, display);
                                    }
                                } else {
                                    displayCache.put(block.getLocation(), (BlockDisplay) entity);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Cleans up BlockDisplay entities in a chunk.
     * Called when a chunk unloads.
     * 
     * @param chunk The chunk that was unloaded
     */
    public void cleanupDisplaysInChunk(org.bukkit.Chunk chunk) {
        World world = chunk.getWorld();
        int chunkX = chunk.getX() * 16;
        int chunkZ = chunk.getZ() * 16;
        
        // Remove from cache
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = world.getMinHeight(); y < world.getMaxHeight(); y++) {
                    Location location = new Location(world, chunkX + x, y, chunkZ + z);
                    displayCache.remove(location);
                }
            }
        }
    }
}
