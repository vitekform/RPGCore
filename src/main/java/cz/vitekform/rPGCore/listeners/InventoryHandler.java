package cz.vitekform.rPGCore.listeners;

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import cz.vitekform.rPGCore.ItemDictionary;
import cz.vitekform.rPGCore.RPGCore;
import cz.vitekform.rPGCore.objects.RPGAttribute;
import cz.vitekform.rPGCore.objects.RPGClass;
import cz.vitekform.rPGCore.objects.RPGPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class InventoryHandler implements Listener {

    @EventHandler
    public void whenPlayerClicksInInventory(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player p) {
            if (event.getView().getTitle().equals("Select your class")) {
                event.setCancelled(true);
                if (event.getCurrentItem() != null && event.getCurrentItem().getType().equals(Material.IRON_SWORD)) {
                    p.sendMessage(Component.text("You chose to become a warrior!", NamedTextColor.GREEN));
                    RPGCore.playerStorage.get(p.getUniqueId()).rpgClass = RPGClass.WARRIOR;
                    p.closeInventory();
                    p.playSound(p, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                }
                else if (event.getCurrentItem() != null && event.getCurrentItem().getType().equals(Material.BOW)) {
                    p.sendMessage(Component.text("You chose to become an archer!", NamedTextColor.GREEN));
                    RPGCore.playerStorage.get(p.getUniqueId()).rpgClass = RPGClass.ARCHER;
                    p.closeInventory();
                    p.playSound(p, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                }
                else if (event.getCurrentItem() != null && event.getCurrentItem().getType().equals(Material.BLAZE_ROD)) {
                    p.sendMessage(Component.text("You chose to become a mage!", NamedTextColor.GREEN));
                    RPGCore.playerStorage.get(p.getUniqueId()).rpgClass = RPGClass.MAGE;
                    p.closeInventory();
                    p.playSound(p, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                }
            }
            else if (event.getView().getTitle().equals("Your attributes")) {
                event.setCancelled(true);
                RPGPlayer rpgp = RPGCore.playerStorage.get(p.getUniqueId());
                int availablePoints = rpgp.attributePoints;
                if (event.getCurrentItem() != null && event.getCurrentItem().getType().equals(Material.IRON_SWORD)) {
                    // check for attribute points
                    if (availablePoints > 0) {
                        rpgp.attributePoints--;
                        int current = rpgp.baseAttributes.get(RPGAttribute.STRENGTH);
                        rpgp.baseAttributes.remove(RPGAttribute.STRENGTH);
                        rpgp.baseAttributes.put(RPGAttribute.STRENGTH, current + 1);
                        p.playSound(p, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                        p.sendMessage(Component.text("You gained 1 strength point!", NamedTextColor.GREEN));
                        p.closeInventory();
                        RPGCore.syncDataWithReality(p);
                    }
                    else {
                        p.playSound(p, Sound.ENTITY_VILLAGER_NO, 1, 1);
                        p.sendMessage(Component.text("You do not have any attribute points left!", NamedTextColor.RED));
                    }
                }
                else if (event.getCurrentItem() != null && event.getCurrentItem().getType().equals(Material.BOW)) {
                    // check for attribute points
                    if (availablePoints > 0) {
                        rpgp.attributePoints--;
                        int current = rpgp.baseAttributes.get(RPGAttribute.DEXTERITY);
                        rpgp.baseAttributes.remove(RPGAttribute.DEXTERITY);
                        rpgp.baseAttributes.put(RPGAttribute.DEXTERITY, current + 1);
                        p.playSound(p, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                        p.sendMessage(Component.text("You gained 1 dexterity point!", NamedTextColor.GREEN));
                        p.closeInventory();
                        RPGCore.syncDataWithReality(p);
                    }
                    else {
                        p.playSound(p, Sound.ENTITY_VILLAGER_NO, 1, 1);
                        p.sendMessage(Component.text("You do not have any attribute points left!", NamedTextColor.RED));
                    }
                }
                else if (event.getCurrentItem() != null && event.getCurrentItem().getType().equals(Material.AMETHYST_SHARD)) {
                    // check for attribute points
                    if (availablePoints > 0) {
                        rpgp.attributePoints--;
                        int current = rpgp.baseAttributes.get(RPGAttribute.INTELLIGENCE);
                        rpgp.baseAttributes.remove(RPGAttribute.INTELLIGENCE);
                        rpgp.baseAttributes.put(RPGAttribute.INTELLIGENCE, current + 1);
                        p.playSound(p, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                        p.sendMessage(Component.text("You gained 1 intelligence point!", NamedTextColor.GREEN));
                        p.closeInventory();
                        RPGCore.syncDataWithReality(p);
                    }
                    else {
                        p.playSound(p, Sound.ENTITY_VILLAGER_NO, 1, 1);
                        p.sendMessage(Component.text("You do not have any attribute points left!", NamedTextColor.RED));
                    }
                }
                else if (event.getCurrentItem() != null && event.getCurrentItem().getType().equals(Material.IRON_CHESTPLATE)) {
                    // check for attribute points
                    if (availablePoints > 0) {
                        rpgp.attributePoints--;
                        int current = rpgp.baseAttributes.get(RPGAttribute.ENDURANCE);
                        rpgp.baseAttributes.remove(RPGAttribute.ENDURANCE);
                        rpgp.baseAttributes.put(RPGAttribute.ENDURANCE, current + 1);
                        p.playSound(p, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                        p.sendMessage(Component.text("You gained 1 endurance point!", NamedTextColor.GREEN));
                        RPGCore.syncDataWithReality(p);
                    }
                    else {
                        p.playSound(p, Sound.ENTITY_VILLAGER_NO, 1, 1);
                        p.sendMessage(Component.text("You do not have any attribute points left!", NamedTextColor.RED));
                    }
                }
                else if (event.getCurrentItem() != null && event.getCurrentItem().getType().equals(Material.GOLDEN_APPLE)) {
                    // check for attribute points
                    if (availablePoints > 0) {
                        rpgp.attributePoints--;
                        int current = rpgp.baseAttributes.get(RPGAttribute.VITALITY);
                        rpgp.baseAttributes.remove(RPGAttribute.VITALITY);
                        rpgp.baseAttributes.put(RPGAttribute.VITALITY, current + 1);
                        p.playSound(p, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                        p.sendMessage(Component.text("You gained 1 vitality point!", NamedTextColor.GREEN));
                        RPGCore.syncDataWithReality(p);
                    }
                    else {
                        p.playSound(p, Sound.ENTITY_VILLAGER_NO, 1, 1);
                        p.sendMessage(Component.text("You do not have any attribute points left!", NamedTextColor.RED));
                    }
                }
                rpgp.updateItemStats();
                event.getInventory().setItem(26, ItemDictionary.attributePointsItem(rpgp).build());
            }

            // Update stats for any inventory click that could affect equipped items
            RPGPlayer rpgp = RPGCore.playerStorage.get(p.getUniqueId());
            if (rpgp != null) {
                // Delay the stats update to ensure the inventory change is processed first
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        rpgp.updateItemStats();
                    }
                }.runTaskLater(RPGCore.getPlugin(RPGCore.class), 1L);
            }
        }
    }

    @EventHandler
    public void whenPlayerSwitchesItemInOffhand(PlayerSwapHandItemsEvent event) {
        Player p = event.getPlayer();
        RPGPlayer rpgp = RPGCore.playerStorage.get(p.getUniqueId());
        if (rpgp != null) {
            // Delay the stats update to ensure the item swap is processed first
            new BukkitRunnable() {
                @Override
                public void run() {
                    rpgp.updateItemStats();
                }
            }.runTaskLater(RPGCore.getPlugin(RPGCore.class), 1L);
        }
    }

    @EventHandler
    public void whenPlayerSwitchMainItem(PlayerItemHeldEvent event) {
        Player p = event.getPlayer();
        RPGPlayer rpgp = RPGCore.playerStorage.get(p.getUniqueId());
        if (rpgp != null) {
            // Update the player's stats after switching items
            new BukkitRunnable() {
                @Override
                public void run() {
                    rpgp.updateItemStats();
                }
            }.runTaskLater(RPGCore.getPlugin(RPGCore.class), 1L); // Delay to ensure the item switch is processed
        }
    }

    @EventHandler
    public void whenPlayerChangesArmor(PlayerArmorChangeEvent event) {
        Player p = event.getPlayer();
        RPGPlayer rpgp = RPGCore.playerStorage.get(p.getUniqueId());
        if (rpgp != null) {
            // Delay the stats update to ensure the armor change is processed first
            new BukkitRunnable() {
                @Override
                public void run() {
                    rpgp.updateItemStats();
                }
            }.runTaskLater(RPGCore.getPlugin(RPGCore.class), 1L);
        }
    }
}
