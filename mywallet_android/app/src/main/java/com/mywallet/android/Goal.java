package com.mywallet.android;

public class Goal {
    public final int id;
    public final String name;
    public final double targetAmount;
    public final double savedAmount;

    public Goal(int id, String name, double targetAmount, double savedAmount) {
        this.id = id;
        this.name = name;
        this.targetAmount = targetAmount;
        this.savedAmount = savedAmount;
    }

    public int progressPercent() {
        if (targetAmount <= 0) {
            return 0;
        }
        return Math.min(100, (int) ((savedAmount / targetAmount) * 100));
    }

    @Override
    public String toString() {
        return "#" + id + "  " + name + "  " + savedAmount + "/" + targetAmount + " (" + progressPercent() + "%)";
    }
}
