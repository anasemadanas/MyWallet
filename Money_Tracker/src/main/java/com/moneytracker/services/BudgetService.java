package com.moneytracker.services;

public class BudgetService implements IBudgetService {

    @Override
    public void createBudget(double amount, int month, int year){

    }

    @Override
    public boolean validateBudget(double amount, int month, int year){
        return false;
    }

    @Override
    public boolean checkBudget(int month, int year){
        return false;
    }

    @Override
    public void addToBudget(double amount, int month, int year){

    }
    @Override
    public void deductFromBudget(double amount, int month, int year){

    }

    @Override
    public void deleteBudget(int budgetId){

    }

    @Override
    public void updateBudget(int budgetId, double amount){

    }
    @Override
    public double getBudgetBalance(int month, int year){
        return 0;
    }

}
