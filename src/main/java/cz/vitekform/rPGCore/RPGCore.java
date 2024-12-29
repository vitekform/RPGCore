package cz.vitekform.rPGCore;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import cz.vitekform.rPGCore.commands.args.classes.RPGCoreSubcommandArgument;
import cz.vitekform.rPGCore.commands.args.enums.RPGCoreSubcommand;
import cz.vitekform.rPGCore.listeners.InventoryHandler;
import cz.vitekform.rPGCore.listeners.LoginHandler;
import cz.vitekform.rPGCore.listeners.PlayerDamageHandler;
import cz.vitekform.rPGCore.objects.RPGClass;
import cz.vitekform.rPGCore.objects.RPGPlayer;
import cz.vitekform.rPGCore.pluginUtils.PluginUpdater;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class RPGCore extends JavaPlugin {

    public static final Map<UUID, RPGPlayer> playerStorage = new HashMap<>();

    @Override
    public void onEnable() {
        getLogger().info(ChatColor.YELLOW + "Loading ganamaga's RPGCore...");

        if (PluginUpdater.isLatest()) {
            getLogger().info(ChatColor.GREEN + "You are running the latest version of RPGCore.");
        } else {
            getLogger().info(ChatColor.RED + "You are not running the latest version of RPGCore. Please update to build " + PluginUpdater.latestBuild(PluginUpdater.buildChannelString()) + " from " + PluginUpdater.buildChannelString() + " Build Channel. Using /rpg update");
        }

        final LifecycleEventManager<Plugin> manager = this.getLifecycleManager();
        manager.registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            final Commands commands = event.registrar();
            commands.register(
                    Commands.literal("rpgcore")
                            .then(
                                    Commands.argument("subcommand", new RPGCoreSubcommandArgument())
                                            .executes(ctx -> {
                                                handleMainCommand(ctx, this);
                                                return Command.SINGLE_SUCCESS;
                                            })
                            )
                            .build(), "The main command for RPGCore.", List.of("rpg", "rpgc", "core")
            );

            commands.register(
                    Commands.literal("class")
                            .executes(
                                    ctx -> {
                                        if (ctx.getSource().getSender() instanceof Player p) {
                                            if (playerStorage.containsKey(p.getUniqueId())) {
                                                RPGPlayer pl = playerStorage.get(p.getUniqueId());
                                                if (pl.rpgClass == RPGClass.NONE) {
                                                    Inventory gui = Bukkit.createInventory(p, 27, "Select your class");

                                                    ItemStack nothing = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
                                                    ItemMeta nothingMeta = nothing.getItemMeta();
                                                    nothingMeta.displayName(Component.text(" "));
                                                    nothing.setItemMeta(nothingMeta);

                                                    for (int i = 0; i < 27; i++) {
                                                        gui.setItem(i, nothing);
                                                    }

                                                    gui.setItem(10, ItemDictionary.warriorClassItem().build());
                                                    gui.setItem(13, ItemDictionary.archerClassItem().build());
                                                    gui.setItem(16, ItemDictionary.mageClassItem().build());

                                                    p.openInventory(gui);
                                                }
                                                else {
                                                    p.sendMessage(Component.text("You already have a class!", NamedTextColor.RED));
                                                }
                                            }
                                        }

                                        return Command.SINGLE_SUCCESS;
                                    }
                            ).build(), "Opens Class Selection Menu", List.of("classes")
            );
        });

        // Register events

        Bukkit.getPluginManager().registerEvents(new InventoryHandler(), this);
        Bukkit.getPluginManager().registerEvents(new LoginHandler(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerDamageHandler(), this);

        super.onEnable();
        getLogger().info(ChatColor.GREEN + "RPGCore Loaded!");
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    private void handleMainCommand(final CommandContext<io.papermc.paper.command.brigadier.CommandSourceStack> ctx, RPGCore plugin) {
        if (ctx.getSource().getSender().hasPermission("rpgcore.admin")) {
            if (ctx.getArgument("subcommand", RPGCoreSubcommand.class).equals(RPGCoreSubcommand.UPDATE)) {
                ctx.getSource().getSender().sendMessage(PluginUpdater.update());
                if (!PluginUpdater.isLatest()) {
                    Bukkit.getPluginManager().disablePlugin(plugin);
                }
            } else if (ctx.getArgument("subcommand", RPGCoreSubcommand.class).equals(RPGCoreSubcommand.VERSION)) {
                ctx.getSource().getSender().sendMessage(Component.text("Currently running version " + PluginUpdater.pluginVersion + " Build " + PluginUpdater.build + " from " + PluginUpdater.buildChannelString() + " Build Channel", NamedTextColor.GREEN));
            }
            else if (ctx.getArgument("subcommand", RPGCoreSubcommand.class).equals(RPGCoreSubcommand.HELP)) {
                Map<String, Component> help = new HashMap<>();
                help.put("update", Component.text("Update the plugin.", NamedTextColor.GREEN));
                help.put("version", Component.text("Get the current version of the plugin.", NamedTextColor.GREEN));
                help.put("help", Component.text("Get help.", NamedTextColor.GREEN));

                for (Map.Entry<String, Component> entry : help.entrySet()) {
                    ctx.getSource().getSender().sendMessage(Component.text("/rpgcore " + entry.getKey() + " - ", NamedTextColor.GRAY).append(entry.getValue()));
                }
            }
            else {
                ctx.getSource().getSender().sendMessage(Component.text("Unknown subcommand.", NamedTextColor.RED));
            }
        }
    }

    public static FileConfiguration safeGetConfig(String name) {
        String dataFolder = System.getProperty("user.dir") + "/plugins/RPGCore";
        if (!new File(dataFolder).exists()) {
            new File(dataFolder).mkdir();
        }

        if (!new java.io.File(dataFolder, name).exists()) {
            try {
                new File(dataFolder, name).createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return YamlConfiguration.loadConfiguration(new File(dataFolder, name));
    }

    public static void syncDataWithReality(Player p) {
        RPGPlayer pl = playerStorage.get(p.getUniqueId());
        pl.maxHealth = pl.endurance * 10;
        if (pl.maxHealth == 0) {
            pl.maxHealth = 1;
        }
        if (pl.health > pl.maxHealth) {
            pl.health = pl.maxHealth;
        }
        pl.maxMana = pl.intelligence * 10;

        playerStatsCycleStart(pl);
    }

    private static void playerStatsCycleStart(RPGPlayer p) {
        Player pl = Bukkit.getPlayer(p.uuid);
        pl.setMaxHealth(40);

        new BukkitRunnable() {
            @Override
            public void run() {
                double maxHealth = p.maxHealth;
                double health = p.health;
                int maxMana = p.maxMana;
                int mana = p.mana;
                int level = p.level;
                int exp = p.exp;
                int maxExp = p.maxExp;
                int defense = p.defense;

                double healthFactor = health / maxHealth;

                pl.setHealth(healthFactor * 40);

                pl.sendActionBar(Component.text(ChatColor.RED + "❤ " + health + "/" + maxHealth + "  " + ChatColor.BLUE + "✦ " + mana + "/" + maxMana + "  " + ChatColor.GREEN + "\uD83D\uDEE1 " + defense, NamedTextColor.WHITE));
            }
        }.runTaskTimer(RPGCore.getPlugin(RPGCore.class), 10, 5);
    }
}
