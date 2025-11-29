package cz.vitekform.rPGCore;

import cz.vitekform.rPGCore.objects.RPGClass;
import cz.vitekform.rPGCore.objects.RPGFoodItem;
import cz.vitekform.rPGCore.objects.RPGItem;
import cz.vitekform.rPGCore.objects.RPGPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemDictionary {

    public static final Map<String, RPGItem> items = new HashMap<>();

    public static RPGItem warriorClassItem() {
        RPGItem rpgItem = new RPGItem();
        rpgItem.itemName = Component.text("Warrior").color(NamedTextColor.RED);
        rpgItem.itemLore = List.of(Component.text("A strong and brave warrior.").color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
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
        rpgItem.itemLore = List.of(Component.text("A skilled archer").color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
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
        rpgItem.itemLore = List.of(Component.text("A powerful mage.").color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
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

    public static RPGItem strengthAttributeItem() {
        RPGItem rpgItem = new RPGItem();
        rpgItem.itemName = Component.text("Strength").color(NamedTextColor.RED);
        rpgItem.itemLore = List.of(Component.text("Strength increases your:").color(NamedTextColor.RED), Component.text("Melee damage"), Component.text("Carry weight"), Component.text("And chance to block melee attacks"));
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

    public static RPGItem dexterityAttributeItem() {
        RPGItem rpgItem = new RPGItem();
        rpgItem.itemName = Component.text("Dexterity").color(NamedTextColor.GREEN);
        rpgItem.itemLore = List.of(Component.text("Dexterity increases your:").color(NamedTextColor.GREEN), Component.text("Ranged damage"), Component.text("Chance to dodge ranged attacks"), Component.text("And chance to dodge melee attacks"));
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

    public static RPGItem intelligenceAttributeItem() {
        RPGItem rpgItem = new RPGItem();
        rpgItem.itemName = Component.text("Intelligence").color(NamedTextColor.AQUA);
        rpgItem.itemLore = List.of(Component.text("Intelligence increases your:").color(NamedTextColor.AQUA), Component.text("Maximum mana capacity"), Component.text("Damage using spells (and healing)"), Component.text("And decreases the chance of miscast"));
        rpgItem.reqLevel = 0;
        rpgItem.reqClass = RPGClass.ANY;
        rpgItem.attack = 0;
        rpgItem.attackSpeed = 0;
        rpgItem.defense = 0;
        rpgItem.health = 0;
        rpgItem.speed = 0;
        rpgItem.material = Material.AMETHYST_SHARD;
        return rpgItem;
    }

    public static RPGItem enduranceAttributeItem() {
        RPGItem rpgItem = new RPGItem();
        rpgItem.itemName = Component.text("Endurance").color(NamedTextColor.WHITE);
        rpgItem.itemLore = List.of(Component.text("Endurance increases your:").color(NamedTextColor.WHITE), Component.text("Max Health"), Component.text("And max stamina"));
        rpgItem.reqLevel = 0;
        rpgItem.reqClass = RPGClass.ANY;
        rpgItem.attack = 0;
        rpgItem.attackSpeed = 0;
        rpgItem.defense = 0;
        rpgItem.health = 0;
        rpgItem.speed = 0;
        rpgItem.material = Material.IRON_CHESTPLATE;
        return rpgItem;
    }

    public static RPGItem vitalityAttributeItem() {
        RPGItem rpgItem = new RPGItem();
        rpgItem.itemName = Component.text("Vitality").color(NamedTextColor.RED);
        rpgItem.itemLore = List.of(Component.text("Vitality increases your:").color(NamedTextColor.RED), Component.text("Health regeneration"), Component.text("Mana Regeneration"), Component.text("And Stamina Regeneration"));
        rpgItem.reqLevel = 0;
        rpgItem.reqClass = RPGClass.ANY;
        rpgItem.attack = 0;
        rpgItem.attackSpeed = 0;
        rpgItem.defense = 0;
        rpgItem.health = 0;
        rpgItem.speed = 0;
        rpgItem.material = Material.GOLDEN_APPLE;
        return rpgItem;
    }

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

    // Initial weapon for Warrior class
    public static RPGItem adventurerSword() {
        RPGItem rpgItem = new RPGItem();
        rpgItem.itemName = Component.text("Adventurer's Sword").color(NamedTextColor.WHITE);
        rpgItem.itemLore = List.of(Component.text("Sword that every adventurer needs in their equipment! Quality assured!").color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
        rpgItem.reqLevel = 1;
        rpgItem.reqClass = RPGClass.WARRIOR;
        rpgItem.attack = 6;
        rpgItem.attackSpeed = 1.6; // Normal attack speed
        rpgItem.defense = 0;
        rpgItem.health = 0;
        rpgItem.speed = 0;
        rpgItem.slotReq = -1; // Any because for some reason off hand is broken (or I can't just figure out how the f*** it works)
        // -1 = ANY
        // 0 = Main Hand
        // 1 = Helmet
        // 2 = Chestplate
        // 3 = Leggings
        // 4 = Boots
        // 5 = Off Hand
        rpgItem.material = Material.IRON_SWORD;
        rpgItem.critChance = 15.0; // 15% crit chance
        return rpgItem;
    }

    public static RPGItem adventurerHelmet() {
        RPGItem rpgItem = new RPGItem();
        rpgItem.itemName = Component.text("Adventurer's Helmet").color(NamedTextColor.WHITE);
        rpgItem.itemLore = List.of(Component.text("Helmet that every adventurer needs! Guaranteed to protect your head against weather I guess.").color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
        rpgItem.reqLevel = 1;
        rpgItem.reqClass = RPGClass.WARRIOR;
        rpgItem.attack = 0;
        rpgItem.attackSpeed = 0;
        rpgItem.defense = 1;
        rpgItem.health = 5;
        rpgItem.speed = 0;
        rpgItem.slotReq = 1; // Helmet
        rpgItem.material = Material.LEATHER_HELMET;
        rpgItem.critChance = 0.0; // No crit chance for helmet
        return rpgItem;
    }

    public static RPGItem adventurerChestplate() {
        RPGItem rpgItem = new RPGItem();
        rpgItem.itemName = Component.text("Adventurer's Chestplate").color(NamedTextColor.WHITE);
        rpgItem.itemLore = List.of(Component.text("Chestplate that every adventurer needs! Guaranteed to protect your chest against something... You will probably find out what it is for").color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
        rpgItem.reqLevel = 1;
        rpgItem.reqClass = RPGClass.WARRIOR;
        rpgItem.attack = 0;
        rpgItem.attackSpeed = 0;
        rpgItem.defense = 3;
        rpgItem.health = 10;
        rpgItem.speed = 0;
        rpgItem.slotReq = 2; // Chestplate
        rpgItem.material = Material.LEATHER_CHESTPLATE;
        rpgItem.critChance = 0.0; // No crit chance for chestplate
        return rpgItem;
    }

    public static RPGItem adventurerLeggings() {
        RPGItem rpgItem = new RPGItem();
        rpgItem.itemName = Component.text("Adventurer's Leggings").color(NamedTextColor.WHITE);
        rpgItem.itemLore = List.of(Component.text("Leggings that every adventurer needs! Guaranteed to protect your legs and other parts against cold? I hope...").color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
        rpgItem.reqLevel = 1;
        rpgItem.reqClass = RPGClass.WARRIOR;
        rpgItem.attack = 0;
        rpgItem.attackSpeed = 0;
        rpgItem.defense = 2;
        rpgItem.health = 7;
        rpgItem.speed = 0;
        rpgItem.slotReq = 3; // Leggings
        rpgItem.material = Material.LEATHER_LEGGINGS;
        rpgItem.critChance = 0.0; // No crit chance for leggings
        return rpgItem;
    }

    public static RPGItem adventurerBoots() {
        RPGItem rpgItem = new RPGItem();
        rpgItem.itemName = Component.text("Adventurer's Boots").color(NamedTextColor.WHITE);
        rpgItem.itemLore = List.of(Component.text("Boots that every adventurer needs! Guaranteed to protect your feet against trench foot").color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
        rpgItem.reqLevel = 1;
        rpgItem.reqClass = RPGClass.WARRIOR;
        rpgItem.attack = 0;
        rpgItem.attackSpeed = 0;
        rpgItem.defense = 1;
        rpgItem.health = 3;
        rpgItem.speed = 0;
        rpgItem.slotReq = 4; // Boots
        rpgItem.material = Material.LEATHER_BOOTS;
        rpgItem.critChance = 0.0; // No crit chance for boots
        return rpgItem;
    }

    // For unknown reason the RPGFoodItem doesn't actually work (the handler is broken D:)
    public static RPGFoodItem staleBread() {
        RPGFoodItem rpgItem = new RPGFoodItem();
        rpgItem.itemName = Component.text("Stale Bread").color(NamedTextColor.WHITE);
        rpgItem.itemLore = List.of(Component.text("A piece of stale bread. Not very tasty, but it will fill your stomach.").color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
        rpgItem.reqLevel = 0;
        rpgItem.reqClass = RPGClass.ANY;
        rpgItem.attack = 0;
        rpgItem.attackSpeed = 0;
        rpgItem.defense = 0;
        rpgItem.health = 0;
        rpgItem.speed = 0;
        rpgItem.slotReq = -1; // Any
        rpgItem.material = Material.BREAD;
        rpgItem.foodAmount = 2; // Restores 2 hunger points
        rpgItem.saturationAmount = 0.4f; // Restores 0.4 saturation points
        rpgItem.critChance = 0.0; // No crit chance for food
        return rpgItem;
    }
}
