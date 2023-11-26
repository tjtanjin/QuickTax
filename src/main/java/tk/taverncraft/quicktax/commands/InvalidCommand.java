package tk.taverncraft.quicktax.commands;

import org.bukkit.command.CommandSender;

import tk.taverncraft.quicktax.Main;
import tk.taverncraft.quicktax.utils.MessageManager;

/**
 * InvalidCommand contains the execute method for when a user inputs an unrecognised command.
 */
public class InvalidCommand {
    Main main;

    /**
     * Constructor for InvalidCommand.
     */
    public InvalidCommand(Main main) {
        this.main = main;
    }

    /**
     * Handles all invalid commands from the user.
     *
     * @param sender user who sent the command
     *
     * @return true at end of execution
     */
    public boolean execute(CommandSender sender) {
        MessageManager.sendMessage(sender, "invalid-command");
        return true;
    }
}
