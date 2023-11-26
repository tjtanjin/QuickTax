package tk.taverncraft.quicktax;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import tk.taverncraft.quicktax.commands.CommandParser;
import tk.taverncraft.quicktax.commands.CommandTabCompleter;
import tk.taverncraft.quicktax.events.DependencyLoadEvent;
import tk.taverncraft.quicktax.events.SignBreakEvent;
import tk.taverncraft.quicktax.events.SignPlaceEvent;
import tk.taverncraft.quicktax.storage.StorageManager;
import tk.taverncraft.quicktax.utils.*;

/**
 * The plugin class.
 */
public class Main extends JavaPlugin {

    private static final Logger log = Logger.getLogger("Minecraft");

    // Vault
    private static Economy econ = null;
    private static Permission perms = null;

    // Config
    private FileConfiguration config;
    private FileConfiguration scheduleConfig;
    private FileConfiguration signsConfig;

    private StatsManager statsManager;
    private StorageManager storageManager;

    @Override
    public void onDisable() {
        log.info(String.format("[%s] Disabled Version %s", getDescription().getName(), getDescription().getVersion()));
    }

    @Override
    public void onEnable() {
        new UpdateChecker(this, 96495).getVersion(version -> {
            if (this.getDescription().getVersion().equalsIgnoreCase(version)) {
                getLogger().info("You are using the latest version of QuickTax!");
            } else {
                getLogger().info("A new version of QuickTax is now available on spigot!");
            }
        });

        // vault setup
        if (!setupEconomy() ) {
            log.severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        setupPermissions();

        this.storageManager = new StorageManager(this);

        // config setup
        this.storageManager.createConfig();
        this.storageManager.createMessageFile();
        this.storageManager.createSignsConfig();

        this.storageManager.initializeValues(false);
        this.statsManager = new StatsManager(this);

        this.getCommand("quicktax").setTabCompleter(new CommandTabCompleter());
        this.getCommand("quicktax").setExecutor(new CommandParser(this));

        int pluginId = 	12958;
        Metrics metrics = new Metrics(this, pluginId);

        // Optional: Add custom charts
        metrics.addCustomChart(new Metrics.SimplePie("chart_id", () -> "My value"));

        if (Bukkit.getVersion().contains("1.11") || Bukkit.getVersion().contains("1.12")) {
            Bukkit.getScheduler().runTaskLater(this, this::loadDependencies, 1);
        } else {
            this.getServer().getPluginManager().registerEvents(new DependencyLoadEvent(this), this);
        }

        getServer().getPluginManager().registerEvents(new SignPlaceEvent(this), this);
        getServer().getPluginManager().registerEvents(new SignBreakEvent(this), this);
    }

    /**
     * Get server stats config.
     */
    public FileConfiguration getServerStatsConfig() {
        File serverStatsFile = new File(getDataFolder(), "serverstats.yml");
        FileConfiguration serverStatsConfig = new YamlConfiguration();
        if (!serverStatsFile.exists()) {
            serverStatsFile.getParentFile().mkdirs();
            serverStatsConfig.set("total-tax-collected", 0);
            serverStatsConfig.set("total-tax-balance", 0);
            try {
                serverStatsConfig.save(serverStatsFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            serverStatsConfig.load(serverStatsFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
        return serverStatsConfig;
    }

    /**
     * Loads dependencies.
     */
    public void loadDependencies() {
        checkGriefPrevention();
        checkPlaceholderAPI();
    }


    /**
     * Checks if GriefPrevention is present.
     */
    private void checkGriefPrevention() {
        if (Bukkit.getPluginManager().getPlugin("GriefPrevention") != null) {
            getLogger().info(String.format("[%s] - GriefPrevention found, integrated with plugin!", getDescription().getName()));
        }
    }

    /**
     * Checks if PAPI is present.
     */
    private void checkPlaceholderAPI() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            getLogger().info(String.format("[%s] - PlaceholderAPI found, integrated with plugin!", getDescription().getName()));
            new PapiManager(this).register();
        }
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        perms = rsp.getProvider();
        return perms != null;
    }

    public static Economy getEconomy() {
        return econ;
    }

    public static Permission getPermissions() {
        return perms;
    }

    public FileConfiguration getConfig() {
        return this.config;
    }

    public FileConfiguration getScheduleConfig() {
        return this.scheduleConfig;
    }

    public FileConfiguration getSignsConfig() {
        return this.signsConfig;
    }

    public void setSignsConfig(FileConfiguration signsConfig) {
        this.signsConfig = signsConfig;
    }

    public void setConfig(FileConfiguration config) {
        this.config = config;
    }

    public void setScheduleConfig(FileConfiguration scheduleConfig) {
        this.scheduleConfig = scheduleConfig;
    }

    public StatsManager getStatsManager() {
        return this.statsManager;
    }

    public StorageManager getStorageManager() {
        return this.storageManager;
    }
}