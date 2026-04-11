package com.moneytracker.services;

public interface IBudgetService {

    void createBudget(double amount, int month, int year);

    boolean validateBudget(double amount, int month, int year);

    // ---------------- check if budget exists and update after spending ----------------
    boolean checkBudget(int month, int year);

    void deductFromBudget(double amount, int month, int year);

    void addToBudget(double amount, int month, int year);

    // ---------------- future ----------------
    double getBudgetBalance(int month, int year);

    void deleteBudget(int budgetId);

    void updateBudget(int budgetId, double amount);
}