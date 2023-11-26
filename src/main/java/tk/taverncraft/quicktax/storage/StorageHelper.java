package tk.taverncraft.quicktax.storage;

import java.util.UUID;

/**
 * Interface to determine what type of storage to use.
 */
public interface StorageHelper {

    /**
     * Saves information to storage.
     *
     * @param uuid uuid of player
     * @param lastTaxPaid last tax paid by player
     */
    void saveToStorage (UUID uuid, double lastTaxPaid);

    /**
     * Gets user values from database.
     */
    void getFromDatabase();

    /**
     * Inserts user values into database.
     */
    void insertIntoDatabase(String values);

    /**
     * Inserts query into database.
     */
    void insertIntoDatabase();
}

