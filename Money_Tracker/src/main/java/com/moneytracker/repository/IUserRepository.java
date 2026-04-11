package com.moneytracker.repository;

import com.moneytracker.models.User;

public interface IUserRepository {

    User FindUser(String username, String password);

}
