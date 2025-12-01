package cz.vitekform.rPGCore;

import cz.vitekform.rPGCore.objects.RPGClass;
import cz.vitekform.rPGCore.objects.RPGItem;
import cz.vitekform.rPGCore.objects.RPGPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ItemDictionary {

    public static final Map<String, RPGItem> items = new HashMap<>();
    public static final Map<UUID, RPGItem> itemRegistry = new HashMap<>();

    /**
     * Get an item by its string key from items.yml
     * @param key The item key (e.g., "adventurer_sword")
     * @return The RPGItem or null if not found
     */
    public static RPGItem getItem(String key) {
        return items.get(key);
    }

    /**
     * Get an item by its UUID from itemRegistry
     * @param uuid The item UUID
     * @return The RPGItem or null if not found
     */
    public static RPGItem getItem(UUID uuid) {
        return itemRegistry.get(uuid);
    }

    // Dynamic item that depends on player state - cannot be loaded from YAML
    public static RPGItem attributePointsItem(RPGPlayer player) {
        RPGItem rpgItem = new RPGItem();
        rpgItem.itemName = Component.text("Free Attribute Points").color(NamedTextColor.GREEN);
        rpgItem.itemLore = List.of(Component.text("You have ").color(NamedTextColor.GREEN).append(Component.text(player.attributePoints + " ")).color(NamedTextColor.RED).append(Component.text("free attribute points.")).color(NamedTextColor.GREEN));
        rpgItem.reqLevel = 0;
        rpgItem.reqClass = RPGClass.ANY;
        rpgItem.attack = 0;
        rpgItem.attackSpeed = 0;
        rpgItem.defense = 0;
        rpgItem.health = 0;
        rpgItem.speed = 0;
        rpgItem.material = Material.SUNFLOWER;
        return rpgItem;
    }
}
