package tk.taverncraft.quicktax.utils;

import java.io.File;
import java.io.IOException;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import tk.taverncraft.quicktax.Main;

/**
 * Validator performs common validations that are required as necessary.
 */
public class ValidationManager {

    Main main;

    /**
     * Constructor for Validator.
     */
    public ValidationManager(Main main) {
        this.main = main;
    }

    /**
     * Validates if sender has permission and sends a message if not.
     *
     * @param permission permission node to check for
     * @param sender the player executing the command
     */
    public boolean hasPermission(String permission, CommandSender sender) {
        if (sender.hasPermission(permission)) {
            return true;
        }
        MessageManager.sendMessage(sender, "no-permission");
        return false;
    }

    /**
     * Validates if inputted player exist and sends a message if not.
     *
     * @param name the name of the player to check for
     * @param sender the player executing the command
     */
    public boolean playerExist(String name, CommandSender sender) {
        if (name.length() > 16 || this.main.getServer().getOfflinePlayer(name).getFirstPlayed() == 0L) {
            MessageManager.sendMessage(sender, "player-not-exist",
                    new String[]{"%player%"},
                    new String[]{name});
            return false;
        }
        return true;
    }

    /**
     * Validates if inputted schedule exist and sends a message if not.
     *
     * @param scheduleName the name of the schedule to check for
     * @param sender the player executing the command
     */
    public boolean scheduleExist(String scheduleName, CommandSender sender) {
        File scheduleFile = new File(this.main.getDataFolder(), "schedule.yml");
        if (!scheduleFile.exists()) {
            MessageManager.sendMessage(sender, "schedule-not-exist",
                    new String[]{"%schedule%"},
                    new String[]{scheduleName});
            return false;
        }
        FileConfiguration schedule = new YamlConfiguration();
        try {
            schedule.load(scheduleFile);
        } catch (IOException | InvalidConfigurationException e) {
            MessageManager.sendMessage(sender, "schedule-not-exist",
                    new String[]{"%schedule%"},
                    new String[]{scheduleName});
            return false;
        }
        if (schedule.getString("schedules." + scheduleName) != null) {
            return true;
        }
        MessageManager.sendMessage(sender, "schedule-not-exist",
                new String[]{"%schedule%"},
                new String[]{scheduleName});
        return false;
    }

    /**
     * Validates if storing of data is allowed.
     *
     * @param sender the player executing the command
     */
    public boolean doStoreData(CommandSender sender) {
        String storageType = this.main.getConfig().getString("storage-type", "none");
        boolean storeData = false;
        if (storageType.equalsIgnoreCase("yaml") || storageType.equalsIgnoreCase("mysql")) {
            storeData = true;
        }
        if (sender != null && !storeData) {
            MessageManager.sendMessage(sender, "storage-disabled");
        }
        return storeData;
    }

    /**
     * Validates if server has enough tax balance for amount to be withdrawn.
     *
     * @param amount the amount to withdraw
     * @param totalTaxBalance the current server tax balance
     */
    public boolean serverHasTaxBalance(double amount, double totalTaxBalance) {
        return amount <= totalTaxBalance;
    }

    /**
     * Validates if a given string contains a double value
     *
     * @param value value to check on
     * @param sender executor of the command
     */
    public boolean isDouble(String value, CommandSender sender) {
        try {
            Double.parseDouble(value);
            return true;
        } catch (NumberFormatException e) {
            MessageManager.sendMessage(sender, "invalid-syntax");
            return false;
        }
    }
}
