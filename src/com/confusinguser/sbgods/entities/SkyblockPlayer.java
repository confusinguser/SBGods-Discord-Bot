package com.confusinguser.sbgods.entities;

import java.util.ArrayList;

public class SkyblockPlayer {

	private String UUID;
	private String displayName;
	private ArrayList<String> skyblockProfiles;

	public SkyblockPlayer() {
		this.UUID = "";
		this.displayName = "";
		this.skyblockProfiles = new ArrayList<String>();
	}

	public SkyblockPlayer(String uuid, String displayName, ArrayList<String> skyblockProfiles) {
		this.UUID = uuid;
		this.displayName = displayName;
		this.skyblockProfiles = skyblockProfiles;
	}

	public String getUUID() {
		return UUID;
	}

	public String getDisplayName() {
		return displayName;
	}

	public ArrayList<String> getSkyblockProfiles() {
		return skyblockProfiles;
	}
}
