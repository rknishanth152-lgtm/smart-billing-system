package com.smartbilling.database;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * DatabaseConnection
 * 
 * Provides centralized JDBC connection management for MySQL.
 * Includes built-in defaults, optional external configuration overrides,
 * and automatic conservative database initialization for clean deployment.
 */
public class DatabaseConnection {

    // Built-in Default Configuration Parameters
    private static String host = "localhost";
    private static String port = "3306";
    private static String dbName = "smart_billing_db";
    private static String user = "root";
    private static String password = "root123";

    private static String url;
    private static String serverUrl;

    private static volatile boolean initialized = false;

    static {
        try {
            // Load MySQL JDBC Driver class dynamically
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("[ERROR] MySQL JDBC Driver not found in classpath!");
            e.printStackTrace();
        }

        loadConfiguration();
    }

    /**
     * Loads optional external configuration from config.properties or environment variables,
     * falling back to default values if not present.
     */
    private static void loadConfiguration() {
        File configFile = new File("config.properties");
        if (configFile.exists() && configFile.isFile()) {
            try (InputStream input = new FileInputStream(configFile)) {
                Properties prop = new Properties();
                prop.load(input);
                if (prop.getProperty("db.host") != null) host = prop.getProperty("db.host").trim();
                if (prop.getProperty("db.port") != null) port = prop.getProperty("db.port").trim();
                if (prop.getProperty("db.name") != null) dbName = prop.getProperty("db.name").trim();
                if (prop.getProperty("db.user") != null) user = prop.getProperty("db.user").trim();
                if (prop.getProperty("db.password") != null) password = prop.getProperty("db.password").trim();
                System.out.println("[INFO] Loaded database configuration from config.properties");
            } catch (Exception e) {
                System.err.println("[WARNING] Could not read config.properties, using default connection settings.");
            }
        } else {
            if (System.getenv("DB_HOST") != null) host = System.getenv("DB_HOST");
            if (System.getenv("DB_PORT") != null) port = System.getenv("DB_PORT");
            if (System.getenv("DB_NAME") != null) dbName = System.getenv("DB_NAME");
            if (System.getenv("DB_USER") != null) user = System.getenv("DB_USER");
            if (System.getenv("DB_PASSWORD") != null) password = System.getenv("DB_PASSWORD");
        }

        serverUrl = "jdbc:mysql://" + host + ":" + port + "/?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        url = "jdbc:mysql://" + host + ":" + port + "/" + dbName + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    }

    /**
     * Obtains a new active connection to the MySQL database.
     * Ensures database is initialized if necessary.
     * 
     * @return Connection object
     * @throws SQLException if a database access error occurs or credentials fail
     */
    public static Connection getConnection() throws SQLException {
        if (!initialized) {
            synchronized (DatabaseConnection.class) {
                if (!initialized) {
                    initializeDatabaseIfNeeded();
                    initialized = true;
                }
            }
        }
        return DriverManager.getConnection(url, user, password);
    }

    /**
     * Conservative auto-initialization check:
     * Checks if database and tables exist. If missing, runs packaged /schema.sql resource.
     */
    private static void initializeDatabaseIfNeeded() throws SQLException {
        boolean dbExists = false;
        boolean tablesExist = false;

        // 1. Check server connection and whether database exists
        try (Connection conn = DriverManager.getConnection(serverUrl, user, password);
             Statement stmt = conn.createStatement()) {
            
            try (ResultSet rs = stmt.executeQuery("SHOW DATABASES LIKE '" + dbName + "'")) {
                if (rs.next()) {
                    dbExists = true;
                }
            }
            if (!dbExists) {
                System.out.println("[INFO] Database '" + dbName + "' does not exist. Creating database...");
                stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + dbName);
                dbExists = true;
            }
        } catch (SQLException e) {
            System.err.println("[ERROR] Failed to connect to MySQL server at " + host + ":" + port);
            throw new SQLException("Could not connect to MySQL server at " + host + ":" + port 
                    + ". Please ensure MySQL service is running and credentials are valid.", e);
        }

        // 2. Check if tables exist in dbName
        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement()) {
            
            try (ResultSet rs = stmt.executeQuery("SHOW TABLES LIKE 'users'")) {
                if (rs.next()) {
                    tablesExist = true;
                }
            }

            if (!tablesExist) {
                System.out.println("[INFO] Tables missing in '" + dbName + "'. Initializing schema from classpath /schema.sql...");
                executeSchemaResource(conn);
                System.out.println("[INFO] Database schema and seed data initialized successfully.");
            } else {
                System.out.println("[INFO] Database '" + dbName + "' and existing tables verified cleanly.");
            }
        } catch (SQLException e) {
            System.err.println("[ERROR] Error inspecting or initializing database schema: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Reads /schema.sql resource from classpath and executes SQL statements.
     */
    private static void executeSchemaResource(Connection conn) throws SQLException {
        try (InputStream in = DatabaseConnection.class.getResourceAsStream("/schema.sql")) {
            if (in == null) {
                System.err.println("[WARNING] /schema.sql resource not found on classpath!");
                return;
            }
            
            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String trimmed = line.trim();
                    if (trimmed.startsWith("--") || trimmed.isEmpty()) {
                        continue;
                    }
                    sb.append(line).append("\n");
                }
            }

            String[] statements = sb.toString().split(";");
            try (Statement stmt = conn.createStatement()) {
                for (String sql : statements) {
                    String trimmedSql = sql.trim();
                    if (!trimmedSql.isEmpty()) {
                        stmt.execute(trimmedSql);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Exception executing schema initialization SQL: " + e.getMessage());
            if (e instanceof SQLException) {
                throw (SQLException) e;
            } else {
                throw new SQLException("Failed to load or execute /schema.sql resource", e);
            }
        }
    }

    /**
     * Utility method to test database connectivity.
     * 
     * @return String describing connection success or specific failure reason.
     */
    public static String testConnection() {
        try (Connection conn = getConnection()) {
            if (conn != null && !conn.isClosed()) {
                return "SUCCESS: Successfully connected to database '" + dbName + "' at " + host + ":" + port;
            }
        } catch (SQLException e) {
            return "FAILURE: Could not connect to MySQL database.\n"
                    + "Error Code: " + e.getErrorCode() + "\n"
                    + "SQL State: " + e.getSQLState() + "\n"
                    + "Message: " + e.getMessage();
        }
        return "FAILURE: Unknown connection status.";
    }

    /**
     * Helper method to safely close a connection.
     */
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                System.err.println("[WARNING] Error closing database connection: " + e.getMessage());
            }
        }
    }
}
