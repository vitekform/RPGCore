package cz.vitekform.rPGCore;

import cz.vitekform.rPGCore.objects.RPGEntity;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.logging.Logger;

public class EntityLoader {

    private final JavaPlugin plugin;
    private final Logger logger;

    public EntityLoader(JavaPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }

    public void loadEntities() {
        File entitiesFile = new File(plugin.getDataFolder(), "entities.yml");

        // Save default entities.yml if it doesn't exist
        if (!entitiesFile.exists()) {
            plugin.saveResource("entities.yml", false);
        }

        FileConfiguration entitiesConfig = YamlConfiguration.loadConfiguration(entitiesFile);

        // Load defaults from jar to merge any new entities
        try (InputStream defaultStream = plugin.getResource("entities.yml")) {
            if (defaultStream != null) {
                YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream, StandardCharsets.UTF_8));
                entitiesConfig.setDefaults(defaultConfig);
            }
        } catch (Exception e) {
            logger.warning("Failed to load default entities.yml: " + e.getMessage());
        }

        ConfigurationSection entitiesSection = entitiesConfig.getConfigurationSection("entities");
        if (entitiesSection == null) {
            logger.warning("No entities section found in entities.yml");
            return;
        }

        for (String key : entitiesSection.getKeys(false)) {
            ConfigurationSection entitySection = entitiesSection.getConfigurationSection(key);
            if (entitySection == null) {
                continue;
            }

            try {
                RPGEntity entity = loadEntity(entitySection, key);
                if (entity != null) {
                    EntityDictionary.entities.put(key, entity);
                    logger.info("Loaded entity: " + key);
                }
            } catch (Exception e) {
                logger.warning("Failed to load entity: " + key + " - " + e.getMessage());
            }
        }

        logger.info("Loaded " + EntityDictionary.entities.size() + " entities from entities.yml");
    }

    private RPGEntity loadEntity(ConfigurationSection section, String key) {
        // Required fields
        String id = section.getString("id", key);
        String visibleName = section.getString("visibleName");
        String entityTypeStr = section.getString("entityType");

        if (visibleName == null || entityTypeStr == null) {
            logger.warning("Entity " + key + " is missing required fields (visibleName, entityType)");
            return null;
        }

        EntityType entityType;
        try {
            entityType = EntityType.valueOf(entityTypeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            logger.warning("Invalid entity type for entity " + key + ": " + entityTypeStr);
            return null;
        }

        RPGEntity entity = new RPGEntity();
        entity.visibleName = visibleName;
        entity.entityType = entityType;

        // Optional attributes with defaults
        entity.level = section.getInt("level", 1);
        entity.maxHealth = section.getDouble("maxHealth", 20.0);
        entity.health = section.getDouble("health", entity.maxHealth);
        entity.attack = section.getDouble("attack", 1.0);
        entity.defense = section.getInt("defense", 0);
        entity.speed = section.getDouble("speed", 0.1);
        entity.experienceAfterDefeat = section.getInt("experienceAfterDefeat", 0);
        entity.isBoss = section.getBoolean("isBoss", false);
        entity.isFriendly = section.getBoolean("isFriendly", false);
        entity.hasVisibleName = section.getBoolean("hasVisibleName", false);

        // Load drops
        List<String> dropsList = section.getStringList("drops");
        for (String drop : dropsList) {
            if (ItemDictionary.items.containsKey(drop)) {
                entity.drops.add(ItemDictionary.items.get(drop));
            } else {
                logger.warning("Unknown item in drops for entity " + key + ": " + drop);
            }
        }

        // Resource pack related attributes
        entity.texturePath = section.getString("texture.path", null);
        entity.modelPath = section.getString("model.path", null);

        return entity;
    }
}
