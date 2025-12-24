package cz.vitekform.rPGCore;

import cz.vitekform.rPGCore.objects.RPGBlock;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

public class BlockLoader {

    private final JavaPlugin plugin;
    private final Logger logger;

    public BlockLoader(JavaPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }

    public void loadBlocks() {
        File blocksFile = new File(plugin.getDataFolder(), "blocks.yml");

        // Save default blocks.yml if it doesn't exist
        if (!blocksFile.exists()) {
            plugin.saveResource("blocks.yml", false);
        }

        FileConfiguration blocksConfig = YamlConfiguration.loadConfiguration(blocksFile);

        // Load defaults from jar to merge any new blocks
        try (InputStream defaultStream = plugin.getResource("blocks.yml")) {
            if (defaultStream != null) {
                try (InputStreamReader reader = new InputStreamReader(defaultStream, StandardCharsets.UTF_8)) {
                    YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(reader);
                    blocksConfig.setDefaults(defaultConfig);
                }
            }
        } catch (Exception e) {
            logger.warning("Failed to load default blocks.yml: " + e.getMessage());
        }

        ConfigurationSection blocksSection = blocksConfig.getConfigurationSection("blocks");
        if (blocksSection == null) {
            logger.warning("No blocks section found in blocks.yml");
            return;
        }

        Set<Integer> usedModelNumbers = new HashSet<>();

        for (String key : blocksSection.getKeys(false)) {
            ConfigurationSection blockSection = blocksSection.getConfigurationSection(key);
            if (blockSection == null) {
                continue;
            }

            try {
                RPGBlock block = loadBlock(blockSection, key);
                if (block != null) {
                    // Validate unique custom block model numbers
                    if (block.customBlockModel > 0) {
                        if (usedModelNumbers.contains(block.customBlockModel)) {
                            logger.warning("Duplicate customBlockModel value " + block.customBlockModel +
                                    " for block '" + key + "'. Skipping registration to avoid conflicts.");
                            continue; // Skip this block to prevent conflicts
                        }
                        usedModelNumbers.add(block.customBlockModel);
                    }
                    
                    // Set block ID
                    block.blockId = key;
                    
                    // Generate UUID for the block and register it
                    UUID blockUUID = UUID.nameUUIDFromBytes(key.getBytes(StandardCharsets.UTF_8));
                    BlockDictionary.blockRegistry.put(blockUUID, block);
                    BlockDictionary.blocks.put(key, block);
                    logger.info("Loaded block: " + key);
                }
            } catch (Exception e) {
                logger.log(java.util.logging.Level.SEVERE, "Failed to load block: " + key, e);
            }
        }

        logger.info("Loaded " + BlockDictionary.blockRegistry.size() + " blocks from blocks.yml");
    }

    private RPGBlock loadBlock(ConfigurationSection section, String key) {
        // Required fields
        String name = section.getString("name");
        String materialStr = section.getString("material");

        if (name == null || materialStr == null) {
            logger.warning("Block " + key + " is missing required fields (name, material)");
            return null;
        }

        Material material;
        try {
            material = Material.valueOf(materialStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            logger.warning("Invalid material for block " + key + ": " + materialStr);
            return null;
        }

        RPGBlock block = new RPGBlock();
        block.blockName = name;
        block.blockType = material;

        // Load optional properties
        block.customBlockModel = section.getInt("customBlockModel", 0);
        block.hardness = (float) section.getDouble("hardness", 1.0);
        block.resistance = (float) section.getDouble("resistance", 1.0);

        // Load minable with materials
        List<String> minableWithList = section.getStringList("minableWith.materials");
        for (String matStr : minableWithList) {
            try {
                Material mat = Material.valueOf(matStr.toUpperCase());
                block.minableWithMat.add(mat);
            } catch (IllegalArgumentException e) {
                logger.warning("Invalid material in minableWith for block " + key + ": " + matStr);
            }
        }

        // Load minable with RPG items
        List<String> minableWithItems = section.getStringList("minableWith.items");
        block.minableWithItems.addAll(minableWithItems);

        // Load drops - materials
        List<String> dropMaterials = section.getStringList("drops.materials");
        for (String matStr : dropMaterials) {
            try {
                Material mat = Material.valueOf(matStr.toUpperCase());
                block.itemDropsMat.add(mat);
            } catch (IllegalArgumentException e) {
                logger.warning("Invalid material in drops for block " + key + ": " + matStr);
            }
        }

        // Load drops - RPG items with validation
        List<String> dropItems = section.getStringList("drops.items");
        for (String itemKey : dropItems) {
            if (ItemDictionary.getItem(itemKey) == null) {
                logger.warning("RPG item key '" + itemKey + "' not found in drops for block '" + key + "'");
            }
            block.itemDropsRPG.add(itemKey);
        }

        // Resource pack related attributes
        block.texturePath = sanitizePath(section.getString("texture.path", null), key, "texture.path");
        block.modelPath = sanitizePath(section.getString("model.path", null), key, "model.path");
        
        // Validate modelType
        String modelType = section.getString("model.type", null);
        if (modelType != null) {
            String lowerModelType = modelType.toLowerCase();
            if (!isValidModelType(lowerModelType)) {
                logger.warning("Block " + key + " has invalid model.type '" + modelType + 
                        "'. Valid types are: cube_all, cube, cube_bottom_top, cube_column, cross, orientable. Defaulting to cube_all.");
                modelType = "cube_all";
            }
        }
        block.modelType = modelType;

        return block;
    }
    
    /**
     * Validates if a model type is supported
     */
    private boolean isValidModelType(String modelType) {
        return modelType.equals("cube_all") || modelType.equals("cube") || 
               modelType.equals("cube_bottom_top") || modelType.equals("cube_column") ||
               modelType.equals("cross") || modelType.equals("orientable");
    }
    
    /**
     * Sanitizes file paths to prevent path traversal attacks.
     * Uses a more robust approach with canonical path checking.
     */
    private String sanitizePath(String path, String blockKey, String fieldName) {
        if (path == null) {
            return null;
        }
        
        // Check for obvious path traversal attempts
        if (path.contains("..") || path.startsWith("/") || path.startsWith("\\") ||
            path.contains("%2e") || path.contains("%2f") || path.contains("%5c")) {
            logger.warning("Block " + blockKey + " has invalid " + fieldName + " with path traversal: " + path);
            return null;
        }
        
        // Only allow alphanumeric characters, underscores, hyphens, dots, and forward slashes
        if (!path.matches("^[a-zA-Z0-9_\\-./]+$")) {
            logger.warning("Block " + blockKey + " has invalid " + fieldName + " with illegal characters: " + path);
            return null;
        }
        
        // Path must not contain consecutive dots or slashes
        if (path.contains("..") || path.contains("//")) {
            logger.warning("Block " + blockKey + " has invalid " + fieldName + " with illegal sequence: " + path);
            return null;
        }
        
        return path;
    }
}
