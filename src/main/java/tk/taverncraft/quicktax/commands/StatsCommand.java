package tk.taverncraft.quicktax.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import tk.taverncraft.quicktax.Main;
import tk.taverncraft.quicktax.utils.MessageManager;
import tk.taverncraft.quicktax.utils.ValidationManager;

/**
 * StatsCommand contains the execute method for when a user views the server, others or self stats.
 */
public class StatsCommand {

    private final String statsSelfPerm = "quicktax.stats.self";
    private final String statsOthersPerm = "quicktax.stats.others";
    private final String statsServerPerm = "quicktax.server.stats";
    Main main;
    ValidationManager validationManager;

    /**
     * Constructor for StatsCommand.
     */
    public StatsCommand(Main main) {
        this.main = main;
        this.validationManager = new ValidationManager(main);
    }

    /**
     * Shows users their own tax stats.
     *
     * @param sender user who sent the command
     *
     * @return true at end of execution
     */
    public boolean execute(CommandSender sender) {
        if (!validationManager.hasPermission(statsSelfPerm, sender) || !validationManager.doStoreData(sender)) {
            return true;
        }

        if (main.getStorageManager().isLoading()) {
            MessageManager.sendMessage(sender, "player-load-in-progress");
            return true;
        }

        String[] stats = this.main.getStatsManager().getPlayerStats(Bukkit.getOfflinePlayer(sender.getName()));
        MessageManager.sendMessage(sender, "player-stats",
                new String[]{"%player%", "%latesttaxpaid%", "%totaltaxpaid%"},
                new String[]{sender.getName(), stats[0], stats[1]});
        return true;
    }

    /**
     * Views the stats of another player.
     *
     * @param sender user who sent the command
     * @param args command arguments
     *
     * @return true at end of execution
     */
    public boolean execute(CommandSender sender, String[] args) {
        String name = args[1];
        if (!validationManager.hasPermission(statsOthersPerm, sender) || !validationManager.doStoreData(sender)) {
            return true;
        }

        if (!this.validationManager.playerExist(args[1], sender)) {
            return true;
        }

        if (main.getStorageManager().isLoading()) {
            MessageManager.sendMessage(sender, "player-load-in-progress");
            return true;
        }

        String[] stats = this.main.getStatsManager().getPlayerStats(Bukkit.getOfflinePlayer(name));
        MessageManager.sendMessage(sender, "player-stats",
                new String[]{"%player%", "%latesttaxpaid%", "%totaltaxpaid%"},
                new String[]{name, stats[0], stats[1]});

        return true;
    }

    /**
     * Views the stats of the server.
     *
     * @param sender user who sent the command
     * @param forServer a boolean for method overloading to get server stats
     *
     * @return true at end of execution
     */
    public boolean execute(CommandSender sender, boolean forServer) {
        if (!validationManager.hasPermission(statsServerPerm, sender) || !validationManager.doStoreData(sender)) {
            return true;
        }

        String[] stats = this.main.getStatsManager().getServerStats();
        MessageManager.sendMessage(sender, "server-stats",
                new String[]{"%totaltaxcollected%", "%totaltaxbalance%"},
                new String[]{stats[0], stats[1]});
        return true;
    }
}

