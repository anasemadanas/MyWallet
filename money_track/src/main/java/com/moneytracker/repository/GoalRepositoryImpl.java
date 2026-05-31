package com.moneytracker.repository;

import com.moneytracker.database.DatabaseManager;
import com.moneytracker.models.Goal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class GoalRepositoryImpl implements IGoalRepository {
    private final int userId;

    public GoalRepositoryImpl() {
        this(1);
    }

    public GoalRepositoryImpl(int userId) {
        this.userId = userId;
    }

    public void createGoal(String name, double targetAmount, double initialSaved) {
        executeUpdate("INSERT INTO goals (name, target_amount, saved_amount, user_id) VALUES (?, ?, ?, ?)",
                name, targetAmount, initialSaved, userId);
    }

    public void addSavings(int goalId, double amount) {
        executeUpdate("UPDATE goals SET saved_amount = saved_amount + ? WHERE id = ? AND user_id = ?",
                amount, goalId, userId);
    }

    public void deleteGoal(int goalId) {
        executeUpdate("DELETE FROM goals WHERE id = ? AND user_id = ?", goalId, userId);
    }

    public List<Goal> getAllGoals() {
        List<Goal> goals = new ArrayList<>();
        String sql = "SELECT id, name, target_amount, saved_amount FROM goals WHERE user_id = ? ORDER BY id";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    goals.add(new Goal(
                            resultSet.getInt("id"),
                            resultSet.getString("name"),
                            resultSet.getDouble("target_amount"),
                            resultSet.getDouble("saved_amount")
                    ));
                }
            }
            return goals;
        } catch (SQLException ex) {
            throw new IllegalStateException("Could not load goals", ex);
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
            throw new IllegalStateException("Could not update goals", ex);
        }
    }
}
