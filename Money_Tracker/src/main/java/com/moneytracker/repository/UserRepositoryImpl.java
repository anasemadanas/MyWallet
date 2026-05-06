package com.moneytracker.repository;

import com.moneytracker.models.User;

public class UserRepositoryImpl implements IUserRepository {

    @Override
    public User findUser(String username, String password) {

        // TODO: replace this with DB logic later

        if ("admin".equals(username) && "1234".equals(password)) {
            return new User(username);
        }

        return null;
    }
}