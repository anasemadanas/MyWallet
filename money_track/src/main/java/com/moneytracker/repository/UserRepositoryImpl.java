package com.moneytracker.repository;

import com.moneytracker.database.DatabaseManager;
import com.moneytracker.models.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class UserRepositoryImpl implements IUserRepository {
    private static final int DEFAULT_PERMISSIONS = 7;

    @Override
    public User findUser(String username, String password) {
        String sql = "SELECT id, username, permissions FROM users WHERE username = ? AND password = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            statement.setString(2, password);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return new User(
                            resultSet.getInt("id"),
                            resultSet.getString("username"),
                            resultSet.getInt("permissions")
                    );
                }
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Could not load user", ex);
        }
        return null;
    }

    @Override
    public boolean usernameExists(String username) {
        String sql = "SELECT 1 FROM users WHERE username = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Could not check username", ex);
        }
    }

    @Override
    public User createUser(String username, String password, String recoveryAnswer) {
        String sql = "INSERT INTO users (username, password, permissions, recovery_answer) VALUES (?, ?, ?, ?)";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, username);
            statement.setString(2, password);
            statement.setInt(3, DEFAULT_PERMISSIONS);
            statement.setString(4, recoveryAnswer);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                int id = keys.next() ? keys.getInt(1) : 0;
                return new User(id, username, DEFAULT_PERMISSIONS);
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Could not create user", ex);
        }
    }

    @Override
    public boolean resetPassword(String username, String recoveryAnswer, String newPassword) {
        String sql = """
                UPDATE users
                SET password = ?
                WHERE username = ? AND lower(recovery_answer) = lower(?)
                """;
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, newPassword);
            statement.setString(2, username);
            statement.setString(3, recoveryAnswer);
            return statement.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new IllegalStateException("Could not reset password", ex);
        }
    }
}
