package tk.taverncraft.quicktax.utils;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import tk.taverncraft.quicktax.Main;
import tk.taverncraft.quicktax.leaderboard.SignHelper;


/**
 * StatsManager handles all logic for updating of player/server stats.
 */
public class StatsManager {
    Main main;
    ValidationManager validationManager;
    private ConcurrentHashMap<UUID, Double> playerTotalTaxPaidCache;
    private ConcurrentHashMap<UUID, Double> playerLatestTaxPaidCache;
    private ArrayList<UUID> playerTotalTaxPaidKeys;
    private ArrayList<Double> playerTotalTaxPaidValues;
    private boolean isUpdating;
    BukkitTask scheduledTask;

    /**
     * Constructor for StatsManager.
     */
    public StatsManager(Main main) {
        this.main = main;
        this.validationManager = new ValidationManager(main);
        initializeValues();
    }

    /**
     * Initializes all values to default.
     */
    public void initializeValues() throws NullPointerException {
        cancelScheduledTask();
        playerTotalTaxPaidCache = new ConcurrentHashMap<>();
        playerLatestTaxPaidCache = new ConcurrentHashMap<>();
        playerTotalTaxPaidKeys = new ArrayList<>();
        playerTotalTaxPaidValues = new ArrayList<>();
        isUpdating = false;
    }

    /**
     * Scheduled entry point for updating leaderboard.
     *
     * @param frequency frequency of update
     * @param delay delay to first update
     */
    public void scheduleLeaderboardUpdate(int frequency, int delay) {
        if (!validationManager.doStoreData(null)) {
            return;
        }
        if (frequency == -1 && delay == 0) {
            scheduledTask = new BukkitRunnable() {

                @Override
                public void run() {
                    if (isUpdating) {
                        main.getLogger().info("Scheduled leaderboard update could not be " +
                                "carried out because an existing update is in progress.");
                        return;
                    }
                    isUpdating = true;
                    updateLeaderboard(Bukkit.getConsoleSender());
                    isUpdating = false;
                }

            }.runTaskAsynchronously(main);
            return;
        }
        long interval = frequency * 20L;
        long delayTicks = delay * 20L;
        scheduledTask = new BukkitRunnable() {

            @Override
            public void run() {
                if (isUpdating) {
                    main.getLogger().info("Scheduled leaderboard update could not be " +
                            "carried out because an existing update is in progress.");
                    return;
                }
                isUpdating = true;
                updateLeaderboard(Bukkit.getConsoleSender());
                isUpdating = false;
            }

        }.runTaskTimerAsynchronously(main, delayTicks, interval);
    }

    /**
     * Manual entry point for updating leaderboard.
     *
     * @param sender user executing the update
     */
    public void manualUpdateLeaderboard(CommandSender sender) {
        if (!validationManager.doStoreData(null)) {
            return;
        }
        new BukkitRunnable() {

            @Override
            public void run() {
                isUpdating = true;
                updateLeaderboard(sender);
                isUpdating = false;
            }

        }.runTaskAsynchronously(main);
    }

    /**
     * Updates the entire leaderboard. May take a while to run if player-base is large.
     *
     * @param sender user executing the update
     */
    private void updateLeaderboard(CommandSender sender) {
        MessageManager.sendMessage(sender, "update-started");
        HashMap<UUID, Double> tempSortedCache = sortByValue(playerTotalTaxPaidCache);
        updateLeaderboardArrayList(tempSortedCache);
        MessageManager.setUpLeaderboard(tempSortedCache);
        MessageManager.sendMessage(sender, "update-complete");
        Bukkit.getScheduler().runTask(main, () -> {
            try {
                new SignHelper(main).updateSigns();
            } catch (NullPointerException e) {
            }
        });
    }

    /**
     * Sorts players by total tax paid.
     *
     * @param hm hashmap of player total tax paid to sort
     */
    public HashMap<UUID, Double> sortByValue(ConcurrentHashMap<UUID, Double> hm) {
        List<Map.Entry<UUID, Double> > list = new LinkedList<>(hm.entrySet());

        list.sort((o1, o2) -> (o2.getValue()).compareTo(o1.getValue()));

        HashMap<UUID, Double> temp = new LinkedHashMap<>();
        for (Map.Entry<UUID, Double> aa : list) {
            temp.put(aa.getKey(), aa.getValue());
        }
        return temp;
    }

    /**
     * Updates leaderboard array list for easy papi access.
     */
    public void updateLeaderboardArrayList(HashMap<UUID, Double> tempSortedCache) {
        Set<UUID> keySet = tempSortedCache.keySet();
        this.playerTotalTaxPaidKeys = new ArrayList<>(keySet);
        Collection<Double> values = tempSortedCache.values();
        this.playerTotalTaxPaidValues = new ArrayList<>(values);
    }

    /**
     * Updates the stats of the server.
     *
     * @param amount the amount of tax to update
     * @param isAdd boolean to indicate if amount is to be added or subtracted
     */
    public void updateServerStats(double amount, boolean isAdd) {
        FileConfiguration serverStatsConfig = this.main.getServerStatsConfig();
        File serverStatsFile = new File(this.main.getDataFolder(), "serverstats.yml");
        if (isAdd) {
            serverStatsConfig.set("total-tax-collected", serverStatsConfig.getInt("total-tax-collected") + amount);
            serverStatsConfig.set("total-tax-balance", serverStatsConfig.getInt("total-tax-balance") + amount);
        } else {
            serverStatsConfig.set("total-tax-balance", serverStatsConfig.getInt("total-tax-balance") - amount);
        }

        try {
            serverStatsConfig.save(serverStatsFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Adds to the server balance as an admin.
     *
     * @param amount the amount of balance to add
     */
    public void addServerBalanceAsAdmin(double amount) {
        FileConfiguration serverStatsConfig = this.main.getServerStatsConfig();
        File serverStatsFile = new File(this.main.getDataFolder(), "serverstats.yml");
        serverStatsConfig.set("total-tax-balance", serverStatsConfig.getInt("total-tax-balance") + amount);
        try {
            serverStatsConfig.save(serverStatsFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Takes from the server balance as an admin.
     *
     * @param amount the amount of balance to take
     */
    public void takeServerBalanceAsAdmin(double amount) {
        FileConfiguration serverStatsConfig = this.main.getServerStatsConfig();
        File serverStatsFile = new File(this.main.getDataFolder(), "serverstats.yml");
        serverStatsConfig.set("total-tax-balance", serverStatsConfig.getInt("total-tax-balance") - amount);
        try {
            serverStatsConfig.save(serverStatsFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sets the server balance as an admin.
     *
     * @param amount the amount of balance to set
     */
    public void setServerBalanceAsAdmin(double amount) {
        FileConfiguration serverStatsConfig = this.main.getServerStatsConfig();
        File serverStatsFile = new File(this.main.getDataFolder(), "serverstats.yml");
        serverStatsConfig.set("total-tax-balance", amount);
        try {
            serverStatsConfig.save(serverStatsFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets the stats of a player.
     *
     * @param player the player to get stats for
     */
    public String[] getPlayerStats(OfflinePlayer player) {
        UUID uuid = player.getUniqueId();
        Double latestTaxPaid = playerLatestTaxPaidCache.get(uuid);
        Double totalTaxPaid = playerTotalTaxPaidCache.get(uuid);
        if (latestTaxPaid == null) {
            latestTaxPaid = 0.0;
        }

        if (totalTaxPaid == null) {
            totalTaxPaid = 0.0;
        }

        return new String[]{new BigDecimal(latestTaxPaid).setScale(2, RoundingMode.HALF_UP).toPlainString(),
            new BigDecimal(totalTaxPaid).setScale(2, RoundingMode.HALF_UP).toPlainString()};
    }

    public double getPlayerTotalTaxPaidCache(UUID uuid) {
        return playerTotalTaxPaidCache.get(uuid);
    }

    /**
     * Gets the stats of the server.
     */
    public String[] getServerStats() {
        FileConfiguration playerConfig = this.main.getServerStatsConfig();
        String totalTaxCollected = new BigDecimal(playerConfig.getString("total-tax-collected", "0")).setScale(2, RoundingMode.HALF_UP).toPlainString();
        String totalTaxBalance = new BigDecimal(playerConfig.getString("total-tax-balance", "0")).setScale(2, RoundingMode.HALF_UP).toPlainString();

        return new String[]{totalTaxCollected, totalTaxBalance};
    }

    public String getTopPlayerName(int index) {
        UUID uuid = this.playerTotalTaxPaidKeys.get(index);
        if (uuid != null) {
            OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
            return player.getName();
        } else {
            return "None";
        }
    }

    public String getTopPlayerTaxPaid(int index) {
        Double value = this.playerTotalTaxPaidValues.get(index);
        if (value != null) {
            return String.format("%.02f", value);
        } else {
            return "None";
        }
    }

    public void cancelScheduledTask() {
        if (scheduledTask != null) {
            scheduledTask.cancel();
            scheduledTask = null;
        }
        isUpdating = false;
    }

    public void setLatestTaxPaidCache(HashMap<UUID, Double> playerLatestTaxPaidCache) {
        this.playerLatestTaxPaidCache = new ConcurrentHashMap<>(playerLatestTaxPaidCache);
    }

    public void setTotalTaxPaidCache(HashMap<UUID, Double> playerTotalTaxPaidCache) {
        this.playerTotalTaxPaidCache = new ConcurrentHashMap<>(playerTotalTaxPaidCache);
    }

    public void saveToCache(UUID uuid, double taxPaid) {
        Double taxPaidSoFar = playerTotalTaxPaidCache.get(uuid);
        if (taxPaidSoFar == null) {
            taxPaidSoFar = (double) 0;
        }
        playerLatestTaxPaidCache.put(uuid, taxPaid);
        playerTotalTaxPaidCache.put(uuid, taxPaidSoFar + taxPaid);
    }

    public boolean isUpdating() {
        return this.isUpdating;
    }
}
