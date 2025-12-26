package cz.vitekform.rPGCore.objects;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RPGPlayer {

    public UUID uuid;
    public String ingameNick;
    public String rpgName;
    public int level;
    public int defense_Base;
    public int exp;
    public int maxExp;
    public int skillPoints;
    public Map<RPGAttribute, Integer> baseAttributes;
    public Map<RPGAttribute, Integer> itemAttributes;
    public double critChance_Base;
    public double critChance_Items;

    public Map<RPGAttribute, Integer> totalAttributes() {
        return Map.of(
            RPGAttribute.STRENGTH, baseAttributes.getOrDefault(RPGAttribute.STRENGTH, 0) + itemAttributes.getOrDefault(RPGAttribute.STRENGTH, 0),
            RPGAttribute.DEXTERITY, baseAttributes.getOrDefault(RPGAttribute.DEXTERITY, 0) + itemAttributes.getOrDefault(RPGAttribute.DEXTERITY, 0),
            RPGAttribute.INTELLIGENCE, baseAttributes.getOrDefault(RPGAttribute.INTELLIGENCE, 0) + itemAttributes.getOrDefault(RPGAttribute.INTELLIGENCE, 0),
            RPGAttribute.VITALITY, baseAttributes.getOrDefault(RPGAttribute.VITALITY, 0) + itemAttributes.getOrDefault(RPGAttribute.VITALITY, 0),
            RPGAttribute.ENDURANCE, baseAttributes.getOrDefault(RPGAttribute.ENDURANCE, 0) + itemAttributes.getOrDefault(RPGAttribute.ENDURANCE, 0)
        );
    }

    public void giveItem(RPGItem item) {
        // Check if player has enough space in inventory
        Player player = Bukkit.getPlayer(uuid);
        Inventory inventory = player.getInventory();
        if (inventory.firstEmpty() == -1) {
            player.sendMessage("You didn't have enough space in your inventory to receive this item! Instead it was dropped on the ground.");
            player.getWorld().dropItemNaturally(player.getLocation(), item.build());
            return;
        }
        // Add item to inventory
        ItemStack itemStack = item.build();
        if (itemStack != null) {
            inventory.addItem(itemStack);
            player.sendMessage("You received: " + item.itemName);
        } else {
            player.sendMessage("Failed to create item: " + item.itemName);
        }
    }

    public void giveItem(RPGItem item, int amount) {
        // Check if player has enough space in inventory
        Player player = Bukkit.getPlayer(uuid);
        Inventory inventory = player.getInventory();
        if (inventory.firstEmpty() == -1) {
            player.sendMessage("You didn't have enough space in your inventory to receive this item! Instead it was dropped on the ground.");
            player.getWorld().dropItemNaturally(player.getLocation(), item.build(amount));
            return;
        }
        // Add item to inventory
        ItemStack itemStack = item.build(amount);
        if (itemStack != null) {
            inventory.addItem(itemStack);
            player.sendMessage("You received: " + item.itemName + " x" + amount);
        } else {
            player.sendMessage("Failed to create item: " + item.itemName);
        }
    }

    public int totalSkillPoints;
    public int attributePoints;
    public int totalAttributePoints;
    public double maxHealth_Base;
    public double health;
    public int maxMana_Base;
    public int mana;
    public RPGClass rpgClass;

    public int attackDMG_Base;
    public double attackSPD_Base;
    public double speed_Base;

    public double attackDMG_Items;
    public double attackSPD_Items;
    public int defense_Items;
    public int health_Items;
    public double speed_Items;
    public int mana_Items;

    public RPGPlayer(UUID uuid) {
        this.uuid = uuid;
        this.ingameNick = Bukkit.getPlayer(uuid).getName();
    }

    public void levelUp() {
        this.level++;
        this.maxExp = reqExpForLevel(this.level);
        this.exp = 0;
        this.skillPoints += 1;
        this.attributePoints += 5;
        this.totalSkillPoints += 1;
        this.totalAttributePoints += 5;
        updateItemStats();
        Player p = Bukkit.getPlayer(uuid);
        p.playSound(p, Sound.ENTITY_PLAYER_LEVELUP, 1F, 1F);
        p.sendMessage(Component.text("You have leveled up to level " + level + "!").color(NamedTextColor.GREEN));
        p.sendMessage(Component.text("You now have " + skillPoints + " free skill points and " + attributePoints + " free attribute points.").color(NamedTextColor.GREEN));
    }

    public void updateItemStats() {
        Player p = Bukkit.getPlayer(uuid);
        Inventory i = p.getInventory();

        // First reset all item stats
        attackDMG_Items = 0;
        attackSPD_Items = 0;
        defense_Items = 0;
        health_Items = 0;
        speed_Items = 0;
        mana_Items = 0;

        // Get all items to check
        ItemStack helmetItem = i.getItem(39);
        ItemStack chestplateItem = i.getItem(38);
        ItemStack leggingsItem = i.getItem(37);
        ItemStack bootsItem = i.getItem(36);
        ItemStack handItem = p.getItemInHand();
        ItemStack offHandItem = i.getItem(40);

        List<ItemStack> itemsToCount = new ArrayList<>();
        itemsToCount.add(helmetItem);
        itemsToCount.add(chestplateItem);
        itemsToCount.add(leggingsItem);
        itemsToCount.add(bootsItem);
        itemsToCount.add(handItem);
        itemsToCount.add(offHandItem);

        // Define keys
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

        // Calculate all stats from valid items
        for (ItemStack is : itemsToCount) {
            if (is != null && is.getType() != Material.AIR && is.hasItemMeta()) {
                int slotInInv = -1;
                for (int x = 0; x < p.getInventory().getSize(); x++) {
                    if (p.getInventory().getItem(x) == is) {
                        slotInInv = x;
                        break; // Found it, no need to continue checking
                    }
                }

                ItemMeta im = is.getItemMeta();
                PersistentDataContainer pdc = im.getPersistentDataContainer();

                String classStr = pdc.getOrDefault(key_class, PersistentDataType.STRING, "");
                if (classStr.isEmpty()) continue;

                RPGClass reqClass = RPGClass.valueOf(classStr);
                if (reqClass == rpgClass || reqClass == RPGClass.ANY) {
                    int levelReq = pdc.getOrDefault(key_level, PersistentDataType.INTEGER, 0);
                    if (level >= levelReq) {
                        int slotReq = pdc.getOrDefault(key_slot, PersistentDataType.INTEGER, -1);
                        boolean matchesSlot = false;
                        if (slotReq == 0 && is.equals(handItem)) {
                            matchesSlot = true;
                        }
                        else if (slotReq == 1 && is.equals(helmetItem)) {
                            matchesSlot = true;
                        }
                        else if (slotReq == 2 && is.equals(chestplateItem)) {
                            matchesSlot = true;
                        }
                        else if (slotReq == 3 && is.equals(leggingsItem)) {
                            matchesSlot = true;
                        }
                        else if (slotReq == 4 && is.equals(bootsItem)) {
                            matchesSlot = true;
                        }
                        else if (slotReq == 5 && is.equals(offHandItem)) {
                            matchesSlot = true;
                        }
                        else if (slotReq == -1) {
                            matchesSlot = true; // -1 means any slot
                        }
                        if (matchesSlot) {
                            // Add stats to player's total
                            attackDMG_Items += pdc.getOrDefault(key_attack, PersistentDataType.DOUBLE, 0D);
                            attackSPD_Items += pdc.getOrDefault(key_attack_speed, PersistentDataType.DOUBLE, 0D);
                            defense_Items += pdc.getOrDefault(key_defense, PersistentDataType.INTEGER, 0);
                            health_Items += pdc.getOrDefault(key_health, PersistentDataType.INTEGER, 0);
                            speed_Items += pdc.getOrDefault(key_speed, PersistentDataType.DOUBLE, 0D);
                            mana_Items += pdc.getOrDefault(key_mana, PersistentDataType.INTEGER, 0);
                            critChance_Items += pdc.getOrDefault(key_crit_chance, PersistentDataType.DOUBLE, 0D);
                        } else {
                            Component displayName = is.getItemMeta().displayName();
                            if (displayName == null) {
                                displayName = Component.text(is.getType().name());
                            }
                            p.sendMessage(Component.text("You cannot equip ").color(NamedTextColor.RED)
                                    .append(displayName)
                                    .append(Component.text(" in this slot!").color(NamedTextColor.RED)));
                        }
                    } else {
                        p.sendMessage(Component.text("You are not high enough level to use one or more of your equipped items!").color(NamedTextColor.RED));
                    }
                } else {
                    p.sendMessage(Component.text("You are using one or more items from a different class than your own!").color(NamedTextColor.RED));
                }
            }
        }

        // Now update the item attributes after all stats are calculated
        if (handItem != null && handItem.getType() != Material.AIR && handItem.hasItemMeta()) {
            ItemMeta handMeta = handItem.getItemMeta();
            handMeta.removeAttributeModifier(Attribute.ATTACK_SPEED);
            handMeta.removeAttributeModifier(Attribute.ATTACK_DAMAGE);
            handMeta.addAttributeModifier(Attribute.ATTACK_SPEED,
                    new AttributeModifier(new NamespacedKey("rpgcore", "attack_speed"),
                            attackSPD_Items + attackSPD_Base, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND.getGroup()));
            // Set attack damage to 0 since it's handled by your system
            handMeta.addAttributeModifier(Attribute.ATTACK_DAMAGE,
                    new AttributeModifier(new NamespacedKey("rpgcore", "attack_damage"),
                            0, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND.getGroup()));
            handItem.setItemMeta(handMeta);
        }

        double totalSpeed = speed_Base + speed_Items;
        p.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(totalSpeed);
    }

    private static int linearXP(int level) {
        return 50 + 50 * level; // for example 50xp per level
    }

    // polynomické XP
    private static int polynomialXP(int level, double pol) {
        return (int)(100 * Math.pow(level, pol)); // yes polynom
    }

    private static int reqExpForLevel(int level) {
        /*
        1-5 --- Linear because we cant scare new players that much
        6-15 --- Polynomial with pol of 1.3
        16-25 --- Polynomial with pol of 1.5
        26-35 --- Polynomial with pol of 1.7
        36-50 --- Polynomial with pol of 2.0
        51+ --- Polynomial with base of pol 2.0 that increases for each 25 levels by .25
        Have fun! (i guess)
         */
        if (level <= 5) return linearXP(level);
        else if (level <= 15) return polynomialXP(level, 1.3);
        else if (level <= 25) return polynomialXP(level, 1.5);
        else if (level <= 35) return polynomialXP(level, 1.7);
        else if (level <= 50) return polynomialXP(level, 2.0);
        else {
            double pol = 2.0;
            for (int i = 50; i <= level; i += 25) pol += .25;
            return polynomialXP(level, pol);
        }
    }

    public void handleExpAdd(int experienceAfterDefeat) {
        this.exp += experienceAfterDefeat;
        while (this.exp >= reqExpForLevel(this.level + 1)) {
            this.levelUp();
        }
    }
}
