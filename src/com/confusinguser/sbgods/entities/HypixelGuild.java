package com.confusinguser.sbgods.entities;

import java.util.HashMap;
import java.util.Map;

public enum HypixelGuild {
    SBG("5cd01bdf77ce84cf1204cd61", 500000, 26, "Skyblock Gods", "SBG Guild Member", "SBG"),
    SBDG("5e4e6d0d8ea8c9feb3f0e44f", 50000, 18, "Skyblock Forceful", "SBF", "SBF Guild Member", "SBDG Guild Member", "Skyblock Demigods", "SBDG", "Skyblock Demi Gods");

    private final String guildId;
    private final int skillReq;
    private final int slayerReq;
    private final String[] names;
    private int playerSize = 125;

    private Map<Player, SkillLevels> skillExpMap = new HashMap<>();
    private Map<Player, SlayerExp> slayerExpMap = new HashMap<>();
    private Map<Player, Double> totalCoinsMap = new HashMap<>();
    private int leaderboardProgress;

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

    public Map<Player, SkillLevels> getSkillExpMap() {
        return skillExpMap;
    }

    public void setAvgSkillLevelMap(Map<Player, SkillLevels> skillLevelMap) {
        this.skillExpMap = skillLevelMap;
    }

    public Map<Player, SlayerExp> getSlayerExpMap() {
        return slayerExpMap;
    }

    public void setSlayerExpMap(Map<Player, SlayerExp> slayerExpMap) {
        this.slayerExpMap = slayerExpMap;
    }

    public Map<Player, Double> getTotalCoinsMap() {
        return totalCoinsMap;
    }

    public void setTotalCoinsMap(Map<Player, Double> totalCoinsMap) {
        this.totalCoinsMap = totalCoinsMap;
    }

    public boolean isAltNameIgnoreCase(String input) {
        for (String name : names) {
            if (name.equalsIgnoreCase(input)) {
                return true;
            }
        }
        return false;
    }

    public int getPlayerSize() {
        return playerSize;
    }

    public void setPlayerSize(int playerSize) {
        this.playerSize = playerSize;
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
}
