package com.confusinguser.sbgods.entities;

import com.confusinguser.sbgods.SBGods;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Player {

    private final String UUID;
    private final String displayName;
    private final String discordTag;
    private final boolean online;
    private final SBGods main;
    private final int lastLogin;
    private final int lastLogout;
    private final List<String> skyblockProfiles;
    private String guildRank = null;

    public Player() {
        this.UUID = null;
        this.displayName = null;
        this.discordTag = null;
        this.online = false;
        this.lastLogin = 0;
        this.lastLogout = 0;
        this.main = SBGods.getInstance();
        this.skyblockProfiles = new ArrayList<>();
    }

    public Player(String uuid, String displayName, String discordTag, int lastLogin, int lastLogout, List<String> skyblockProfiles) {
        this.UUID = uuid;
        this.displayName = displayName;
        this.discordTag = discordTag;
        this.online = lastLogin > lastLogout;
        this.lastLogin = lastLogin;
        this.lastLogout = lastLogout;
        this.main = SBGods.getInstance();
        this.skyblockProfiles = skyblockProfiles;
    }

    public Player(String uuid, String guildRank, int guildJoined) {
        this.UUID = uuid;
        this.displayName = null;
        this.discordTag = null;
        this.guildRank = guildRank;
        this.online = false;
        this.lastLogin = 0;
        this.lastLogout = 0;
        this.main = SBGods.getInstance();
        this.skyblockProfiles = null;
    }

    public Player(String uuid, String displayName, String discordTag, int lastLogin, int lastLogout, List<String> skyblockProfiles, String guildRank) {
        this.UUID = uuid;
        this.displayName = displayName;
        this.discordTag = discordTag;
        this.online = lastLogin > lastLogout;
        this.lastLogin = lastLogin;
        this.lastLogout = lastLogout;
        this.main = SBGods.getInstance();
        this.skyblockProfiles = skyblockProfiles;
        this.guildRank = guildRank;
    }

    public static Player mergePlayerAndGuildMember(Player player, Player guildMember) {
        return new Player(player.UUID, player.displayName, player.discordTag, player.lastLogin, player.lastLogout, player.skyblockProfiles, guildMember.guildRank);
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

    public String getGuildRank() {
        return guildRank;
    }

    public String getDiscordTag() {
        return discordTag;
    }

    public int getLastLogin() {
        return lastLogin;
    }

    public int getLastLogout() {
        return lastLogout;
    }

    public boolean isOnline() {
        return online;
    }

    public List<String> getSkyblockProfiles() {
        return skyblockProfiles;
    }

    /**
     * @return The skill leaderboard position or {@code -1} if player is not in guild or {@code -2} if bot is still loading
     */
    public int getSkillPos(boolean includeStaff) {
        if (getGuildId() == null) return -1;
        HypixelGuild guild = HypixelGuild.getGuildById(getGuildId());
        if (guild == null) return -1;
        if (guild.getSkillExpMap().isEmpty()) return -2;

        List<Map.Entry<Player, SkillLevels>> list = new ArrayList<>(guild.getSkillExpMap().entrySet());
        if (!includeStaff) {
            list = list.stream().filter(player -> Stream.of("Trial Helper", "Helper", "Moderator", "Officer", "Bot Developer", "Co Owner").anyMatch(s -> !player.getKey().getGuildRank().contains(s))).collect(Collectors.toList());
        }
        list.sort(Comparator.comparingDouble(entry -> -entry.getValue().getAvgSkillLevel()));
        return list.stream().map(Map.Entry::getKey).map(Player::getDisplayName).collect(Collectors.toList()).indexOf(getDisplayName());
    }

    /**
     * @return The slayer leaderboard position or {@code -1} if player is not in guild or {@code -2} if bot is still loading
     */
    public int getSlayerPos(boolean includeStaff) {
        if (getGuildId() == null) return -1;
        HypixelGuild guild = HypixelGuild.getGuildById(getGuildId());
        if (guild == null) return -1;
        if (guild.getSlayerExpMap().isEmpty()) return -2;

        List<Map.Entry<Player, SlayerExp>> list = new ArrayList<>(guild.getSlayerExpMap().entrySet());
        if (!includeStaff) {
            list = list.stream().filter(player -> Stream.of("Trial Helper", "Helper", "Moderator", "Officer", "Bot Developer", "Co Owner").anyMatch(s -> !player.getKey().getGuildRank().contains(s))).collect(Collectors.toList());
        }
        list.sort(Comparator.comparingDouble(entry -> -entry.getValue().getTotalExp()));
        return list.stream().map(Map.Entry::getKey).map(Player::getDisplayName).collect(Collectors.toList()).indexOf(getDisplayName());
    }
}
