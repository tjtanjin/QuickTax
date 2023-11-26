package tk.taverncraft.quicktax.storage;

import java.util.UUID;

import tk.taverncraft.quicktax.Main;

/**
 * Empty helper to handle cases where no storage is required.
 */
public class NoneHelper implements StorageHelper {
    Main main;

    /**
     * Constructor for NoneHelper.
     */
    public NoneHelper(Main main) {
        this.main = main;
    }

    public void saveToStorage(UUID uuid, double amount) {}

    public void getFromDatabase() {}

    public void insertIntoDatabase(String values) {}

    public void insertIntoDatabase() {}
}
