package com.moneytracker.database;

import com.moneytracker.common.AppPaths;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    private static final Path LEGACY_DATABASE_FILE = Path.of("data").resolve("money_tracker.db");

    private DatabaseManager() {
    }

    public static Connection getConnection() throws SQLException {
        try {
            prepareDatabaseDirectory();
        } catch (Exception ex) {
            throw new SQLException("Unable to create database directory", ex);
        }

        Connection connection = DriverManager.getConnection("jdbc:sqlite:" + getDatabaseFile());
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
        }
        return connection;
    }

    public static boolean databaseExists() {
        return Files.isRegularFile(getDatabaseFile());
    }

    public static Path getDatabaseFile() {
        return AppPaths.databaseFile().toAbsolutePath().normalize();
    }

    private static void prepareDatabaseDirectory() throws java.io.IOException {
        Files.createDirectories(AppPaths.dataDirectory());

        Path databaseFile = getDatabaseFile();
        if (Files.exists(databaseFile) || AppPaths.isCustomAppDirectory()) {
            return;
        }

        Path legacyDatabaseFile = LEGACY_DATABASE_FILE.toAbsolutePath().normalize();
        if (Files.isRegularFile(legacyDatabaseFile) && !legacyDatabaseFile.equals(databaseFile)) {
            Files.copy(legacyDatabaseFile, databaseFile, StandardCopyOption.COPY_ATTRIBUTES);
        }
    }
}
