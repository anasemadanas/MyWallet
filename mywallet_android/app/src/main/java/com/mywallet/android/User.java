package com.mywallet.android;

public class User {
    public final int id;
    public final String username;
    public final int permissions;

    public User(int id, String username, int permissions) {
        this.id = id;
        this.username = username;
        this.permissions = permissions;
    }
}
