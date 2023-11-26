package tk.taverncraft.quicktax.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Calendar;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitTask;

import tk.taverncraft.quicktax.Main;

/**
 * ScheduleManager is in charge of loading and unloading schedules for runs.
 */
public class ScheduleManager {

    private static final ArrayList<BukkitTask> activeScheduleTasks = new ArrayList<>();
    public static ConcurrentHashMap<String, Schedule> schedules = null;
    public static final HashMap<String, String> timezoneMap = new HashMap<>();
    private static boolean isEnabled;
    private static boolean isRunning = false;

    // timezone mappings
    static {
        timezoneMap.put("GMT-12", "Etc/GMT+12");
        timezoneMap.put("GMT-11", "Etc/GMT+11");
        timezoneMap.put("GMT-10", "Etc/GMT+10");
        timezoneMap.put("GMT-9", "Etc/GMT+9");
        timezoneMap.put("GMT-8", "Etc/GMT+8");
        timezoneMap.put("GMT-7", "Etc/GMT+7");
        timezoneMap.put("GMT-6", "Etc/GMT+6");
        timezoneMap.put("GMT-5", "Etc/GMT+5");
        timezoneMap.put("GMT-4", "Etc/GMT+4");
        timezoneMap.put("GMT-3", "Etc/GMT+3");
        timezoneMap.put("GMT-2", "Etc/GMT+2");
        timezoneMap.put("GMT-1", "Etc/GMT+1");
        timezoneMap.put("GMT+0", "Etc/GMT+0");
        timezoneMap.put("GMT+1", "Etc/GMT-1");
        timezoneMap.put("GMT+2", "Etc/GMT-2");
        timezoneMap.put("GMT+3", "Etc/GMT-3");
        timezoneMap.put("GMT+4", "Etc/GMT-4");
        timezoneMap.put("GMT+5", "Etc/GMT-5");
        timezoneMap.put("GMT+6", "Etc/GMT-6");
        timezoneMap.put("GMT+7", "Etc/GMT-7");
        timezoneMap.put("GMT+8", "Etc/GMT-8");
        timezoneMap.put("GMT+9", "Etc/GMT-9");
        timezoneMap.put("GMT+10", "Etc/GMT-10");
        timezoneMap.put("GMT+11", "Etc/GMT-11");
        timezoneMap.put("GMT+12", "Etc/GMT-12");
    }

    /**
     * Checks if schedules are globally enabled and if so attempt to load schedules. Also checks if schedules are
     * supposed to run on start or reload.
     *
     * @param main plugin class
     * @param scheduleConfig configuration for schedule
     * @param isStartOrReload indicates if this is being run on start or reload
     */
    public static void runScheduleCheck(Main main, FileConfiguration scheduleConfig, boolean isStartOrReload, CommandSender sender) {
        isEnabled = scheduleConfig.getBoolean(("enabled"));
        if (isStartOrReload && !scheduleConfig.getBoolean("autostart")) {
            return;
        }

        if (isEnabled) {
            loadSchedules(main, scheduleConfig);
            MessageManager.sendMessage(sender, "schedule-run");
            isRunning = true;
        }
    }

    /**
     * Schedules a task to be repeated at intervals.
     *
     * @param main plugin class
     * @param task the task to repeat
     * @param schedule schedule to schedule task for
     */
    private static BukkitTask scheduleRepeatAtTime(Main main, Runnable task, Schedule schedule) {

        int frequency = schedule.getFrequency();
        String timezone = schedule.getTimezone();
        boolean startFromFixedTime = schedule.getStartFromFixedTime();

        if (!startFromFixedTime) {
            // get new schedule time
            return scheduleNewTime(main, task, schedule);
        }
        // check if next scheduled time is valid
        String scheduledTime = schedule.getValidScheduledTime(main);
        if (scheduledTime != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy | HH:mm:ss");
            sdf.setTimeZone(TimeZone.getTimeZone(timezone));
            try {
                Calendar cal = Calendar.getInstance();
                cal.setTimeZone(TimeZone.getTimeZone(timezone));
                long now = cal.getTimeInMillis();
                cal.setTime(sdf.parse(scheduledTime));
                long ticks = (cal.getTimeInMillis() - now) / 50L;
                if (ticks > 0) {
                    long interval = frequency * 20L;
                    schedule.populateRunTimeToView(sdf, scheduledTime);
                    return Bukkit.getScheduler().runTaskTimerAsynchronously(main, task, ticks, interval);
                }
            } catch (ParseException e) {
                // invalid time, generate new timing
            }
        }

        // get new schedule time
        return scheduleNewTime(main, task, schedule);
    }

    /**
     * Schedules a new timing to base schedule on.
     *
     * @param main plugin class
     * @param task the task to repeat
     * @param schedule schedule to schedule task for
     */
    private static BukkitTask scheduleNewTime(Main main, Runnable task, Schedule schedule) {
        int hour = schedule.getHour();
        int minute = schedule.getMinute();
        int second = schedule.getSecond();
        int frequency = schedule.getFrequency();
        String timezone = schedule.getTimezone();
        boolean startFromFixedTime = schedule.getStartFromFixedTime();

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy | HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone(timezone));
        Calendar cal = Calendar.getInstance();
        long ticks;
        cal.setTimeZone(TimeZone.getTimeZone(timezone));
        long now = cal.getTimeInMillis();

        // logic to check if schedule should run 1 day later
        if (cal.get(Calendar.HOUR_OF_DAY) > hour) {
            cal.add(Calendar.DATE, 1);
        } else if (cal.get(Calendar.HOUR_OF_DAY) == hour) {
            if (cal.get(Calendar.MINUTE) > minute) {
                cal.add(Calendar.DATE, 1);
            } else if (cal.get(Calendar.MINUTE) == minute) {
                if (cal.get(Calendar.SECOND) >= second) {
                    cal.add(Calendar.DATE, 1);
                }
            }
        }

        // set next run time
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, second);
        cal.set(Calendar.MILLISECOND, 0);
        if (startFromFixedTime) {
            ticks = (cal.getTimeInMillis() - now) / 50L;
        } else {
            ticks = 0;
        }
        long interval = frequency * 20L;
        schedule.populateRunTimeToView(sdf, sdf.format(cal.getTime()));
        return Bukkit.getScheduler().runTaskTimerAsynchronously(main, task, ticks, interval);
    }

    /**
     * Get all schedules and set them up.
     *
     * @param main plugin class
     * @param scheduleConfig configuration for schedule
     */
    private static void loadSchedules(Main main, FileConfiguration scheduleConfig) {
        ConcurrentHashMap<String, Schedule> schedules = getSchedules(scheduleConfig);
        if (schedules == null) {
            return;
        }

        for (Schedule schedule : schedules.values()) {
            if (schedule.isEnabled()) {
                setUpSchedule(main, schedule);
            }
        }
    }

    /**
     * Sets up a schedule and adds its id for tracking.
     *
     * @param main plugin class
     * @param schedule schedule to set up and track
     */
    private static void setUpSchedule(Main main, Schedule schedule) {
        BukkitTask task = scheduleRepeatAtTime(main, () -> schedule.run(main), schedule);
        activeScheduleTasks.add(task);
    }

    /**
     * Gets all schedules.
     *
     * @param scheduleConfig configuration for schedule
     */
    public static ConcurrentHashMap<String, Schedule> getSchedules(FileConfiguration scheduleConfig) {
        if (schedules == null) {
            schedules = new ConcurrentHashMap<>();
        } else {
            return schedules;
        }

        Set<String> scheduleNames;
        try {
            scheduleNames = scheduleConfig.getConfigurationSection("schedules").getKeys(false);
        } catch (NullPointerException e) {
            return null;
        }

        for (String scheduleName : scheduleNames) {
            Schedule schedule = Schedule.getSchedule(scheduleConfig, scheduleName, "schedules." + scheduleName);
            if (schedule != null) {
                schedules.put(scheduleName, schedule);
            } else {
                MessageManager.sendMessage(Bukkit.getConsoleSender(), "invalid-schedule");
            }
        }
        return schedules;
    }

    /**
     * Stops all running schedules.
     */
    public static void stopAllSchedules(CommandSender sender) {
        for (BukkitTask task : activeScheduleTasks) {
            task.cancel();
        }
        if (isRunning) {
            MessageManager.sendMessage(sender, "schedule-stop");
        }
        schedules = null;
        isRunning = false;
    }

    public static boolean getIsRunning() {
        return isRunning;
    }

    public static boolean getIsEnabled() {
        return isEnabled;
    }

    public static Schedule getSchedule(String scheduleName) {
        if (schedules == null) {
            return null;
        }
        return schedules.get(scheduleName);
    }
}
