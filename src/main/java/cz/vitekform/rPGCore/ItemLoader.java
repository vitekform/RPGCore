package cz.vitekform.rPGCore;

import cz.vitekform.rPGCore.objects.RPGClass;
import cz.vitekform.rPGCore.objects.RPGFoodItem;
import cz.vitekform.rPGCore.objects.RPGItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

public class ItemLoader {

    private final JavaPlugin plugin;
    private final Logger logger;

    public ItemLoader(JavaPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }

    public void loadItems() {
        File itemsFile = new File(plugin.getDataFolder(), "items.yml");

        // Save default items.yml if it doesn't exist
        if (!itemsFile.exists()) {
            plugin.saveResource("items.yml", false);
        }

        FileConfiguration itemsConfig = YamlConfiguration.loadConfiguration(itemsFile);

        // Load defaults from jar to merge any new items
        try (InputStream defaultStream = plugin.getResource("items.yml")) {
            if (defaultStream != null) {
                YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream, StandardCharsets.UTF_8));
                itemsConfig.setDefaults(defaultConfig);
            }
        } catch (Exception e) {
            logger.warning("Failed to load default items.yml: " + e.getMessage());
        }

        ConfigurationSection itemsSection = itemsConfig.getConfigurationSection("items");
        if (itemsSection == null) {
            logger.warning("No items section found in items.yml");
            return;
        }

        for (String key : itemsSection.getKeys(false)) {
            ConfigurationSection itemSection = itemsSection.getConfigurationSection(key);
            if (itemSection == null) {
                continue;
            }

            try {
                RPGItem item = loadItem(itemSection, key);
                if (item != null) {
                    // Generate UUID for the item and register it
                    UUID itemUUID = UUID.nameUUIDFromBytes(key.getBytes(StandardCharsets.UTF_8));
                    ItemDictionary.itemRegistry.put(itemUUID, item);
                    ItemDictionary.items.put(key, item);
                    logger.info("Loaded item: " + key);
                }
            } catch (Exception e) {
                logger.warning("Failed to load item: " + key + " - " + e.getMessage());
            }
        }

        logger.info("Loaded " + ItemDictionary.itemRegistry.size() + " items from items.yml");
    }

    private RPGItem loadItem(ConfigurationSection section, String key) {
        // Required fields
        String id = section.getString("id", key);
        String name = section.getString("name");
        String materialStr = section.getString("material");

        if (name == null || materialStr == null) {
            logger.warning("Item " + key + " is missing required fields (name, material)");
            return null;
        }

        Material material;
        try {
            material = Material.valueOf(materialStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            logger.warning("Invalid material for item " + key + ": " + materialStr);
            return null;
        }

        // Check if it's a food item
        boolean isFood = section.getBoolean("isFood", false);
        RPGItem item = isFood ? new RPGFoodItem() : new RPGItem();

        // Set name with color
        String nameColor = section.getString("nameColor", "WHITE");
        TextColor color = getTextColor(nameColor);
        item.itemName = Component.text(name).color(color);

        // Set material
        item.material = material;

        // Set lore
        List<String> loreList = section.getStringList("lore");
        String loreColor = section.getString("loreColor", "GRAY");
        TextColor loreTextColor = getTextColor(loreColor);
        List<Component> loreComponents = new ArrayList<>();
        for (String loreLine : loreList) {
            loreComponents.add(Component.text(loreLine).color(loreTextColor).decoration(TextDecoration.ITALIC, false));
        }
        item.itemLore = loreComponents;

        // Optional attributes with defaults
        item.reqLevel = section.getInt("reqLevel", 0);

        String reqClassStr = section.getString("reqClass", "ANY");
        try {
            item.reqClass = RPGClass.valueOf(reqClassStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            item.reqClass = RPGClass.ANY;
        }

        item.attack = section.getDouble("attack", 0);
        item.attackSpeed = section.getDouble("attackSpeed", 0);
        item.defense = section.getInt("defense", 0);
        item.health = section.getInt("health", 0);
        item.speed = section.getDouble("speed", 0);
        item.mana = section.getInt("mana", 0);
        item.critChance = section.getDouble("critChance", 0);
        item.max_durability = section.getInt("max_durability", -1);
        item.slotReq = section.getInt("slotReq", -1);
        
        // Resource pack related attributes
        item.texturePath = section.getString("texture.path", null);
        item.modelPath = section.getString("model.path", null);
        item.modelType = section.getString("model.type", null);
        
        // Armor-specific resource pack attributes
        item.armorTexturePath = section.getString("armor.texture.path", null);
        item.armorLayerType = section.getString("armor.layer.type", null);

        // Food-specific attributes
        if (item instanceof RPGFoodItem foodItem) {
            foodItem.foodAmount = section.getInt("foodAmount", 0);
            foodItem.saturationAmount = (float) section.getDouble("saturationAmount", 0);
        }

        return item;
    }

    private TextColor getTextColor(String colorName) {
        return switch (colorName.toUpperCase()) {
            case "BLACK" -> NamedTextColor.BLACK;
            case "DARK_BLUE" -> NamedTextColor.DARK_BLUE;
            case "DARK_GREEN" -> NamedTextColor.DARK_GREEN;
            case "DARK_AQUA" -> NamedTextColor.DARK_AQUA;
            case "DARK_RED" -> NamedTextColor.DARK_RED;
            case "DARK_PURPLE" -> NamedTextColor.DARK_PURPLE;
            case "GOLD" -> NamedTextColor.GOLD;
            case "GRAY" -> NamedTextColor.GRAY;
            case "DARK_GRAY" -> NamedTextColor.DARK_GRAY;
            case "BLUE" -> NamedTextColor.BLUE;
            case "GREEN" -> NamedTextColor.GREEN;
            case "AQUA" -> NamedTextColor.AQUA;
            case "RED" -> NamedTextColor.RED;
            case "LIGHT_PURPLE" -> NamedTextColor.LIGHT_PURPLE;
            case "YELLOW" -> NamedTextColor.YELLOW;
            default -> NamedTextColor.WHITE;
        };
    }
}
