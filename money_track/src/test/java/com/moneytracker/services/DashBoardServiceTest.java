package com.moneytracker.services;

import static org.junit.jupiter.api.Assertions.*;

import com.moneytracker.models.Budget;
import com.moneytracker.models.Transaction;
import com.moneytracker.repository.IBudgetRepository;
import com.moneytracker.repository.ITransactionRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class DashBoardServiceTest {

    @Test
    void get_balance_for_month_sums_transactions_for_selected_month() {

        FakeTransactionRepo transactionRepo = new FakeTransactionRepo();
        transactionRepo.monthTransactions = List.of(
                new Transaction(10.0, "Food", 5, 2026),
                new Transaction(-2.5, "Adjustment", 5, 2026),
                new Transaction(40.0, "Bills", 5, 2026)
        );
        DashBoardService service = new DashBoardService(
                transactionRepo,
                new BudgetService(new FakeBudgetRepo())
        );

        assertEquals(47.5, service.getBalanceForMonth(5, 2026));
        assertEquals(5, transactionRepo.requestedMonth);
        assertEquals(2026, transactionRepo.requestedYear);
    }

    @Test
    void get_budget_summary_formats_total_remaining_and_spent() {

        FakeBudgetRepo budgetRepo = new FakeBudgetRepo();
        budgetRepo.budget = new Budget(3, 125.5, 500.0, 5, 2026);
        DashBoardService service = new DashBoardService(
                new FakeTransactionRepo(),
                new BudgetService(budgetRepo)
        );

        assertEquals("Total: 500.00 | Remaining: 125.50 | Spent: 374.50",
                service.getBudgetSummary(5, 2026));
    }

    @Test
    void get_budget_for_category_groups_transactions_in_order() {

        FakeTransactionRepo transactionRepo = new FakeTransactionRepo();
        transactionRepo.monthTransactions = List.of(
                new Transaction(10.0, "Food", 5, 2026),
                new Transaction(15.0, "Bills", 5, 2026),
                new Transaction(7.5, "Food", 5, 2026)
        );
        DashBoardService service = new DashBoardService(
                transactionRepo,
                new BudgetService(new FakeBudgetRepo())
        );

        Map<String, Double> result = service.getBudgetForCategory(5, 2026);

        assertEquals(List.of("Food", "Bills"), new ArrayList<>(result.keySet()));
        assertEquals(17.5, result.get("Food"));
        assertEquals(15.0, result.get("Bills"));
    }

    @Test
    void transaction_getters_delegate_to_repository() {

        FakeTransactionRepo transactionRepo = new FakeTransactionRepo();
        transactionRepo.allTransactions = List.of(new Transaction(1, 30.0, "Fuel", 4, 2026));
        transactionRepo.monthTransactions = List.of(new Transaction(2, 20.0, "Food", 5, 2026));
        DashBoardService service = new DashBoardService(
                transactionRepo,
                new BudgetService(new FakeBudgetRepo())
        );

        assertEquals(transactionRepo.allTransactions, service.getAllTransactions());
        assertEquals(transactionRepo.monthTransactions, service.getTransactionsForMonth(5, 2026));
    }

    private static class FakeTransactionRepo implements ITransactionRepository {
        private List<Transaction> allTransactions = List.of();
        private List<Transaction> monthTransactions = List.of();
        private int requestedMonth;
        private int requestedYear;

        @Override
        public void addTransaction(Transaction transaction) {
        }

        @Override
        public void deleteTransaction(int transactionId) {
        }

        @Override
        public void updateTransaction(int transactionId, double newAmount, int newMonth, int newYear) {
        }

        @Override
        public List<Transaction> getTransactions() {
            return allTransactions;
        }

        @Override
        public Transaction getTransactionById(int transactionId) {
            return null;
        }

        @Override
        public List<Transaction> getTransactionsByMonth(int month, int year) {
            requestedMonth = month;
            requestedYear = year;
            return monthTransactions;
        }
    }

    private static class FakeBudgetRepo implements IBudgetRepository {
        private Budget budget = new Budget(null, 0, 0, 1, 2026);

        @Override
        public void createBudget(double amount, int month, int year) {
        }

        @Override
        public boolean checkBudget(int month, int year) {
            return false;
        }

        @Override
        public void addToBudget(double amount, int month, int year) {
        }

        @Override
        public void deductFromBudget(double amountSpent, int month, int year) {
        }

        @Override
        public void updateBudget(int budgetId, double amount) {
        }

        @Override
        public void deleteBudget(int budgetId) {
        }

        @Override
        public void increaseTotalBudget(double amount, int month, int year) {
        }

        @Override
        public double getBudgetBalance(int month, int year) {
            return budget.getAmount();
        }

        @Override
        public Budget getBudget(int month, int year) {
            return budget;
        }
    }
}
