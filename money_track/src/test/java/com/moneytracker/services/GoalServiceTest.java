package com.moneytracker.services;

import static org.junit.jupiter.api.Assertions.*;

import com.moneytracker.models.Goal;
import com.moneytracker.repository.IGoalRepository;
import java.util.List;
import org.junit.jupiter.api.Test;

public class GoalServiceTest {

    @Test
    void add_goal_trims_name_and_delegates_to_repository() {

        FakeGoalRepo repo = new FakeGoalRepo();
        GoalService service = new GoalService(repo);

        service.addGoal(" Vacation ", 1000.0, 125.0);

        assertEquals("Vacation", repo.createdName);
        assertEquals(1000.0, repo.createdTargetAmount);
        assertEquals(125.0, repo.createdInitialSaved);
    }

    @Test
    void add_goal_rejects_invalid_input() {

        GoalService service = new GoalService(new FakeGoalRepo());

        assertThrows(IllegalArgumentException.class, () -> service.addGoal(null, 1000.0, 0));
        assertThrows(IllegalArgumentException.class, () -> service.addGoal(" ", 1000.0, 0));
        assertThrows(IllegalArgumentException.class, () -> service.addGoal("Vacation", 0, 0));
        assertThrows(IllegalArgumentException.class, () -> service.addGoal("Vacation", 1000.0, -1));
        assertThrows(IllegalArgumentException.class, () -> service.addGoal("Vacation", 1000.0, 1000.01));
    }

    @Test
    void add_savings_rejects_non_positive_amount() {

        GoalService service = new GoalService(new FakeGoalRepo());

        assertThrows(IllegalArgumentException.class, () -> service.addSavings(4, 0));
        assertThrows(IllegalArgumentException.class, () -> service.addSavings(4, -1));
    }

    @Test
    void goal_operations_delegate_to_repository() {

        FakeGoalRepo repo = new FakeGoalRepo();
        repo.goals = List.of(new Goal(1, "Laptop", 800.0, 300.0));
        GoalService service = new GoalService(repo);

        service.addSavings(1, 50.0);
        service.deleteGoal(1);

        assertEquals(1, repo.savedGoalId);
        assertEquals(50.0, repo.savedAmount);
        assertEquals(1, repo.deletedGoalId);
        assertEquals(repo.goals, service.getAllGoals());
    }

    private static class FakeGoalRepo implements IGoalRepository {
        private String createdName;
        private double createdTargetAmount;
        private double createdInitialSaved;
        private int savedGoalId;
        private double savedAmount;
        private int deletedGoalId;
        private List<Goal> goals = List.of();

        @Override
        public void createGoal(String name, double targetAmount, double initialSaved) {
            createdName = name;
            createdTargetAmount = targetAmount;
            createdInitialSaved = initialSaved;
        }

        @Override
        public void addSavings(int goalId, double amount) {
            savedGoalId = goalId;
            savedAmount = amount;
        }

        @Override
        public void deleteGoal(int goalId) {
            deletedGoalId = goalId;
        }

        @Override
        public List<Goal> getAllGoals() {
            return goals;
        }
    }
}
