package com.moneytracker.models;

public class Goal {
    private final Integer id;
    private final String name;
    private final double targetAmount;
    private final double savedAmount;

    public Goal(Integer id, String name, double targetAmount, double savedAmount) {
        this.id = id;
        this.name = name;
        this.targetAmount = targetAmount;
        this.savedAmount = savedAmount;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getTargetAmount() {
        return targetAmount;
    }

    public double getSavedAmount() {
        return savedAmount;
    }

    public int getProgressPercent() {
        if (targetAmount <= 0) {
            return 0;
        }
        return Math.min((int) ((savedAmount / targetAmount) * 100), 100);
    }

    public boolean isComplete() {
        return savedAmount >= targetAmount;
    }
}
