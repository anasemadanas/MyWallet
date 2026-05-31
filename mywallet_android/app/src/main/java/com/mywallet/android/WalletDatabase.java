package com.mywallet.android;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class WalletDatabase extends SQLiteOpenHelper {
    private static final String DB_NAME = "mywallet.db";
    private static final int DB_VERSION = 1;
    private static final int DEFAULT_PERMISSIONS = 7;

    public WalletDatabase(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("""
                CREATE TABLE users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT NOT NULL UNIQUE,
                    password TEXT NOT NULL,
                    permissions INTEGER NOT NULL DEFAULT 7,
                    recovery_answer TEXT NOT NULL DEFAULT ''
                )
                """);
        db.execSQL("""
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
        db.execSQL("""
                CREATE TABLE transactions (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL,
                    amount REAL NOT NULL,
                    category TEXT NOT NULL,
                    month INTEGER NOT NULL CHECK(month BETWEEN 1 AND 12),
                    year INTEGER NOT NULL CHECK(year >= 2020),
                    FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE
                )
                """);
        db.execSQL("""
                CREATE TABLE goals (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL,
                    name TEXT NOT NULL,
                    target_amount REAL NOT NULL,
                    saved_amount REAL NOT NULL DEFAULT 0,
                    FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE
                )
                """);
        db.execSQL("CREATE INDEX idx_budgets_user_month_year ON budgets (user_id, month, year)");
        db.execSQL("CREATE INDEX idx_transactions_user_month_year ON transactions (user_id, month, year)");
        db.execSQL("CREATE INDEX idx_goals_user ON goals (user_id)");
        seedUsers(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS goals");
        db.execSQL("DROP TABLE IF EXISTS transactions");
        db.execSQL("DROP TABLE IF EXISTS budgets");
        db.execSQL("DROP TABLE IF EXISTS users");
        onCreate(db);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    private void seedUsers(SQLiteDatabase db) {
        createSeedUser(db, "admin", "1234", -1, "admin");
        createSeedUser(db, "user", "user", DEFAULT_PERMISSIONS, "user");
        createSeedUser(db, "zaid", "zaid", DEFAULT_PERMISSIONS, "zaid");
        createSeedUser(db, "hamza", "9999", DEFAULT_PERMISSIONS, "hamza");
    }

    private void createSeedUser(SQLiteDatabase db, String username, String password, int permissions, String recoveryAnswer) {
        ContentValues values = new ContentValues();
        values.put("username", username);
        values.put("password", password);
        values.put("permissions", permissions);
        values.put("recovery_answer", recoveryAnswer);
        db.insertWithOnConflict("users", null, values, SQLiteDatabase.CONFLICT_IGNORE);
    }

    public User login(String username, String password) {
        try (Cursor cursor = getReadableDatabase().query(
                "users",
                new String[]{"id", "username", "permissions"},
                "username = ? AND password = ?",
                new String[]{normalize(username), password.trim()},
                null,
                null,
                null
        )) {
            if (cursor.moveToFirst()) {
                return new User(cursor.getInt(0), cursor.getString(1), cursor.getInt(2));
            }
            return null;
        }
    }

    public boolean usernameExists(String username) {
        try (Cursor cursor = getReadableDatabase().query(
                "users",
                new String[]{"id"},
                "username = ?",
                new String[]{normalize(username)},
                null,
                null,
                null
        )) {
            return cursor.moveToFirst();
        }
    }

    public User createUser(String username, String password, String recoveryAnswer) {
        String normalized = normalize(username);
        ContentValues values = new ContentValues();
        values.put("username", normalized);
        values.put("password", password.trim());
        values.put("permissions", DEFAULT_PERMISSIONS);
        values.put("recovery_answer", recoveryAnswer.trim());
        long id = getWritableDatabase().insertOrThrow("users", null, values);
        return new User((int) id, normalized, DEFAULT_PERMISSIONS);
    }

    public boolean resetPassword(String username, String recoveryAnswer, String newPassword) {
        ContentValues values = new ContentValues();
        values.put("password", newPassword.trim());
        int changed = getWritableDatabase().update(
                "users",
                values,
                "username = ? AND lower(recovery_answer) = lower(?)",
                new String[]{normalize(username), recoveryAnswer.trim()}
        );
        return changed > 0;
    }

    public Budget getBudget(int userId, int month, int year) {
        try (Cursor cursor = getReadableDatabase().query(
                "budgets",
                new String[]{"id", "amount", "total_amount", "month", "year"},
                "user_id = ? AND month = ? AND year = ?",
                new String[]{String.valueOf(userId), String.valueOf(month), String.valueOf(year)},
                null,
                null,
                null
        )) {
            if (cursor.moveToFirst()) {
                return new Budget(cursor.getInt(0), cursor.getDouble(1), cursor.getDouble(2), cursor.getInt(3), cursor.getInt(4));
            }
            return new Budget(0, 0, 0, month, year);
        }
    }

    public void addBudget(int userId, double amount, int month, int year) {
        Budget existing = getBudget(userId, month, year);
        SQLiteDatabase db = getWritableDatabase();
        if (existing.id > 0) {
            ContentValues values = new ContentValues();
            values.put("amount", existing.amount + amount);
            values.put("total_amount", existing.totalAmount + amount);
            db.update("budgets", values, "id = ? AND user_id = ?", new String[]{String.valueOf(existing.id), String.valueOf(userId)});
            return;
        }
        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("amount", amount);
        values.put("total_amount", amount);
        values.put("month", month);
        values.put("year", year);
        db.insertOrThrow("budgets", null, values);
    }

    public void addTransaction(int userId, double amount, String category, int month, int year) {
        Budget budget = getBudget(userId, month, year);
        if (budget.id == 0) {
            throw new IllegalArgumentException("No budget found for this month/year");
        }
        if (amount > budget.amount) {
            throw new IllegalArgumentException("Amount exceeds available budget");
        }
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues tx = new ContentValues();
            tx.put("user_id", userId);
            tx.put("amount", amount);
            tx.put("category", category.trim());
            tx.put("month", month);
            tx.put("year", year);
            db.insertOrThrow("transactions", null, tx);

            ContentValues budgetValues = new ContentValues();
            budgetValues.put("amount", budget.amount - amount);
            db.update("budgets", budgetValues, "id = ? AND user_id = ?", new String[]{String.valueOf(budget.id), String.valueOf(userId)});
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public List<WalletTransaction> getTransactions(int userId) {
        List<WalletTransaction> transactions = new ArrayList<>();
        try (Cursor cursor = getReadableDatabase().query(
                "transactions",
                new String[]{"id", "amount", "category", "month", "year"},
                "user_id = ?",
                new String[]{String.valueOf(userId)},
                null,
                null,
                "year DESC, month DESC, id DESC"
        )) {
            while (cursor.moveToNext()) {
                transactions.add(new WalletTransaction(cursor.getInt(0), cursor.getDouble(1), cursor.getString(2), cursor.getInt(3), cursor.getInt(4)));
            }
            return transactions;
        }
    }

    public void deleteTransaction(int userId, WalletTransaction transaction) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete("transactions", "id = ? AND user_id = ?", new String[]{String.valueOf(transaction.id), String.valueOf(userId)});
            Budget budget = getBudget(userId, transaction.month, transaction.year);
            if (budget.id > 0) {
                ContentValues values = new ContentValues();
                values.put("amount", budget.amount + transaction.amount);
                db.update("budgets", values, "id = ? AND user_id = ?", new String[]{String.valueOf(budget.id), String.valueOf(userId)});
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public List<Goal> getGoals(int userId) {
        List<Goal> goals = new ArrayList<>();
        try (Cursor cursor = getReadableDatabase().query(
                "goals",
                new String[]{"id", "name", "target_amount", "saved_amount"},
                "user_id = ?",
                new String[]{String.valueOf(userId)},
                null,
                null,
                "id DESC"
        )) {
            while (cursor.moveToNext()) {
                goals.add(new Goal(cursor.getInt(0), cursor.getString(1), cursor.getDouble(2), cursor.getDouble(3)));
            }
            return goals;
        }
    }

    public void addGoal(int userId, String name, double target, double saved) {
        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("name", name.trim());
        values.put("target_amount", target);
        values.put("saved_amount", saved);
        getWritableDatabase().insertOrThrow("goals", null, values);
    }

    public void addSavings(int userId, Goal goal, double amount) {
        ContentValues values = new ContentValues();
        values.put("saved_amount", goal.savedAmount + amount);
        getWritableDatabase().update("goals", values, "id = ? AND user_id = ?", new String[]{String.valueOf(goal.id), String.valueOf(userId)});
    }

    public void deleteGoal(int userId, Goal goal) {
        getWritableDatabase().delete("goals", "id = ? AND user_id = ?", new String[]{String.valueOf(goal.id), String.valueOf(userId)});
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }
}
