package com.moneytracker.services;

import com.moneytracker.models.Budget;
import com.moneytracker.models.Transaction;
import com.moneytracker.repository.ITransactionRepository;
import com.moneytracker.repository.TransactionRepositoryImpl;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DashBoardService implements IDashBoardService {
    private final ITransactionRepository transactionRepository;
    private final BudgetService budgetService;

    public DashBoardService() {
        this(new TransactionRepositoryImpl(), new BudgetService());
    }

    public DashBoardService(int userId) {
        this(new TransactionRepositoryImpl(userId), new BudgetService(userId));
    }

    public DashBoardService(ITransactionRepository transactionRepository, BudgetService budgetService) {
        this.transactionRepository = transactionRepository;
        this.budgetService = budgetService;
    }

    @Override
    public void showAbout() {
    }

    @Override
    public void openGuide() {
    }

    @Override
    public void saveData(int month, int year) {
    }

    @Override
    public void exportData(int month, int year) {
    }

    @Override
    public double getCurrentMonthBalance() {
        LocalDate today = LocalDate.now();
        return getBalanceForMonth(today.getMonthValue(), today.getYear());
    }

    @Override
    public double getBalanceForMonth(int month, int year) {
        return getTransactionsForMonth(month, year).stream()
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    @Override
    public String getBudgetSummary(int month, int year) {
        Budget budget = budgetService.getBudget(month, year);
        double spent = Math.max(0, budget.getTotalAmount() - budget.getAmount());
        return String.format("Total: %.2f | Remaining: %.2f | Spent: %.2f",
                budget.getTotalAmount(), budget.getAmount(), spent);
    }

    @Override
    public Map<String, Double> getBudgetForCategory(int month, int year) {
        return getTransactionsForMonth(month, year).stream()
                .collect(Collectors.groupingBy(
                        Transaction::getCategory,
                        LinkedHashMap::new,
                        Collectors.summingDouble(Transaction::getAmount)
                ));
    }

    @Override
    public List<Transaction> getAllTransactions() {
        return transactionRepository.getTransactions();
    }

    @Override
    public List<Transaction> getTransactionsForMonth(int month, int year) {
        return transactionRepository.getTransactionsByMonth(month, year);
    }
}
