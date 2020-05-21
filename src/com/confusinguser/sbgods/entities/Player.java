package com.confusinguser.sbgods.entities;

import com.confusinguser.sbgods.SBGods;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Player {

    private final String UUID;
    private final String displayName;
    private final String discordTag;
    private final boolean online;
    private final SBGods main;
    private final List<String> skyblockProfiles;

    public Player(SBGods main) {
        this.UUID = null;
        this.displayName = null;
        this.discordTag = null;
        this.online = false;
        this.main = main;
        this.skyblockProfiles = new ArrayList<>();
    }

    public Player(String uuid, String displayName, String discordTag, boolean online, List<String> skyblockProfiles, SBGods main) {
        this.UUID = uuid;
        this.displayName = displayName;
        this.discordTag = discordTag;
        this.online = online;
        this.main = main;
        this.skyblockProfiles = skyblockProfiles;
    }

    public String getUUID() {
        return UUID;
    }

    public String getGuildId() {
        if (getUUID() == null) {
            return null;
        }
        return main.getApiUtil().getGuildIDFromUUID(getUUID());
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDiscordTag() {
        return discordTag;
    }

    public boolean isOnline() {
        return online;
    }

    public List<String> getSkyblockProfiles() {
        return skyblockProfiles;
    }

    public int getSkillPos() {
        HypixelGuild guild = HypixelGuild.getGuildById(getGuildId());
        if (guild == null) return -1;
        if (guild.getSkillExpMap().isEmpty()) return -2;

        List<Map.Entry<String, SkillLevels>> list = new ArrayList<>(guild.getSkillExpMap().entrySet());
        list.sort(Comparator.comparingDouble(entry -> -entry.getValue().getAvgSkillLevel()));
        return list.stream().map(Map.Entry::getKey).collect(Collectors.toList()).indexOf(getDisplayName());
    }

    public int getSlayerPos() {
        HypixelGuild guild = HypixelGuild.getGuildById(getGuildId());
        if (guild == null) return -1;
        if (guild.getSlayerExpMap().isEmpty()) return -2;

        List<Map.Entry<String, SlayerExp>> list = new ArrayList<>(guild.getSlayerExpMap().entrySet());
        list.sort(Comparator.comparingDouble(entry -> -entry.getValue().getTotalExp()));
        return list.stream().map(Map.Entry::getKey).collect(Collectors.toList()).indexOf(getDisplayName());
    }
}
