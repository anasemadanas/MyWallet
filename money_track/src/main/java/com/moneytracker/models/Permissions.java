package com.moneytracker.models;

public enum Permissions {
    ADD_TRANSACTION(0b0001),
    ADD_BUDGET(0b0010),
    LIST_TRANSACTION(0b0100);

    private final int mask;

    Permissions(int mask) {
        this.mask = mask;
    }

    public int getMask() {
        return mask;
    }

    public static boolean hasPermission(int userPermissions, Permissions permission) {
        return userPermissions == -1 || (userPermissions & permission.mask) == permission.mask;
    }
}
