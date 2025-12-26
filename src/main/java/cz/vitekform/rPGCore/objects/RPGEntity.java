package cz.vitekform.rPGCore.objects;

import cz.vitekform.rPGCore.RPGCore;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;

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

    // Custom model/texture properties for resource pack
    public String modelPath;       // Path to custom model file in /plugins/RPGCore/entities/models/
    public String texturePath;     // Path to custom texture file in /plugins/RPGCore/entities/textures/
    public String customModelKey;  // The key used in the resourcepack for this entity's model

    // Equipment slots
    private ItemStack helmet;
    private ItemStack chestplate;
    private ItemStack leggings;
    private ItemStack boots;
    private ItemStack mainHand;
    private ItemStack offHand;

    // Reference to actual entity
    private UUID entityUUID;
    private UUID displayEntityUUID;  // For custom model display entity
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
        this.modelPath = null;
        this.texturePath = null;
        this.customModelKey = null;
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

            // Apply custom model if available
            if (customModelKey != null && !customModelKey.isEmpty()) {
                applyCustomModel(livingEntity);
            }

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

                // Remove display entity if present
                if (displayEntityUUID != null) {
                    Entity displayEntity = Bukkit.getEntity(displayEntityUUID);
                    if (displayEntity != null) {
                        displayEntity.remove();
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

    /**
     * Applies a custom model to the entity using an ItemDisplay entity.
     * The display entity rides on top of the main entity and shows the custom model.
     */
    private void applyCustomModel(LivingEntity entity) {
        try {
            // Create an ItemDisplay entity at the same location
            Location loc = entity.getLocation();
            ItemDisplay display = (ItemDisplay) entity.getWorld().spawnEntity(loc, EntityType.ITEM_DISPLAY);
            
            // Create an item with the custom model
            ItemStack displayItem = new ItemStack(Material.PAPER);
            org.bukkit.inventory.meta.ItemMeta meta = displayItem.getItemMeta();
            
            // Set the custom model key
            NamespacedKey modelKey = NamespacedKey.fromString(customModelKey);
            if (modelKey != null) {
                meta.setItemModel(modelKey);
                displayItem.setItemMeta(meta);
                
                // Set the item on the display entity
                display.setItemStack(displayItem);
                
                // Configure display properties
                display.setBillboard(Display.Billboard.VERTICAL);  // Face the player
                display.setViewRange(64.0f);  // Visible from 64 blocks away
                
                // Make the display entity ride the main entity
                entity.addPassenger(display);
                
                // Make the main entity invisible so only custom model shows
                entity.setInvisible(true);
                
                // Store the display entity UUID
                this.displayEntityUUID = display.getUniqueId();
            }
        } catch (Exception e) {
            // If custom model application fails, log warning but don't crash
            Bukkit.getLogger().warning("Failed to apply custom model to entity: " + e.getMessage());
        }
    }
}