package tk.taverncraft.quicktax.commands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import tk.taverncraft.quicktax.Main;
import tk.taverncraft.quicktax.utils.MessageManager;
import tk.taverncraft.quicktax.utils.TaxManager;
import tk.taverncraft.quicktax.utils.ValidationManager;

/**
 * AdminCommand contains the execute method for when an admin modifies the server balance.
 */
public class AdminCommand {

    private final String adminAddPerm = "quicktax.server.admin.add";
    private final String adminTakePerm = "quicktax.server.admin.take";
    private final String adminSetPerm = "quicktax.server.admin.set";
    Main main;
    TaxManager taxManager;
    ValidationManager validationManager;

    /**
     * Constructor for AdminCommand.
     */
    public AdminCommand(Main main) {
        this.main = main;
        this.taxManager = new TaxManager(main);
        this.validationManager = new ValidationManager(main);
    }

    /**
     * Pay tax to the server.
     *
     * @param sender user who sent the command
     * @param args command arguments
     *
     * @return true at end of execution
     */
    public boolean execute(CommandSender sender, String[] args) {
        if (!validationManager.doStoreData(sender)) {
            return true;
        }

        String modifyType = args[2];

        if (modifyType.equalsIgnoreCase("ADD") && validationManager.hasPermission(adminAddPerm, sender)) {
            addToServerBalance(sender, args[3]);
        } else if (modifyType.equalsIgnoreCase("TAKE") && validationManager.hasPermission(adminTakePerm, sender)) {
            removeFromServerBalance(sender, args[3]);
        } else if (modifyType.equalsIgnoreCase("SET") && validationManager.hasPermission(adminSetPerm, sender)) {
            setServerBalance(sender, args[3]);
        }
        return true;
    }

    /**
     * Adds specified amount to the server balance.
     *
     * @param sender user who sent the command
     * @param strAmount amount to withdraw
     */
    private void addToServerBalance(CommandSender sender, String strAmount) {
        if (!this.validationManager.isDouble(strAmount, sender)) {
            return;
        }

        double amount = Double.parseDouble(strAmount);
        this.main.getStatsManager().addServerBalanceAsAdmin(amount);
        MessageManager.sendMessage(sender, "admin-add-server-balance",
            new String[]{"%amount%"},
            new String[]{strAmount});
    }

    /**
     * Removes specified amount from the server balance.
     *
     * @param sender user who sent the command
     * @param strAmount amount to withdraw
     */
    private void removeFromServerBalance(CommandSender sender, String strAmount) {
        if (!this.validationManager.isDouble(strAmount, sender)) {
            return;
        }

        double amount = Double.parseDouble(strAmount);

        double totalTaxBalance = Double.parseDouble(this.main.getStatsManager().getServerStats()[1]);
        if (!validationManager.serverHasTaxBalance(amount, totalTaxBalance)) {
            MessageManager.sendMessage(sender, "tax-withdraw-fail");
            return;
        }

        this.main.getStatsManager().takeServerBalanceAsAdmin(amount);
        MessageManager.sendMessage(sender, "admin-take-server-balance",
            new String[]{"%amount%"},
            new String[]{strAmount});
    }

    /**
     * Sets specified amount to the server balance.
     *
     * @param sender user who sent the command
     * @param strAmount amount to withdraw
     */
    private void setServerBalance(CommandSender sender, String strAmount) {
        if (!this.validationManager.isDouble(strAmount, sender)) {
            return;
        }

        double amount = Double.parseDouble(strAmount);
        this.main.getStatsManager().setServerBalanceAsAdmin(amount);
        MessageManager.sendMessage(sender, "admin-set-server-balance",
            new String[]{"%amount%"},
            new String[]{strAmount});
    }
}

