package cz.vitekform.rPGCore;

import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public final class RPGCore extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info(ChatColor.YELLOW + "Loading ganamaga's RPGCore...");
        super.onEnable();
        getLogger().info(ChatColor.GREEN + "RPGCore Loaded!");
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }
}
