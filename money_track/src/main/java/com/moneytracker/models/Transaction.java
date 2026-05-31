package com.moneytracker.models;

public class Transaction {
    private final Integer id;
    private final double amount;
    private final String category;
    private final int month;
    private final int year;

    public Transaction(double amount, String category, int month, int year) {
        this(null, amount, category, month, year);
    }

    public Transaction(Integer id, double amount, String category, int month, int year) {
        this.id = id;
        this.amount = amount;
        this.category = category;
        this.month = month;
        this.year = year;
    }

    public Integer getId() {
        return id;
    }

    public double getAmount() {
        return amount;
    }

    public String getCategory() {
        return category;
    }

    public int getMonth() {
        return month;
    }

    public int getYear() {
        return year;
    }
}
