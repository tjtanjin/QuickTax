package tk.taverncraft.quicktax.commands;

import org.bukkit.command.CommandSender;

import tk.taverncraft.quicktax.Main;
import tk.taverncraft.quicktax.utils.*;

/**
 * ScheduleCommand contains the execute method for when a user starts/stops/views tax collection schedules.
 */
public class ScheduleCommand {

    private final String scheduleStartPerm = "quicktax.schedule.start";
    private final String scheduleStopPerm = "quicktax.schedule.stop";
    private final String scheduleViewAllPerm = "quicktax.schedule.view.*";
    private final String scheduleViewEnabledPerm = "quicktax.schedule.view.enabled";
    private final String scheduleViewDisabledPerm = "quicktax.schedule.view.disabled";
    Main main;
    TaxManager taxManager;
    ValidationManager validationManager;

    /**
     * Constructor for ScheduleCommand.
     */
    public ScheduleCommand(Main main) {
        this.main = main;
        this.taxManager = new TaxManager(main);
        this.validationManager = new ValidationManager(main);
    }

    /**
     * Starts, stops, or views tax collection schedules.
     *
     * @param sender user who sent the command
     * @param args command arguments
     *
     * @return true at end of execution
     */
    public boolean execute(CommandSender sender, String[] args) {
        String action = args[1];

        if (action.equals("start") && validationManager.hasPermission(scheduleStartPerm, sender)) {
            this.startSchedule(sender);
            return true;
        }

        if (action.equals("stop") && validationManager.hasPermission(scheduleStopPerm, sender)) {
            this.stopSchedule(sender);
            return true;
        }

        String scheduleName;
        try {
            scheduleName = args[2];
            if (!validationManager.scheduleExist(scheduleName, sender)) {
                return true;
            }
        } catch (IndexOutOfBoundsException e) {
            scheduleName = null;
        }

        if (action.equals("view") && sender.hasPermission(scheduleViewAllPerm)) {
            MessageManager.sendSchedules(sender, "schedule-view", ScheduleManager.getSchedules(this.main.getScheduleConfig()), "all", scheduleName);
            return true;
        }

        if (action.equals("view") && sender.hasPermission(scheduleViewEnabledPerm)
                && validationManager.hasPermission(scheduleViewDisabledPerm, sender)) {
            MessageManager.sendSchedules(sender, "schedule-view", ScheduleManager.getSchedules(this.main.getScheduleConfig()), "all", scheduleName);
            return true;
        }

        if (action.equals("view") && sender.hasPermission(scheduleViewEnabledPerm)) {
            MessageManager.sendSchedules(sender, "schedule-view", ScheduleManager.getSchedules(this.main.getScheduleConfig()), "enabled", scheduleName);
            return true;
        }

        if (action.equals("view") && validationManager.hasPermission(scheduleViewDisabledPerm, sender)) {
            MessageManager.sendSchedules(sender, "schedule-view", ScheduleManager.getSchedules(this.main.getScheduleConfig()), "disabled", scheduleName);
            return true;
        }

        if (action.equals("view")) {
            return true;
        }

        MessageManager.sendMessage(sender, "invalid-syntax");
        return true;
    }

    /**
     * Checks and tells the ScheduleManager to start running schedules.
     *
     * @param sender user who sent the command
     */
    public void startSchedule(CommandSender sender) {
        if (main.getStorageManager().isLoading()) {
            MessageManager.sendMessage(sender, "player-load-in-progress");
            return;
        }

        if (ScheduleManager.getIsRunning()) {
            MessageManager.sendMessage(sender, "schedule-already-running");
            return;
        }

        if (ScheduleManager.getIsEnabled()) {
            ScheduleManager.runScheduleCheck(this.main, this.main.getScheduleConfig(), false, sender);
        } else {
            MessageManager.sendMessage(sender, "schedule-disabled");
        }
    }

    /**
     * Checks and tells the ScheduleManager to stop running schedules.
     *
     * @param sender user who sent the command
     */
    public void stopSchedule(CommandSender sender) {
        if (main.getStorageManager().isLoading()) {
            MessageManager.sendMessage(sender, "player-load-in-progress");
            return;
        }

        if (ScheduleManager.getIsRunning()) {
            ScheduleManager.stopAllSchedules(sender);
        } else {
            MessageManager.sendMessage(sender, "schedule-not-running");
        }
    }
}

