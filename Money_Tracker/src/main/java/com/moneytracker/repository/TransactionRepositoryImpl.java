package com.moneytracker.repository;

import com.moneytracker.models.Transaction;

import java.util.List;

public class TransactionRepositoryImpl implements ITransactionRepository {
    @Override
    public void addTransaction(Transaction transaction){

    }
    @Override
    public void deleteTransaction(int transactionId)
    {

    }
    @Override
    public void updateTransaction(int transactionId, double newAmount, int newMonth, int newYear)
    {

    }
    @Override
    public List<Transaction> getTransactions()
    {
        return null;
    }

    @Override
    public Transaction getTransactionById(int transactionId)
    {
        return null;
    }
    @Override
    public List<Transaction> getTransactionsByMonth(int month, int year)
    {
        return null;
    }
}
