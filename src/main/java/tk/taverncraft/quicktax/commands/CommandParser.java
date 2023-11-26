package tk.taverncraft.quicktax.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import tk.taverncraft.quicktax.Main;

/**
 * CommandParser contains the onCommand method that handles user command input.
 */
public class CommandParser implements CommandExecutor {
    Main main;

    /**
     * Constructor for CommandParser.
     */
    public CommandParser(Main main) {
        this.main = main;
    }

    /**
     * Entry point of commands.
     */
    public boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
        if (cmd.getName().equalsIgnoreCase("quicktax")) {

            // if no arguments provided or is null, return invalid command
            if (args.length == 0) {
                return new InvalidCommand(this.main).execute(sender);
            }

            final String chatCmd = args[0];

            if (chatCmd == null) {
                return new InvalidCommand(this.main).execute(sender);
            }

            // command to for tax collection schedule (start/stop/view)
            if (chatCmd.equals("schedule")) {
                if (args.length >= 2) {
                    return new ScheduleCommand(this.main).execute(sender, args);
                }
            }

            // command to collect tax (all/rank/balance/job/name)
            if (chatCmd.startsWith("collectall") || chatCmd.startsWith("collectbal")
                    || chatCmd.startsWith("collectrank") || chatCmd.startsWith("collectname")) {
                return new CollectCommand(this.main).execute(sender, args);
            }

            // command to pay tax to the server
            if (chatCmd.equals("pay")) {
                if (args.length == 2) {
                    return new PayCommand(this.main).execute(sender, args[1]);
                }
            }

            // command to view stats (self/others)
            if (chatCmd.equals("stats")) {
                if (args.length == 2) {
                    return new StatsCommand(this.main).execute(sender, args);
                } else {
                    return new StatsCommand(this.main).execute(sender);
                }
            }

            // command to view server stats or withdraw server balance
            if (chatCmd.equals("server")) {
                if (args.length == 2 && args[1].equalsIgnoreCase("stats")) {
                    return new StatsCommand(this.main).execute(sender, true);
                } else if (args.length > 2 && args[1].equalsIgnoreCase("withdraw")) {
                    return new WithdrawCommand(this.main).execute(sender, args);
                }
            }

            // command to view leaderboard
            if (chatCmd.equals("top")) {
                return new TopCommand(this.main).execute(sender, args);
            }

            // command to manually update leaderboard
            if (chatCmd.equals("update")) {
                return new UpdateCommand(this.main).execute(sender);
            }

            // command to view all commands
            if (chatCmd.equals("help")) {
                return new HelpCommand(this.main).execute(sender, args);
            }

            // command to reload plugin
            if (chatCmd.equals("reload")) {
                return new ReloadCommand(this.main).execute(sender);
            }

            return new InvalidCommand(this.main).execute(sender);
        }
        return true;
    }

}

