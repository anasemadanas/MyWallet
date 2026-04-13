package com.moneytracker.repository;

import java.util.List;
import com.moneytracker.models.Goal;

public interface IGoalRepository {

        void createGoal(String name, double targetAmount, double initialSaved);

        void addSavings(int goalId, double amount);

        void deleteGoal(int goalId);

        List<Goal> getAllGoals();

}