package com.moneytracker.models;

import java.util.Objects;

public class User {

    private int id;
    private String username;
    private int permissions;

    public User(String username) {
        this(0, username, 0);
    }

    public User(int id, String username, int permissions) {
        this.id = id;
        this.username = username;
        this.permissions = permissions;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getPermissions() {
        return permissions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return Objects.equals(username, user.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username);
    }
}
