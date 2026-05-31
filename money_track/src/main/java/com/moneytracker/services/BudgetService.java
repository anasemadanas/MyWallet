package com.moneytracker.services;

import com.moneytracker.models.Budget;
import com.moneytracker.repository.BudgetRepositoryImpl;
import com.moneytracker.repository.IBudgetRepository;

public class BudgetService implements IBudgetService {
    private final IBudgetRepository budgetRepository;

    public BudgetService() {
        this(new BudgetRepositoryImpl());
    }

    public BudgetService(int userId) {
        this(new BudgetRepositoryImpl(userId));
    }

    public BudgetService(IBudgetRepository budgetRepository) {
        this.budgetRepository = budgetRepository;
    }

    @Override
    public void createBudget(double amount, int month, int year) {
        validateBudget(amount, month, year);
        budgetRepository.createBudget(amount, month, year);
    }

    @Override
    public boolean validateBudget(double amount, int month, int year) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        validateMonthYear(month, year);
        if (amount > 1_000_000) {
            throw new IllegalArgumentException("Amount must be less than 1,000,000");
        }
        return true;
    }

    @Override
    public boolean checkBudget(int month, int year) {
        return budgetRepository.checkBudget(month, year);
    }

    @Override
    public void addToBudget(double amount, int month, int year) {
        budgetRepository.addToBudget(amount, month, year);
    }

    @Override
    public void deductFromBudget(double amount, int month, int year) {
        budgetRepository.deductFromBudget(amount, month, year);
    }

    @Override
    public void deleteBudget(int budgetId) {
        budgetRepository.deleteBudget(budgetId);
    }

    @Override
    public void updateBudget(int budgetId, double amount) {
        budgetRepository.updateBudget(budgetId, amount);
    }

    @Override
    public double getBudgetBalance(int month, int year) {
        return budgetRepository.getBudgetBalance(month, year);
    }

    public Budget getBudget(int month, int year) {
        return budgetRepository.getBudget(month, year);
    }

    static void validateMonthYear(int month, int year) {
        if (year < 2020 || year > 2100) {
            throw new IllegalArgumentException("Year must be between 2020 and 2100");
        }
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Month must be between 1 and 12");
        }
    }
}
