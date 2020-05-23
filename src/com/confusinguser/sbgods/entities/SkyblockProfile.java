package com.confusinguser.sbgods.entities;

import com.confusinguser.sbgods.entities.banking.BankTransaction;

import java.util.ArrayList;
import java.util.List;

public class SkyblockProfile {

    private final List<Player> members;
    private final List<BankTransaction> bankHistory;
    private final double balance;

    public SkyblockProfile(List<Player> members, List<BankTransaction> bankHistory, double balance) {
        this.members = members;
        this.bankHistory = bankHistory;
        this.balance = balance;
    }

    public SkyblockProfile() {
        this.members = new ArrayList<>();
        this.bankHistory = new ArrayList<>();
        this.balance = 0;
    }

    public List<Player> getMembers() {
        return members;
    }

    public List<BankTransaction> getBankHistory() {
        return bankHistory;
    }

    public double getBalance() {
        return balance;
    }
}
