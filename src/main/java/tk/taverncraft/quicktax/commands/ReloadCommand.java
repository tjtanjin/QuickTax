package tk.taverncraft.quicktax.commands;

import org.bukkit.command.CommandSender;

import tk.taverncraft.quicktax.Main;
import tk.taverncraft.quicktax.utils.MessageManager;
import tk.taverncraft.quicktax.utils.ScheduleManager;
import tk.taverncraft.quicktax.utils.TaxManager;
import tk.taverncraft.quicktax.utils.ValidationManager;

/**
 * ReloadCommand contains the execute method for when a user inputs command to reload plugin.
 */
public class ReloadCommand {

    private final String reloadPerm = "quicktax.reload";
    Main main;
    ValidationManager validationManager;

    /**
     * Constructor for ReloadCommand.
     */
    public ReloadCommand(Main main) {
        this.main = main;
        this.validationManager = new ValidationManager(main);
    }

    /**
     * Reloads all files.
     *
     * @param sender user who sent the command
     *
     * @return true at end of execution
     */
    public boolean execute(CommandSender sender) {
        if (!validationManager.hasPermission(reloadPerm, sender)) {
            return true;
        }

        if (TaxManager.isCollecting) {
            MessageManager.sendMessage(sender, "tax-collect-already-running");
            return true;
        }

        try {
            ScheduleManager.stopAllSchedules(sender);
            main.getStorageManager().createConfig();
            main.getStorageManager().createMessageFile();
            this.main.getStorageManager().initializeValues(true);
            this.main.getStatsManager().initializeValues();
            if (!validationManager.doStoreData(null)) {
                MessageManager.resetLeaderboard();
            }
            MessageManager.sendMessage(sender, "reload-success");
        } catch (Exception e) {
            MessageManager.sendMessage(sender, "reload-fail");
        }
        return true;
    }
}

