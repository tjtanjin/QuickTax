package tk.taverncraft.quicktax.commands;

import org.bukkit.command.CommandSender;

import tk.taverncraft.quicktax.Main;
import tk.taverncraft.quicktax.utils.MessageManager;
import tk.taverncraft.quicktax.utils.ValidationManager;

/**
 * HelpCommand contains the execute method for when a user inputs the command to get help for the plugin.
 */
public class HelpCommand {

    private final String helpPerm = "quicktax.help";
    Main main;
    ValidationManager validationManager;

    /**
     * Constructor for HelpCommand.
     */
    public HelpCommand(Main main) {
        this.main = main;
        this.validationManager = new ValidationManager(main);
    }

    /**
     * Shows a list of commands to the user.
     *
     * @param sender user who sent the command
     * @param args command arguments
     *
     * @return true at end of execution
     */
    public boolean execute(CommandSender sender, String[] args) {
        if (!validationManager.hasPermission(helpPerm, sender)) {
            return true;
        }

        try {
            int pageNum = Integer.parseInt(args[1]);
            MessageManager.showHelpBoard(sender, pageNum);
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            MessageManager.showHelpBoard(sender, 1);
        }

        return true;
    }
}
