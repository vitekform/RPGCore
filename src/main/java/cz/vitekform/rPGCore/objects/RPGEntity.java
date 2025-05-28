package cz.vitekform.rPGCore.objects;

import cz.vitekform.rPGCore.RPGCore;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RPGEntity {

    public String visibleName;
    public int level;
    public double maxHealth;
    public double health;
    public double attack;
    public int defense;
    public double speed;
    public int experienceAfterDefeat;
    public List<RPGItem> drops;
    public List<ItemStack> dropsAsItemStacks() {
        List<ItemStack> itemStacks = new ArrayList<>();
        for (RPGItem drop : drops) {
            itemStacks.add(drop.build());
        }
        return itemStacks;
    }
    public boolean isBoss;
    public boolean isFriendly;
    public boolean hasVisibleName;
    public EntityType entityType;

    // Equipment slots
    private ItemStack helmet;
    private ItemStack chestplate;
    private ItemStack leggings;
    private ItemStack boots;
    private ItemStack mainHand;
    private ItemStack offHand;

    // Reference to actual entity
    private UUID entityUUID;
    private BossBar bossBar;

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

    public static boolean isRPGEntity(LivingEntity e) {
        return RPGCore.entityStorage.containsKey(e.getUniqueId());
    }

    public void setHelmet(ItemStack helmet) {
        this.helmet = helmet;
        updateEquipment();
    }

    public void setChestplate(ItemStack chestplate) {
        this.chestplate = chestplate;
        updateEquipment();
    }

    public void setLeggings(ItemStack leggings) {
        this.leggings = leggings;
        updateEquipment();
    }

    public void setBoots(ItemStack boots) {
        this.boots = boots;
        updateEquipment();
    }

    public void setMainHand(ItemStack mainHand) {
        this.mainHand = mainHand;
        updateEquipment();
    }

    public void setOffHand(ItemStack offHand) {
        this.offHand = offHand;
        updateEquipment();
    }

    public Entity spawnIn(Location location) {
        if (entityType == null) {
            throw new IllegalStateException("Entity type must be set before spawning");
        }

        World world = location.getWorld();
        Entity entity = world.spawnEntity(location, entityType);

        if (entity instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity) entity;

            // Set entity attributes
            if (hasVisibleName) {
                livingEntity.customName(Component.text(visibleName));
                livingEntity.setCustomNameVisible(true);
            }

            // Update entity equipment
            this.entityUUID = entity.getUniqueId();
            updateEquipment();

            // Store entity in plugin storage
            RPGCore.entityStorage.put(entity.getUniqueId(), this);

            // Show boss bar if it's a boss
            if (isBoss) {
                showBossbar();
            }
        }

        return entity;
    }

    public void setHostile(boolean hostile) {
        this.isFriendly = !hostile;
    }

    public void teleport(Location location) {
        if (entityUUID != null) {
            Entity entity = Bukkit.getEntity(entityUUID);
            if (entity != null) {
                entity.teleport(location);
            }
        }
    }

    public void showBossbar() {
        if (!isBoss || entityUUID == null) return;

        Entity entity = Bukkit.getEntity(entityUUID);
        if (entity == null) return;

        // Calculate health percentage
        double healthPercentage = health / maxHealth;

        // Create or update the boss bar
        if (bossBar == null) {
            bossBar = BossBar.bossBar(
                    Component.text(visibleName + " [Lvl " + level + "]"),
                    (float) healthPercentage,
                    BossBar.Color.RED,
                    BossBar.Overlay.PROGRESS
            );
        } else {
            bossBar.name(Component.text(visibleName + " [Lvl " + level + "]"));
            bossBar.progress((float) healthPercentage);
        }

        // Show boss bar to nearby players
        for (org.bukkit.entity.Player player : Bukkit.getOnlinePlayers()) {
            if (player.getWorld().equals(entity.getWorld()) &&
                    player.getLocation().distance(entity.getLocation()) < 50) {
                player.showBossBar(bossBar);
            } else {
                player.hideBossBar(bossBar);
            }
        }
    }

    public void despawn() {
        if (entityUUID != null) {
            Entity entity = Bukkit.getEntity(entityUUID);
            if (entity != null) {
                // Remove boss bar if present
                if (bossBar != null) {
                    for (org.bukkit.entity.Player player : Bukkit.getOnlinePlayers()) {
                        player.hideBossBar(bossBar);
                    }
                }

                // Remove entity from storage and world
                RPGCore.entityStorage.remove(entityUUID);
                entity.remove();
            }
        }
    }

    private void updateEquipment() {
        if (entityUUID == null) return;

        Entity entity = Bukkit.getEntity(entityUUID);
        if (entity instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity) entity;
            EntityEquipment equipment = livingEntity.getEquipment();

            if (equipment != null) {
                if (helmet != null) equipment.setHelmet(helmet);
                if (chestplate != null) equipment.setChestplate(chestplate);
                if (leggings != null) equipment.setLeggings(leggings);
                if (boots != null) equipment.setBoots(boots);
                if (mainHand != null) equipment.setItemInMainHand(mainHand);
                if (offHand != null) equipment.setItemInOffHand(offHand);

                // Prevent equipment drops
                equipment.setHelmetDropChance(0);
                equipment.setChestplateDropChance(0);
                equipment.setLeggingsDropChance(0);
                equipment.setBootsDropChance(0);
                equipment.setItemInMainHandDropChance(0);
                equipment.setItemInOffHandDropChance(0);
            }
        }
    }
}