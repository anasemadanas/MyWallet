package com.moneytracker.repository;

import com.moneytracker.models.Budget;

public interface IBudgetRepository {

    void createBudget(double amount, int month, int year);

    boolean checkBudget(int month, int year);

    void deductFromBudget(double amountSpent, int month, int year);

    double getBudgetBalance(int month, int year);

    void addToBudget(double amount, int month, int year);

    // ---------------- future ----------------
    Budget getBudget(int month, int year);

    void updateBudget(int budgetId, double amount);

    void deleteBudget(int budgetId);
}