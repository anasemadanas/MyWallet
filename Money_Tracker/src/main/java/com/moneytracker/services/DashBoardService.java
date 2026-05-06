package com.moneytracker.services;
import java.util.List;
import java.util.Map;

import com.moneytracker.models.Transaction;



public class DashBoardService implements IDashBoardService {
    @Override
    public void showAbout(){

    }
    @Override
    public void openGuide(){

    }
    @Override
    public void saveData(int month, int year) {

    }
    @Override
    public void exportData(int month, int year){

    }

    @Override
    public double getCurrentMonthBalance(){
        return 0;
    }

    @Override
    public double getBalanceForMonth(int month, int year){
        return 0;
    }

    @Override
    public String getBudgetSummary(int month, int year){
        return null;
    }
    @Override
    public Map<String, Double> getBudgetForCategory(int month, int year){
        return null;
    }
    @Override
    public List<Transaction> getAllTransactions(){
        return null;
    }
    @Override
    public List<Transaction> getTransactionsForMonth(int month, int year){
        return null;
    }

}
