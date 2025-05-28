package cz.vitekform.rPGCore.listeners;

import cz.vitekform.rPGCore.RPGCore;
import cz.vitekform.rPGCore.objects.RPGEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;

public class EntityDamageHandler implements Listener {

    @EventHandler
    public void whenEntityDamaged(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player) {
            return; // Because this is handled in PlayerDamageHandler
        }
        if (event.getEntity() instanceof LivingEntity le && event.getDamager() instanceof Player p) {
            double damage = RPGCore.playerStorage.get(p.getUniqueId()).attackDMG_Base + RPGCore.playerStorage.get(p.getUniqueId()).attackDMG_Items;
            double critChance = RPGCore.playerStorage.get(p.getUniqueId()).critChance_Base + RPGCore.playerStorage.get(p.getUniqueId()).critChance_Items;
            // Check if the damage is a critical hit
            if (Math.random() < (critChance / 100)) {
                damage *= 2; // Double the damage for critical hits
                // Spawn bunch of particles around the entity
                le.getWorld().spawnParticle(org.bukkit.Particle.CRIT, le.getLocation(), 10, 0.5, 0.5, 0.5, 0.1);
            }
            event.setDamage(damage);
        }
    }

    @EventHandler
    public void whenEntityDies(EntityDeathEvent event) {
        if (event.getDamageSource().getCausingEntity() instanceof Player p) {
            RPGEntity rpgEntity = RPGCore.entityStorage.get(event.getEntity().getUniqueId());
            if (rpgEntity != null) {
                // Give experience to the player
                RPGCore.playerStorage.get(p.getUniqueId()).handleExpAdd(rpgEntity.experienceAfterDefeat);
                // Optionally, you can also handle drops here
                event.getDrops().addAll(rpgEntity.dropsAsItemStacks());

                RPGCore.entityStorage.remove(event.getEntity().getUniqueId());
            }
        }
    }
}
