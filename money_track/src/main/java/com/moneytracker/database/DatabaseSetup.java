package com.moneytracker.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseSetup {
    private DatabaseSetup() {
    }

    public static void initialize() {
        boolean existingDatabase = DatabaseManager.databaseExists();
        try (Connection connection = DatabaseManager.getConnection();
             Statement statement = connection.createStatement()) {
            if (existingDatabase && schemaIsReady(statement)) {
                return;
            }

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
            seedUsersIfEmpty(statement);
            statement.executeUpdate("UPDATE users SET recovery_answer = username WHERE recovery_answer = ''");
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

    private static boolean schemaIsReady(Statement statement) throws SQLException {
        return hasColumns(statement, "users", "id", "username", "password", "permissions", "recovery_answer")
                && hasColumns(statement, "budgets", "id", "user_id", "amount", "month", "year", "total_amount")
                && hasColumns(statement, "transactions", "id", "user_id", "amount", "category", "month", "year")
                && hasColumns(statement, "goals", "id", "user_id", "name", "target_amount", "saved_amount")
                && indexExists(statement, "idx_transactions_user_month_year")
                && indexExists(statement, "idx_budgets_user_month_year")
                && indexExists(statement, "idx_goals_user");
    }

    private static boolean hasColumns(Statement statement, String table, String... expectedColumns) throws SQLException {
        java.util.Set<String> columns = new java.util.HashSet<>();
        try (ResultSet resultSet = statement.executeQuery("PRAGMA table_info(" + table + ")")) {
            while (resultSet.next()) {
                columns.add(resultSet.getString("name").toLowerCase(java.util.Locale.ROOT));
            }
        }

        for (String expectedColumn : expectedColumns) {
            if (!columns.contains(expectedColumn.toLowerCase(java.util.Locale.ROOT))) {
                return false;
            }
        }
        return true;
    }

    private static boolean indexExists(Statement statement, String indexName) throws SQLException {
        try (ResultSet resultSet = statement.executeQuery(
                "SELECT 1 FROM sqlite_master WHERE type = 'index' AND name = '" + indexName + "'")) {
            return resultSet.next();
        }
    }

    private static void seedUsersIfEmpty(Statement statement) throws SQLException {
        try (ResultSet resultSet = statement.executeQuery("SELECT 1 FROM users LIMIT 1")) {
            if (resultSet.next()) {
                return;
            }
        }

        statement.executeUpdate("""
                INSERT OR IGNORE INTO users (username, password, permissions, recovery_answer)
                VALUES
                ('admin', '1234', -1, 'admin'),
                ('user', 'user', 7, 'user'),
                ('zaid', 'zaid', 7, 'zaid'),
                ('hamza', '9999', 7, 'hamza')
                """);
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
