package com.confusinguser.sbgods.entities;

import java.util.HashMap;

public enum HypixelGuild {
	SBG ("5cd01bdf77ce84cf1204cd61", 250000, 822425, "Skyblock Gods", "SBG"),
	SBDG("5e4e6d0d8ea8c9feb3f0e44f", 30000, 222425, "Skyblock Demigods", "SBDG", "Skyblock Demi Gods");

	private String guildId;
	private int slayerExpRec;
	private int skillExpRec;
	private String[] names;
	
	private HashMap<String, SkillLevels> skillExpHashmap = new HashMap<String, SkillLevels>();
	private HashMap<String, SlayerExp> slayerExpHashmap = new HashMap<String, SlayerExp>();

	private HypixelGuild(String guildId, int slayerExpRec, int skillExpReq, String... names) {
		this.guildId = guildId;
		this.slayerExpRec = slayerExpRec;
		this.skillExpRec = skillExpReq;
		this.names = names;
	}

	public String getGuildId() {
		return guildId;
	}
	
	public int getSlayerExpRec() {
		return slayerExpRec;
	}

	public int getSkillExpRec() {
		return skillExpRec;
	}

	public HashMap<String, SkillLevels> getSkillExpHashmap() {
		return skillExpHashmap;
	}

	public void setAvgSkillLevelHashMap(HashMap<String, SkillLevels> skillExpHashmap) {
		this.skillExpHashmap = skillExpHashmap;
	}

	public HashMap<String, SlayerExp> getSlayerExpHashmap() {
		return slayerExpHashmap;
	}

	public void setSlayerExpHashMap(HashMap<String, SlayerExp> slayerExpHashmap) {
		this.slayerExpHashmap = slayerExpHashmap;
	}

	public boolean isAltNameIgnoreCase(String input) {
		for (String name : names) {
			if (name.equalsIgnoreCase(input)) {
				return true;
			}
		}
		return false;
	}

	public static HypixelGuild getEnum(String input) {
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
}
