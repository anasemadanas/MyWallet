package com.moneytracker.services;

public interface IBudgetService {

    void createBudget(double amount, int month, int year);

    boolean validateBudget(double amount, int month, int year);
    boolean checkBudget(int month, int year);

    void addToBudget(double amount, int month, int year);
    void deductFromBudget(double amount, int month, int year);

    void deleteBudget(int budgetId);
    void updateBudget(int budgetId, double amount);

    double getBudgetBalance(int month, int year);
}