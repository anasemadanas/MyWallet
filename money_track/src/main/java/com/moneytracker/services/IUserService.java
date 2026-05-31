package com.moneytracker.services;
import com.moneytracker.models.User;


public interface IUserService {

    User login(String username, String password);
    User createUser(String username, String password, String confirmPassword, String recoveryAnswer);
    boolean resetPassword(String username, String recoveryAnswer, String newPassword, String confirmPassword);

}
