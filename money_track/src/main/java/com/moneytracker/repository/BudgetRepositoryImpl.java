package com.moneytracker.repository;

import com.moneytracker.database.DatabaseManager;
import com.moneytracker.models.Budget;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BudgetRepositoryImpl implements IBudgetRepository {
    private final int userId;

    public BudgetRepositoryImpl() {
        this(1);
    }

    public BudgetRepositoryImpl(int userId) {
        this.userId = userId;
    }

    @Override
    public void createBudget(double amount, int month, int year) {
        String sql = """
                INSERT INTO budgets (user_id, amount, total_amount, month, year)
                VALUES (?, ?, ?, ?, ?)
                ON CONFLICT(user_id, month, year) DO UPDATE SET
                    amount = amount + excluded.amount,
                    total_amount = total_amount + excluded.total_amount
                """;
        executeUpdate(sql, userId, amount, amount, month, year);
    }

    @Override
    public boolean checkBudget(int month, int year) {
        return getBudget(month, year).getId() != null;
    }

    @Override
    public void addToBudget(double amount, int month, int year) {
        executeUpdate("UPDATE budgets SET amount = amount + ? WHERE user_id = ? AND month = ? AND year = ?",
                amount, userId, month, year);
    }

    @Override
    public void deductFromBudget(double amountSpent, int month, int year) {
        executeUpdate("UPDATE budgets SET amount = amount - ? WHERE user_id = ? AND month = ? AND year = ?",
                amountSpent, userId, month, year);
    }

    @Override
    public void updateBudget(int budgetId, double amount) {
        executeUpdate("UPDATE budgets SET amount = ?, total_amount = ? WHERE id = ? AND user_id = ?",
                amount, amount, budgetId, userId);
    }

    @Override
    public void deleteBudget(int budgetId) {
        executeUpdate("DELETE FROM budgets WHERE id = ? AND user_id = ?", budgetId, userId);
    }

    @Override
    public void increaseTotalBudget(double amount, int month, int year) {
        executeUpdate("""
                        UPDATE budgets
                        SET amount = amount + ?, total_amount = total_amount + ?
                        WHERE user_id = ? AND month = ? AND year = ?
                        """,
                amount, amount, userId, month, year);
    }

    @Override
    public double getBudgetBalance(int month, int year) {
        return getBudget(month, year).getAmount();
    }

    @Override
    public Budget getBudget(int month, int year) {
        String sql = """
                SELECT id, amount, total_amount, month, year
                FROM budgets
                WHERE user_id = ? AND month = ? AND year = ?
                """;
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            statement.setInt(2, month);
            statement.setInt(3, year);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return new Budget(
                            resultSet.getInt("id"),
                            resultSet.getDouble("amount"),
                            resultSet.getDouble("total_amount"),
                            resultSet.getInt("month"),
                            resultSet.getInt("year")
                    );
                }
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Could not load budget", ex);
        }
        return new Budget(null, 0, 0, month, year);
    }

    private void executeUpdate(String sql, Object... values) {
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < values.length; i++) {
                statement.setObject(i + 1, values[i]);
            }
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("Could not update budget", ex);
        }
    }
}
