package com.confusinguser.sbgods.entities;

import java.util.ArrayList;

public class Player {

    private final String UUID;
    private final String displayName;
    private final String discordTag;
    private final Boolean online;
    private final ArrayList<String> skyblockProfiles;

    public Player() {
        this.UUID = null;
        this.displayName = null;
        this.discordTag = null;
        this.online = false;
        this.skyblockProfiles = new ArrayList<>();
    }

    public Player(String uuid, String displayName, String discordTag, Boolean online, ArrayList<String> skyblockProfiles) {
        this.UUID = uuid;
        this.displayName = displayName;
        this.discordTag = discordTag;
        this.online = online;
        this.skyblockProfiles = skyblockProfiles;
    }

    public String getUUID() {
        return UUID;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDiscordTag() {
        return discordTag;
    }

    public Boolean getIsOnline(){return online;}

    public ArrayList<String> getSkyblockProfiles() {
        return skyblockProfiles;
    }
}
