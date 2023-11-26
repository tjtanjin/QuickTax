package tk.taverncraft.quicktax.commands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import tk.taverncraft.quicktax.Main;
import tk.taverncraft.quicktax.utils.MessageManager;
import tk.taverncraft.quicktax.utils.TaxManager;
import tk.taverncraft.quicktax.utils.ValidationManager;

/**
 * PayCommand contains the execute method for when a user pays tax directly to the server.
 */
public class PayCommand {

    private final String payPerm = "quicktax.pay";
    Main main;
    TaxManager taxManager;
    ValidationManager validationManager;

    /**
     * Constructor for PayCommand.
     */
    public PayCommand(Main main) {
        this.main = main;
        this.taxManager = new TaxManager(main);
        this.validationManager = new ValidationManager(main);
    }

    /**
     * Pay tax to the server.
     *
     * @param sender user who sent the command
     * @param strAmount amount to pay to the server
     *
     * @return true at end of execution
     */
    public boolean execute(CommandSender sender, String strAmount) {
        if (!validationManager.hasPermission(payPerm, sender)) {
            return true;
        }

        if (!this.validationManager.isDouble(strAmount, sender)) {
            return true;
        }

        if (main.getStorageManager().isLoading()) {
            MessageManager.sendMessage(sender, "player-load-in-progress");
            return true;
        }

        double amount = Double.parseDouble(strAmount);

        OfflinePlayer player = Bukkit.getOfflinePlayer(sender.getName());
        if (taxManager.updatePlayerNoClaims(player, amount, 0, false)) {
            if (validationManager.doStoreData(null)) {
                this.main.getStatsManager().updateServerStats(amount, true);
                if (this.main.getConfig().getString("storage-type", "none").equalsIgnoreCase("mysql")) {
                    double totalTaxPaid = Double.parseDouble(main.getStatsManager().getPlayerStats(player)[1]);
                    main.getStorageManager().getStorageHelper().insertIntoDatabase("('" + player.getUniqueId() + "', '" + amount + "', '" + totalTaxPaid + "'), ");
                }
            }
        } else {
            MessageManager.sendMessage(sender, "player-pay-tax-fail",
                    new String[]{"%player%"},
                    new String[]{player.getName()});
        }
        return true;
    }
}

