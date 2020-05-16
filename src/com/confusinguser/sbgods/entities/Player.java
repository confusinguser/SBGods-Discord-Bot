package com.confusinguser.sbgods.entities;

import java.util.ArrayList;

public class Player {

    private final String UUID;
    private final String displayName;
    private final String discordTag;
    private final ArrayList<String> skyblockProfiles;

    public Player() {
        this.UUID = null;
        this.displayName = null;
        this.discordTag = null;
        this.skyblockProfiles = new ArrayList<>();
    }

    public Player(String uuid, String displayName, String discordTag, ArrayList<String> skyblockProfiles) {
        this.UUID = uuid;
        this.displayName = displayName;
        this.discordTag = discordTag;
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

    public ArrayList<String> getSkyblockProfiles() {
        return skyblockProfiles;
    }
}
