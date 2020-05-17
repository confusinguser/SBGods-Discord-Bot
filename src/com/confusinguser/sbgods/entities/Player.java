package com.confusinguser.sbgods.entities;

import com.confusinguser.sbgods.SBGods;
import net.dv8tion.jda.api.entities.Invite;
import org.json.JSONObject;

import java.util.ArrayList;

public class Player {

    private final String UUID;
    private final String displayName;
    private final String discordTag;
    private final Boolean online;
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

    public Player(String uuid, String displayName, String discordTag, Boolean online, ArrayList<String> skyblockProfiles, SBGods main) {
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

        String response = main.getApiUtil().getResponse("https://api.hypixel.net/" + "findGuild" + "?key=" + main.getNextApiKey() + "&byUuid=" + UUID, 300000);
        if (response == null) return null;

        JSONObject jsonObject = new JSONObject(response);

        return jsonObject.getString("guild");
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
