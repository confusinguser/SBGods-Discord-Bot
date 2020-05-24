package com.confusinguser.sbgods.entities;

import com.confusinguser.sbgods.SBGods;

import java.util.HashMap;
import java.util.Map;

public enum HypixelGuild {
    SBG("5cd01bdf77ce84cf1204cd61", 300000, 26, "Skyblock Gods", "SBG Guild Member", "SBG"),
    SBDG("5e4e6d0d8ea8c9feb3f0e44f", 1000, 10, "Skyblock Forceful", "SBF", "SBF Guild Member", "SBDG Guild Member", "Skyblock Demigods", "SBDG", "Skyblock Demi Gods");

    private final String guildId;
    private final int skillReq;
    private final int slayerReq;
    private final String[] names;
    private int playerSize = 125;

    private Map<String, SkillLevels> skillExpMap = new HashMap<>();
    private Map<String, SlayerExp> slayerExpHashmap = new HashMap<>();
    private int slayerProgress;
    private int skillProgress;

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
        for (HypixelGuild guild : values()) {
            if (guild.getGuildId().contentEquals(id)) return guild;
        }
        return null;
    }

    public String getGuildId() {
        return guildId;
    }

    public Map<String, SkillLevels> getSkillExpMap() {
        return skillExpMap;
    }

    public void setAvgSkillLevelHashMap(Map<String, SkillLevels> skillExpMap) {
        this.skillExpMap = skillExpMap;
    }

    public Map<String, SlayerExp> getSlayerExpMap() {
        return slayerExpHashmap;
    }

    public void setSlayerExpHashMap(Map<String, SlayerExp> slayerExpMap) {
        this.slayerExpHashmap = slayerExpMap;
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

    public int getSlayerProgress() {
        return slayerProgress;
    }

    public void setSlayerProgress(int slayerProgress) {
        this.slayerProgress = slayerProgress;
    }

    public int getSkillProgress() {
        return skillProgress;
    }

    public void setSkillProgress(int skillProgress) {
        this.skillProgress = skillProgress;
    }

    public String getDisplayName() {
        return names[0];
    }
}
