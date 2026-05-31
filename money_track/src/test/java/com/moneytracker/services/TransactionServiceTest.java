package com.moneytracker.services;

import static org.junit.jupiter.api.Assertions.*;

import com.moneytracker.models.Budget;
import com.moneytracker.models.Transaction;
import com.moneytracker.repository.IBudgetRepository;
import com.moneytracker.repository.ITransactionRepository;
import java.util.List;
import org.junit.jupiter.api.Test;

public class TransactionServiceTest {

    @Test
    void add_transaction_validates_adds_transaction_and_deducts_budget() {

        FakeTransactionRepo transactionRepo = new FakeTransactionRepo();
        FakeBudgetRepo budgetRepo = new FakeBudgetRepo();
        budgetRepo.budget = new Budget(1, 300.0, 500.0, 5, 2026);
        TransactionService service = new TransactionService(
                transactionRepo,
                new BudgetService(budgetRepo)
        );

        service.addTransaction(75.0, "Food", 5, 2026);

        assertEquals(75.0, transactionRepo.addedTransaction.getAmount());
        assertEquals("Food", transactionRepo.addedTransaction.getCategory());
        assertEquals(5, transactionRepo.addedTransaction.getMonth());
        assertEquals(2026, transactionRepo.addedTransaction.getYear());
        assertEquals(75.0, budgetRepo.deductedAmount);
    }

    @Test
    void add_transaction_rejects_missing_budget() {

        FakeBudgetRepo budgetRepo = new FakeBudgetRepo();
        budgetRepo.budget = new Budget(null, 100.0, 0, 5, 2026);
        TransactionService service = new TransactionService(
                new FakeTransactionRepo(),
                new BudgetService(budgetRepo)
        );

        RuntimeException ex = assertThrows(IllegalArgumentException.class, () ->
                service.addTransaction(50.0, "Food", 5, 2026));

        assertTrue(ex.getMessage().contains("No budget found"));
    }

    @Test
    void validate_transaction_accepts_valid_transaction() {

        TransactionService service = new TransactionService(
                new FakeTransactionRepo(),
                new BudgetService(new FakeBudgetRepo())
        );

        assertTrue(service.validateTransaction(50.0, 5, 2026, 50.0));
    }

    @Test
    void validate_transaction_rejects_invalid_values() {

        TransactionService service = new TransactionService(
                new FakeTransactionRepo(),
                new BudgetService(new FakeBudgetRepo())
        );

        assertThrows(IllegalArgumentException.class, () -> service.validateTransaction(0, 5, 2026, 100));
        assertThrows(IllegalArgumentException.class, () -> service.validateTransaction(-1, 5, 2026, 100));
        assertThrows(IllegalArgumentException.class, () -> service.validateTransaction(50, 0, 2026, 100));
        assertThrows(IllegalArgumentException.class, () -> service.validateTransaction(50, 5, 2019, 100));
        assertThrows(IllegalArgumentException.class, () -> service.validateTransaction(101, 5, 2026, 100));
    }

    @Test
    void edit_transaction_increase_updates_transaction_and_deducts_difference() {

        FakeTransactionRepo transactionRepo = new FakeTransactionRepo();
        transactionRepo.transactionById = new Transaction(7, 100.0, "Food", 5, 2026);
        FakeBudgetRepo budgetRepo = new FakeBudgetRepo();
        budgetRepo.budgetBalance = 50.0;
        TransactionService service = new TransactionService(
                transactionRepo,
                new BudgetService(budgetRepo)
        );

        service.editTransaction(7, 130.0, 5, 2026);

        assertEquals(7, transactionRepo.updatedTransactionId);
        assertEquals(130.0, transactionRepo.updatedAmount);
        assertEquals(30.0, budgetRepo.deductedAmount);
        assertEquals(0.0, budgetRepo.addedAmount);
    }

    @Test
    void edit_transaction_decrease_updates_transaction_and_adds_difference() {

        FakeTransactionRepo transactionRepo = new FakeTransactionRepo();
        transactionRepo.transactionById = new Transaction(7, 100.0, "Food", 5, 2026);
        FakeBudgetRepo budgetRepo = new FakeBudgetRepo();
        budgetRepo.budgetBalance = 50.0;
        TransactionService service = new TransactionService(
                transactionRepo,
                new BudgetService(budgetRepo)
        );

        service.editTransaction(7, 80.0, 5, 2026);

        assertEquals(7, transactionRepo.updatedTransactionId);
        assertEquals(80.0, transactionRepo.updatedAmount);
        assertEquals(20.0, budgetRepo.addedAmount);
        assertEquals(0.0, budgetRepo.deductedAmount);
    }

    @Test
    void edit_transaction_rejects_invalid_edit() {

        FakeTransactionRepo transactionRepo = new FakeTransactionRepo();
        FakeBudgetRepo budgetRepo = new FakeBudgetRepo();
        budgetRepo.budgetBalance = 10.0;
        TransactionService service = new TransactionService(
                transactionRepo,
                new BudgetService(budgetRepo)
        );

        assertThrows(IllegalArgumentException.class, () -> service.editTransaction(7, 80.0, 5, 2026));

        transactionRepo.transactionById = new Transaction(7, 100.0, "Food", 5, 2026);

        assertThrows(IllegalArgumentException.class, () -> service.editTransaction(7, 0, 5, 2026));
        assertThrows(IllegalArgumentException.class, () -> service.editTransaction(7, 120.0, 5, 2026));
    }

    @Test
    void delete_transaction_removes_transaction_and_restores_budget() {

        FakeTransactionRepo transactionRepo = new FakeTransactionRepo();
        transactionRepo.transactionById = new Transaction(7, 60.0, "Food", 5, 2026);
        FakeBudgetRepo budgetRepo = new FakeBudgetRepo();
        TransactionService service = new TransactionService(
                transactionRepo,
                new BudgetService(budgetRepo)
        );

        service.deleteTransaction(7);

        assertEquals(7, transactionRepo.deletedTransactionId);
        assertEquals(60.0, budgetRepo.addedAmount);
        assertEquals(5, budgetRepo.addedMonth);
        assertEquals(2026, budgetRepo.addedYear);
    }

    @Test
    void delete_transaction_ignores_missing_transaction() {

        FakeTransactionRepo transactionRepo = new FakeTransactionRepo();
        FakeBudgetRepo budgetRepo = new FakeBudgetRepo();
        TransactionService service = new TransactionService(
                transactionRepo,
                new BudgetService(budgetRepo)
        );

        service.deleteTransaction(7);

        assertEquals(0, transactionRepo.deletedTransactionId);
        assertEquals(0.0, budgetRepo.addedAmount);
    }

    @Test
    void warnings_describe_risky_budget_or_transaction_changes() {

        FakeBudgetRepo budgetRepo = new FakeBudgetRepo();
        budgetRepo.budget = new Budget(1, 250.0, 500.0, 5, 2026);
        TransactionService service = new TransactionService(
                new FakeTransactionRepo(),
                new BudgetService(budgetRepo)
        );

        assertEquals("This transaction will use 80.0% of your budget. Continue?",
                service.getBudgetWarning(150.0, 5, 2026));
        assertNull(service.getBudgetWarning(100.0, 5, 2026));
        assertEquals("This will increase the transaction by 25.50. Continue?",
                service.getTransactionWarning(100.0, 125.5));
        assertEquals("This will decrease the transaction by 10.00. Continue?",
                service.getTransactionWarning(100.0, 90.0));
        assertNull(service.getTransactionWarning(100.0, 100.0));
    }

    @Test
    void getters_delegate_to_dependencies() {

        FakeTransactionRepo transactionRepo = new FakeTransactionRepo();
        transactionRepo.transactions = List.of(new Transaction(1, 60.0, "Food", 5, 2026));
        FakeBudgetRepo budgetRepo = new FakeBudgetRepo();
        budgetRepo.budgetBalance = 400.0;
        TransactionService service = new TransactionService(
                transactionRepo,
                new BudgetService(budgetRepo)
        );

        assertEquals(transactionRepo.transactions, service.getTransactions());
        assertEquals(400.0, service.getBudgetBalance(5, 2026));
    }

    private static class FakeTransactionRepo implements ITransactionRepository {
        private Transaction addedTransaction;
        private int deletedTransactionId;
        private int updatedTransactionId;
        private double updatedAmount;
        private Transaction transactionById;
        private List<Transaction> transactions = List.of();

        @Override
        public void addTransaction(Transaction transaction) {
            addedTransaction = transaction;
        }

        @Override
        public void deleteTransaction(int transactionId) {
            deletedTransactionId = transactionId;
        }

        @Override
        public void updateTransaction(int transactionId, double newAmount, int newMonth, int newYear) {
            updatedTransactionId = transactionId;
            updatedAmount = newAmount;
        }

        @Override
        public List<Transaction> getTransactions() {
            return transactions;
        }

        @Override
        public Transaction getTransactionById(int transactionId) {
            return transactionById;
        }

        @Override
        public List<Transaction> getTransactionsByMonth(int month, int year) {
            return List.of();
        }
    }

    private static class FakeBudgetRepo implements IBudgetRepository {
        private Budget budget = new Budget(1, 100.0, 100.0, 5, 2026);
        private double addedAmount;
        private int addedMonth;
        private int addedYear;
        private double deductedAmount;
        private double budgetBalance;

        @Override
        public void createBudget(double amount, int month, int year) {
        }

        @Override
        public boolean checkBudget(int month, int year) {
            return budget.getId() != null;
        }

        @Override
        public void addToBudget(double amount, int month, int year) {
            addedAmount = amount;
            addedMonth = month;
            addedYear = year;
        }

        @Override
        public void deductFromBudget(double amountSpent, int month, int year) {
            deductedAmount = amountSpent;
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
            return budgetBalance;
        }

        @Override
        public Budget getBudget(int month, int year) {
            return budget;
        }
    }
}
