package com.moneytracker.services;

import java.util.List;
import java.util.Map;
import com.moneytracker.models.Transaction;

public interface IDashBoardService {

    void showAbout();
    void openGuide();

    void saveData(int month, int year);
    void exportData(int month, int year);

    double getCurrentMonthBalance();
    double getBalanceForMonth(int month, int year);

    String getBudgetSummary(int month, int year);
    Map<String, Double> getBudgetForCategory(int month, int year);

    List<Transaction> getAllTransactions();
    List<Transaction> getTransactionsForMonth(int month, int year);

}