package cz.vitekform.rPGCore.listeners;

import cz.vitekform.rPGCore.RPGCore;
import cz.vitekform.rPGCore.objects.RPGClass;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class InventoryHandler implements Listener {

    @EventHandler
    public void whenPlayerClicksInInventory(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player p) {
            if (event.getView().getTitle().equals("Select your class")) {
                event.setCancelled(true);
                if (event.getCurrentItem().getType().equals(Material.IRON_SWORD)) {
                    p.sendMessage(Component.text("You chose your class to be a warrior!", NamedTextColor.GREEN));
                    RPGCore.playerStorage.get(p.getUniqueId()).rpgClass = RPGClass.WARRIOR;
                    p.closeInventory();
                    p.playSound(p, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                }
            }
            if (event.getInventory().getHolder() instanceof Player) {
                // It is player inventory
                p.sendMessage("Slot: "+ event.getSlot());
                if (event.getSlot() >= 36 && event.getSlot() <= 39) {
                    // It is armor
                }
            }
        }
    }
}
