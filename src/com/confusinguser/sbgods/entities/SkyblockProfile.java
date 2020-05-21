package com.confusinguser.sbgods.entities;

import com.confusinguser.sbgods.entities.banking.BankTransaction;

import java.util.ArrayList;
import java.util.List;

public class SkyblockProfile {

    private final List<Player> members;
    private final List<BankTransaction> bankHistory;

    public SkyblockProfile(List<Player> members, List<BankTransaction> bankHistory) {
        this.members = members;
        this.bankHistory = bankHistory;
    }

    public SkyblockProfile() {
        this.members = new ArrayList<>();
        this.bankHistory = new ArrayList<>();
    }

    public List<Player> getMembers() {
        return members;
    }

    public List<BankTransaction> getBankHistory() {
        return bankHistory;
    }
}
