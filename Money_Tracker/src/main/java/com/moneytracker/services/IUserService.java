package com.moneytracker.services;
import com.moneytracker.models.User;


public interface IUserService {

    User login(String username, String password);

}
