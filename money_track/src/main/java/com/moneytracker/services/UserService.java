package com.moneytracker.services;

import com.moneytracker.models.User;
import com.moneytracker.repository.IUserRepository;
import com.moneytracker.repository.UserRepositoryImpl;

public class UserService implements IUserService {
    private final IUserRepository userRepo;
    private int loginAttempts = 0;
    private final int maxAttempts = 4;

    public UserService() {
        this(new UserRepositoryImpl());
    }

    public UserService(IUserRepository userRepo) {
        this.userRepo = userRepo;
    }

    @Override
    public User login(String username, String password) {
        username = normalizeUsername(username);
        password = password == null ? "" : password.trim();

        if (loginAttempts >= maxAttempts) {
            throw new RuntimeException("Too many login attempts");
        }

        User user = userRepo.findUser(username, password);

        if (user != null) {
            loginAttempts = 0;
            return user;
        }

        loginAttempts++;
        if (loginAttempts >= maxAttempts) {
            throw new RuntimeException("Too many login attempts");
        }
        return null;
    }

    @Override
    public User createUser(String username, String password, String confirmPassword, String recoveryAnswer) {
        username = normalizeUsername(username);
        password = password == null ? "" : password.trim();
        confirmPassword = confirmPassword == null ? "" : confirmPassword.trim();
        recoveryAnswer = recoveryAnswer == null ? "" : recoveryAnswer.trim();

        validateUsername(username);
        validatePassword(password, confirmPassword);
        if (recoveryAnswer.length() < 2) {
            throw new IllegalArgumentException("Recovery answer must be at least 2 characters");
        }
        if (userRepo.usernameExists(username)) {
            throw new IllegalArgumentException("Username already exists");
        }
        return userRepo.createUser(username, password, recoveryAnswer);
    }

    @Override
    public boolean resetPassword(String username, String recoveryAnswer, String newPassword, String confirmPassword) {
        username = normalizeUsername(username);
        recoveryAnswer = recoveryAnswer == null ? "" : recoveryAnswer.trim();
        newPassword = newPassword == null ? "" : newPassword.trim();
        confirmPassword = confirmPassword == null ? "" : confirmPassword.trim();

        validateUsername(username);
        validatePassword(newPassword, confirmPassword);
        if (recoveryAnswer.isEmpty()) {
            throw new IllegalArgumentException("Recovery answer is required");
        }
        return userRepo.resetPassword(username, recoveryAnswer, newPassword);
    }

    private String normalizeUsername(String username) {
        return username == null ? "" : username.trim().toLowerCase();
    }

    private void validateUsername(String username) {
        if (username.length() < 3) {
            throw new IllegalArgumentException("Username must be at least 3 characters");
        }
        if (!username.matches("[a-z0-9_]+")) {
            throw new IllegalArgumentException("Username can use letters, numbers, and underscore only");
        }
    }

    private void validatePassword(String password, String confirmPassword) {
        if (password.length() < 4) {
            throw new IllegalArgumentException("Password must be at least 4 characters");
        }
        if (!password.equals(confirmPassword)) {
            throw new IllegalArgumentException("Passwords do not match");
        }
    }

    public int getLoginAttempts() {
        return loginAttempts;
    }

    public void setLoginAttempts(int loginAttempts) {
        this.loginAttempts = loginAttempts;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }
}
