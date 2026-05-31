package com.moneytracker.services;

import java.util.List;
import com.moneytracker.models.Transaction;

public interface ITransactionService {

    void addTransaction(double amountTrans, String category, int month, int year);

    boolean validateTransaction(double amountTrans, int month, int year, double budget);

    void editTransaction(int tid, double newAmount, int month, int year);
    void deleteTransaction(int transactionId);

    String getBudgetWarning(double amountTrans, int month, int year);
    String getTransactionWarning(double oldAmount, double newAmount);

    List<Transaction> getTransactions();
    double getBudgetBalance(int month, int year);

}