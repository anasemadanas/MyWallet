package com.moneytracker.services;

import static org.junit.jupiter.api.Assertions.*;

import com.moneytracker.models.User;
import com.moneytracker.repository.IUserRepository;
import org.junit.jupiter.api.Test;

public class UserServiceTest {

    @Test
    void login_returns_user_and_resets_attempts() {

        User fakeUser = new User("admin");

        UserService service = new UserService(new FakeUserRepo(fakeUser));
        service.setLoginAttempts(3);

        User result = service.login(" Admin ", " secret ");

        assertEquals(fakeUser, result);
        assertEquals(0, service.getLoginAttempts());
    }

    @Test
    void login_increments_attempts_for_invalid_credentials() {

        UserService service = new UserService(new FakeUserRepo(null));

        User result = service.login("admin", "wrong");

        assertNull(result);
        assertEquals(1, service.getLoginAttempts());
    }

    @Test
    void login_locks_after_max_attempts() {

        UserService service = new UserService(new FakeUserRepo(null));
        service.setLoginAttempts(service.getMaxAttempts() - 1);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            service.login("admin", "wrong");
        });

        assertTrue(ex.getMessage().contains("Too many login attempts"));
    }

    private static class FakeUserRepo implements IUserRepository {
        private final User user;

        private FakeUserRepo(User user) {
            this.user = user;
        }

        @Override
        public User findUser(String username, String password) {
            return user;
        }

        @Override
        public boolean usernameExists(String username) {
            return false;
        }

        @Override
        public User createUser(String username, String password, String recoveryAnswer) {
            return new User(10, username, 7);
        }

        @Override
        public boolean resetPassword(String username, String recoveryAnswer, String newPassword) {
            return true;
        }
    }
}
