package com.moneytracker.repository;

import java.util.List;
import com.moneytracker.models.Goal;

public interface IGoalRepository {

        void createGoal(String name, double targetAmount, double initialSaved);

        List<Goal> getAllGoals();

        void addSavings(int goalId, double amount);

        void deleteGoal(int goalId);

}

