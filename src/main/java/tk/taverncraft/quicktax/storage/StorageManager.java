package tk.taverncraft.quicktax.storage;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import tk.taverncraft.quicktax.Main;
import tk.taverncraft.quicktax.events.SignBreakEvent;
import tk.taverncraft.quicktax.events.SignPlaceEvent;
import tk.taverncraft.quicktax.utils.MessageManager;
import tk.taverncraft.quicktax.utils.ScheduleManager;

public class StorageManager {
    Main main;
    private String storageType;
    StorageHelper storageHelper;
    private boolean isLoading;
    BukkitTask loadPlayersTask;

    public StorageManager(Main main) {
        this.main = main;
    }

    public void initializeValues(boolean isReload) {
        cancelLoadTask();
        storageType = this.main.getConfig().getString("storage-type");
        if (storageType.equalsIgnoreCase("yaml")) {
            storageHelper = new YamlHelper(main);
            loadPlayersTask = new BukkitRunnable() {

                @Override
                public void run() {
                    isLoading = true;
                    loadYamlPlayerInfo();
                    isLoading = false;
                    postPlayerLoadHandler(isReload);
                }

            }.runTaskAsynchronously(main);
        } else if (storageType.equalsIgnoreCase("mysql")) {
            storageHelper = new SqlHelper(main);
            loadPlayersTask = new BukkitRunnable() {

                @Override
                public void run() {
                    isLoading = true;
                    loadSqlPlayerInfo();
                    isLoading = false;
                    postPlayerLoadHandler(isReload);
                }

            }.runTaskAsynchronously(main);
        } else {
            storageHelper = new NoneHelper(main);
            this.createScheduleConfig(Bukkit.getConsoleSender());
        }
    }

    /**
     * Runs schedule checks and updates leaderboard after player data has been fully loaded.
     */
    private void postPlayerLoadHandler(boolean isReload) {
        Bukkit.getScheduler().runTask(main, () -> {
            try {
                this.createScheduleConfig(Bukkit.getConsoleSender());
            } catch (NullPointerException e) {
            }
        });
        if (main.getConfig().getBoolean("update-on-start", false) && !isReload) {
            main.getStatsManager().scheduleLeaderboardUpdate(main.getConfig().getInt("update-interval", 3600), 3);
        } else {
            main.getStatsManager().scheduleLeaderboardUpdate(main.getConfig().getInt("update-interval", 3600), main.getConfig().getInt("update-interval", 3600));
        }
    }

    /**
     * Loads player info from yml file into cache.
     */
    private void loadYamlPlayerInfo() {
        File folder = new File(this.main.getDataFolder() + "/playerData");
        if (!folder.exists()) {
            folder.mkdir();
        }

        HashMap<UUID, Double> tempPlayerTotalTaxPaidCache = new HashMap<>();
        HashMap<UUID, Double> tempPlayerLatestTaxPaidCache = new HashMap<>();

        File[] listOfFiles = Objects.requireNonNull(folder.listFiles(), "playerData folder appears to be missing or empty!");
        for (File file : listOfFiles) {
            String fileName = file.getName();
            if (file.getName().endsWith(".yml")) {
                YamlConfiguration playerConfig = YamlConfiguration.loadConfiguration(file);
                UUID uuid = UUID.fromString(fileName.substring(0, fileName.length() - 4));
                double latestTaxPaid = playerConfig.getDouble("latest-tax-paid");
                double totalTaxPaid = playerConfig.getDouble("total-tax-paid");
                tempPlayerLatestTaxPaidCache.put(uuid,
                    new BigDecimal(latestTaxPaid).setScale(2, RoundingMode.HALF_UP).doubleValue());
                tempPlayerTotalTaxPaidCache.put(uuid, new BigDecimal(totalTaxPaid).setScale(2,
                    RoundingMode.HALF_UP).doubleValue());
            }
        }
        main.getStatsManager().setTotalTaxPaidCache(tempPlayerTotalTaxPaidCache);
        main.getStatsManager().setLatestTaxPaidCache(tempPlayerLatestTaxPaidCache);
    }

    /**
     * Loads player info from sql into cache.
     */
    private void loadSqlPlayerInfo() {
        storageHelper.getFromDatabase();
    }

    /**
     * Creates config file.
     */
    public void createConfig() {
        File configFile = new File(main.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            configFile.getParentFile().mkdirs();
            main.saveResource("config.yml", false);
        }

        FileConfiguration config = new YamlConfiguration();
        try {
            config.load(configFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }

        this.main.setConfig(config);
    }

    /**
     * Creates schedule file.
     */
    public void createScheduleConfig(CommandSender sender) {
        File scheduleFile = new File(main.getDataFolder(), "schedule.yml");
        if (!scheduleFile.exists()) {
            scheduleFile.getParentFile().mkdirs();
            main.saveResource("schedule.yml", false);
        }

        FileConfiguration scheduleConfig = new YamlConfiguration();
        try {
            scheduleConfig.load(scheduleFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }

        this.main.setScheduleConfig(scheduleConfig);
        ScheduleManager.runScheduleCheck(this.main, scheduleConfig, true, sender);
    }

    /**
     * Creates signs config file.
     */
    public void createSignsConfig() {
        File signsFile = new File(main.getDataFolder(), "signs.yml");
        if (!signsFile.exists()) {
            signsFile.getParentFile().mkdirs();
            main.saveResource("signs.yml", false);
        }

        FileConfiguration signsConfig = new YamlConfiguration();
        try {
            signsConfig.load(signsFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }

        this.main.setSignsConfig(signsConfig);
    }

    /**
     * Creates message file.
     */
    public void createMessageFile() {
        String langFileName = main.getConfig().getString("lang-file");

        // default language
        if (langFileName == null) {
            langFileName = "en.yml";
        }

        File langFile = new File(main.getDataFolder() + "/lang", langFileName);
        FileConfiguration lang = new YamlConfiguration();
        if (!langFile.exists()) {
            langFile.getParentFile().mkdirs();
            try {
                Path dest = Paths.get(main.getDataFolder() + "/lang/" + langFileName);
                Files.copy(main.getResource(langFileName), dest);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            lang.load(langFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }

        MessageManager.setMessages(lang);
    }

    /**
     * Get player config and create if not exist.
     *
     * @param uuid uuid of player to get config for
     */
    public FileConfiguration getPlayerConfig(UUID uuid) {
        String playerFileName = uuid.toString();
        File playerFile = new File(main.getDataFolder() + "/playerData", playerFileName + ".yml");
        FileConfiguration playerConfig = new YamlConfiguration();
        if (!playerFile.exists()) {
            playerFile.getParentFile().mkdirs();
            playerConfig.set("latest-tax-paid", 0);
            playerConfig.set("total-tax-paid", 0);
            try {
                playerConfig.save(playerFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            playerConfig.load(playerFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
        return playerConfig;
    }

    public StorageHelper getStorageHelper() {
        return this.storageHelper;
    }

    public void cancelLoadTask() {
        if (loadPlayersTask != null) {
            loadPlayersTask.cancel();
            loadPlayersTask = null;
        }

        SqlHelper.query = "";
    }

    public boolean isLoading() {
        return this.isLoading;
    }
}
