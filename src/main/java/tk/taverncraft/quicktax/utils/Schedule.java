package tk.taverncraft.quicktax.utils;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import me.clip.placeholderapi.PlaceholderAPI;

import tk.taverncraft.quicktax.Main;

/**
 * Schedule contains all information related to a single schedule required for starting, stopping and viewing.
 */
public class Schedule {
    private static final String[] allowedTypes = {"collectrank", "collectbal", "collectactivity", "collectall"};

    String name;
    boolean enabled;
    boolean startFromFixedTime;
    String timezone;
    String timezoneInGMT;
    int hour;
    int minute;
    int second;
    int frequency;
    String type;
    String nextRunTime = "None";
    double lastCollected = 0;
    boolean updateLeaderboardOnRun;
    List<String> commands;

    /**
     * Constructor for Schedule.
     */
    public Schedule(String name, boolean enabled, boolean startFromFixedTime, String timezone, String timezoneInGmt,
                    int hour, int minute, int second, int frequency, String type, boolean updateLeaderboardOnRun, List<String> commands) {
        this.name = name;
        this.enabled = enabled;
        this.startFromFixedTime = startFromFixedTime;
        this.timezone = timezone;
        this.timezoneInGMT = timezoneInGmt;
        this.hour = hour;
        this.minute = minute;
        this.second = second;
        this.frequency = frequency;
        this.type = type;
        this.updateLeaderboardOnRun = updateLeaderboardOnRun;
        this.commands = commands;
    }

    /**
     * Gets a schedule based on the given schedule name.
     *
     * @param schedule the schedule configuration
     * @param scheduleName the name of the schedule to get
     * @param schedulePath the path within the schedule configuration to get schedule for
     */
    public static Schedule getSchedule(FileConfiguration schedule, String scheduleName, String schedulePath) {
        boolean enabled;
        boolean startFromFixedTime;
        String timezone;
        String timezoneInGmt;
        int hour;
        int minute;
        int second;
        int frequency;
        String type;
        boolean updateLeaderboardOnRun;
        List<String> commands;

        try {
            enabled = schedule.getBoolean(schedulePath + ".enabled");
            startFromFixedTime = schedule.getBoolean(schedulePath + ".startFromFixedTime");
            timezoneInGmt = schedule.getString(schedulePath + ".timezone");
            timezone = ScheduleManager.timezoneMap.get(timezoneInGmt);
            hour = schedule.getInt(schedulePath + ".hour");
            minute = schedule.getInt(schedulePath + ".minute");
            second = schedule.getInt(schedulePath + ".second");
            frequency = schedule.getInt(schedulePath + ".frequency");
            type = schedule.getString(schedulePath + ".type");
            updateLeaderboardOnRun = schedule.getBoolean(schedulePath + ".update-leaderboard-on-run", false);
            commands = schedule.getStringList(schedulePath + ".commands");
        } catch (Exception e) {
            return null;
        }

        if (!isValidScheduleFields(timezone, hour, minute, second, type)) {
            return null;
        }

        return new Schedule(scheduleName, enabled, startFromFixedTime, timezone, timezoneInGmt, hour, minute,
                second, frequency, type, updateLeaderboardOnRun, commands);

    }

    /**
     * Checks if a schedule has valid fields.
     *
     * @param timezone timezone to base schedule on
     * @param hour hour to set schedule with
     * @param minute minute to set schedule with
     * @param second second to set schedule with
     * @param type type of collection to run
     */
    private static boolean isValidScheduleFields(String timezone, int hour, int minute, int second, String type) {
        if (timezone == null) {
            return false;
        }
        if (hour > 23 || hour < 0 || minute > 59 || minute < 0 || second > 69 || second < 0) {
            return false;
        }
        return Arrays.asList(allowedTypes).contains(type);
    }

    /**
     * Runs the tax collection.
     *
     * @param main plugin class
     */
    public void run(Main main) {
        if (main.getStorageManager().isLoading()) {
            main.getLogger().info("Scheduled tax collection could not be " +
                    "carried out because player data is still being loaded.");
            return;
        }

        if (TaxManager.isCollecting) {
            main.getLogger().info("Scheduled tax collection could not be " +
                    "carried out because an existing collection is in progress.");
            return;
        }

        TaxManager taxManager = new TaxManager(main);
        if (this.type.equals("collectall")) {
            taxManager.collectAll(Bukkit.getConsoleSender());
        } else if (this.type.equals("collectrank")) {
            taxManager.collectRank(Bukkit.getConsoleSender());
        } else if (this.type.equals("collectactivity")) {
            taxManager.collectActivity(Bukkit.getConsoleSender());
        } else {
            taxManager.collectBal(Bukkit.getConsoleSender());
        }
        this.updateScheduleInfo(main, taxManager.getTotalTaxCollected());
        this.runPostCollectionCommands(main);
        if (updateLeaderboardOnRun) {
            main.getStatsManager().manualUpdateLeaderboard(Bukkit.getConsoleSender());
        }
    }

    /**
     * Updates the next run time of the schedule.
     *
     */
    public String calculateNextRunTime() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.SECOND, frequency);
        Date date = cal.getTime();
        DateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy | HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone(timezone));
        return sdf.format(date);
    }

    public void updateScheduleInfo(Main main, double totalTaxCollected) {
        String calculatedTime = calculateNextRunTime();
        this.nextRunTime = calculatedTime;
        this.saveScheduleInfoToFile(main, this, this.nextRunTime, totalTaxCollected);
    }

    /**
     * Populates the runtime to schedule view.
     *
     * @param sdf datetime formatter
     * @param scheduledTime time for next run
     */
    public void populateRunTimeToView(SimpleDateFormat sdf, String scheduledTime) {
        try {
            this.nextRunTime = sdf.format(sdf.parse(scheduledTime));
        } catch (Exception e) {
            this.nextRunTime = "None";
        }
    }

    /**
     * Sets the last tax collected for this schedule.
     */
    public void populateLastCollected(Main main) {
        File scheduleTimeFile = new File(main.getDataFolder() + "/schedules", this.getName() + ".yml");
        if (!scheduleTimeFile.exists()) {
            return;
        }

        FileConfiguration scheduleTimeConfig = new YamlConfiguration();
        try {
            scheduleTimeConfig.load(scheduleTimeFile);
            this.lastCollected = scheduleTimeConfig.getDouble("last-collected", 0);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    /**
     * Uniquely identifies a schedule by its fields.
     */
    public String getUniqueIdentifier() {

        return this.name +
                this.enabled +
                this.startFromFixedTime +
                this.timezone +
                this.timezoneInGMT +
                this.hour +
                this.minute +
                this.second +
                this.frequency +
                this.type;
    }

    /**
     * Checks if a schedule still has a valid next time (no longer valid if schedule was edited).
     *
     * @param main plugin class
     */
    public String getValidScheduledTime(Main main) {
        File scheduleTimeFile = new File(main.getDataFolder() + "/schedules", this.getName() + ".yml");
        if (!scheduleTimeFile.exists()) {
            return null;
        }

        FileConfiguration scheduleTimeConfig = new YamlConfiguration();
        try {
            scheduleTimeConfig.load(scheduleTimeFile);
            return scheduleTimeConfig.getString(this.getUniqueIdentifier());
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Updates schedule time file and creates it if it does not exist.
     *
     * @param main plugin instance
     * @param schedule schedule to update time in file for
     * @param time time for schedule's next run
     * @param totalTaxCollected the tax collected for this schedule run
     */
    public void saveScheduleInfoToFile(Main main, Schedule schedule, String time, double totalTaxCollected) {
        File scheduleInfoFile = new File(main.getDataFolder() + "/schedules", schedule.getName() + ".yml");
        FileConfiguration scheduleInfoConfig = new YamlConfiguration();
        if (!scheduleInfoFile.exists()) {
            scheduleInfoFile.getParentFile().mkdirs();
            String identifier = schedule.getUniqueIdentifier();
            scheduleInfoConfig.set(identifier, time);
            try {
                scheduleInfoConfig.save(scheduleInfoFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            String identifier = schedule.getUniqueIdentifier();
            scheduleInfoConfig.set(identifier, time);
            scheduleInfoConfig.set("last-collected", totalTaxCollected);
            scheduleInfoConfig.save(scheduleInfoFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Runs post-collection commands after replacing placeholders.
     *
     * @param main plugin instance
     */
    private void runPostCollectionCommands(Main main) {
        for (String command : commands) {
            String parsedCommand = PlaceholderAPI.setPlaceholders(null, command);
            new BukkitRunnable() {
                @Override
                public void run() {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), parsedCommand);
                }
            }.runTask(main);
        }
    }

    /**
     * Checks if a schedule is enabled.
     */
    public boolean isEnabled() {
        return this.enabled;
    }

    public String getName() {
        return this.name;
    }

    public boolean getStartFromFixedTime() {
        return startFromFixedTime;
    }

    public String getTimezone() {
        return this.timezone;
    }

    public String getTimezoneInGMT() {
        return this.timezoneInGMT;
    }

    public int getHour() {
        return this.hour;
    }

    public int getMinute() {
        return this.minute;
    }

    public int getSecond() {
        return this.second;
    }

    public int getFrequency() {
        return this.frequency;
    }

    public String getType() {
        return this.type;
    }

    public String getNextRunTime() {
        return this.nextRunTime;
    }

    public double getLastCollected() {
        return this.lastCollected;
    }
}