package com.moneytracker.database;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    private static final Path DATABASE_DIR = Path.of("data");
    private static final Path DATABASE_FILE = DATABASE_DIR.resolve("money_tracker.db");
    private static final String JDBC_URL = "jdbc:sqlite:" + DATABASE_FILE.toAbsolutePath();

    private DatabaseManager() {
    }

    public static Connection getConnection() throws SQLException {
        try {
            Files.createDirectories(DATABASE_DIR);
        } catch (Exception ex) {
            throw new SQLException("Unable to create database directory", ex);
        }

        Connection connection = DriverManager.getConnection(JDBC_URL);
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
        }
        return connection;
    }

    public static Path getDatabaseFile() {
        return DATABASE_FILE.toAbsolutePath();
    }
}
