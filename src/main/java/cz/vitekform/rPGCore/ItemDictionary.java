package cz.vitekform.rPGCore;

import cz.vitekform.rPGCore.objects.RPGClass;
import cz.vitekform.rPGCore.objects.RPGItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemDictionary {

    public static final Map<String, RPGItem> items = new HashMap<>();

    public static RPGItem warriorClassItem() {
        RPGItem rpgItem = new RPGItem();
        rpgItem.itemName = ChatColor.RED + "Warrior";
        rpgItem.itemLore = List.of(ChatColor.GRAY + "A strong and brave warrior.");
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
        rpgItem.itemName = ChatColor.GREEN + "Archer";
        rpgItem.itemLore = List.of(ChatColor.GRAY + "A skilled archer.");
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
        rpgItem.itemName = ChatColor.BLUE + "Mage";
        rpgItem.itemLore = List.of(ChatColor.GRAY + "A powerful mage.");
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
}
