package com.moneytracker.services;
import com.moneytracker.models.User;

public class UserService implements IUserService {


        private UserRepo userRepo;

        private int loginAttempts = 0;
        private final int maxAttempts = 3;

        public UserService(UserRepo userRepo) {
            this.userRepo = userRepo;
        }

        public User login(String username, String password) {

            if (loginAttempts >= maxAttempts) {
                throw new RuntimeException("Too many login attempts");
            }

            User user = userRepo.findUser(username, password);

            if (user != null) {
                loginAttempts = 0;
                return user;
            } else {
                loginAttempts++;
                return null;
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
