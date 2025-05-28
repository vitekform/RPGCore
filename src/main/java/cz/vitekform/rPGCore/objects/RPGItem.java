package cz.vitekform.rPGCore.objects;

import cz.vitekform.rPGCore.ItemDictionary;
import cz.vitekform.rPGCore.RPGCore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RPGItem {

    public Component itemName;
    public List<Component> itemLore;
    public int reqLevel;
    public RPGClass reqClass;
    public double attack;
    public double attackSpeed;
    public int defense;
    public int health;
    public double speed;
    public int mana;
    public double critChance;

    public int slotReq;
    public Material material;

    public RPGItem() {
        this.itemName = Component.newline();
        this.itemLore = new ArrayList<>();
        this.reqLevel = 0;
        this.reqClass = RPGClass.ANY;
        this.attack = 0;
        this.attackSpeed = 0;
        this.defense = 0;
        this.health = 0;
        this.speed = 0;
        this.critChance = 0;
        this.slotReq = -1; // Any
        this.material = Material.PAPER;
    }

    public ItemStack build() {
        ItemStack i = new ItemStack(material);
        ItemMeta im = i.getItemMeta();
        itemName = RPGCore.fancyText(List.of(itemName.decorations(Map.of(TextDecoration.BOLD, TextDecoration.State.TRUE, TextDecoration.ITALIC, TextDecoration.State.FALSE)))).getFirst();
        im.displayName(itemName);

        List<Component> lore = new ArrayList<>(itemLore != null ? itemLore : new ArrayList<>());
        if (reqClass != RPGClass.ANY) {
            lore.add(Component.text("Class: " + getNormalName(reqClass)).decoration(TextDecoration.ITALIC, false).color(NamedTextColor.WHITE));
        }
        if (reqLevel > 0) {
            lore.add(Component.text("Required Level: " + reqLevel).decoration(TextDecoration.ITALIC, false).color(NamedTextColor.WHITE));
        }
        if (attack > 0) {
            lore.add(Component.text("Attack: " + attack).decoration(TextDecoration.ITALIC, false).color(NamedTextColor.GRAY));
        }
        if (attackSpeed > 0) {
            lore.add(Component.text("Attack Speed: " + attackSpeed).decoration(TextDecoration.ITALIC, false).color(NamedTextColor.GRAY));
        }
        if (critChance > 0) {
            lore.add(Component.text("Critical Chance: " + critChance + "%").decoration(TextDecoration.ITALIC, false).color(NamedTextColor.LIGHT_PURPLE));
        }
        if (defense > 0) {
            lore.add(Component.text("Defense: " + defense).decoration(TextDecoration.ITALIC, false).color(NamedTextColor.GREEN));
        }
        if (health > 0) {
            lore.add(Component.text("Health: " + health).decoration(TextDecoration.ITALIC, false).color(NamedTextColor.RED));
        }
        if (speed > 0) {
            lore.add(Component.text("Speed: " + speed).decoration(TextDecoration.ITALIC, false).color(NamedTextColor.WHITE));
        }
        if (mana > 0) {
            lore.add(Component.text("Mana: " + mana).decoration(TextDecoration.ITALIC, false).color(NamedTextColor.AQUA));
        }
        if (slotReq != -1) {
            if (slotReq == 0) {
                lore.add(Component.text("Slot: Main Hand").decoration(TextDecoration.ITALIC, false).color(NamedTextColor.WHITE));
            } else if (slotReq == 1) {
                lore.add(Component.text("Slot: Helmet").decoration(TextDecoration.ITALIC, false).color(NamedTextColor.WHITE));
            } else if (slotReq == 2) {
                lore.add(Component.text("Slot: Chestplate").decoration(TextDecoration.ITALIC, false).color(NamedTextColor.WHITE));
            } else if (slotReq == 3) {
                lore.add(Component.text("Slot: Leggings").decoration(TextDecoration.ITALIC, false).color(NamedTextColor.WHITE));
            } else if (slotReq == 4) {
                lore.add(Component.text("Slot: Boots").decoration(TextDecoration.ITALIC, false).color(NamedTextColor.WHITE));
            } else if (slotReq == -5) {
                lore.add(Component.text("Slot: Offhand").decoration(TextDecoration.ITALIC, false).color(NamedTextColor.WHITE));
            }
        }

        lore = RPGCore.fancyText(lore);
        im.lore(lore);

        NamespacedKey key_class = new NamespacedKey("rpgcore", "rpg_item_class");
        NamespacedKey key_level = new NamespacedKey("rpgcore", "rpg_item_level");
        NamespacedKey key_attack = new NamespacedKey("rpgcore", "rpg_item_attack");
        NamespacedKey key_attack_speed = new NamespacedKey("rpgcore", "rpg_item_attack_speed");
        NamespacedKey key_defense = new NamespacedKey("rpgcore", "rpg_item_defense");
        NamespacedKey key_health = new NamespacedKey("rpgcore", "rpg_item_health");
        NamespacedKey key_speed = new NamespacedKey("rpgcore", "rpg_item_speed");
        NamespacedKey key_mana = new NamespacedKey("rpgcore", "rpg_item_mana");
        NamespacedKey key_slot = new NamespacedKey("rpgcore", "rpg_item_slot");
        NamespacedKey key_crit_chance = new NamespacedKey("rpgcore", "rpg_item_crit_chance");

        PersistentDataContainer pdc = im.getPersistentDataContainer();
        pdc.set(key_class, PersistentDataType.STRING, reqClass.name());
        pdc.set(key_level, PersistentDataType.INTEGER, reqLevel);
        pdc.set(key_attack, PersistentDataType.DOUBLE, attack);
        pdc.set(key_attack_speed, PersistentDataType.DOUBLE, attackSpeed);
        pdc.set(key_defense, PersistentDataType.INTEGER, defense);
        pdc.set(key_health, PersistentDataType.INTEGER, health);
        pdc.set(key_speed, PersistentDataType.DOUBLE, speed);
        pdc.set(key_mana, PersistentDataType.INTEGER, mana);
        pdc.set(key_slot, PersistentDataType.INTEGER, slotReq);
        pdc.set(key_crit_chance, PersistentDataType.DOUBLE, critChance);

        i.setItemMeta(im);

        return i;
    }

    public ItemStack build(int amount) {
        ItemStack itemStack = build();
        itemStack.setAmount(amount);
        return itemStack;
    }

    private String getNormalName(RPGClass rpgClass) {
        return rpgClass.toString().substring(0, 1).toUpperCase() + rpgClass.toString().substring(1).toLowerCase();
    }
}
