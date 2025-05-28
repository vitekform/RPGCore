package cz.vitekform.rPGCore;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import cz.vitekform.rPGCore.commands.args.classes.RPGCoreSubcommandArgument;
import cz.vitekform.rPGCore.commands.args.enums.RPGCoreSubcommand;
import cz.vitekform.rPGCore.listeners.EntityDamageHandler;
import cz.vitekform.rPGCore.listeners.InventoryHandler;
import cz.vitekform.rPGCore.listeners.LoginHandler;
import cz.vitekform.rPGCore.listeners.PlayerDamageHandler;
import cz.vitekform.rPGCore.objects.*;
import cz.vitekform.rPGCore.pluginUtils.PluginUpdater;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
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
    public static final Map<UUID, RPGEntity> entityStorage = new HashMap<>();

    public static List<Component> fancyText(List<Component> original) {
        List<String> lines = new java.util.ArrayList<>();
        List<TextColor> colors = new java.util.ArrayList<>();
        List<Map<TextDecoration, TextDecoration.State>> decorations = new java.util.ArrayList<>();
        List<String> nLines = new java.util.ArrayList<>();
        for (Component c : original) {
            String plainText = PlainTextComponentSerializer.plainText().serialize(c);
            lines.add(plainText);
            colors.add(c.color());
            if (!c.decorations().isEmpty()) {
                decorations.add(c.decorations());
            }
            else {
                decorations.add(Map.of(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            }
        }

        for (String s : lines) {
            char[] chars = s.toCharArray();
            List<Character> nChars = new java.util.ArrayList<>();
            for (char c : chars) {
                if (c == 'a' || c == 'A') {
                    c = 'ᴀ';
                }
                else if (c == 'b' || c == 'B') {
                    c = 'ʙ';
                }
                else if (c == 'c' || c == 'C') {
                    c = 'ᴄ';
                }
                else if (c == 'd' || c == 'D') {
                    c = 'ᴅ';
                }
                else if (c == 'e' || c == 'E') {
                    c = 'ᴇ';
                }
                else if (c == 'f' || c == 'F') {
                    c = 'ꜰ';
                }
                else if (c == 'g' || c == 'G') {
                    c = 'ɢ';
                }
                else if (c == 'h' || c == 'H') {
                    c = 'ʜ';
                }
                else if (c == 'i' || c == 'I') {
                    c = 'ɪ';
                }
                else if (c == 'j' || c == 'J') {
                    c = 'ᴊ';
                }
                else if (c == 'k' || c == 'K') {
                    c = 'ᴋ';
                }
                else if (c == 'l' || c == 'L') {
                    c = 'ʟ';
                }
                else if (c == 'm' || c == 'M') {
                    c = 'ᴍ';
                }
                else if (c == 'n' || c == 'N') {
                    c = 'ɴ';
                }
                else if (c == 'o' || c == 'O') {
                    c = 'ᴏ';
                }
                else if (c == 'p' || c == 'P') {
                    c = 'ᴘ';
                }
                else if (c == 'q' || c == 'Q') {
                    c = 'ǫ';
                }
                else if (c == 'r' || c == 'R') {
                    c = 'ʀ';
                }
                else if (c == 's' || c == 'S') {
                    c = 's';
                }
                else if (c == 't' || c == 'T') {
                    c = 'ᴛ';
                }
                else if (c == 'u' || c == 'U') {
                    c = 'ᴜ';
                }
                else if (c == 'v' || c == 'V') {
                    c = 'ᴠ';
                }
                else if (c == 'w' || c == 'W') {
                    c = 'ᴡ';
                }
                else if (c == 'x' || c == 'X') {
                    c = 'x';
                }
                else if (c == 'y' || c == 'Y') {
                    c = 'ʏ';
                }
                else if (c == 'z' || c == 'Z') {
                    c = 'ᴢ';
                }
                nChars.add(c);
            }
            String nString = "";
            for (Character c : nChars) {
                nString += c;
            }
            if (!nString.isBlank()) {
                nLines.add(nString);
                colors.add(colors.get(lines.indexOf(s))); // also keep color in sync
            }
        }
        List<Component> nComponents = new java.util.ArrayList<>();
        for (int i = 0; i < nLines.size(); i++) {
            nComponents.add(Component.text(nLines.get(i), colors.get(i)).decorations(decorations.get(i)));
        }
        return nComponents;
    }

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
        Bukkit.getPluginManager().registerEvents(new EntityDamageHandler(), this);

        // Load entity data
        FileConfiguration entityData = safeGetConfig("entityData.yml");
        if (entityData.contains("entity")) {
            for (String key : entityData.getConfigurationSection("entity").getKeys(false)) {
                String path = "entity." + key + ".";
                RPGEntity entity = new RPGEntity();
                entity.visibleName = entityData.getString(path + "visibleName", "");
                entity.level = entityData.getInt(path + "level", 1);
                entity.maxHealth = entityData.getInt(path + "maxHealth", 20);
                entity.health = entityData.getInt(path + "health", 20);
                entity.attack = entityData.getDouble(path + "attack", 1.0);
                entity.defense = entityData.getInt(path + "defense", 0);
                entity.speed = entityData.getDouble(path + "speed", 0.1);
                entity.experienceAfterDefeat = entityData.getInt(path + "experienceAfterDefeat", 0);
                List<String> drops = entityData.getStringList(path + "drops");
                for (String drop : drops) {
                    if (ItemDictionary.items.containsKey(drop)) {
                        entity.drops.add(ItemDictionary.items.get(drop));
                    }
                }
                entity.isBoss = entityData.getBoolean(path + "isBoss", false);
                entity.isFriendly = entityData.getBoolean(path + "isFriendly", false);
                entity.hasVisibleName = entityData.getBoolean(path + "hasVisibleName", false);
                UUID uuid = UUID.fromString(key);
                entityStorage.put(uuid, entity);
            }
        }
        super.onEnable();
        getLogger().info(ChatColor.GREEN + "RPGCore Loaded!");
    }

    @Override
    public void onDisable() {
        getLogger().info(ChatColor.YELLOW + "Disabling ganamaga's RPGCore...");
        // Save entity data
        FileConfiguration entityData = safeGetConfig("entityData.yml");
        for (Map.Entry<UUID, RPGEntity> entry : entityStorage.entrySet()) {
            RPGEntity entity = entry.getValue();
            Entity nEntity = Bukkit.getEntity(entry.getKey());
            Location loc = null;
            if (nEntity != null && nEntity instanceof LivingEntity) {
                loc = nEntity.getLocation();
            } else {
                getLogger().warning("Entity with UUID " + entry.getKey() + " not found in the world (This is most likely caused by natural despawn). Skipping saving its data.");
            }
            String path = "entity." + entry.getKey().toString() + ".";
            entityData.set(path + "visibleName", entity.visibleName);
            entityData.set(path + "level", entity.level);
            entityData.set(path + "maxHealth", entity.maxHealth);
            entityData.set(path + "health", entity.health);
            entityData.set(path + "attack", entity.attack);
            entityData.set(path + "defense", entity.defense);
            entityData.set(path + "speed", entity.speed);
            entityData.set(path + "experienceAfterDefeat", entity.experienceAfterDefeat);
            // Save drops
            List<String> drops = new java.util.ArrayList<>();
            for (RPGItem drop : entity.drops) {
                drops.add(drop.itemName.toString());
            }
            entityData.set(path + "drops", drops);
            entityData.set(path + "isBoss", entity.isBoss);
            entityData.set(path + "isFriendly", entity.isFriendly);
            entityData.set(path + "hasVisibleName", entity.hasVisibleName);
            if (entity.entityType != null) {
                entityData.set(path + "entityType", entity.entityType.name());
            }
        }
        try {
            entityData.save(new File(getDataFolder(), "entityData.yml"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        super.onDisable();
        getLogger().info(ChatColor.RED + "RPGCore Disabled!");
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
            else if (ctx.getArgument("subcommand", RPGCoreSubcommand.class).equals(RPGCoreSubcommand.GIVE)) {
                if (ctx.getSource().getSender() instanceof Player p) {
                    RPGPlayer pl = playerStorage.get(p.getUniqueId());
                    if (pl != null) {
                        pl.giveItem(ItemDictionary.adventurerSword());
                        ctx.getSource().getSender().sendMessage(Component.text("You have been given an Adventurer's Sword!", NamedTextColor.GREEN));
                    } else {
                        ctx.getSource().getSender().sendMessage(Component.text("You are not a registered RPG player.", NamedTextColor.RED));
                    }
                } else {
                    ctx.getSource().getSender().sendMessage(Component.text("This command can only be used by players.", NamedTextColor.RED));
                }
            }
            else if (ctx.getArgument("subcommand", RPGCoreSubcommand.class).equals(RPGCoreSubcommand.SUMMON)) {
                if (ctx.getSource().getSender() instanceof Player p) {
                    RPGPlayer pl = playerStorage.get(p.getUniqueId());
                    if (pl != null) {
                        // Summon a test entity
                        Location loc = p.getLocation();
                        RPGEntity entity = EntityDictionary.BANDIT_TUTORIAL_MELEE();
                        UUID uuid = entity.spawnIn(loc).getUniqueId();
                        entityStorage.put(uuid, entity);
                        ctx.getSource().getSender().sendMessage(Component.text("Test entity summoned!", NamedTextColor.GREEN));
                    } else {
                        ctx.getSource().getSender().sendMessage(Component.text("You are not a registered RPG player.", NamedTextColor.RED));
                    }
                } else {
                    ctx.getSource().getSender().sendMessage(Component.text("This command can only be used by players.", NamedTextColor.RED));
                }
            }
            else if (ctx.getArgument("subcommand", RPGCoreSubcommand.class).equals(RPGCoreSubcommand.TEST)) {
                Player p = (Player) ctx.getSource().getSender();
                RPGPlayer pl = playerStorage.get(p.getUniqueId());
                pl.updateItemStats();
                p.sendMessage(Component.text("Your stats have been updated.", NamedTextColor.GREEN));
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
        pl.maxHealth_Base = pl.totalAttributes().get(RPGAttribute.ENDURANCE) * 10;
        if (pl.maxHealth_Base == 0) {
            pl.maxHealth_Base = 1;
        }
        if (pl.health > pl.maxHealth_Base + pl.health_Items) {
            pl.health = pl.maxHealth_Base + pl.health_Items;
        }
        pl.maxMana_Base = pl.totalAttributes().get(RPGAttribute.INTELLIGENCE) * 10;

        playerStatsCycleStart(pl);
    }

    private static void playerStatsCycleStart(RPGPlayer p) {
        Player pl = Bukkit.getPlayer(p.uuid);
        pl.setMaxHealth(40);

        new BukkitRunnable() {
            @Override
            public void run() {
                double maxHealth = p.maxHealth_Base + p.health_Items;
                double health = p.health;
                int maxMana = p.maxMana_Base + p.mana_Items;
                int mana = p.mana;
                int level = p.level;
                int exp = p.exp;
                int maxExp = p.maxExp;
                int defense = p.defense_Base + p.defense_Items;

                double healthFactor = health / maxHealth;

                pl.setHealth(healthFactor * 40);

                pl.sendActionBar(Component.text(ChatColor.RED + "❤ " + health + "/" + maxHealth + "  " + ChatColor.BLUE + "✦ " + mana + "/" + maxMana + "  " + ChatColor.GREEN + "\uD83D\uDEE1 " + defense, NamedTextColor.WHITE));
            }
        }.runTaskTimer(RPGCore.getPlugin(RPGCore.class), 10, 5);
    }
}
