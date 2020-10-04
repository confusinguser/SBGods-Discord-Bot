package com.confusinguser.sbgods.entities.leaderboard;

import org.json.JSONException;
import org.json.JSONObject;

public class LeaderboardValues {
    private final SlayerExp slayerExp;
    private final BankBalance bankBalance;
    private final SkillLevels skillLevels;
    private final DungeonExps dungeonExps;

    public LeaderboardValues(SlayerExp slayerExp, BankBalance bankBalance, SkillLevels skillLevels, DungeonExps dungeonExps) {
        this.slayerExp = slayerExp;
        this.bankBalance = bankBalance;
        this.skillLevels = skillLevels;
        this.dungeonExps = dungeonExps;
    }

    public static LeaderboardValues fromJSON(JSONObject data) {
        SlayerExp slayerExp = null;
        try {
            slayerExp = new SlayerExp(data.getInt("slayerZombie"), data.getInt("slayerSpider"), data.getInt("slayerWolf"));
        } catch (JSONException ignored) {
        }
        SkillLevels skillLevels = null;
        try {
            skillLevels = new SkillLevels(data.getInt("skillAlchemy"), data.getInt("skillCombat"),
                    data.getInt("skillEnchanting"), data.getInt("skillFarming"),
                    data.getInt("skillFishing"), data.getInt("skillForaging"),
                    data.getInt("skillMining"), data.getInt("skillTaming"),
                    data.getInt("skillCarpentry"), data.getInt("skillRunecrafting"), false);
        } catch (JSONException ignored) {
        }
        DungeonExps dungeonExps = null;
        try {
            dungeonExps = new DungeonExps(data.getDouble("dungeonClassHealer"),
                    data.getDouble("dungeonClassMage"),
                    data.getDouble("dungeonClassBerserk"),
                    data.getDouble("dungeonClassArcher"),
                    data.getDouble("dungeonClassTank"),
                    data.getDouble("dungeonDungeonCatacombs"));
        } catch (JSONException ignored) {
        }
        return new LeaderboardValues(slayerExp, null, skillLevels, dungeonExps);
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

    public DungeonExps getDungeonLevels() {
        return dungeonExps;
    }
}
