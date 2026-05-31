package com.moneytracker.services;

import com.moneytracker.models.Goal;
import com.moneytracker.repository.GoalRepositoryImpl;
import com.moneytracker.repository.IGoalRepository;

import java.util.List;

public class GoalService implements IGoalService {
    private final IGoalRepository goalRepository;

    public GoalService() {
        this(new GoalRepositoryImpl());
    }

    public GoalService(int userId) {
        this(new GoalRepositoryImpl(userId));
    }

    public GoalService(IGoalRepository goalRepository) {
        this.goalRepository = goalRepository;
    }

    @Override
    public void addGoal(String name, double targetAmount, double initialSaved) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Goal name cannot be empty");
        }
        if (targetAmount <= 0) {
            throw new IllegalArgumentException("Target amount must be greater than 0");
        }
        if (initialSaved < 0 || initialSaved > targetAmount) {
            throw new IllegalArgumentException("Initial savings must be between 0 and the target amount");
        }
        goalRepository.createGoal(name.trim(), targetAmount, initialSaved);
    }

    @Override
    public void addSavings(int goalId, double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0");
        }
        goalRepository.addSavings(goalId, amount);
    }

    @Override
    public void deleteGoal(int goalId) {
        goalRepository.deleteGoal(goalId);
    }

    @Override
    public List<Goal> getAllGoals() {
        return goalRepository.getAllGoals();
    }
}
