package cz.vitekform.rPGCore.objects;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RPGPlayer {

    public UUID uuid;
    public String ingameNick;
    public String rpgName;
    public int level;
    public int defense;
    public int exp;
    public int maxExp;
    public int skillPoints;
    public int strength;
    public int dexterity;
    public int intelligence;
    public int vitality;
    public int endurance;
    public int totalSkillPoints;
    public int attributePoints;
    public int totalAttributePoints;
    public double maxHealth;
    public double health;
    public int maxMana;
    public int mana;
    public RPGClass rpgClass;

    public int strengthTotal;
    public int dexterityTotal;
    public int intelligenceTotal;
    public int vitalityTotal;
    public int enduranceTotal;

    public double attackDMG_Items;
    public double attackSPD_Items;
    public int defense_Items;
    public int health_Items;
    public double speed_Items;
    public int mana_Items;

    public RPGPlayer(UUID uuid, String ingameNick, String rpgName, int level, int exp, int maxExp, int skillPoints, int strength, int dexterity, int intelligence, int vitality, int endurance, int totalSkillPoints, int attributePoints, double maxHealth, double health, int maxMana, int mana, RPGClass rpgClass, int totalAttributePoints, int defense) {
        this.totalAttributePoints = totalAttributePoints;
        this.defense = defense;
        this.uuid = uuid;
        this.ingameNick = ingameNick;
        this.rpgName = rpgName;
        this.level = level;
        this.exp = exp;
        this.maxExp = maxExp;
        this.skillPoints = skillPoints;
        this.strength = strength;
        this.dexterity = dexterity;
        this.intelligence = intelligence;
        this.vitality = vitality;
        this.endurance = endurance;
        this.totalSkillPoints = totalSkillPoints;
        this.attributePoints = attributePoints;
        this.maxHealth = maxHealth;
        this.health = health;
        this.maxMana = maxMana;
        this.mana = mana;
        this.rpgClass = rpgClass;
    }

    public RPGPlayer(UUID uuid) {
        this.uuid = uuid;
        this.ingameNick = Bukkit.getPlayer(uuid).getName();
    }

    public void levelUp() {
        this.level++;
        this.maxExp = (int) (this.maxExp * 1.1);
        this.exp = 0;
        this.skillPoints += 1;
        this.attributePoints += 5;
        this.totalSkillPoints += 1;
        this.totalAttributePoints += 5;
    }

    public void updateItemStats() {
        Player p = Bukkit.getPlayer(uuid);
        Inventory i = p.getInventory();

        ItemStack helmetItem = i.getItem(39);
        ItemStack chestplateItem = i.getItem(38);
        ItemStack leggingsItem = i.getItem(37);
        ItemStack bootsItem = i.getItem(36);
        ItemStack handItem = p.getActiveItem();
        ItemStack offHandItem = i.getItem(40);

        NamespacedKey key_class = new NamespacedKey("rpgcore", "rpg_item_class");
        NamespacedKey key_level = new NamespacedKey("rpgcore", "rpg_item_level");
        NamespacedKey key_attack = new NamespacedKey("rpgcore", "rpg_item_attack");
        NamespacedKey key_attack_speed = new NamespacedKey("rpgcore", "rpg_item_attack_speed");
        NamespacedKey key_defense = new NamespacedKey("rpgcore", "rpg_item_defense");
        NamespacedKey key_health = new NamespacedKey("rpgcore", "rpg_item_health");
        NamespacedKey key_speed = new NamespacedKey("rpgcore", "rpg_item_speed");
        NamespacedKey key_mana = new NamespacedKey("rpgcore", "rpg_item_mana");
        NamespacedKey key_slot = new NamespacedKey("rpgcore", "rpg_item_slot");

        List<ItemStack> itemToCount = new ArrayList<>();

        itemToCount.add(helmetItem);
        itemToCount.add(chestplateItem);
        itemToCount.add(leggingsItem);
        itemToCount.add(bootsItem);
        itemToCount.add(handItem);
        itemToCount.add(offHandItem);

        double totalAttackSpeed = 0D;
        double totalAttack = 0D;
        int totalDefense = 0;
        int totalHealth = 0;
        double totalSpeed = 0D;
        int totalMana = 0;

        for (ItemStack is : itemToCount) {
            if (is != null && is.getType() != Material.AIR && is.getItemMeta()
                                                              != null) {
                int slotInInv = -1;
                for (int x = 0; x < p.getInventory().getSize(); x++) {
                    if (p.getInventory().getItem(x) == is) {
                        slotInInv = x;
                    }
                }
                ItemMeta im = is.getItemMeta();
                PersistentDataContainer pdc = im.getPersistentDataContainer();
                RPGClass reqClass = RPGClass.valueOf(pdc.getOrDefault(key_class, PersistentDataType.STRING, null));
                if (reqClass == rpgClass || reqClass == RPGClass.ANY) {
                    int levelReq = pdc.getOrDefault(key_level, PersistentDataType.INTEGER, 0);
                    if (level >= levelReq) {
                        int slotReq = pdc.getOrDefault(key_slot, PersistentDataType.INTEGER, -1);
                        if (slotReq == -1 || slotReq == slotInInv) {
                            double attackAdd = pdc.getOrDefault(key_attack, PersistentDataType.DOUBLE, 0D);
                            double attackSpeedAdd = pdc.getOrDefault(key_attack_speed, PersistentDataType.DOUBLE, 0D);
                            int defenseAdd = pdc.getOrDefault(key_defense, PersistentDataType.INTEGER, 0);
                            int healthAdd = pdc.getOrDefault(key_health, PersistentDataType.INTEGER, 0);
                            double speedAdd = pdc.getOrDefault(key_speed, PersistentDataType.DOUBLE, 0D);
                            int manaAdd = pdc.getOrDefault(key_mana, PersistentDataType.INTEGER, 0);

                            totalAttack += attackAdd;
                            totalAttackSpeed += attackSpeedAdd;
                            totalDefense += defenseAdd;
                            totalHealth += healthAdd;
                            totalSpeed += speedAdd;
                            totalMana += manaAdd;
                        }
                    }
                }
            }
        }

        attackDMG_Items = totalAttack;
        attackSPD_Items = totalAttackSpeed;
        defense_Items = totalDefense;
        health_Items = totalHealth;
        speed_Items = totalSpeed;
        mana_Items = totalMana;
    }
}
