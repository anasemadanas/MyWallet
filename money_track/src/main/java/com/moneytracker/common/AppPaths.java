package com.moneytracker.common;

import java.nio.file.Path;
import java.util.Locale;

public final class AppPaths {
    private static final String APP_DIR_PROPERTY = "mywallet.app.dir";
    private static final String WINDOWS_PUBLIC_APP_DIR = "MoneyManager";
    private static final String UNIX_PUBLIC_APP_DIR = "MoneyManager";

    private AppPaths() {
    }

    public static Path appDirectory() {
        String override = System.getProperty(APP_DIR_PROPERTY);
        if (override != null && !override.isBlank()) {
            return Path.of(override).toAbsolutePath().normalize();
        }

        String osName = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        if (osName.contains("win")) {
            String publicDirectory = System.getenv("PUBLIC");
            if (publicDirectory != null && !publicDirectory.isBlank()) {
                return Path.of(publicDirectory, WINDOWS_PUBLIC_APP_DIR).toAbsolutePath().normalize();
            }
            return Path.of("C:", "Users", "Public", WINDOWS_PUBLIC_APP_DIR).toAbsolutePath().normalize();
        }

        if (osName.contains("mac")) {
            return Path.of("/Users", "Shared", UNIX_PUBLIC_APP_DIR).toAbsolutePath().normalize();
        }

        return Path.of("/var", "tmp", UNIX_PUBLIC_APP_DIR).toAbsolutePath().normalize();
    }

    public static Path dataDirectory() {
        return appDirectory().resolve("data");
    }

    public static Path databaseFile() {
        return dataDirectory().resolve("money_tracker.db");
    }

    public static Path logDirectory() {
        return appDirectory().resolve("logs");
    }

    public static Path logFile() {
        return logDirectory().resolve("mywallet.log");
    }

    public static boolean isCustomAppDirectory() {
        String override = System.getProperty(APP_DIR_PROPERTY);
        return override != null && !override.isBlank();
    }
}
