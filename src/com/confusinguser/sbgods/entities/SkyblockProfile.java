package com.confusinguser.sbgods.entities;

import com.confusinguser.sbgods.entities.leaderboard.BankBalance;

import java.util.ArrayList;
import java.util.List;

public class SkyblockProfile {

    private final List<Player> members;
    private final String cuteName;
    private final BankBalance balance;

    public SkyblockProfile(List<Player> members, String cuteName, BankBalance balance) {
        this.members = members;
        this.cuteName = cuteName;
        this.balance = balance;
    }

    public SkyblockProfile() {
        this.members = new ArrayList<>();
        this.cuteName = "";
        this.balance = new BankBalance();
    }

    public List<Player> getMembers() {
        return members;
    }

    public String getCuteName() {
        return cuteName;
    }

    public BankBalance getBalance() {
        return balance;
    }
}
