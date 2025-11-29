package cz.vitekform.rPGCore.listeners;

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import cz.vitekform.rPGCore.RPGCore;
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
                    p.sendMessage(Component.text("You chose your class to be a warrior!", NamedTextColor.GREEN));
                    RPGCore.playerStorage.get(p.getUniqueId()).rpgClass = RPGClass.WARRIOR;
                    p.closeInventory();
                    p.playSound(p, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                }
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
