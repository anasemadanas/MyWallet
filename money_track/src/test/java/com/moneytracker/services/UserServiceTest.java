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

    @Test
    void create_user_normalizes_input_and_delegates_to_repository() {

        FakeUserRepo repo = new FakeUserRepo(null);
        UserService service = new UserService(repo);

        User result = service.createUser(" New_User ", " pass ", " pass ", " blue ");

        assertEquals(new User(10, "new_user", 7), result);
        assertEquals("new_user", repo.createdUsername);
        assertEquals("pass", repo.createdPassword);
        assertEquals("blue", repo.createdRecoveryAnswer);
    }

    @Test
    void create_user_rejects_invalid_input() {

        UserService service = new UserService(new FakeUserRepo(null));

        assertThrows(IllegalArgumentException.class, () ->
                service.createUser("ab", "pass", "pass", "blue"));
        assertThrows(IllegalArgumentException.class, () ->
                service.createUser("bad user", "pass", "pass", "blue"));
        assertThrows(IllegalArgumentException.class, () ->
                service.createUser("valid_user", "abc", "abc", "blue"));
        assertThrows(IllegalArgumentException.class, () ->
                service.createUser("valid_user", "pass", "different", "blue"));
        assertThrows(IllegalArgumentException.class, () ->
                service.createUser("valid_user", "pass", "pass", "x"));
    }

    @Test
    void create_user_rejects_duplicate_username() {

        FakeUserRepo repo = new FakeUserRepo(null);
        repo.usernameExists = true;
        UserService service = new UserService(repo);

        RuntimeException ex = assertThrows(IllegalArgumentException.class, () ->
                service.createUser("valid_user", "pass", "pass", "blue"));

        assertTrue(ex.getMessage().contains("Username already exists"));
    }

    @Test
    void reset_password_normalizes_input_and_delegates_to_repository() {

        FakeUserRepo repo = new FakeUserRepo(null);
        repo.resetResult = true;
        UserService service = new UserService(repo);

        boolean result = service.resetPassword(" Valid_User ", " Blue ", " pass ", " pass ");

        assertTrue(result);
        assertEquals("valid_user", repo.resetUsername);
        assertEquals("Blue", repo.resetRecoveryAnswer);
        assertEquals("pass", repo.resetNewPassword);
    }

    @Test
    void reset_password_rejects_invalid_input() {

        UserService service = new UserService(new FakeUserRepo(null));

        assertThrows(IllegalArgumentException.class, () ->
                service.resetPassword("ab", "blue", "pass", "pass"));
        assertThrows(IllegalArgumentException.class, () ->
                service.resetPassword("valid_user", "", "pass", "pass"));
        assertThrows(IllegalArgumentException.class, () ->
                service.resetPassword("valid_user", "blue", "abc", "abc"));
        assertThrows(IllegalArgumentException.class, () ->
                service.resetPassword("valid_user", "blue", "pass", "different"));
    }

    private static class FakeUserRepo implements IUserRepository {
        private final User user;
        private boolean usernameExists;
        private boolean resetResult;
        private String createdUsername;
        private String createdPassword;
        private String createdRecoveryAnswer;
        private String resetUsername;
        private String resetRecoveryAnswer;
        private String resetNewPassword;

        private FakeUserRepo(User user) {
            this.user = user;
        }

        @Override
        public User findUser(String username, String password) {
            return user;
        }

        @Override
        public boolean usernameExists(String username) {
            return usernameExists;
        }

        @Override
        public User createUser(String username, String password, String recoveryAnswer) {
            createdUsername = username;
            createdPassword = password;
            createdRecoveryAnswer = recoveryAnswer;
            return new User(10, username, 7);
        }

        @Override
        public boolean resetPassword(String username, String recoveryAnswer, String newPassword) {
            resetUsername = username;
            resetRecoveryAnswer = recoveryAnswer;
            resetNewPassword = newPassword;
            return resetResult;
        }
    }
}
