package tk.taverncraft.quicktax.commands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import tk.taverncraft.quicktax.Main;
import tk.taverncraft.quicktax.utils.MessageManager;
import tk.taverncraft.quicktax.utils.ValidationManager;

/**
 * WithdrawCommand contains the execute method for when a user withdraws money from server tax balance to self or others.
 */
public class WithdrawCommand {

    private final String withdrawSelfPerm = "quicktax.server.withdraw.self";
    private final String withdrawOthersPerm = "quicktax.server.withdraw.others";
    Main main;
    ValidationManager validationManager;

    /**
     * Constructor for WithdrawCommand.
     */
    public WithdrawCommand(Main main) {
        this.main = main;
        this.validationManager = new ValidationManager(main);
    }

    /**
     * Withdraws money from server tax balance.
     *
     * @param sender user who sent the command
     * @param args arguments of the command
     *
     * @return true at end of execution
     */
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length == 3) {
            return this.withdrawForSelf(sender, args[2]);
        } else {
            return this.withdrawForOthers(sender, args[2], args[3]);
        }
    }

    /**
     * Withdraws money for self.
     *
     * @param sender user who sent the command
     * @param strAmount amount to withdraw
     */
    public boolean withdrawForSelf(CommandSender sender, String strAmount) {
        if (!validationManager.hasPermission(withdrawSelfPerm, sender) || !validationManager.doStoreData(sender)) {
            return true;
        }

        if (!this.validationManager.isDouble(strAmount, sender)) {
            return true;
        }

        double amount = Double.parseDouble(strAmount);

        try {
            double totalTaxBalance = Double.parseDouble(this.main.getStatsManager().getServerStats()[1]);
            if (validationManager.serverHasTaxBalance(amount, totalTaxBalance)) {
                OfflinePlayer player = Bukkit.getOfflinePlayer(sender.getName());
                Main.getEconomy().depositPlayer(player, amount);
                MessageManager.sendMessage(sender, "tax-withdraw-success");
                this.main.getStatsManager().updateServerStats(amount, false);
            } else {
                MessageManager.sendMessage(sender, "tax-withdraw-fail");
            }
        } catch (Exception e) {
            // vault might throw an error here related to null user, remove when resolved
        }
        return true;
    }

    /**
     * Withdraws money for others.
     *
     * @param sender user who sent the command
     * @param strAmount amount to withdraw
     * @param name player to withdraw for
     */
    public boolean withdrawForOthers(CommandSender sender, String strAmount, String name) {
        if (!validationManager.hasPermission(withdrawOthersPerm, sender)
                || !validationManager.doStoreData(sender)
                || !validationManager.playerExist(name, sender)) {
            return true;
        }

        if (!this.validationManager.isDouble(strAmount, sender)) {
            return true;
        }

        double amount = Double.parseDouble(strAmount);

        try {
            double totalTaxBalance = Double.parseDouble(this.main.getStatsManager().getServerStats()[1]);
            if (validationManager.serverHasTaxBalance(amount, totalTaxBalance)) {
                OfflinePlayer player = Bukkit.getOfflinePlayer(name);
                Main.getEconomy().depositPlayer(player, amount);
                MessageManager.sendMessage(sender, "tax-withdraw-success");
                if (player.isOnline()) {
                    MessageManager.sendMessage(player.getPlayer(), "player-receive-tax-money",
                            new String[]{"%player%", "%amount%"},
                            new String[]{player.getName(), strAmount});
                }
                this.main.getStatsManager().updateServerStats(amount, false);
            } else {
                MessageManager.sendMessage(sender, "tax-withdraw-fail");
            }
        } catch (Exception e) {
            // vault might throw an error here related to null user, remove when resolved
        }
        return true;
    }
}

