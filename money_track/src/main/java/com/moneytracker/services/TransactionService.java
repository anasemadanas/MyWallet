package com.moneytracker.services;

import com.moneytracker.models.Budget;
import com.moneytracker.models.Transaction;
import com.moneytracker.repository.ITransactionRepository;
import com.moneytracker.repository.TransactionRepositoryImpl;

import java.util.List;

public class TransactionService implements ITransactionService {
    private final ITransactionRepository transactionRepository;
    private final BudgetService budgetService;

    public TransactionService() {
        this(new TransactionRepositoryImpl(), new BudgetService());
    }

    public TransactionService(int userId) {
        this(new TransactionRepositoryImpl(userId), new BudgetService(userId));
    }

    public TransactionService(ITransactionRepository transactionRepository, BudgetService budgetService) {
        this.transactionRepository = transactionRepository;
        this.budgetService = budgetService;
    }

    @Override
    public void addTransaction(double amountTrans, String category, int month, int year) {
        Budget budget = budgetService.getBudget(month, year);
        validateTransaction(amountTrans, month, year, budget.getAmount());
        if (budget.getId() == null || budget.getTotalAmount() <= 0) {
            throw new IllegalArgumentException("No budget found for the selected month/year");
        }
        transactionRepository.addTransaction(new Transaction(amountTrans, category, month, year));
        budgetService.deductFromBudget(amountTrans, month, year);
    }

    @Override
    public boolean validateTransaction(double amountTrans, int month, int year, double budget) {
        if (amountTrans <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0");
        }
        BudgetService.validateMonthYear(month, year);
        if (amountTrans > budget) {
            throw new IllegalArgumentException("Amount exceeds the available budget");
        }
        return true;
    }

    @Override
    public double getBudgetBalance(int month, int year) {
        return budgetService.getBudgetBalance(month, year);
    }

    @Override
    public List<Transaction> getTransactions() {
        return transactionRepository.getTransactions();
    }

    @Override
    public void editTransaction(int tid, double newAmount, int month, int year) {
        Transaction old = transactionRepository.getTransactionById(tid);
        if (old == null) {
            throw new IllegalArgumentException("Transaction not found");
        }
        BudgetService.validateMonthYear(month, year);
        if (newAmount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0");
        }
        double difference = newAmount - old.getAmount();
        if (difference > budgetService.getBudgetBalance(month, year)) {
            throw new IllegalArgumentException("This edit exceeds the available budget");
        }
        transactionRepository.updateTransaction(tid, newAmount, month, year);
        if (difference > 0) {
            budgetService.deductFromBudget(difference, month, year);
        } else if (difference < 0) {
            budgetService.addToBudget(Math.abs(difference), month, year);
        }
    }

    @Override
    public void deleteTransaction(int transactionId) {
        Transaction transaction = transactionRepository.getTransactionById(transactionId);
        if (transaction == null) {
            return;
        }
        transactionRepository.deleteTransaction(transactionId);
        budgetService.addToBudget(transaction.getAmount(), transaction.getMonth(), transaction.getYear());
    }

    @Override
    public String getBudgetWarning(double amountTrans, int month, int year) {
        Budget budget = budgetService.getBudget(month, year);
        if (budget.getTotalAmount() <= 0) {
            return null;
        }
        double spentAfter = budget.getTotalAmount() - (budget.getAmount() - amountTrans);
        double percentage = (spentAfter / budget.getTotalAmount()) * 100;
        if (percentage >= 80) {
            return String.format("This transaction will use %.1f%% of your budget. Continue?", percentage);
        }
        return null;
    }

    @Override
    public String getTransactionWarning(double oldAmount, double newAmount) {
        double diff = newAmount - oldAmount;
        if (diff == 0) {
            return null;
        }
        return diff > 0
                ? String.format("This will increase the transaction by %.2f. Continue?", diff)
                : String.format("This will decrease the transaction by %.2f. Continue?", Math.abs(diff));
    }
}
