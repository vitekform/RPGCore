package cz.vitekform.rPGCore.listeners;

import cz.vitekform.rPGCore.RPGCore;
import cz.vitekform.rPGCore.objects.RPGClass;
import cz.vitekform.rPGCore.objects.RPGPlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.IOException;

public class LoginHandler implements Listener {

    @EventHandler
    public void whenPlayerJoins(PlayerJoinEvent event) {
        Player p = event.getPlayer();
        FileConfiguration data = RPGCore.safeGetConfig("pdata.yml");
        if (data.contains("player." + p.getUniqueId())) {
            String rpgName = data.getString("player." + p.getUniqueId() + ".rpgName");
            int level = data.getInt("player." + p.getUniqueId() + ".level");
            int exp = data.getInt("player." + p.getUniqueId() + ".exp");
            int maxExp = data.getInt("player." + p.getUniqueId() + ".maxExp");
            int defense = data.getInt("player." + p.getUniqueId() + ".defense");
            int skillPoints = data.getInt("player." + p.getUniqueId() + ".skillPoints");
            int strength = data.getInt("player." + p.getUniqueId() + ".strength");
            int dexterity = data.getInt("player." + p.getUniqueId() + ".dexterity");
            int intelligence = data.getInt("player." + p.getUniqueId() + ".intelligence");
            int vitality = data.getInt("player." + p.getUniqueId() + ".vitality");
            int endurance = data.getInt("player." + p.getUniqueId() + ".endurance");
            int totalSkillPoints = data.getInt("player." + p.getUniqueId() + ".totalSkillPoints");
            int attributePoints = data.getInt("player." + p.getUniqueId() + ".attributePoints");
            double maxHealth = data.getInt("player." + p.getUniqueId() + ".maxHealth");
            double health = data.getInt("player." + p.getUniqueId() + ".health");
            int maxMana = data.getInt("player." + p.getUniqueId() + ".maxMana");
            int mana = data.getInt("player." + p.getUniqueId() + ".mana");
            String rpgClass = data.getString("player." + p.getUniqueId() + ".rpgClass");
            RPGPlayer rpgp = new RPGPlayer(p.getUniqueId(), p.getName(), rpgName, level, exp, maxExp, skillPoints, strength, dexterity, intelligence, vitality, endurance, totalSkillPoints, attributePoints, maxHealth, health, maxMana, mana, RPGClass.valueOf(rpgClass), 0, defense);
            RPGCore.playerStorage.put(p.getUniqueId(), rpgp);
        }
        else {
            RPGPlayer rpgp = new RPGPlayer(p.getUniqueId());
            rpgp.rpgClass = RPGClass.NONE;
            rpgp.maxHealth = 1D;
            rpgp.health = 1D;
            RPGCore.playerStorage.put(p.getUniqueId(), rpgp);
        }
        RPGCore.syncDataWithReality(p);
    }

    @EventHandler
    public void whenPlayerLeave(PlayerQuitEvent event) {
        Player p = event.getPlayer();

        FileConfiguration data = RPGCore.safeGetConfig("pdata.yml");
        RPGPlayer rpgp = RPGCore.playerStorage.get(p.getUniqueId());
        data.set("player." + p.getUniqueId() + ".rpgName", rpgp.rpgName);
        data.set("player." + p.getUniqueId() + ".level", rpgp.level);
        data.set("player." + p.getUniqueId() + ".exp", rpgp.exp);
        data.set("player." + p.getUniqueId() + ".maxExp", rpgp.maxExp);
        data.set("player." + p.getUniqueId() + ".skillPoints", rpgp.skillPoints);
        data.set("player." + p.getUniqueId() + ".strength", rpgp.strength);
        data.set("player." + p.getUniqueId() + ".dexterity", rpgp.dexterity);
        data.set("player." + p.getUniqueId() + ".intelligence", rpgp.intelligence);
        data.set("player." + p.getUniqueId() + ".vitality", rpgp.vitality);
        data.set("player." + p.getUniqueId() + ".endurance", rpgp.endurance);
        data.set("player." + p.getUniqueId() + ".totalSkillPoints", rpgp.totalSkillPoints);
        data.set("player." + p.getUniqueId() + ".attributePoints", rpgp.attributePoints);
        data.set("player." + p.getUniqueId() + ".maxHealth", rpgp.maxHealth);
        data.set("player." + p.getUniqueId() + ".health", rpgp.health);
        data.set("player." + p.getUniqueId() + ".maxMana", rpgp.maxMana);
        data.set("player." + p.getUniqueId() + ".mana", rpgp.mana);
        data.set("player." + p.getUniqueId() + ".rpgClass", rpgp.rpgClass.toString());
        RPGCore.playerStorage.remove(p.getUniqueId());

        try {
            data.save(System.getProperty("user.dir") + "/plugins/RPGCore/pdata.yml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
