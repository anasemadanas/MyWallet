package com.moneytracker.repository;

import com.moneytracker.models.User;

public interface IUserRepository {

    User findUser(String username, String password);

}