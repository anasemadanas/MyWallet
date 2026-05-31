package com.moneytracker.services;

import static org.junit.jupiter.api.Assertions.*;

import com.moneytracker.models.Budget;
import com.moneytracker.repository.IBudgetRepository;
import org.junit.jupiter.api.Test;

public class BudgetServiceTest {

    @Test
    void create_budget_validates_and_delegates_to_repository() {

        FakeBudgetRepo repo = new FakeBudgetRepo();
        BudgetService service = new BudgetService(repo);

        service.createBudget(500.0, 5, 2026);

        assertEquals(500.0, repo.createdAmount);
        assertEquals(5, repo.createdMonth);
        assertEquals(2026, repo.createdYear);
    }

    @Test
    void validate_budget_accepts_valid_budget() {

        BudgetService service = new BudgetService(new FakeBudgetRepo());

        assertTrue(service.validateBudget(100.0, 1, 2020));
        assertTrue(service.validateBudget(1_000_000.0, 12, 2100));
    }

    @Test
    void validate_budget_rejects_invalid_values() {

        BudgetService service = new BudgetService(new FakeBudgetRepo());

        assertThrows(IllegalArgumentException.class, () -> service.validateBudget(0, 1, 2026));
        assertThrows(IllegalArgumentException.class, () -> service.validateBudget(-1, 1, 2026));
        assertThrows(IllegalArgumentException.class, () -> service.validateBudget(100, 0, 2026));
        assertThrows(IllegalArgumentException.class, () -> service.validateBudget(100, 13, 2026));
        assertThrows(IllegalArgumentException.class, () -> service.validateBudget(100, 1, 2019));
        assertThrows(IllegalArgumentException.class, () -> service.validateBudget(100, 1, 2101));
        assertThrows(IllegalArgumentException.class, () -> service.validateBudget(1_000_000.01, 1, 2026));
    }

    @Test
    void budget_operations_delegate_to_repository() {

        FakeBudgetRepo repo = new FakeBudgetRepo();
        repo.checkBudgetResult = true;
        repo.budgetBalance = 275.25;
        repo.budget = new Budget(9, 275.25, 500.0, 6, 2026);
        BudgetService service = new BudgetService(repo);

        assertTrue(service.checkBudget(6, 2026));
        service.addToBudget(25.0, 6, 2026);
        service.deductFromBudget(40.0, 6, 2026);
        service.updateBudget(9, 450.0);
        service.deleteBudget(9);

        assertEquals(275.25, service.getBudgetBalance(6, 2026));
        assertEquals(repo.budget, service.getBudget(6, 2026));
        assertEquals(6, repo.checkedMonth);
        assertEquals(2026, repo.checkedYear);
        assertEquals(25.0, repo.addedAmount);
        assertEquals(40.0, repo.deductedAmount);
        assertEquals(9, repo.updatedBudgetId);
        assertEquals(450.0, repo.updatedAmount);
        assertEquals(9, repo.deletedBudgetId);
    }

    private static class FakeBudgetRepo implements IBudgetRepository {
        private double createdAmount;
        private int createdMonth;
        private int createdYear;
        private boolean checkBudgetResult;
        private int checkedMonth;
        private int checkedYear;
        private double addedAmount;
        private double deductedAmount;
        private int updatedBudgetId;
        private double updatedAmount;
        private int deletedBudgetId;
        private double budgetBalance;
        private Budget budget = new Budget(null, 0, 0, 1, 2026);

        @Override
        public void createBudget(double amount, int month, int year) {
            createdAmount = amount;
            createdMonth = month;
            createdYear = year;
        }

        @Override
        public boolean checkBudget(int month, int year) {
            checkedMonth = month;
            checkedYear = year;
            return checkBudgetResult;
        }

        @Override
        public void addToBudget(double amount, int month, int year) {
            addedAmount = amount;
        }

        @Override
        public void deductFromBudget(double amountSpent, int month, int year) {
            deductedAmount = amountSpent;
        }

        @Override
        public void updateBudget(int budgetId, double amount) {
            updatedBudgetId = budgetId;
            updatedAmount = amount;
        }

        @Override
        public void deleteBudget(int budgetId) {
            deletedBudgetId = budgetId;
        }

        @Override
        public void increaseTotalBudget(double amount, int month, int year) {
        }

        @Override
        public double getBudgetBalance(int month, int year) {
            return budgetBalance;
        }

        @Override
        public Budget getBudget(int month, int year) {
            return budget;
        }
    }
}
