package com.confusinguser.sbgods.entities;

import com.confusinguser.sbgods.entities.leaderboard.LeaderboardValues;

import java.util.HashMap;
import java.util.Map;

public enum HypixelGuild {
    SBG("5fea32eb8ea8c9724b8e3f3c", 750000, 30, "Skyblock Gods", "SBG Guild Member", "SBG"),
    SBF("5e4e6d0d8ea8c9feb3f0e44f", 150000, 24, "Skyblock Forceful", "SBF Guild Member", "SBF");

    private final String guildId;
    private final int skillReq;
    private final int slayerReq;
    private final String[] names;

    private Map<Player, LeaderboardValues> playerStatMap = new HashMap<>();
    private int leaderboardProgress;
    private int playerSize = 125;

    HypixelGuild(String guildId, int slayerReq, int skillReq, String... names) {
        this.guildId = guildId;
        this.slayerReq = slayerReq;
        this.skillReq = skillReq;
        this.names = names;
    }

    public static HypixelGuild getGuildByName(String input) {
        for (HypixelGuild guild : values()) {
            if (guild.isAltNameIgnoreCase(input)) {
                return guild;
            }
        }
        return null;
    }

    public static HypixelGuild getGuildById(String id) {
        if (id == null) return null;
        for (HypixelGuild guild : values()) {
            if (guild.getGuildId().contentEquals(id)) return guild;
        }
        return null;
    }

    public String getGuildId() {
        return guildId;
    }

    public Map<Player, LeaderboardValues> getPlayerStatMap() {
        return playerStatMap;
    }

    public void setPlayerStatMap(Map<Player, LeaderboardValues> playerStatMap) {
        this.playerStatMap = playerStatMap;
    }

    public boolean isAltNameIgnoreCase(String input) {
        for (String name : names) {
            if (name.equalsIgnoreCase(input)) {
                return true;
            }
        }
        return false;
    }

    public int getSkillReq() {
        return skillReq;
    }

    public int getSlayerReq() {
        return slayerReq;
    }

    public int getLeaderboardProgress() {
        return leaderboardProgress;
    }

    public void setLeaderboardProgress(int leaderboardProgress) {
        this.leaderboardProgress = leaderboardProgress;
    }

    public String getDisplayName() {
        return names[0];
    }

    public int getPlayerSize() {
        return playerSize;
    }

    public void setPlayerSize(int playerSize) {
        this.playerSize = playerSize;
    }
}
