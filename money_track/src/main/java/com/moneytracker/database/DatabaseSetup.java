package com.moneytracker.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseSetup {
    private DatabaseSetup() {
    }

    public static void initialize() {
        try (Connection connection = DatabaseManager.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS users (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        username TEXT NOT NULL UNIQUE,
                        password TEXT NOT NULL,
                        permissions INTEGER NOT NULL DEFAULT 7,
                        recovery_answer TEXT NOT NULL DEFAULT ''
                    )
                    """);

            addColumnIfMissing(statement, "users", "recovery_answer", "TEXT NOT NULL DEFAULT ''");
            seedUsers(statement);
            migrateBudgets(statement);
            migrateTransactions(statement);
            migrateGoals(statement);

            statement.executeUpdate("CREATE INDEX IF NOT EXISTS idx_transactions_user_month_year ON transactions (user_id, month, year)");
            statement.executeUpdate("CREATE INDEX IF NOT EXISTS idx_budgets_user_month_year ON budgets (user_id, month, year)");
            statement.executeUpdate("CREATE INDEX IF NOT EXISTS idx_goals_user ON goals (user_id)");
        } catch (SQLException ex) {
            throw new IllegalStateException("Could not initialize SQLite database", ex);
        }
    }

    private static void seedUsers(Statement statement) throws SQLException {
        statement.executeUpdate("""
                INSERT OR IGNORE INTO users (username, password, permissions, recovery_answer)
                VALUES
                ('admin', '1234', -1, 'admin'),
                ('user', 'user', 7, 'user'),
                ('zaid', 'zaid', 7, 'zaid'),
                ('hamza', '9999', 7, 'hamza')
                """);
        statement.executeUpdate("UPDATE users SET recovery_answer = username WHERE recovery_answer = ''");
    }

    private static void migrateBudgets(Statement statement) throws SQLException {
        String sql = tableSql(statement, "budgets");
        if (sql == null) {
            statement.executeUpdate("""
                    CREATE TABLE budgets (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        user_id INTEGER NOT NULL,
                        amount REAL NOT NULL,
                        month INTEGER NOT NULL CHECK(month BETWEEN 1 AND 12),
                        year INTEGER NOT NULL CHECK(year >= 2020),
                        total_amount REAL NOT NULL,
                        UNIQUE(user_id, month, year),
                        FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE
                    )
                    """);
            return;
        }

        if (!sql.contains("user_id") || sql.contains("UNIQUE(month, year)")) {
            statement.executeUpdate("ALTER TABLE budgets RENAME TO budgets_old");
            statement.executeUpdate("""
                    CREATE TABLE budgets (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        user_id INTEGER NOT NULL,
                        amount REAL NOT NULL,
                        month INTEGER NOT NULL CHECK(month BETWEEN 1 AND 12),
                        year INTEGER NOT NULL CHECK(year >= 2020),
                        total_amount REAL NOT NULL,
                        UNIQUE(user_id, month, year),
                        FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE
                    )
                    """);
            statement.executeUpdate("""
                    INSERT INTO budgets (id, user_id, amount, month, year, total_amount)
                    SELECT id, 1, amount, month, year, total_amount FROM budgets_old
                    """);
            statement.executeUpdate("DROP TABLE budgets_old");
        }
    }

    private static void migrateTransactions(Statement statement) throws SQLException {
        statement.executeUpdate("""
                CREATE TABLE IF NOT EXISTS transactions (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    amount REAL NOT NULL,
                    category TEXT NOT NULL,
                    month INTEGER NOT NULL CHECK(month BETWEEN 1 AND 12),
                    year INTEGER NOT NULL CHECK(year >= 2020),
                    user_id INTEGER NOT NULL DEFAULT 1,
                    FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE
                )
                """);
        addColumnIfMissing(statement, "transactions", "user_id", "INTEGER NOT NULL DEFAULT 1");
    }

    private static void migrateGoals(Statement statement) throws SQLException {
        statement.executeUpdate("""
                CREATE TABLE IF NOT EXISTS goals (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    target_amount REAL NOT NULL,
                    saved_amount REAL DEFAULT 0.0,
                    user_id INTEGER NOT NULL DEFAULT 1,
                    FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE
                )
                """);
        addColumnIfMissing(statement, "goals", "user_id", "INTEGER NOT NULL DEFAULT 1");
    }

    private static void addColumnIfMissing(Statement statement, String table, String column, String definition) throws SQLException {
        try (ResultSet columns = statement.executeQuery("PRAGMA table_info(" + table + ")")) {
            while (columns.next()) {
                if (column.equalsIgnoreCase(columns.getString("name"))) {
                    return;
                }
            }
        }
        statement.executeUpdate("ALTER TABLE " + table + " ADD COLUMN " + column + " " + definition);
    }

    private static String tableSql(Statement statement, String table) throws SQLException {
        try (ResultSet resultSet = statement.executeQuery("SELECT sql FROM sqlite_master WHERE type = 'table' AND name = '" + table + "'")) {
            return resultSet.next() ? resultSet.getString("sql") : null;
        }
    }
}
