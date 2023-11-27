package tk.taverncraft.quicktax.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import tk.taverncraft.quicktax.Main;
import tk.taverncraft.quicktax.utils.MessageManager;
import tk.taverncraft.quicktax.utils.TaxManager;
import tk.taverncraft.quicktax.utils.ValidationManager;

/**
 * CollectCommand contains the execute method for when a user inputs the command to tax players.
 */
public class CollectCommand {

    private final String collectNamePerm = "quicktax.collectname";
    private final String collectAllPerm = "quicktax.collectall";
    private final String collectRankPerm = "quicktax.collectrank";
    private final String collectBalPerm = "quicktax.collectbal";
    private final String collectActivityPerm = "quicktax.collectactivity";

    Main main;
    TaxManager taxManager;
    ValidationManager validationManager;

    /**
     * Constructor for CollectCommand.
     */
    public CollectCommand(Main main) {
        this.main = main;
        this.taxManager = new TaxManager(main);
        this.validationManager = new ValidationManager(main);
    }

    /**
     * Starts collecting taxes from players.
     *
     * @param sender user who sent the command
     * @param args arguments of the command
     *
     * @return true at end of execution
     */
    public boolean execute(CommandSender sender, String[] args) {

        if (TaxManager.isCollecting) {
            MessageManager.sendMessage(sender, "tax-collect-already-running");
            return true;
        }

        try {
            switch (args[0]) {
            case "collectall":
                if (executeForAll(sender)) {
                    break;
                }
                return true;
            case "collectrank":
                if (executeForRank(sender)) {
                    break;
                }
                return true;
            case "collectbal":
                if (executeForBal(sender)) {
                    break;
                }
                return true;
            case "collectactivity":
                if (executeForActivity(sender)) {
                    break;
                }
                return true;
            case "collectname":
                if (executeForName(sender, args)) {
                    break;
                }
            default:
                return true;
            }
        } catch (NullPointerException e) {
            main.getLogger().warning("A strange incident was logged, please report this to the plugin author.");
            // vault might throw an error here related to null user, remove when resolved
        }

        // collect tax asynchronously depending on type
        Bukkit.getScheduler().runTaskAsynchronously(this.main, TaxManager.task);
        return true;
    }

    /**
     * Executes checks and logic for collecting tax from all players.
     *
     * @param sender user who sent the command
     *
     * @return true if collection was successful
     */
    public boolean executeForAll(CommandSender sender) throws NullPointerException {
        if (!validationManager.hasPermission(collectAllPerm, sender)) {
            return false;
        }

        if (main.getStorageManager().isLoading()) {
            MessageManager.sendMessage(sender, "player-load-in-progress");
            return true;
        }

        TaxManager.task = () -> {
            TaxManager.isCollecting = true;
            taxManager.collectAll(sender);
            TaxManager.isCollecting = false;
        };
        MessageManager.sendMessage(sender, "tax-collect-all-in-progress");
        main.getLogger().info("Collecting tax from every player...");
        return true;
    }

    /**
     * Executes checks and logic for collecting tax from players based on rank.
     *
     * @param sender user who sent the command
     *
     * @return true if collection was successful
     */
    public boolean executeForRank(CommandSender sender) throws NullPointerException {
        if (!validationManager.hasPermission(collectRankPerm, sender)) {
            return false;
        }

        if (main.getStorageManager().isLoading()) {
            MessageManager.sendMessage(sender, "player-load-in-progress");
            return true;
        }

        TaxManager.task = () -> {
            TaxManager.isCollecting = true;
            taxManager.collectRank(sender);
            TaxManager.isCollecting = false;
        };
        MessageManager.sendMessage(sender, "tax-collect-rank-in-progress");
        main.getLogger().info("Collecting tax by rank...");
        return true;
    }

    /**
     * Executes checks and logic for collecting tax from players based on balance.
     *
     * @param sender user who sent the command
     *
     * @return true if collection was successful
     */
    public boolean executeForBal(CommandSender sender) throws NullPointerException {
        if (!validationManager.hasPermission(collectBalPerm, sender)) {
            return false;
        }

        if (main.getStorageManager().isLoading()) {
            MessageManager.sendMessage(sender, "player-load-in-progress");
            return true;
        }

        TaxManager.task = () -> {
            TaxManager.isCollecting = true;
            taxManager.collectBal(sender);
            TaxManager.isCollecting = false;
        };
        MessageManager.sendMessage(sender, "tax-collect-bal-in-progress");
        main.getLogger().info("Collecting tax by balance...");
        return true;
    }

    /**
     * Executes checks and logic for collecting tax from players based on last seen activity.
     *
     * @param sender user who sent the command
     *
     * @return true if collection was successful
     */
    public boolean executeForActivity(CommandSender sender) throws NullPointerException {
        if (!validationManager.hasPermission(collectActivityPerm, sender)) {
            return false;
        }

        if (main.getStorageManager().isLoading()) {
            MessageManager.sendMessage(sender, "player-load-in-progress");
            return true;
        }

        TaxManager.task = () -> {
            TaxManager.isCollecting = true;
            taxManager.collectActivity(sender);
            TaxManager.isCollecting = false;
        };
        MessageManager.sendMessage(sender, "tax-collect-activity-in-progress");
        main.getLogger().info("Collecting tax by activity...");
        return true;
    }

    /**
     * Executes checks and logic for collecting tax from player based on name.
     *
     * @param sender user who sent the command
     * @param args arguments of the command
     *
     * @return true if collection was successful
     */
    public boolean executeForName(CommandSender sender, String[] args) throws NullPointerException {
        if (!validationManager.hasPermission(collectNamePerm, sender)) {
            return false;
        }
        if (args.length < 3) {
            MessageManager.sendMessage(sender, "invalid-syntax");
            return false;
        }
        if (!this.validationManager.playerExist(args[1], sender)) {
            return false;
        }

        if (main.getStorageManager().isLoading()) {
            MessageManager.sendMessage(sender, "player-load-in-progress");
            return true;
        }

        if (this.validationManager.isDouble(args[2], sender)) {
            TaxManager.task = () -> {
                TaxManager.isCollecting = true;
                taxManager.collectIndividual(sender, args[1], args[2]);
                TaxManager.isCollecting = false;
            };
            MessageManager.sendMessage(sender, "tax-collect-individual-in-progress",
                    new String[]{"%player%"},
                    new String[]{args[1]});
            main.getLogger().info("Collecting tax from " + args[1] + "...");
            return true;
        } else {
            MessageManager.sendMessage(sender, "invalid-syntax");
            return false;
        }
    }
}
