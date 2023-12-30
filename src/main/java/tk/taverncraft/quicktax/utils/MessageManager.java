package tk.taverncraft.quicktax.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.util.ChatPaginator;

import static org.bukkit.util.ChatPaginator.GUARANTEED_NO_WRAP_CHAT_PAGE_WIDTH;

/**
 * MessageManager handles all formatting and sending of messages to the command sender.
 */
public class MessageManager {
    private static final HashMap<String, String> messageKeysMap = new HashMap<>();

    private static String completeLeaderboard;

    /**
     * Sets the messages to use.
     *
     * @param lang the configuration to base the messages on
     */
    public static void setMessages(FileConfiguration lang) {
        Set<String> messageKeysSet = lang.getConfigurationSection("").getKeys(false);

        for (String messageKey : messageKeysSet) {
            messageKeysMap.put(messageKey, formatMessageColor(lang.get(messageKey).toString() + " "));
        }
    }

    /**
     * Sends message to the sender.
     *
     * @param sender sender to send message to
     * @param messageKey key to get message with
     */
    public static void sendMessage(CommandSender sender, String messageKey) {
        String message = getMessage(messageKey);
        sender.sendMessage(message);
    }

    /**
     * Sends message to the sender, replacing placeholders.
     *
     * @param sender sender to send message to
     * @param messageKey key to get message with
     * @param keys placeholder keys
     * @param values placeholder values
     */
    public static void sendMessage(CommandSender sender, String messageKey, String[] keys, String[] values) {
        String message = getMessage(messageKey);
        for (int i = 0; i < keys.length; i++) {
            message = message.replaceAll(keys[i], values[i]);
        }
        sender.sendMessage(message);
    }

    /**
     * Prepares schedules to the sender, replacing placeholders
     *
     * @param sender sender to send message to
     * @param messageKey key to get message with
     * @param schedules schedules to print view for
     * @param type type of schedules to send
     * @param scheduleName name of schedule if specified
     */
    public static void sendSchedules(CommandSender sender, String messageKey, ConcurrentHashMap<String, Schedule> schedules,
             String type, String scheduleName) {
        try {
            if (type.equals("all")) {
                if (scheduleName == null) {
                    sendAll(sender, messageKey, schedules);
                } else {
                    ConcurrentHashMap<String, Schedule> tempSchedule = new ConcurrentHashMap<>();
                    tempSchedule.put(scheduleName, ScheduleManager.getSchedule(scheduleName));
                    sendAll(sender, messageKey, tempSchedule);
                }
            } else if (type.equals("enabled")) {
                if (scheduleName == null) {
                    sendEnabled(sender, messageKey, schedules);
                } else {
                    ConcurrentHashMap<String, Schedule> tempSchedule = new ConcurrentHashMap<>();
                    tempSchedule.put(scheduleName, ScheduleManager.getSchedule(scheduleName));
                    sendEnabled(sender, messageKey, tempSchedule);
                }
            } else {
                if (scheduleName == null) {
                    sendDisabled(sender, messageKey, schedules);
                } else {
                    ConcurrentHashMap<String, Schedule> tempSchedule = new ConcurrentHashMap<>();
                    tempSchedule.put(scheduleName, ScheduleManager.getSchedule(scheduleName));
                    sendDisabled(sender, messageKey, tempSchedule);
                }
            }
        } catch (NullPointerException e) {
            MessageManager.sendMessage(sender, "schedule-not-exist");
        }
    }

    /**
     * Retrieves message value given the message key.
     *
     * @param messageKey key to retrieve message with
     */
    public static String getMessage(String messageKey) {
        String prefix = messageKeysMap.get("prefix");
        return prefix.substring(0, prefix.length() - 1) + messageKeysMap.get(messageKey);
    }

    /**
     * Sends all schedules.
     *
     * @param sender sender to send message to
     * @param messageKey key to get message with
     * @param schedules schedules to print view for
     */
    private static void sendAll(CommandSender sender, String messageKey, ConcurrentHashMap<String, Schedule> schedules) throws NullPointerException {
        String messageTemplate = getMessage(messageKey);
        StringBuilder message = new StringBuilder();
        for (Schedule schedule : schedules.values()) {
            message.append(messageTemplate);
            message = new StringBuilder(message.toString().replaceAll("%schedulename%", schedule.getName())
                    .replaceAll("%enabled%", String.valueOf(schedule.isEnabled()))
                    .replaceAll("%fixedstarttime%", String.valueOf(schedule.getStartFromFixedTime()))
                    .replaceAll("%timezone%", schedule.getTimezoneInGMT())
                    .replaceAll("%hour%", String.valueOf(schedule.getHour()))
                    .replaceAll("%minute%", String.valueOf(schedule.getMinute()))
                    .replaceAll("%second%", String.valueOf(schedule.getSecond()))
                    .replaceAll("%frequency%", String.valueOf(schedule.getFrequency()))
                    .replaceAll("%type%", schedule.getType())
                    .replaceAll("%nextruntime%", schedule.getNextRunTime()));
            message.append("\n\n");
        }
        sender.sendMessage(message.toString());
    }

    /**
     * Sends enabled schedules.
     *
     * @param sender sender to send message to
     * @param messageKey key to get message with
     * @param schedules schedules to print view for
     */
    private static void sendEnabled(CommandSender sender, String messageKey, ConcurrentHashMap<String, Schedule> schedules) throws NullPointerException {
        String messageTemplate = getMessage(messageKey);
        StringBuilder message = new StringBuilder();
        for (Schedule schedule : schedules.values()) {
            if (schedule.isEnabled()) {
                message.append(messageTemplate);
                message = new StringBuilder(message.toString().replaceAll("%schedulename%", schedule.getName())
                        .replaceAll("%enabled%", String.valueOf(schedule.isEnabled()))
                        .replaceAll("%fixedstarttime%", String.valueOf(schedule.getStartFromFixedTime()))
                        .replaceAll("%timezone%", schedule.getTimezoneInGMT())
                        .replaceAll("%hour%", String.valueOf(schedule.getHour()))
                        .replaceAll("%minute%", String.valueOf(schedule.getMinute()))
                        .replaceAll("%second%", String.valueOf(schedule.getSecond()))
                        .replaceAll("%frequency%", String.valueOf(schedule.getFrequency()))
                        .replaceAll("%type%", schedule.getType())
                        .replaceAll("%nextruntime%", schedule.getNextRunTime()));
                message.append("\n\n");
            }
        }
        sender.sendMessage(message.toString());
    }

    /**
     * Sends disabled schedules.
     *
     * @param sender sender to send message to
     * @param messageKey key to get message with
     * @param schedules schedules to print view for
     */
    private static void sendDisabled(CommandSender sender, String messageKey, ConcurrentHashMap<String, Schedule> schedules) throws NullPointerException {
        String messageTemplate = getMessage(messageKey);
        StringBuilder message = new StringBuilder();
        for (Schedule schedule : schedules.values()) {
            if (!schedule.isEnabled()) {
                message.append(messageTemplate);
                message = new StringBuilder(message.toString().replaceAll("%schedulename%", schedule.getName())
                        .replaceAll("%enabled%", String.valueOf(schedule.isEnabled()))
                        .replaceAll("%fixedstarttime%", String.valueOf(schedule.getStartFromFixedTime()))
                        .replaceAll("%timezone%", schedule.getTimezoneInGMT())
                        .replaceAll("%hour%", String.valueOf(schedule.getHour()))
                        .replaceAll("%minute%", String.valueOf(schedule.getMinute()))
                        .replaceAll("%second%", String.valueOf(schedule.getSecond()))
                        .replaceAll("%frequency%", String.valueOf(schedule.getFrequency()))
                        .replaceAll("%type%", schedule.getType())
                        .replaceAll("%nextruntime%", schedule.getNextRunTime()));
                message.append("\n\n");
            }
        }
        sender.sendMessage(message.toString());
    }

    /**
     * Shows help menu to the user.
     *
     * @param sender sender to send message to
     * @param pageNum page number to view
     */
    public static void showHelpBoard(CommandSender sender, int pageNum) {
        int linesPerPage = 12;
        int positionsPerPage = 10;

        String header = getMessage("help-header");
        String footer = messageKeysMap.get("help-footer");
        String[] messageBody = messageKeysMap.get("help-body").split("\n", -1);
        StringBuilder message = new StringBuilder(header + "\n");
        int position = 1;
        int currentPage = 1;
        for (String body : messageBody) {
            message.append(body).append("\n");
            if (position % positionsPerPage == 0) {
                currentPage++;
                message = new StringBuilder(message.append(footer).append("\n").toString().replaceAll("%page%", String.valueOf(currentPage)));
                message.append(header).append("\n");
            }
            position++;
        }

        ChatPaginator.ChatPage page = ChatPaginator.paginate(message.toString(), pageNum, GUARANTEED_NO_WRAP_CHAT_PAGE_WIDTH, linesPerPage);
        for (String line : page.getLines()) {
            sender.sendMessage(line);
        }
    }

    /**
     * Shows leaderboard to the user.
     *
     * @param sender sender to send message to
     * @param pageNum page number of leaderboard
     */
    public static void showLeaderboard(CommandSender sender, int pageNum) {
        if (completeLeaderboard == null) {
            sendMessage(sender, "no-updated-leaderboard");
            return;
        }

        int linesPerPage = 12;
        ChatPaginator.ChatPage page = ChatPaginator.paginate(completeLeaderboard, pageNum, GUARANTEED_NO_WRAP_CHAT_PAGE_WIDTH, linesPerPage);
        for (String line : page.getLines()) {
            sender.sendMessage(line);
        }
    }

    /**
     * Sets up message for leaderboard beforehand to improve performance.
     *
     * @param leaderboard hashmap of leaderboard positions
     */
    public static void setUpLeaderboard(HashMap<UUID, Double> leaderboard) {
        int positionsPerPage = 10;

        String header = getMessage("leaderboard-header");
        String footer = messageKeysMap.get("leaderboard-footer");
        String messageTemplate = messageKeysMap.get("leaderboard-body");
        StringBuilder message = new StringBuilder(header);
        int position = 1;
        int currentPage = 1;

        for (HashMap.Entry<UUID, Double> entry : leaderboard.entrySet()) {
            UUID uuid = entry.getKey();
            OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);

            // handle null player names (happens if world folder is deleted)
            if (player.getName() == null) {
                continue;
            }

            double totalTaxPaid = entry.getValue();
            message.append(messageTemplate);
            message = new StringBuilder(message.toString().replaceAll("%num%", String.valueOf(position))
                    .replaceAll("%player%", player.getName())
                    .replaceAll("%totaltaxpaid%", new BigDecimal(totalTaxPaid).setScale(2, RoundingMode.CEILING).toPlainString()));
            if (position % positionsPerPage == 0) {
                currentPage++;
                message = new StringBuilder(message.append(footer).toString().replaceAll("%page%", String.valueOf(currentPage)));
                message.append(header);
            }
            position++;
        }

        completeLeaderboard = message.toString();
    }

    /**
     * Resets leaderboard.
     */
    public static void resetLeaderboard() {
        completeLeaderboard = null;
    }

    /**
     * Gets the formatting for the leaderboard sign.
     *
     * @param keys placeholder keys
     * @param values placeholder values
     */
    public static String getSignFormat(String[] keys, String[] values) {
        String message = messageKeysMap.get("leaderboard-sign");
        for (int i = 0; i < keys.length; i++) {
            message = message.replaceAll(keys[i], values[i]);
        }
        return message;
    }

    /**
     * Formats color in chat messages.
     *
     * @param message message to format
     */
    private static String formatMessageColor(String message) {
        Pattern pattern = Pattern.compile("(?<!\\\\)#[a-fA-F0-9]{6}");
        Matcher matcher = pattern.matcher(message);
        while (matcher.find()) {
            String hexCode = message.substring(matcher.start(), matcher.end());
            String replaceSharp = hexCode.replace('#', 'x');

            char[] ch = replaceSharp.toCharArray();
            StringBuilder builder = new StringBuilder("");
            for (char c : ch) {
                builder.append("&" + c);
            }

            message = message.replace(hexCode, builder.toString());
            matcher = pattern.matcher(message);
        }

        message = message.replace("\\#", "#");
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
