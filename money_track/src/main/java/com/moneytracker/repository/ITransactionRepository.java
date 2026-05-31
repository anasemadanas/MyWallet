package com.moneytracker.repository;

import java.util.List;
import com.moneytracker.models.Transaction;

public interface ITransactionRepository {

    void addTransaction(Transaction transaction);

    void deleteTransaction(int transactionId);

    void updateTransaction(int transactionId, double newAmount, int newMonth, int newYear);

    List<Transaction> getTransactions();

    Transaction getTransactionById(int transactionId);

    List<Transaction> getTransactionsByMonth(int month, int year);
}