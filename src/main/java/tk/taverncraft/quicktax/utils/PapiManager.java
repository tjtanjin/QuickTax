package tk.taverncraft.quicktax.utils;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;

import tk.taverncraft.quicktax.Main;

/**
 * An expansion class for PAPI.
 */
public class PapiManager extends PlaceholderExpansion {

    private final Main main;

    public PapiManager(Main main) {
        this.main = main;
    }

    @Override
    public String getAuthor() {
        return "tjtanjin - FrozenFever";
    }

    @Override
    public String getIdentifier() {
        return "qtax";
    }

    @Override
    public String getVersion() {
        return main.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true; // This is required or else PlaceholderAPI will unregister the Expansion on reload
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        if (params.equalsIgnoreCase("server_tax_collected")) {
            String[] stats = main.getStatsManager().getServerStats();
            return stats[0];
        }

        if (params.equalsIgnoreCase("server_tax_balance")) {
            String[] stats = main.getStatsManager().getServerStats();
            return stats[1];
        }

        if (params.equalsIgnoreCase("player_last_paid")) {
            String[] stats = main.getStatsManager().getPlayerStats(player);
            return stats[0];
        }

        if (params.equalsIgnoreCase("player_total_paid")) {
            String[] stats = main.getStatsManager().getPlayerStats(player);
            return stats[1];
        }

        if (params.startsWith("schedule_next_run_")) {
            String[] args = params.split("_", 4);
            String scheduleName = args[3];
            Schedule schedule = ScheduleManager.getSchedule(scheduleName);
            if (schedule != null) {
                return schedule.getNextRunTime();
            } else {
                return "None";
            }
        }

        if (params.startsWith("schedule_timezone_")) {
            String[] args = params.split("_", 3);
            String scheduleName = args[2];
            Schedule schedule = ScheduleManager.getSchedule(scheduleName);
            if (schedule != null) {
                return schedule.getTimezoneInGMT();
            } else {
                return "None";
            }
        }

        if (params.startsWith("schedule_freq_")) {
            String[] args = params.split("_", 3);
            String scheduleName = args[2];
            Schedule schedule = ScheduleManager.getSchedule(scheduleName);
            if (schedule != null) {
                return String.valueOf(schedule.getFrequency());
            } else {
                return "None";
            }
        }

        if (params.startsWith("schedule_type_")) {
            String[] args = params.split("_", 3);
            String scheduleName = args[2];
            Schedule schedule = ScheduleManager.getSchedule(scheduleName);
            if (schedule != null) {
                return schedule.getType();
            } else {
                return "None";
            }
        }

        if (params.startsWith("top_name_")) {
            String[] args = params.split("_", 3);
            try {
                int index = Integer.parseInt(args[2]) - 1;
                return main.getStatsManager().getTopPlayerName(index);
            } catch (NumberFormatException | IndexOutOfBoundsException e) {
                Bukkit.getConsoleSender().sendMessage(e.getMessage());
                return "None";
            }
        }

        if (params.startsWith("top_tax_")) {
            String[] args = params.split("_", 3);
            try {
                int index = Integer.parseInt(args[2]) - 1;
                return main.getStatsManager().getTopPlayerTaxPaid(index);
            } catch (NumberFormatException | IndexOutOfBoundsException e) {
                return "None";
            }
        }

        return null; // Placeholder is unknown by the Expansion
    }
}
