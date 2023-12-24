package tk.taverncraft.quicktax.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

/**
 * CommandTabCompleter prompts users to tab complete the possible commands based on current input.
 */
public class CommandTabCompleter implements TabCompleter {
    private static final String[] COMMANDS = {"collectall", "collectrank", "collectbal", "collectactivity",
        "collectname", "help", "stats", "top", "update", "reload", "server", "pay", "schedule"};

    /**
     * Overridden method from TabCompleter, entry point for checking of user command to suggest
     * tab complete.
     *
     * @param sender user who sent the command
     * @param mmd command sent by the user
     * @param label exact command name typed by the user
     * @param args arguments following the command name
     * @return list of values as suggestions to tab complete for the user
     */
    @Override
    public List<String> onTabComplete(CommandSender sender, Command mmd, String label, String[] args) {
        final List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            StringUtil.copyPartialMatches(args[0], Arrays.asList(COMMANDS), completions);
        } else if (args.length == 2 && (args[0].equalsIgnoreCase("collectname") || args[0].equalsIgnoreCase("stats"))) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                completions.add(p.getName());
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("server")) {
            completions.add("stats");
            completions.add("withdraw");
            completions.add("admin");
        } else if (args.length == 3 && args[0].equalsIgnoreCase("server") && args[1].equalsIgnoreCase("admin")) {
            completions.add("add");
            completions.add("take");
            completions.add("set");
        } else if (args.length == 4 && args[1].equalsIgnoreCase("withdraw")) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                completions.add(p.getName());
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("schedule")) {
            completions.add("start");
            completions.add("stop");
            completions.add("view");
        }
        Collections.sort(completions);
        return completions;
    }
}