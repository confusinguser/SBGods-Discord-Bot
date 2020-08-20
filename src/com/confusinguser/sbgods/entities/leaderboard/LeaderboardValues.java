package com.confusinguser.sbgods.entities.leaderboard;

public class LeaderboardValues {
    private final SlayerExp slayerExp;
    private final BankBalance bankBalance;
    private final SkillLevels skillLevels;

    public LeaderboardValues(SlayerExp slayerExp, BankBalance bankBalance, SkillLevels skillLevels) {
        this.slayerExp = slayerExp;
        this.bankBalance = bankBalance;
        this.skillLevels = skillLevels;
    }

    public SlayerExp getSlayerExp() {
        return slayerExp;
    }

    public BankBalance getBankBalance() {
        return bankBalance;
    }

    public SkillLevels getSkillLevels() {
        return skillLevels;
    }
}
