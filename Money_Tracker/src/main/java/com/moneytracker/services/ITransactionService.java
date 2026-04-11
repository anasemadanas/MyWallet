package com.moneytracker.services;

import java.util.List;
import com.moneytracker.models.Transaction;

public interface ITransactionService {

    void addTransaction(double amountTrans, String category, int month, int year);

    boolean validateTransaction(double amountTrans, int month, int year, double budget);

    double getBudgetBalance(int month, int year);

    // -----------------------
    List<Transaction> GetTransactions();

    void editTransaction(int tid, double newAmount, int month, int year);

    void deleteTransaction(int transactionId);
}