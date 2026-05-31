package com.moneytracker.models;

public class Budget {
    private final Integer id;
    private final double amount;
    private final double totalAmount;
    private final int month;
    private final int year;

    public Budget(Integer id, double amount, double totalAmount, int month, int year) {
        this.id = id;
        this.amount = amount;
        this.totalAmount = totalAmount;
        this.month = month;
        this.year = year;
    }

    public Integer getId() {
        return id;
    }

    public double getAmount() {
        return amount;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public int getMonth() {
        return month;
    }

    public int getYear() {
        return year;
    }
}
