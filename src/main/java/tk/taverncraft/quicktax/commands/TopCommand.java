package tk.taverncraft.quicktax.commands;

import org.bukkit.command.CommandSender;

import tk.taverncraft.quicktax.Main;
import tk.taverncraft.quicktax.utils.MessageManager;
import tk.taverncraft.quicktax.utils.ValidationManager;

/**
 * TopCommand contains the execute method for when a user views the tax payer leaderboard.
 */
public class TopCommand {

    private final String topPerm = "quicktax.top";
    Main main;
    ValidationManager validationManager;

    /**
     * Constructor for TopCommand.
     */
    public TopCommand(Main main) {
        this.main = main;
        this.validationManager = new ValidationManager(main);
    }

    /**
     * Shows leaderboard to user.
     *
     * @param sender user who sent the command
     * @param args command args possibly containing page number
     *
     * @return true at end of execution
     */
    public boolean execute(CommandSender sender, String[] args) {
        if (!validationManager.hasPermission(topPerm, sender) || !validationManager.doStoreData(sender)) {
            return true;
        }

        if (main.getStatsManager().isUpdating()) {
            MessageManager.sendMessage(sender, "update-in-progress");
            return true;
        }

        int pageNum = 1;
        try {
            pageNum = Integer.parseInt(args[1]);
        } catch (NumberFormatException | IndexOutOfBoundsException ignored) {
        }
        MessageManager.showLeaderboard(sender, pageNum);
        return true;
    }
}


