package cz.vitekform.rPGCore.objects;

import cz.vitekform.rPGCore.RPGCore;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Equippable;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.EquipmentSlot;
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
    public int max_durability; // if -1 then it does not have a max durability
    public int durability;

    public int slotReq;
    public Material material;
    
    // Oraxen integration - reference to Oraxen item ID
    public String oraxenItemId;  // The Oraxen item ID to use as base for this RPG item

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
        this.durability = -1;
        this.max_durability = -1;
        this.oraxenItemId = null;
    }

    public ItemStack build() {
        if (max_durability > 0 && durability == -1) durability = max_durability;
        
        // Get base item from Oraxen if oraxenItemId is provided
        ItemStack i;
        if (oraxenItemId != null && !oraxenItemId.isEmpty()) {
            // Use Oraxen's item as base (commented out until Oraxen dependency is available)
            // Uncomment when Oraxen is available:
            // io.th0rgal.oraxen.api.OraxenItems oraxenItems = io.th0rgal.oraxen.api.OraxenItems.getItemById(oraxenItemId);
            // if (oraxenItems != null) {
            //     i = oraxenItems.build();
            // } else {
            //     i = new ItemStack(material);
            // }
            
            // Temporary: Use material until Oraxen is available
            i = new ItemStack(material);
        } else {
            i = new ItemStack(material);
        }
        
        ItemMeta im = i.getItemMeta();
        itemName = RPGCore.fancyText(List.of(itemName.decorations(Map.of(TextDecoration.BOLD, TextDecoration.State.TRUE, TextDecoration.ITALIC, TextDecoration.State.FALSE)))).getFirst();
        im.displayName(itemName);

        constructLore();
        im.lore(itemLore);

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

        im.setUnbreakable(true);
        
        // Note: Custom models from Oraxen are already applied to the base ItemStack
        // No need to manually apply custom model keys when using Oraxen items

        i.setItemMeta(im);
        
        // Note: Equippable components for armor are handled by Oraxen
        // No need to manually apply equippable components when using Oraxen items

        return i;
    }

    public ItemStack build(int amount) {
        ItemStack itemStack = build();
        itemStack.setAmount(amount);
        return itemStack;
    }

    /**
     * Maps the slotReq value to the corresponding EquipmentSlot.
     * @return The EquipmentSlot or null if not applicable
     */
    private EquipmentSlot getEquipmentSlotFromSlotReq() {
        return switch (slotReq) {
            case 0 -> EquipmentSlot.HAND;
            case 1 -> EquipmentSlot.HEAD;
            case 2 -> EquipmentSlot.CHEST;
            case 3 -> EquipmentSlot.LEGS;
            case 4 -> EquipmentSlot.FEET;
            case 5 -> EquipmentSlot.OFF_HAND;
            default -> null;
        };
    }

    private String getNormalName(RPGClass rpgClass) {
        return rpgClass.toString().substring(0, 1).toUpperCase() + rpgClass.toString().substring(1).toLowerCase();
    }

    private void constructLore() {
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
        if (max_durability > 0) {
            lore.add(Component.text("Durability: " + durability + "/" + max_durability).decoration(TextDecoration.ITALIC, false).color(NamedTextColor.GRAY));
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

        this.itemLore = RPGCore.fancyText(lore);
    }

    public void chipDurabilityAway(ItemStack itemStack, int amount) {
        PersistentDataContainer pdc = itemStack.getItemMeta().getPersistentDataContainer();
        if (max_durability <= 0) return;
        durability -= amount;
        if (durability <= 0) {
            // break the item
            itemStack.setAmount(0);
            return;
        }
        pdc.set(new NamespacedKey("rpgcore", "rpg_item_durability"), PersistentDataType.INTEGER, durability);
        // change the lore value
        constructLore();
        itemStack.getItemMeta().lore(itemLore);
    }
}
