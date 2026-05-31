package com.moneytracker.repository;

import com.moneytracker.models.User;

public interface IUserRepository {
    User findUser(String username, String password);
    boolean usernameExists(String username);
    User createUser(String username, String password, String recoveryAnswer);
    boolean resetPassword(String username, String recoveryAnswer, String newPassword);
}
