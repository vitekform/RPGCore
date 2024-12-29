package cz.vitekform.rPGCore.objects;

import cz.vitekform.rPGCore.ItemDictionary;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class RPGItem {

    public String itemName;
    public List<String> itemLore;
    public int reqLevel;
    public RPGClass reqClass;
    public int attack;
    public int attackSpeed;
    public int defense;
    public int health;
    public int speed;
    public int mana;
    public Material material;
    public String id;

    public RPGItem(String itemName, int reqLevel, RPGClass reqClass, int attack, int attackSpeed, int defense, int health, int speed) {
        this.itemName = itemName;
        this.itemLore = new ArrayList<>();
        this.reqLevel = reqLevel;
        this.reqClass = reqClass;
        this.attack = attack;
        this.attackSpeed = attackSpeed;
        this.defense = defense;
        this.health = health;
        this.speed = speed;
        this.material = Material.PAPER;
        generateId();
    }

    public RPGItem() {
        this.itemName = "";
        this.itemLore = new ArrayList<>();
        this.reqLevel = 0;
        this.reqClass = RPGClass.ANY;
        this.attack = 0;
        this.attackSpeed = 0;
        this.defense = 0;
        this.health = 0;
        this.speed = 0;
        this.material = Material.PAPER;
        generateId();
    }

    public ItemStack build() {
        ItemStack i = new ItemStack(material);
        ItemMeta im = i.getItemMeta();
        im.setDisplayName(itemName);

        List<String> lore = new ArrayList<>(itemLore != null ? itemLore : new ArrayList<>());
        lore.add(" ");
        if (reqClass != RPGClass.ANY) {
            lore.add("Class: " + getNormalName(reqClass));
        }
        if (reqLevel > 0) {
            lore.add("Required Level: " + reqLevel);
        }
        if (attack > 0) {
            lore.add("Attack: " + attack);
        }
        if (attackSpeed > 0) {
            lore.add("Attack Speed: " + attackSpeed);
        }
        if (defense > 0) {
            lore.add("Defense: " + defense);
        }
        if (health > 0) {
            lore.add("Health: " + health);
        }
        if (speed > 0) {
            lore.add("Speed: " + speed);
        }
        if (mana > 0) {
            lore.add("Mana: " + mana);
        }

        im.setLore(lore);

        NamespacedKey key = new NamespacedKey("rpgcore", "rpg_item_id");
        im.getPersistentDataContainer().set(key, PersistentDataType.STRING, id);

        i.setItemMeta(im);

        return i;
    }

    private String getNormalName(RPGClass rpgClass) {
        return rpgClass.toString().substring(0, 1).toUpperCase() + rpgClass.toString().substring(1).toLowerCase();
    }

    private void generateId() {
        String generated = "";
        int length = 64;
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        for (int i = 0; i < length; i++) {
            generated += characters.charAt((int) Math.floor(Math.random() * characters.length()));
        }
        if (ItemDictionary.items.containsKey(generated)) {
            generateId();
            return;
        }
        id = generated;
    }
}
