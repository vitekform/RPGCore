package cz.vitekform.rPGCore;

import cz.vitekform.rPGCore.objects.RPGClass;
import cz.vitekform.rPGCore.objects.RPGItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.ChatColor;
import org.bukkit.Material;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemDictionary {

    public static final Map<String, RPGItem> items = new HashMap<>();

    public static RPGItem warriorClassItem() {
        RPGItem rpgItem = new RPGItem();
        rpgItem.itemName = Component.text("Warrior").color(NamedTextColor.RED);
        rpgItem.itemLore = List.of(Component.text("A strong and brave warrior.").color(NamedTextColor.GRAY));
        rpgItem.reqLevel = 0;
        rpgItem.reqClass = RPGClass.ANY;
        rpgItem.attack = 0;
        rpgItem.attackSpeed = 0;
        rpgItem.defense = 0;
        rpgItem.health = 0;
        rpgItem.speed = 0;
        rpgItem.material = Material.IRON_SWORD;
        return rpgItem;
    }

    public static RPGItem archerClassItem() {
        RPGItem rpgItem = new RPGItem();
        rpgItem.itemName = Component.text("Archer").color(NamedTextColor.GREEN);
        rpgItem.itemLore = List.of(Component.text("A skilled archer").color(NamedTextColor.GRAY));
        rpgItem.reqLevel = 0;
        rpgItem.reqClass = RPGClass.ANY;
        rpgItem.attack = 0;
        rpgItem.attackSpeed = 0;
        rpgItem.defense = 0;
        rpgItem.health = 0;
        rpgItem.speed = 0;
        rpgItem.material = Material.BOW;
        return rpgItem;
    }

    public static RPGItem mageClassItem() {
        RPGItem rpgItem = new RPGItem();
        rpgItem.itemName = Component.text("Mage").color(NamedTextColor.BLUE);
        rpgItem.itemLore = List.of(Component.text("A powerful mage.").color(NamedTextColor.GRAY));
        rpgItem.reqLevel = 0;
        rpgItem.reqClass = RPGClass.ANY;
        rpgItem.attack = 0;
        rpgItem.attackSpeed = 0;
        rpgItem.defense = 0;
        rpgItem.health = 0;
        rpgItem.speed = 0;
        rpgItem.material = Material.BLAZE_ROD;
        return rpgItem;
    }

    // Initial weapon for Warrior class
    public static RPGItem adventurerSword() {
        RPGItem rpgItem = new RPGItem();
        rpgItem.itemName = Component.text("Adventurer's Sword").color(NamedTextColor.WHITE);
        rpgItem.itemLore = List.of(Component.text("Sword that every adventurer needs in their equipment! Quality assured!"));
        rpgItem.reqLevel = 1;
        rpgItem.reqClass = RPGClass.WARRIOR;
        rpgItem.attack = 6;
        rpgItem.attackSpeed = 1.6;
        rpgItem.defense = 0;
        rpgItem.health = 0;
        rpgItem.speed = 0;
        rpgItem.material = Material.IRON_SWORD;
        return rpgItem;
    }
}
