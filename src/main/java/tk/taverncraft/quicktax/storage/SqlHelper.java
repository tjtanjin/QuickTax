package tk.taverncraft.quicktax.storage;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.util.HashMap;
import java.util.UUID;

import tk.taverncraft.quicktax.Main;

/**
 * SqlHelper is responsible for reading/writing from MySQL database.
 */
public class SqlHelper implements StorageHelper {
    Main main;
    String tableName;
    public static String query = "";

    /**
     * Constructor for SqlHelper.
     */
    public SqlHelper(Main main) {
        this.main = main;
    }

    /**
     * Updates the stats of a player into an sql query for insertion into sql.
     *
     * @param uuid uuid of player to update
     * @param latestTaxPaid the latest tax amount paid
     */
    public void saveToStorage(UUID uuid, double latestTaxPaid) {
        SqlHelper.query += "('" + uuid + "', '" + latestTaxPaid + "', '" + main.getStatsManager().getPlayerTotalTaxPaidCache(uuid) + "'), ";
    }

    /**
     * Connects to MySQL database.
     */
    public Connection connectToSql() {
        try {
            Connection conn;
            String dbName = main.getConfig().getString("database-name", "quicktax");
            String tableName = main.getConfig().getString("table-name", "quicktax");
            String port = main.getConfig().getString("port", "3306");
            String url = "jdbc:mysql://" + main.getConfig().getString("host") + ":" + port + "/" + dbName + "?useSSL=false";
            String user = main.getConfig().getString("user", "quicktax");
            String password = main.getConfig().getString("password");

            conn = DriverManager.getConnection(url, user, password);

            if (!databaseExists(dbName, conn)) {
                return null;
            }

            if (!tableExists(tableName, conn)) {
                String query = "CREATE TABLE " + tableName + "("
                        + "UUID VARCHAR (36) NOT NULL, "
                        + "LATEST_TAX_PAID DECIMAL (18, 2), "
                        + "TOTAL_TAX_PAID DECIMAL (18, 2), "
                        + "PRIMARY KEY (UUID))";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.executeUpdate();
                stmt.close();
            }
            return conn;

        } catch (SQLException e){
            main.getLogger().warning(e.getMessage());
            return null;
        }
    }

    /**
     * Checks if database exist.
     *
     * @param dbName name of database
     * @param conn an open connection
     */
    public boolean databaseExists(String dbName, Connection conn) throws SQLException {
        ResultSet rs;
        if (conn != null) {

            rs = conn.getMetaData().getCatalogs();

            while (rs.next()) {
                String catalogs = rs.getString(1);

                if (dbName.equals(catalogs)) {
                    main.getLogger().info("The database " + dbName + " has been found.");
                    return true;
                }
            }

        } else {
            main.getLogger().info("Unable to connect to database.");
        }
        return false;
    }

    /**
     * Checks if table exist and create if not.
     *
     * @param tableName name of table
     * @param conn an open connection
     */
    public boolean tableExists(String tableName, Connection conn) throws SQLException {
        boolean found = false;
        DatabaseMetaData databaseMetaData = conn.getMetaData();
        ResultSet rs = databaseMetaData.getTables(null, null, tableName, null);
        while (rs.next()) {
            String name = rs.getString("TABLE_NAME");
            if (tableName.equals(name)) {
                found = true;
                break;
            }
        }
        this.tableName = tableName;

        return found;
    }

    /**
     * Inserts user values into database.
     */
    public void insertIntoDatabase() {
        if (query.length() == 0) {
            return;
        }
        Connection conn = this.connectToSql();
        if (conn != null) {
            try {
                String header = "INSERT INTO " + tableName + "(UUID, LATEST_TAX_PAID, TOTAL_TAX_PAID) VALUES ";
                String footer = " ON DUPLICATE KEY UPDATE LATEST_TAX_PAID = VALUES(LATEST_TAX_PAID), TOTAL_TAX_PAID = VALUES(TOTAL_TAX_PAID)";
                String finalQuery = header + query.substring(0, query.length() - 2) + footer;
                PreparedStatement stmt = conn.prepareStatement(finalQuery);
                stmt.executeUpdate();
                main.getLogger().info("SQL operation completed.");
                stmt.close();
                conn.close();
            } catch (SQLException e) {
                main.getLogger().warning(e.getMessage());
            }
        }
        query = "";
    }

    /**
     * Inserts user values into database.
     */
    public void insertIntoDatabase(String values) {
        if (values.length() == 0) {
            return;
        }
        Connection conn = this.connectToSql();
        if (conn != null) {
            try {
                String header = "INSERT INTO " + tableName + "(UUID, LATEST_TAX_PAID, TOTAL_TAX_PAID) VALUES ";
                String footer = " ON DUPLICATE KEY UPDATE LATEST_TAX_PAID = VALUES(LATEST_TAX_PAID), TOTAL_TAX_PAID = VALUES(TOTAL_TAX_PAID)";
                String finalQuery = header + values.substring(0, values.length() - 2) + footer;
                PreparedStatement stmt = conn.prepareStatement(finalQuery);
                stmt.executeUpdate();
                main.getLogger().info("SQL operation completed.");
                stmt.close();
                conn.close();
            } catch (SQLException e) {
                main.getLogger().warning(e.getMessage());
            }
        }
    }

    /**
     * Gets user values from database.
     */
    public void getFromDatabase() {
        Connection conn = this.connectToSql();
        if (conn != null) {
            HashMap<UUID, Double> tempLatestTaxPaidCache = new HashMap<>();
            HashMap<UUID, Double> tempTotalTaxPaidCache = new HashMap<>();
            try {
                Statement stmt = conn.createStatement();
                String query = "SELECT * from " + tableName;
                ResultSet rs = stmt.executeQuery(query);
                while (rs.next()) {
                    UUID uuid = UUID.fromString(rs.getString("UUID"));
                    tempLatestTaxPaidCache.put(uuid, new BigDecimal(rs.getDouble("LATEST_TAX_PAID")).setScale(2,
                            RoundingMode.HALF_UP).doubleValue());
                    tempTotalTaxPaidCache.put(uuid, new BigDecimal(rs.getDouble("TOTAL_TAX_PAID")).setScale(2,
                            RoundingMode.HALF_UP).doubleValue());
                }
                main.getLogger().info("SQL operation completed.");
                stmt.close();
                conn.close();
                main.getStatsManager().setLatestTaxPaidCache(tempLatestTaxPaidCache);
                main.getStatsManager().setTotalTaxPaidCache(tempTotalTaxPaidCache);
            } catch (SQLException e) {
                main.getLogger().warning(e.getMessage());
            }
        }
    }
}

