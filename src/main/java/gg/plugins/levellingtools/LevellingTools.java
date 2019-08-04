package gg.plugins.levellingtools;

import gg.plugins.levellingtools.command.util.CommandExecutor;
import gg.plugins.levellingtools.command.util.CommandManager;
import gg.plugins.levellingtools.config.CachedConfig;
import gg.plugins.levellingtools.config.Lang;
import gg.plugins.levellingtools.hook.PlaceholderAPIHook;
import gg.plugins.levellingtools.hook.WorldGuardHook;
import gg.plugins.levellingtools.listener.JoinListener;
import gg.plugins.levellingtools.listener.MineListener;
import gg.plugins.levellingtools.storage.PlayerData;
import gg.plugins.levellingtools.storage.StorageHandler;
import gg.plugins.levellingtools.storage.mongodb.MongoDBHandler;
import gg.plugins.levellingtools.storage.mysql.MySQLHandler;
import gg.plugins.levellingtools.storage.sqlite.SQLiteHandler;
import gg.plugins.levellingtools.util.ConsoleFilter;
import org.apache.logging.log4j.LogManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LevellingTools extends JavaPlugin {

    private StorageHandler storageHandler;
    private CommandManager commandManager;

    public StorageHandler getStorageHandler() {
        return storageHandler;
    }

    public void log(String message) {
        if (CachedConfig.debugMode()) this.getLogger().info("Debugger: " + message);
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();

        new JoinListener(this);
        new MineListener(this);

        if (getConfig().getBoolean("settings.header", true)) {
            getLogger().info("");
            getLogger().info("");
            getLogger().info("  _                        _  _  _                 _                 _      ");
            getLogger().info(" | |                      | || |(_)               | |               | |     ");
            getLogger().info(" | |      ___ __   __ ___ | || | _  _ __    __ _  | |_  ___    ___  | | ___ ");
            getLogger().info(" | |     / _ \\ \\ / // _ \\| || || || '_ \\  / _` | | __|/ _ \\  / _ \\ | |/ __|");
            getLogger().info(" | |____|  __/ \\ V /|  __/| || || || | | || (_| | | |_| (_) || (_) || |\\__ \\");
            getLogger().info(" |______|\\___|  \\_/  \\___||_||_||_||_| |_| \\__, |  \\__|\\___/  \\___/ |_||___/");
            getLogger().info("                                            __/ |                           ");
            getLogger().info("                                           |___/                            ");
            getLogger().info("");
            getLogger().info("");
        }

        Logger mongoLogger = Logger.getLogger("org.mongodb.driver");
        mongoLogger.setLevel(Level.SEVERE);
        org.apache.logging.log4j.core.Logger logger;
        logger = (org.apache.logging.log4j.core.Logger) LogManager.getRootLogger();
        logger.addFilter(new ConsoleFilter());

        hook("PlaceholderAPI");
        hook("WorldGuard");
        handleReload();
        registerCommands();
    }

    @Override
    public void onDisable() {
        PlayerData.users.forEach(((uuid, playerData) -> {
            getStorageHandler().pushData(uuid);
        }));
    }

    private void setupStorage() {
        String storageType = Objects.requireNonNull(getConfig().getString("settings.storage.type", "SQLITE")).toUpperCase();

        if (Arrays.asList("SQLITE", "MYSQL", "MONGODB").contains(storageType)) {
            getLogger().info("Using '" + storageType + "' for data storage.");
        } else {
            getLogger().info("The storage type '" + storageType + "' is invalid, defaulting to SQLITE.");
            storageType = "SQLITE";
        }

        switch (storageType) {
            case "SQLITE":
                storageHandler = new SQLiteHandler(getDataFolder().getPath());
                break;
            case "MYSQL":
                storageHandler = new MySQLHandler(
                        getConfig().getString("settings.storage.prefix", ""),
                        getConfig().getString("settings.storage.host", "localhost"),
                        getConfig().getInt("settings.storage.port", 3306),
                        getConfig().getString("settings.storage.database", "levellingtools"),
                        getConfig().getString("settings.storage.username", "root"),
                        getConfig().getString("settings.storage.password", "qwerty123"));
                break;
            case "MONGODB":
                storageHandler = new MongoDBHandler(
                        getConfig().getString("settings.storage.prefix", ""),
                        getConfig().getString("settings.storage.host", "localhost"),
                        getConfig().getInt("settings.storage.port", 27017),
                        getConfig().getString("settings.storage.database", "levellingtools"),
                        getConfig().getString("settings.storage.username", ""),
                        getConfig().getString("settings.storage.password", "")
                );
                break;
        }
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }

    private void registerCommands() {
        commandManager = new CommandManager(this);
        getCommand("levellingtools").setExecutor(new CommandExecutor(this));
        if (getCommand("levellingtools").getPlugin() != this) {
            getLogger().warning("/levellingtools command is being handled by plugin other than " + getDescription().getName() + ". You must use /levellingtools:levellingtools instead.");
        }
    }

    public void handleReload() {
        reloadConfig();
        Lang.init(this);
        new CachedConfig(this);
        CachedConfig.setup();
        setupStorage();
    }

    private void hook(final String plugin) {
        final boolean enabled = Bukkit.getPluginManager().isPluginEnabled(plugin);
        if (enabled) {
            getLogger().info(String.format("Hooked into %s.", plugin));

            if (plugin.equalsIgnoreCase("PlaceholderAPI")) {
                new PlaceholderAPIHook(this).register();
            }

            if (plugin.equalsIgnoreCase("WorldGuard")) {
                new WorldGuardHook();
            }
        }
    }
}
