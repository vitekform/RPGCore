package cz.vitekform.rPGCore.examples;

import cz.vitekform.rPGCore.api.RPGCoreAPI;
import cz.vitekform.rPGCore.api.RPGCoreAPIProvider;
import cz.vitekform.rPGCore.objects.RPGClass;
import cz.vitekform.rPGCore.objects.RPGEntity;
import cz.vitekform.rPGCore.objects.RPGItem;
import cz.vitekform.rPGCore.objects.RPGPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;

/**
 * Example plugin demonstrating the use of RPGCore Public API.
 * 
 * This is a complete example that shows how to:
 * - Hook into the RPGCore API
 * - Register custom items
 * - Spawn custom entities
 * - Access player data
 * - List all registered items and entities
 */
public class RPGCoreAPIExample extends JavaPlugin implements Listener {
    
    private RPGCoreAPI rpgCoreAPI;
    
    @Override
    public void onEnable() {
        // Check if RPGCore is available
        if (!RPGCoreAPIProvider.isAPIAvailable()) {
            getLogger().severe("RPGCore is not available! This plugin requires RPGCore to function.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        // Get the API instance
        rpgCoreAPI = RPGCoreAPIProvider.getAPI();
        getLogger().info("Successfully hooked into RPGCore API v" + rpgCoreAPI.getVersion());
        
        // Register event listeners
        getServer().getPluginManager().registerEvents(this, this);
        
        // Register custom items
        registerCustomItems();
        
        getLogger().info("RPGCore API Example Plugin enabled!");
    }
    
    /**
     * Register some custom items using the API
     */
    private void registerCustomItems() {
        // Create a custom legendary sword
        RPGItem legendarySword = new RPGItem();
        legendarySword.itemName = Component.text("Legendary Blade").color(NamedTextColor.GOLD);
        legendarySword.material = Material.NETHERITE_SWORD;
        legendarySword.attack = 100.0;
        legendarySword.attackSpeed = 2.0;
        legendarySword.critChance = 15.0;
        legendarySword.reqLevel = 50;
        legendarySword.reqClass = RPGClass.WARRIOR;
        legendarySword.slotReq = 0; // Main hand
        
        if (rpgCoreAPI.registerItem("legendary_blade", legendarySword)) {
            getLogger().info("Registered custom item: legendary_blade");
        }
        
        // Create a custom mage staff
        RPGItem mageStaff = new RPGItem();
        mageStaff.itemName = Component.text("Arcane Staff").color(NamedTextColor.LIGHT_PURPLE);
        mageStaff.material = Material.BLAZE_ROD;
        mageStaff.attack = 30.0;
        mageStaff.mana = 100;
        mageStaff.reqLevel = 25;
        mageStaff.reqClass = RPGClass.MAGE;
        mageStaff.slotReq = 0; // Main hand
        
        if (rpgCoreAPI.registerItem("arcane_staff", mageStaff)) {
            getLogger().info("Registered custom item: arcane_staff");
        }
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Get RPG player data
        RPGPlayer rpgPlayer = rpgCoreAPI.getRPGPlayer(player);
        if (rpgPlayer != null) {
            player.sendMessage(Component.text("Welcome back! Your RPG level is: " + rpgPlayer.level, NamedTextColor.GREEN));
            player.sendMessage(Component.text("Your class is: " + rpgPlayer.rpgClass, NamedTextColor.AQUA));
        }
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Component.text("This command can only be used by players.", NamedTextColor.RED));
            return true;
        }
        
        Player player = (Player) sender;
        
        switch (command.getName().toLowerCase()) {
            case "rpgapi-items":
                listAllItems(player);
                return true;
                
            case "rpgapi-entities":
                listAllEntities(player);
                return true;
                
            case "rpgapi-givelegendary":
                giveLegendarySword(player);
                return true;
                
            case "rpgapi-spawnboss":
                spawnCustomBoss(player);
                return true;
                
            default:
                return false;
        }
    }
    
    /**
     * List all registered items
     */
    private void listAllItems(Player player) {
        Map<String, RPGItem> items = rpgCoreAPI.getItemRegistry();
        player.sendMessage(Component.text("=== Registered Items ===", NamedTextColor.GOLD));
        player.sendMessage(Component.text("Total items: " + items.size(), NamedTextColor.YELLOW));
        
        int count = 0;
        for (Map.Entry<String, RPGItem> entry : items.entrySet()) {
            if (count++ > 10) {
                player.sendMessage(Component.text("... and " + (items.size() - 10) + " more", NamedTextColor.GRAY));
                break;
            }
            player.sendMessage(Component.text("- " + entry.getKey(), NamedTextColor.WHITE));
        }
    }
    
    /**
     * List all active entities
     */
    private void listAllEntities(Player player) {
        Map<java.util.UUID, RPGEntity> entities = rpgCoreAPI.getEntityRegistry();
        player.sendMessage(Component.text("=== Active RPG Entities ===", NamedTextColor.GOLD));
        player.sendMessage(Component.text("Total entities: " + entities.size(), NamedTextColor.YELLOW));
        
        for (Map.Entry<java.util.UUID, RPGEntity> entry : entities.entrySet()) {
            RPGEntity entity = entry.getValue();
            player.sendMessage(Component.text("- " + entity.visibleName + " (Level " + entity.level + ")", NamedTextColor.WHITE));
        }
    }
    
    /**
     * Give the player a legendary sword
     */
    private void giveLegendarySword(Player player) {
        RPGItem sword = rpgCoreAPI.getItem("legendary_blade");
        if (sword == null) {
            player.sendMessage(Component.text("Legendary blade not found!", NamedTextColor.RED));
            return;
        }
        
        player.getInventory().addItem(sword.build());
        player.sendMessage(Component.text("You have been given a Legendary Blade!", NamedTextColor.GOLD));
    }
    
    /**
     * Spawn a custom boss entity
     */
    private void spawnCustomBoss(Player player) {
        // Create a custom boss entity
        RPGEntity boss = new RPGEntity();
        boss.visibleName = "Ancient Dragon";
        boss.level = 75;
        boss.maxHealth = 5000.0;
        boss.health = 5000.0;
        boss.attack = 150.0;
        boss.defense = 50;
        boss.speed = 0.3;
        boss.experienceAfterDefeat = 1000;
        boss.isBoss = true;
        boss.hasVisibleName = true;
        boss.entityType = EntityType.ENDER_DRAGON;
        
        // Spawn the boss at player's location
        Location spawnLocation = player.getLocation().add(5, 0, 0);
        org.bukkit.entity.Entity spawnedEntity = boss.spawnIn(spawnLocation);
        
        player.sendMessage(Component.text("Ancient Dragon has been summoned!", NamedTextColor.DARK_RED));
    }
}
