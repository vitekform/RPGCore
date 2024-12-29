package cz.vitekform.rPGCore.objects;

import org.bukkit.Bukkit;

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
}
