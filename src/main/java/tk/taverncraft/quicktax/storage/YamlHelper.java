package tk.taverncraft.quicktax.storage;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.bukkit.configuration.file.FileConfiguration;

import tk.taverncraft.quicktax.Main;

public class YamlHelper implements StorageHelper {
    Main main;

    public YamlHelper(Main main) {
        this.main = main;
    }

    /**
     * Updates the stats of a player into file.
     *
     * @param uuid uuid of player to save stats for
     * @param amount amount of tax paid
     */
    public void saveToStorage(UUID uuid, double amount) {
        FileConfiguration playerConfig = main.getStorageManager().getPlayerConfig(uuid);
        String playerFileName = uuid.toString();
        File playerFile = new File(this.main.getDataFolder() + "/playerData", playerFileName + ".yml");
        playerConfig.set("latest-tax-paid", amount);
        playerConfig.set("total-tax-paid", playerConfig.getInt("total-tax-paid") + amount);

        try {
            playerConfig.save(playerFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void getFromDatabase() {}

    public void insertIntoDatabase(String values) {}

    public void insertIntoDatabase() {}
}
