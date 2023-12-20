package tk.taverncraft.quicktax.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.PlayerData;
import me.ryanhamshire.GriefPrevention.GriefPrevention;

import org.bukkit.scheduler.BukkitRunnable;
import tk.taverncraft.quicktax.Main;

/**
 * TaxManager handles all logic and action for collecting tax from players.
 */
public class TaxManager {
    Main main;
    ValidationManager validationManager;
    double totalTaxCollected;
    public static boolean isCollecting;
    public static Runnable task;

    // permission nodes for tax exemptions
    private final String exemptFullPerm = "quicktax.exempt.*";
    private final String exemptCollectNamePerm = "quicktax.exempt.collectname";
    private final String exemptCollectAllPerm = "quicktax.exempt.collectall";
    private final String exemptCollectBalPerm = "quicktax.exempt.collectbal";
    private final String exemptCollectRankPerm = "quicktax.exempt.collectrank";
    private final String exemptCollectActivityPerm = "quicktax.exempt.collectactivity";

    /**
     * Constructor for TaxManager.
     */
    public TaxManager(Main main) {
        this.main = main;
        this.validationManager = new ValidationManager(main);
    }

    /**
     * Logic for collecting tax from an individual.
     *
     * @param sender the executor of the command
     * @param name name of player to collect tax from
     * @param amount to collect from individual
     */
    public void collectIndividual(CommandSender sender, String name, String amount) throws NullPointerException {

        updateType<OfflinePlayer, Double, Double, Boolean> func;
        boolean taxClaims = this.main.getConfig().getBoolean("tax-claims");
        if (taxClaims) {
            func = this::updatePlayerWithClaims;
        } else {
            func = this::updatePlayerNoClaims;
        }

        OfflinePlayer player = this.main.getServer().getOfflinePlayer(name);
        if (isTaxExempt(player, exemptCollectNamePerm)) {
            MessageManager.sendMessage(sender, "tax-exempt",
                new String[]{"%player%"},
                new String[]{name});
            return;
        }

        if (func.updatePlayer(player, Double.parseDouble(amount), 0.0, false)) {
            MessageManager.sendMessage(sender, "tax-collect-success-individual",
                    new String[]{"%player%"},
                    new String[]{name});
        } else {
            MessageManager.sendMessage(sender, "tax-collect-fail-individual",
                    new String[]{"%player%"},
                    new String[]{name});
        }

        if (validationManager.doStoreData(null)) {
            this.main.getStatsManager().updateServerStats(this.totalTaxCollected, true);
        }

        if (this.main.getConfig().getString("storage-type", "none").equalsIgnoreCase("mysql")) {
            main.getStorageManager().getStorageHelper().insertIntoDatabase();
        }
    }

    /**
     * Logic for collecting tax from all players.
     *
     * @param sender the executor of the command
     */
    public void collectAll(CommandSender sender) throws NullPointerException {
        double balTaxAmount = this.main.getConfig().getDouble("all.bal-amount", 0);
        double claimsTaxAmount = this.main.getConfig().getDouble("all.claims-ratio", 0);
        boolean usePercentage = this.main.getConfig().getBoolean("all.use-percentage", false);

        updateType<OfflinePlayer, Double, Double, Boolean> func;
        boolean taxClaims = this.main.getConfig().getBoolean("tax-claims");
        if (taxClaims) {
            func = this::updatePlayerWithClaims;
        } else {
            func = this::updatePlayerNoClaims;
        }

        Arrays.stream(this.main.getServer().getOfflinePlayers())
                .forEach(offlinePlayer -> {
                    if (isTaxExempt(offlinePlayer, exemptCollectAllPerm)) {
                        return;
                    }
                    func.updatePlayer(offlinePlayer, balTaxAmount, claimsTaxAmount, usePercentage);
                });
        MessageManager.sendMessage(sender, "tax-collect-success-all");
        if (validationManager.doStoreData(null)) {
            this.main.getStatsManager().updateServerStats(this.totalTaxCollected, true);
        }

        if (this.main.getConfig().getString("storage-type", "none").equalsIgnoreCase("mysql")) {
            main.getStorageManager().getStorageHelper().insertIntoDatabase();
        }
    }

    /**
     * Logic for collecting tax from players by rank.
     *
     * @param sender the executor of the command
     */
    public void collectRank(CommandSender sender) throws NullPointerException {
        boolean usePercentage = this.main.getConfig().getBoolean("rank-bracket.use-percentage", false);
        ConfigurationSection ranks = this.main.getConfig().getConfigurationSection("rank-bracket.ranks");

        updateType<OfflinePlayer, Double, Double, Boolean> func;
        boolean taxClaims = this.main.getConfig().getBoolean("tax-claims");
        if (taxClaims) {
            func = this::updatePlayerWithClaims;
        } else {
            func = this::updatePlayerNoClaims;
        }

        Arrays.stream(this.main.getServer().getOfflinePlayers())
                .forEach(offlinePlayer -> {
                    if (isTaxExempt(offlinePlayer, exemptCollectRankPerm)) {
                        return;
                    }
                    for (String rank : ranks.getKeys(true)) {
                        String[] playerGroups = Main.getPermissions().getPlayerGroups(Bukkit.getWorlds().get(0).getName(), offlinePlayer);
                        if (Arrays.stream(playerGroups).anyMatch(rank::equalsIgnoreCase)) {
                            double balTaxAmount = this.main.getConfig().getDouble("rank-bracket.ranks." + rank + ".bal", 0);
                            double claimsTaxAmount = this.main.getConfig().getDouble("rank-bracket.ranks." + rank + ".claims-ratio", 0);
                            boolean finalUsePercentage = this.main.getConfig().getBoolean("rank-bracket.ranks." + rank + ".use-percentage", usePercentage);
                            func.updatePlayer(offlinePlayer, balTaxAmount, claimsTaxAmount, finalUsePercentage);
                            break;
                        }
                    }
                });

        MessageManager.sendMessage(sender, "tax-collect-success-rank");
        if (validationManager.doStoreData(null)) {
            this.main.getStatsManager().updateServerStats(this.totalTaxCollected, true);
        }

        if (this.main.getConfig().getString("storage-type", "none").equalsIgnoreCase("mysql")) {
            main.getStorageManager().getStorageHelper().insertIntoDatabase();
        }
    }

    /**
     * Logic for collecting tax from players by bal.
     *
     * @param sender the executor of the command
     */
    public void collectBal(CommandSender sender) throws NullPointerException {
        boolean usePercentage = this.main.getConfig().getBoolean("bal-bracket.use-percentage", false);
        ConfigurationSection bals = this.main.getConfig().getConfigurationSection("bal-bracket.amount");
        if (bals == null) {
            this.main.getLogger().info("Cannot find balance bracket, is the config correct?");
            return;
        }
        Set<String> balList = bals.getKeys(false);
        List<Integer> intList = new ArrayList<>();
        for(String s : balList){
            intList.add(Integer.valueOf(s));
        }
        Collections.sort(intList);
        Collections.reverse(intList);

        updateType<OfflinePlayer, Double, Double, Boolean> func;
        boolean taxClaims = this.main.getConfig().getBoolean("tax-claims");
        if (taxClaims) {
            func = this::updatePlayerWithClaims;
        } else {
            func = this::updatePlayerNoClaims;
        }

        Arrays.stream(this.main.getServer().getOfflinePlayers())
                .forEach(offlinePlayer -> {
                    if (isTaxExempt(offlinePlayer, exemptCollectBalPerm)) {
                        return;
                    }
                    for (int bal : intList) {
                        if (offlinePlayer.getName() == null) {
                            continue;
                        }
                        if (BigDecimal.valueOf(Main.getEconomy().getBalance(offlinePlayer)).compareTo(BigDecimal.valueOf((double) bal)) >= 0) {
                            double balTaxAmount = this.main.getConfig().getDouble("bal-bracket.amount." + bal + ".bal", 0);
                            double claimsTaxAmount = this.main.getConfig().getDouble("bal-bracket.amount." + bal + ".claims-ratio", 0);
                            boolean finalUsePercentage = this.main.getConfig().getBoolean("bal-bracket.amount." + bal + ".use-percentage", usePercentage);
                            func.updatePlayer(offlinePlayer, balTaxAmount, claimsTaxAmount, finalUsePercentage);
                            break;
                        }
                    }
                });
        MessageManager.sendMessage(sender, "tax-collect-success-bal");
        if (validationManager.doStoreData(null)) {
            this.main.getStatsManager().updateServerStats(this.totalTaxCollected, true);
        }

        if (this.main.getConfig().getString("storage-type", "none").equalsIgnoreCase("mysql")) {
            main.getStorageManager().getStorageHelper().insertIntoDatabase();
        }
    }

    /**
     * Logic for collecting tax from players by activity.
     *
     * @param sender the executor of the command
     */
    public void collectActivity(CommandSender sender) throws NullPointerException {
        boolean usePercentage = this.main.getConfig().getBoolean("activity-bracket.use-percentage", false);
        ConfigurationSection lastSeenActivities = this.main.getConfig().getConfigurationSection("activity-bracket.last-seen");
        if (lastSeenActivities == null) {
            this.main.getLogger().info("Cannot find activity bracket, is the config correct?");
            return;
        }
        Set<String> lastSeenActivitiesSet = lastSeenActivities.getKeys(false);
        List<Long> longList = new ArrayList<>();
        for(String s : lastSeenActivitiesSet){
            longList.add(Long.valueOf(s));
        }
        Collections.sort(longList);
        Collections.reverse(longList);

        updateType<OfflinePlayer, Double, Double, Boolean> func;
        boolean taxClaims = this.main.getConfig().getBoolean("tax-claims");
        if (taxClaims) {
            func = this::updatePlayerWithClaims;
        } else {
            func = this::updatePlayerNoClaims;
        }

        Arrays.stream(this.main.getServer().getOfflinePlayers())
            .forEach(offlinePlayer -> {
                if (isTaxExempt(offlinePlayer, exemptCollectActivityPerm)) {
                    return;
                }
                for (long lastSeen : longList) {
                    if (offlinePlayer.getName() == null) {
                        continue;
                    }
                    long lastPlayed = offlinePlayer.getLastPlayed();
                    long currentTime = System.currentTimeMillis();
                    long elapsedTime = (currentTime - lastPlayed) / 1000;
                    if (elapsedTime >= lastSeen) {
                        double balTaxAmount = this.main.getConfig().getDouble("activity-bracket.last-seen." + lastSeen + ".bal", 0);
                        double claimsTaxAmount = this.main.getConfig().getDouble("activity-bracket.last-seen." + lastSeen + ".claims-ratio", 0);
                        boolean finalUsePercentage = this.main.getConfig().getBoolean("activity-bracket.last-seen." + lastSeen + ".use-percentage", usePercentage);
                        func.updatePlayer(offlinePlayer, balTaxAmount, claimsTaxAmount, finalUsePercentage);
                        break;
                    }
                }
            });
        MessageManager.sendMessage(sender, "tax-collect-success-activity");
        if (validationManager.doStoreData(null)) {
            this.main.getStatsManager().updateServerStats(this.totalTaxCollected, true);
        }

        if (this.main.getConfig().getString("storage-type", "none").equalsIgnoreCase("mysql")) {
            main.getStorageManager().getStorageHelper().insertIntoDatabase();
        }
    }

    /**
     * Gets amount to subtract from player without claims.
     *
     * @param player player being taxed
     * @param usePercentage whether to deduct by percentage or absolute value
     * @param taxAmount amount of tax to apply
     * @param playerBal current balance of player
     */
    public double getSubtractAmountNoClaims(OfflinePlayer player, boolean usePercentage, double taxAmount, double playerBal) {
        double subtractAmount;
        if (usePercentage) {
            // keep tax percentage between 0 and 1
            taxAmount = Math.min(1, Math.max(0, taxAmount));
            if (playerBal < 0) {
                return 0;
            }
            subtractAmount = playerBal * taxAmount;
        } else {
            subtractAmount = taxAmount;
        }

        // handles case when player has not enough money but subtract amount is positive
        if (subtractAmount > 0 && playerBal < subtractAmount) {
            int debtMode = main.getConfig().getInt("debt-mode", 0);
            if (debtMode == 0) {
                subtractAmount = 0;
            } else if (debtMode == 1) {
                subtractAmount = playerBal;
            }
            penalizePlayer(player, false);
        }

        subtractAmount = new BigDecimal(subtractAmount).setScale(2, RoundingMode.HALF_UP).doubleValue(); //rounding
        return subtractAmount;
    }

    /**
     * Gets amount to subtract from player with claims.
     *
     * @param player player being taxed
     * @param usePercentage whether to deduct by percentage or absolute value
     * @param balTaxAmount amount of tax to apply
     * @param playerBal current balance of player
     * @param claimsTaxAmount amount of bal to tax per claimblock
     * @param playerClaims number of claimblocks player has
     */
    public double getSubtractAmountWithClaims(OfflinePlayer player, boolean usePercentage, double balTaxAmount, double playerBal, double claimsTaxAmount, double playerClaims) {
        double subtractAmount;
        if (usePercentage) {
            // keep tax percentage between 0 and 1
            balTaxAmount = Math.min(1, Math.max(0, balTaxAmount));
            if (playerBal < 0) {
                return 0;
            }
            subtractAmount = playerBal * balTaxAmount;
        } else {
            subtractAmount = balTaxAmount;
        }

        subtractAmount += claimsTaxAmount * playerClaims;

        // handles case when player has not enough money but subtract amount is positive
        if (subtractAmount > 0 && playerBal < subtractAmount) {
            int debtMode = main.getConfig().getInt("debt-mode", 0);
            if (debtMode == 0) {
                subtractAmount = 0;
            } else if (debtMode == 1) {
                subtractAmount = playerBal;
            }
            penalizePlayer(player, true);
        }

        subtractAmount = new BigDecimal(subtractAmount).setScale(2, RoundingMode.HALF_UP).doubleValue(); //rounding
        return subtractAmount;
    }

    /**
     * Logic for updating player balance with claims.
     *
     * @param player player to update
     * @param balTaxAmount amount of bal to tax
     * @param claimsTaxAmount additional amount of bal to tax depending on claimblocks
     * @param usePercentage whether to deduct by percentage or absolute value
     */
    public boolean updatePlayerWithClaims(OfflinePlayer player, double balTaxAmount, double claimsTaxAmount, boolean usePercentage) throws NullPointerException {
        if (player.getName() == null) {
            Bukkit.getLogger().info("Unable to find vault account for: " + player.getUniqueId());
            return false;
        }
        double playerBal = Main.getEconomy().getBalance(player);
        double subtractAmount;

        try {
            PlayerData playerData = GriefPrevention.instance.dataStore.getPlayerData(player.getUniqueId());
            double playerClaims;
            if (main.getConfig().getBoolean("active-claims-only", false)) {
                playerClaims = playerData.getAccruedClaimBlocks() - playerData.getRemainingClaimBlocks();
            } else {
                playerClaims = playerData.getAccruedClaimBlocks();
            }
            subtractAmount = getSubtractAmountWithClaims(player, usePercentage, balTaxAmount, playerBal, claimsTaxAmount, playerClaims);
        } catch (NoClassDefFoundError e) {
            Bukkit.getLogger().warning(e.getMessage());
            subtractAmount = getSubtractAmountNoClaims(player, usePercentage, balTaxAmount, playerBal);
        }

        // early break if nothing to subtract
        if (subtractAmount == 0) {
            return false;
        }

        if (player.isOnline()) {
            if (main.getConfig().getBoolean("enable-sound")) {
                try {
                    player.getPlayer().getWorld().playSound(player.getPlayer().getLocation(),
                        Sound.valueOf(main.getConfig().getString("play-sound")), 1, 1);
                } catch(Exception ignored) {
                }
            }
            MessageManager.sendMessage(player.getPlayer(), "player-pay-tax-success",
                    new String[]{"%player%", "%amount%"},
                    new String[]{player.getName(), String.valueOf(subtractAmount)});
        }

        // needed in rare situations where async access to economy plugin is disallowed
        try {
            if (subtractAmount > 0) {
                Main.getEconomy().withdrawPlayer(player, subtractAmount);
            } else {
                Main.getEconomy().depositPlayer(player, Math.abs(subtractAmount));
            }
        } catch (IllegalStateException e) {
            double finalSubtractAmount = subtractAmount;
            if (subtractAmount > 0) {
                Bukkit.getScheduler().runTask(main, () -> Main.getEconomy().withdrawPlayer(player, finalSubtractAmount));
            } else {
                Bukkit.getScheduler().runTask(main, () -> Main.getEconomy().depositPlayer(player, Math.abs(finalSubtractAmount)));
            }
        }

        if (validationManager.doStoreData(null)) {
            this.totalTaxCollected += subtractAmount;
            this.main.getStatsManager().saveToCache(player.getUniqueId(), subtractAmount);
            this.main.getStorageManager().getStorageHelper().saveToStorage(player.getUniqueId(), subtractAmount);
        }
        return true;
    }

    /**
     * Logic for updating player balance excluding claims.
     *
     * @param player player to update
     * @param balTaxAmount amount of bal to tax
     * @param claimsTaxAmount additional amount of bal to tax depending on claimblocks
     * @param usePercentage whether to deduct by percentage or absolute value
     */
    public boolean updatePlayerNoClaims(OfflinePlayer player, double balTaxAmount, double claimsTaxAmount, boolean usePercentage) throws NullPointerException {
        if (player.getName() == null) {
            Bukkit.getLogger().info("Unable to find vault account for: " + player.getUniqueId());
            return false;
        }
        double playerBal = Main.getEconomy().getBalance(player);
        double subtractAmount;

        subtractAmount = getSubtractAmountNoClaims(player, usePercentage, balTaxAmount, playerBal);
        // early break if nothing to subtract
        if (subtractAmount == 0) {
            return false;
        }

        if (player.isOnline()) {
            if (main.getConfig().getBoolean("enable-sound")) {
                try {
                    player.getPlayer().getWorld().playSound(player.getPlayer().getLocation(),
                        Sound.valueOf(main.getConfig().getString("play-sound")), 1, 1);
                } catch(Exception ignored) {
                }
            }
            MessageManager.sendMessage(player.getPlayer(), "player-pay-tax-success",
                    new String[]{"%player%", "%amount%"},
                    new String[]{player.getName(), String.valueOf(subtractAmount)});
        }

        // needed in rare situations where async access to economy plugin is disallowed
        try {
            if (subtractAmount > 0) {
                Main.getEconomy().withdrawPlayer(player, subtractAmount);
            } else {
                Main.getEconomy().depositPlayer(player, Math.abs(subtractAmount));
            }
        } catch (IllegalStateException e) {
            if (subtractAmount > 0) {
                Bukkit.getScheduler().runTask(main, () -> Main.getEconomy().withdrawPlayer(player, subtractAmount));
            } else {
                Bukkit.getScheduler().runTask(main, () -> Main.getEconomy().depositPlayer(player, Math.abs(subtractAmount)));
            }
        }

        if (validationManager.doStoreData(null)) {
            this.totalTaxCollected += subtractAmount;
            this.main.getStatsManager().saveToCache(player.getUniqueId(), subtractAmount);
            this.main.getStorageManager().getStorageHelper().saveToStorage(player.getUniqueId(), subtractAmount);
        }

        return true;
    }

    /**
     * Checks if a player is exempted from tax.
     *
     * @param player player to check
     * @param permissionNode permission node used to determine if player is exempt
     */
    private boolean isTaxExempt(OfflinePlayer player, String permissionNode) {
        return Main.getPermissions().playerHas(Bukkit.getWorlds().get(0).getName(), player, exemptFullPerm)
            || Main.getPermissions().playerHas(Bukkit.getWorlds().get(0).getName(), player, permissionNode);
    }

    /**
     * Penalizes a player who does not have enough balance to pay taxes.
     *
     * @param player player being penalized
     * @param penalizeClaims if true, will penalize claims as well
     */
    private void penalizePlayer(OfflinePlayer player, boolean penalizeClaims) {
        if (main.getConfig().getBoolean("auto-remove-claims", false) && penalizeClaims) {
            Bukkit.getScheduler().runTask(main, () -> {
                GriefPrevention.instance.dataStore.deleteClaimsForPlayer(player.getUniqueId(), false);
            });
        }

        List<String> debtCommands = main.getConfig().getStringList("debt-commands");
        for (String command : debtCommands) {
            String playerParsedCommand = command.replaceAll("%player%", player.getName());
            String papiParsedCommand = PlaceholderAPI.setPlaceholders(player, playerParsedCommand);
            new BukkitRunnable() {
                @Override
                public void run() {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), papiParsedCommand);
                }
            }.runTask(main);
        }
    }

    /**
     * Interface to determine what type of update to run.
     *
     * @param <T> player to update
     * @param <S> amount of bal to tax
     * @param <U> additional amount of bal to tax depending on claimblocks
     * @param <V> whether to deduct by percentage or absolute value
     */
    @FunctionalInterface
    interface updateType<T, S, U, V> {
        boolean updatePlayer(T player, S balTaxAmount, U claimsTaxAmount, V usePercentage);
    }
}
