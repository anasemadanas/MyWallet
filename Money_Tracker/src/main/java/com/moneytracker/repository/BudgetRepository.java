package com.moneytracker.repository;

import com.moneytracker.models.Budget;

public class BudgetRepository implements IBudgetRepository {

    @Override
    public void createBudget(double amount, int month, int year){

    }
    @Override
    public boolean checkBudget(int month, int year){
            return false;
    }
    @Override
    public void addToBudget(double amount, int month, int year){

    }
    @Override
    public void deductFromBudget(double amountSpent, int month, int year){

    }
    @Override
    public void updateBudget(int budgetId, double amount){

    }
    @Override
    public void deleteBudget(int budgetId){

    }
    @Override
    public void increaseTotalBudget(double amount, int month, int year){

    }
    @Override
    public double getBudgetBalance(int month, int year){
        return 0;
    }

    @Override
    public Budget getBudget(int month, int year){
        return null;
    }

}
