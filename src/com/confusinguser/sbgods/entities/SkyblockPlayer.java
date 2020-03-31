package com.confusinguser.sbgods.entities;

import java.util.ArrayList;

public class SkyblockPlayer {

	private String UUID;
	private String displayName;
	private String discordTag;
	private ArrayList<String> skyblockProfiles;

	public SkyblockPlayer() {
		this.UUID = null;
		this.displayName = null;
		this.discordTag = null;
		this.skyblockProfiles = new ArrayList<String>();
	}

	public SkyblockPlayer(String uuid, String displayName, String discordTag, ArrayList<String> skyblockProfiles) {
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
