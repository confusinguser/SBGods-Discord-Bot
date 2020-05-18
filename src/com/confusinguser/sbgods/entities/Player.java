package com.confusinguser.sbgods.entities;

import com.confusinguser.sbgods.SBGods;

import java.util.ArrayList;

public class Player {

    private final String UUID;
    private final String displayName;
    private final String discordTag;
    private final boolean online;
    private final SBGods main;
    private final ArrayList<String> skyblockProfiles;

    public Player(SBGods main) {
        this.UUID = null;
        this.displayName = null;
        this.discordTag = null;
        this.online = false;
        this.main = main;
        this.skyblockProfiles = new ArrayList<>();
    }

    public Player(String uuid, String displayName, String discordTag, boolean online, ArrayList<String> skyblockProfiles, SBGods main) {
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
        return main.getApiUtil().getGuildFromUUID(getUUID());
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDiscordTag() {
        return discordTag;
    }

    public boolean getIsOnline() {
        return online;
    }

    public ArrayList<String> getSkyblockProfiles() {
        return skyblockProfiles;
    }

}
