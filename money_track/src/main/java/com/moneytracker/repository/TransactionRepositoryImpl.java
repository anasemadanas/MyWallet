package com.moneytracker.repository;

import com.moneytracker.database.DatabaseManager;
import com.moneytracker.models.Transaction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TransactionRepositoryImpl implements ITransactionRepository {
    private final int userId;

    public TransactionRepositoryImpl() {
        this(1);
    }

    public TransactionRepositoryImpl(int userId) {
        this.userId = userId;
    }

    @Override
    public void addTransaction(Transaction transaction) {
        executeUpdate("INSERT INTO transactions (amount, category, month, year, user_id) VALUES (?, ?, ?, ?, ?)",
                transaction.getAmount(), transaction.getCategory(), transaction.getMonth(), transaction.getYear(), userId);
    }

    @Override
    public void deleteTransaction(int transactionId) {
        executeUpdate("DELETE FROM transactions WHERE id = ? AND user_id = ?", transactionId, userId);
    }

    @Override
    public void updateTransaction(int transactionId, double newAmount, int newMonth, int newYear) {
        executeUpdate("UPDATE transactions SET amount = ?, month = ?, year = ? WHERE id = ? AND user_id = ?",
                newAmount, newMonth, newYear, transactionId, userId);
    }

    @Override
    public List<Transaction> getTransactions() {
        return queryTransactions("""
                SELECT id, amount, category, month, year
                FROM transactions
                WHERE user_id = ?
                ORDER BY year DESC, month DESC, id DESC
                """, userId);
    }

    @Override
    public Transaction getTransactionById(int transactionId) {
        List<Transaction> transactions = queryTransactions("""
                SELECT id, amount, category, month, year
                FROM transactions
                WHERE id = ? AND user_id = ?
                """, transactionId, userId);
        return transactions.isEmpty() ? null : transactions.getFirst();
    }

    @Override
    public List<Transaction> getTransactionsByMonth(int month, int year) {
        return queryTransactions("""
                SELECT id, amount, category, month, year
                FROM transactions
                WHERE user_id = ? AND month = ? AND year = ?
                ORDER BY id DESC
                """, userId, month, year);
    }

    private List<Transaction> queryTransactions(String sql, Object... values) {
        List<Transaction> transactions = new ArrayList<>();
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < values.length; i++) {
                statement.setObject(i + 1, values[i]);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    transactions.add(new Transaction(
                            resultSet.getInt("id"),
                            resultSet.getDouble("amount"),
                            resultSet.getString("category"),
                            resultSet.getInt("month"),
                            resultSet.getInt("year")
                    ));
                }
            }
            return transactions;
        } catch (SQLException ex) {
            throw new IllegalStateException("Could not load transactions", ex);
        }
    }

    private void executeUpdate(String sql, Object... values) {
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < values.length; i++) {
                statement.setObject(i + 1, values[i]);
            }
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("Could not update transaction", ex);
        }
    }
}
