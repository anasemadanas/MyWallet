package com.moneytracker.services;

import java.util.List;
import com.moneytracker.models.Goal;

public interface IGoalService {

    void addGoal(String name, double targetAmount, double initialSaved);

    void addSavings(int goalId, double amount);

    void deleteGoal(int goalId);

    List<Goal> getAllGoals();
}