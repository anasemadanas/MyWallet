package com.moneytracker.common;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.SimpleFormatter;

public final class Logger {
    private static final int LOG_FILE_LIMIT_BYTES = 1_048_576;
    private static boolean configured;

    private Logger() {
    }

    public static synchronized void configure() {
        if (configured) {
            return;
        }

        try {
            java.nio.file.Files.createDirectories(AppPaths.logDirectory());
            FileHandler fileHandler = new FileHandler(
                    AppPaths.logFile().toString(),
                    LOG_FILE_LIMIT_BYTES,
                    1,
                    true
            );
            fileHandler.setFormatter(new SimpleFormatter());
            fileHandler.setLevel(Level.INFO);

            java.util.logging.Logger rootLogger = java.util.logging.Logger.getLogger("");
            rootLogger.addHandler(fileHandler);
            rootLogger.setLevel(Level.INFO);
            configured = true;
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(Logger.class.getName())
                    .log(Level.WARNING, "Could not configure file logging", ex);
        }
    }
}
