package com.mywallet.android;

public class Budget {
    public final int id;
    public final double amount;
    public final double totalAmount;
    public final int month;
    public final int year;

    public Budget(int id, double amount, double totalAmount, int month, int year) {
        this.id = id;
        this.amount = amount;
        this.totalAmount = totalAmount;
        this.month = month;
        this.year = year;
    }
}
