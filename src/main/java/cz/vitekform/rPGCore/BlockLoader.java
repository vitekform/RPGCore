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
import java.util.List;
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
                YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream, StandardCharsets.UTF_8));
                blocksConfig.setDefaults(defaultConfig);
            }
        } catch (Exception e) {
            logger.warning("Failed to load default blocks.yml: " + e.getMessage());
        }

        ConfigurationSection blocksSection = blocksConfig.getConfigurationSection("blocks");
        if (blocksSection == null) {
            logger.warning("No blocks section found in blocks.yml");
            return;
        }

        for (String key : blocksSection.getKeys(false)) {
            ConfigurationSection blockSection = blocksSection.getConfigurationSection(key);
            if (blockSection == null) {
                continue;
            }

            try {
                RPGBlock block = loadBlock(blockSection, key);
                if (block != null) {
                    // Generate UUID for the block and register it
                    UUID blockUUID = UUID.nameUUIDFromBytes(key.getBytes(StandardCharsets.UTF_8));
                    BlockDictionary.blockRegistry.put(blockUUID, block);
                    BlockDictionary.blocks.put(key, block);
                    logger.info("Loaded block: " + key);
                }
            } catch (Exception e) {
                logger.warning("Failed to load block: " + key + " - " + e.getMessage());
                e.printStackTrace();
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
        block.blockStateValue = section.getInt("blockStateValue", 0);
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
                block.itemDropsMaterial.add(mat);
            } catch (IllegalArgumentException e) {
                logger.warning("Invalid material in drops for block " + key + ": " + matStr);
            }
        }

        // Load drops - RPG items
        List<String> dropItems = section.getStringList("drops.items");
        block.itemDropsRPG.addAll(dropItems);

        // Resource pack related attributes
        block.texturePath = section.getString("texture.path", null);
        block.modelPath = section.getString("model.path", null);
        block.modelType = section.getString("model.type", null);

        return block;
    }
}
