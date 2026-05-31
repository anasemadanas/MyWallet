package com.mywallet.android;

public class WalletTransaction {
    public final int id;
    public final double amount;
    public final String category;
    public final int month;
    public final int year;

    public WalletTransaction(int id, double amount, String category, int month, int year) {
        this.id = id;
        this.amount = amount;
        this.category = category;
        this.month = month;
        this.year = year;
    }

    @Override
    public String toString() {
        return "#" + id + "  " + category + "  " + amount + "  " + month + "/" + year;
    }
}
