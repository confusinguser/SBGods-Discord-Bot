package com.confusinguser.sbgods.entities.leaderboard;

public class BankBalance extends LeaderboardValue {

    private final double balance;

    public BankBalance(double balance) {
        this.balance = balance;
    }

    public double getCoins() {
        return balance;
    }

    @Override
    public double getValue() {
        return getCoins();
    }

    @Override
    public double getSecondaryValue() {
        return 0;
    }

    @Override
    public boolean isApproximate() {
        return false;
    }
}
