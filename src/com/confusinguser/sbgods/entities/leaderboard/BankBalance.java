package com.confusinguser.sbgods.entities.leaderboard;

public class BankBalance implements LeaderboardValue {

    private final double balance;

    public BankBalance(double balance) {
        this.balance = balance;
    }

    public double getBalance() {
        return balance;
    }

    @Override
    public double getValue() {
        return getBalance();
    }
}
