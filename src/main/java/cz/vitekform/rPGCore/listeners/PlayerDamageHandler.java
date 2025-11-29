package cz.vitekform.rPGCore.listeners;

import cz.vitekform.rPGCore.RPGCore;
import cz.vitekform.rPGCore.objects.RPGPlayer;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

public class PlayerDamageHandler implements Listener {

    @EventHandler
    public void whenEntityDamageEvent(EntityDamageEvent event) {
        if (event.getEntity() instanceof LivingEntity le) {
            if (le instanceof Player p) {
                double dmg = event.getDamage();
                RPGPlayer rp = RPGCore.playerStorage.get(p.getUniqueId());
                int d = (int) dmg - (rp.defense_Base + rp.defense_Items);
                if (d < 0) {
                    d = 0; // Minimum damage is 0
                }
                rp.health -= d;
            }
        }
    }

    @EventHandler
    public void whenPlayerDies(PlayerDeathEvent event) {
        event.setCancelled(true);
        Player p = event.getEntity();
        RPGCore.playerStorage.get(p.getUniqueId()).health = RPGCore.playerStorage.get(p.getUniqueId()).maxHealth_Base + RPGCore.playerStorage.get(p.getUniqueId()).health_Items;
        RPGCore.playerStorage.get(p.getUniqueId()).mana = RPGCore.playerStorage.get(p.getUniqueId()).maxMana_Base + RPGCore.playerStorage.get(p.getUniqueId()).mana_Items;
        p.setHealth(40); // Set health to 20 hearts (40 health points)
        Location l = p.getRespawnLocation();
        if (l == null) {
            l = p.getWorld().getSpawnLocation();
        }
        p.teleport(l);
        event.deathMessage(Component.text("Hi!"));
    }
}
