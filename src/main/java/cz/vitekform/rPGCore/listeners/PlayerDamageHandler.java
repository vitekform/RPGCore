package cz.vitekform.rPGCore.listeners;

import cz.vitekform.rPGCore.RPGCore;
import cz.vitekform.rPGCore.objects.RPGPlayer;
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
                double dmg = event.getDamage() * 10;
                int d = ((int) dmg - RPGCore.playerStorage.get(p.getUniqueId()).defense_Base + RPGCore.playerStorage.get(p.getUniqueId()).defense_Items);
                System.out.println(d);
                RPGCore.playerStorage.get(p.getUniqueId()).health -= d;

                if (RPGCore.playerStorage.get(p.getUniqueId()).health <= 0) {
                    p.setHealth(0);
                    System.out.println("death");
                }
                else {
                    RPGPlayer rp = RPGCore.playerStorage.get(p.getUniqueId());
                    rp.health = rp.health - d;
                    System.out.println(rp.health);
                }
                if (RPGCore.playerStorage.get(p.getUniqueId()).health < 0) {
                    RPGCore.playerStorage.get(p.getUniqueId()).health = RPGCore.playerStorage.get(p.getUniqueId()).maxHealth_Base + RPGCore.playerStorage.get(p.getUniqueId()).health_Items;
                }
            }
        }
    }

    @EventHandler
    public void whenPlayerDies(PlayerDeathEvent event) {
        Player p = event.getEntity();
        RPGCore.playerStorage.get(p.getUniqueId()).health = RPGCore.playerStorage.get(p.getUniqueId()).maxHealth_Base + RPGCore.playerStorage.get(p.getUniqueId()).health_Items;
    }
}
