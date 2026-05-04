package com.moneytracker.services;

import com.moneytracker.models.Transaction;

import java.util.List;

public class TransactionService implements ITransactionService {

    @Override
    public void addTransaction(double amountTrans, String category, int month, int year)
    {

    }

    @Override
    public boolean validateTransaction(double amountTrans, int month, int year, double budget){
        return true;
    }

    @Override
    public double getBudgetBalance(int month, int year){
        return 0;

    }

    // -----------------------
    @Override
    public List<Transaction> GetTransactions(){
        return null;
    }

    @Override
    public void editTransaction(int tid, double newAmount, int month, int year){

    }

    @Override
    public void deleteTransaction(int transactionId){

    }
}
