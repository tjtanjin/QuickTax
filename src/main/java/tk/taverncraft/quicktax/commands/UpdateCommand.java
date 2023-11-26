package tk.taverncraft.quicktax.commands;

import org.bukkit.command.CommandSender;

import tk.taverncraft.quicktax.Main;
import tk.taverncraft.quicktax.utils.MessageManager;
import tk.taverncraft.quicktax.utils.ValidationManager;

/**
 * UpdateCommand contains the execute method for when a user wishes to manually trigger a leaderboard update.
 */
public class UpdateCommand {

    private final String updatePerm = "quicktax.update";
    Main main;
    ValidationManager validationManager;

    /**
     * Constructor for UpdateCommand.
     */
    public UpdateCommand(Main main) {
        this.main = main;
        this.validationManager = new ValidationManager(main);
    }

    /**
     * Updates the leaderboard.
     *
     * @param sender user who sent the command
     *
     * @return true at end of execution
     */
    public boolean execute(CommandSender sender) {
        if (!validationManager.hasPermission(updatePerm, sender) || !validationManager.doStoreData(sender)) {
            return true;
        }

        if (main.getStorageManager().isLoading()) {
            MessageManager.sendMessage(sender, "player-load-in-progress");
            return true;
        }

        if (main.getStatsManager().isUpdating()) {
            MessageManager.sendMessage(sender, "update-in-progress");
            return true;
        }

        main.getStatsManager().manualUpdateLeaderboard(sender);
        return true;
    }

}
