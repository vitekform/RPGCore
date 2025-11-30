package cz.vitekform.rPGCore;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import cz.vitekform.rPGCore.commands.args.classes.RPGCoreSubcommandArgument;
import cz.vitekform.rPGCore.commands.args.enums.RPGCoreSubcommand;
import cz.vitekform.rPGCore.listeners.*;
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
import java.util.*;

public final class RPGCore extends JavaPlugin {

    public static final Map<UUID, RPGPlayer> playerStorage = new HashMap<>();
    public static final Map<UUID, RPGEntity> entityStorage = new HashMap<>();

   public static List<Component> fancyText(List<Component> original) {
    Map<Character, Character> charMap = Map.ofEntries(
        Map.entry('a', 'ᴀ'), Map.entry('A', 'ᴀ'),
        Map.entry('b', 'ʙ'), Map.entry('B', 'ʙ'),
        Map.entry('c', 'ᴄ'), Map.entry('C', 'ᴄ'),
        Map.entry('d', 'ᴅ'), Map.entry('D', 'ᴅ'),
        Map.entry('e', 'ᴇ'), Map.entry('E', 'ᴇ'),
        Map.entry('f', 'ꜰ'), Map.entry('F', 'ꜰ'),
        Map.entry('g', 'ɢ'), Map.entry('G', 'ɢ'),
        Map.entry('h', 'ʜ'), Map.entry('H', 'ʜ'),
        Map.entry('i', 'ɪ'), Map.entry('I', 'ɪ'),
        Map.entry('j', 'ᴊ'), Map.entry('J', 'ᴊ'),
        Map.entry('k', 'ᴋ'), Map.entry('K', 'ᴋ'),
        Map.entry('l', 'ʟ'), Map.entry('L', 'ʟ'),
        Map.entry('m', 'ᴍ'), Map.entry('M', 'ᴍ'),
        Map.entry('n', 'ɴ'), Map.entry('N', 'ɴ'),
        Map.entry('o', 'ᴏ'), Map.entry('O', 'ᴏ'),
        Map.entry('p', 'ᴘ'), Map.entry('P', 'ᴘ'),
        Map.entry('q', 'ǫ'), Map.entry('Q', 'ǫ'),
        Map.entry('r', 'ʀ'), Map.entry('R', 'ʀ'),
        Map.entry('s', 's'), Map.entry('S', 's'),
        Map.entry('t', 'ᴛ'), Map.entry('T', 'ᴛ'),
        Map.entry('u', 'ᴜ'), Map.entry('U', 'ᴜ'),
        Map.entry('v', 'ᴠ'), Map.entry('V', 'ᴠ'),
        Map.entry('w', 'ᴡ'), Map.entry('W', 'ᴡ'),
        Map.entry('x', 'x'), Map.entry('X', 'x'),
        Map.entry('y', 'ʏ'), Map.entry('Y', 'ʏ'),
        Map.entry('z', 'ᴢ'), Map.entry('Z', 'ᴢ')
    );

    List<Component> nComponents = new ArrayList<>();
    for (Component c : original) {
        String plainText = PlainTextComponentSerializer.plainText().serialize(c);
        StringBuilder sb = new StringBuilder();

        for (char ch : plainText.toCharArray()) {
            sb.append(charMap.getOrDefault(ch, ch));
        }

        if (!sb.isEmpty()) {
            TextColor color = c.color();
            Map<TextDecoration, TextDecoration.State> decs = c.decorations();
            if (decs.isEmpty()) {
                decs = Map.of(TextDecoration.ITALIC, TextDecoration.State.FALSE);
            }
            nComponents.add(Component.text(sb.toString(), color).decorations(decs));
        }
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
        });

        // Register events

        Bukkit.getPluginManager().registerEvents(new InventoryHandler(), this);
        Bukkit.getPluginManager().registerEvents(new LoginHandler(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerDamageHandler(), this);
        Bukkit.getPluginManager().registerEvents(new EntityDamageHandler(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerEatFoodHandler(), this);

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
            else if (ctx.getArgument("subcommand", RPGCoreSubcommand.class).equals(RPGCoreSubcommand.CLASS)) {
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
            }
            else if (ctx.getArgument("subcommand", RPGCoreSubcommand.class).equals(RPGCoreSubcommand.ATTRIBUTES)) {
                if (ctx.getSource().getSender() instanceof Player p) {
                    if (playerStorage.containsKey(p.getUniqueId())) {
                        Inventory gui = Bukkit.createInventory(p, 27, "Your attributes");

                        ItemStack nothing = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
                        ItemMeta nothingMeta = nothing.getItemMeta();
                        nothingMeta.displayName(Component.text(" "));
                        nothing.setItemMeta(nothingMeta);

                        for (int i = 0; i < 27; i++) {
                            gui.setItem(i, nothing);
                        }

                        gui.setItem(9, ItemDictionary.strengthAttributeItem().build());
                        gui.setItem(11, ItemDictionary.dexterityAttributeItem().build());
                        gui.setItem(13, ItemDictionary.intelligenceAttributeItem().build());
                        gui.setItem(15, ItemDictionary.enduranceAttributeItem().build());
                        gui.setItem(17, ItemDictionary.vitalityAttributeItem().build());
                        gui.setItem(26, ItemDictionary.attributePointsItem(playerStorage.get(p.getUniqueId())).build());

                        p.openInventory(gui);
                    }
                }
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
            else if (ctx.getArgument("subcommand", RPGCoreSubcommand.class).equals(RPGCoreSubcommand.KIT_ADVENTURER)) {
                if (ctx.getSource().getSender() instanceof Player p) {
                    RPGPlayer pl = playerStorage.get(p.getUniqueId());
                    if (pl != null) {
                        pl.giveItem(ItemDictionary.adventurerSword());
                        pl.giveItem(ItemDictionary.adventurerHelmet());
                        pl.giveItem(ItemDictionary.adventurerChestplate());
                        pl.giveItem(ItemDictionary.adventurerLeggings());
                        pl.giveItem(ItemDictionary.adventurerBoots());
                        pl.giveItem(ItemDictionary.staleBread(), 5);
                        ctx.getSource().getSender().sendMessage(Component.text("You have been given an Adventurer's kit!", NamedTextColor.GREEN));
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
                if (!pl.isOnline() || !playerStorage.containsKey(pl.getUniqueId())) {
                    this.cancel();
                    return;
                }
                double maxHealth = p.maxHealth_Base + p.health_Items;
                double health = p.health;

                if (health > maxHealth) {
                    health = maxHealth; // Ensure health does not exceed max health
                }

                int maxMana = p.maxMana_Base + p.mana_Items;
                int mana = p.mana;

                if (mana > maxMana) {
                    mana = maxMana; // Ensure mana does not exceed max mana
                }

                int defense = p.defense_Base + p.defense_Items;

                double healthFactor = health / maxHealth;

                if (healthFactor > 1.0) {
                    // Cap health factor to prevent display issues
                    healthFactor = 1.0;
                }
                if (healthFactor <= 0) {
                    // death
                    pl.setHealth(0);
                    return;
                }
                pl.setHealth(healthFactor * 40);

                pl.sendActionBar(Component.text(ChatColor.RED + "❤ " + health + "/" + maxHealth + "  " + ChatColor.BLUE + "✦ " + mana + "/" + maxMana + "  " + ChatColor.GREEN + "\uD83D\uDEE1 " + defense, NamedTextColor.WHITE));
            }
        }.runTaskTimer(RPGCore.getPlugin(RPGCore.class), 10, 5);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (pl.isOnline()) {
                    // Get regeneration factor
                    double regenFactor = p.totalAttributes().get(RPGAttribute.VITALITY);
                    if (regenFactor < 1) {
                        regenFactor = 1;
                    }
                    if (pl.getFoodLevel() > 15) {
                        if (p.health < p.maxHealth_Base + p.health_Items) {
                            p.health += regenFactor;
                            if (p.health > p.maxHealth_Base + p.health_Items) {
                                p.health = p.maxHealth_Base + p.health_Items;
                            }
                        }
                    }
                    // Regenrate mana
                    if (pl.getFoodLevel() > 15) {
                        if (p.mana < p.maxMana_Base + p.mana_Items) {
                            p.mana += (int) regenFactor;
                            if (p.mana > p.maxMana_Base + p.mana_Items) {
                                p.mana = p.maxMana_Base + p.mana_Items;
                            }
                        }
                    }
                } else {
                    this.cancel();
                }
            }
        }.runTaskTimer(RPGCore.getPlugin(RPGCore.class), 10, 20);
    }
}
