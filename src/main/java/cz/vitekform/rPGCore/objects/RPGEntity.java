package cz.vitekform.rPGCore.objects;

import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.List;

public class RPGEntity {

    public String visibleName;
    public int level;
    public int maxHealth;
    public int health;
    public int attack;
    public int defense;
    public int speed;
    public int experienceAfterDefeat;
    public List<RPGItem> drops;
    public boolean isBoss;
    public boolean isFriendly;
    public boolean hasVisibleName;

    public EntityType entityType;

    public RPGEntity(String visibleName, int level, int maxHealth, int health, int attack, int defense, int speed, int experienceAfterDefeat, List<RPGItem> drops, boolean isBoss, boolean isFriendly, boolean hasVisibleName) {
        this.visibleName = visibleName;
        this.level = level;
        this.maxHealth = maxHealth;
        this.health = health;
        this.attack = attack;
        this.defense = defense;
        this.speed = speed;
        this.experienceAfterDefeat = experienceAfterDefeat;
        this.drops = drops;
        this.isBoss = isBoss;
        this.isFriendly = isFriendly;
        this.hasVisibleName = hasVisibleName;
    }

    public RPGEntity() {
        this.visibleName = "";
        this.level = 0;
        this.maxHealth = 0;
        this.health = 0;
        this.attack = 0;
        this.defense = 0;
        this.speed = 0;
        this.experienceAfterDefeat = 0;
        this.drops = new ArrayList<>();
        this.isBoss = false;
        this.isFriendly = false;
        this.hasVisibleName = false;
    }
}
