package com.confusinguser.sbgods;

import java.util.ArrayList;
import java.util.HashMap;

import com.confusinguser.sbgods.entities.SkillLevels;
import com.confusinguser.sbgods.entities.SkyblockPlayer;

public class LeaderboardUpdater implements Runnable {

	SBGods sbgods;

	int slayerProgress;
	int skillProgress;

	public LeaderboardUpdater() {
		this.sbgods = Start.sbgods;
	}

	public void run() {
		while(true) {
			updateLeaderboardCache();
			try {
				Thread.sleep(5 * 60 * 1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void updateLeaderboardCache() {
		sbgods.getDiscord().skillCommand.setAvgSkillLevelHashMap(getAvgSkillLevelHashMap());
		sbgods.getDiscord().slayerCommand.setSlayerXPHashMap(getSlayerXPHashMap());
	}


	private HashMap<String, SkillLevels> getAvgSkillLevelHashMap() {
		HashMap<String, SkillLevels> usernameSkillLevels = new HashMap<String, SkillLevels>();
		ArrayList<SkyblockPlayer> guildMembers = sbgods.getApiUtil().getGuildMembers();

		for (int i = 0; i < guildMembers.size(); i++) {
			String UUID = guildMembers.get(i).getUUID();
			SkyblockPlayer thePlayer = sbgods.getApiUtil().getSkyblockProfilesAndUsernameFromUUID(UUID);

			SkillLevels highestSkillLevels = new SkillLevels();
			// Get avg. skill level of the profile that has the highest
			for (String profile : thePlayer.getSkyblockProfiles()) {

				SkillLevels skillLevels = sbgods.getApiUtil().getProfileSkills(profile, UUID);
				if (highestSkillLevels.getAvgSkillLevel() < skillLevels.getAvgSkillLevel()) {
					highestSkillLevels = skillLevels;
				}

				if (highestSkillLevels.getAvgSkillLevel() == 0) {
					skillLevels = sbgods.getApiUtil().getProfileSkillsAlternate(UUID);

					if (highestSkillLevels.getAvgSkillLevel() < skillLevels.getAvgSkillLevel()) {
						highestSkillLevels = skillLevels;
					}
				}
			}
			usernameSkillLevels.put(thePlayer.getDisplayName(), highestSkillLevels);
		}
		return usernameSkillLevels;
	}

	private HashMap<String, Integer> getSlayerXPHashMap() {
		ArrayList<SkyblockPlayer> guildMembers = sbgods.getApiUtil().getGuildMembers();
		HashMap<String, Integer> usernameSlayerXP = new HashMap<String, Integer>();

		for (int i = 0; i < guildMembers.size(); i++) {
			String UUID = guildMembers.get(i).getUUID();
			SkyblockPlayer thePlayer = sbgods.getApiUtil().getSkyblockProfilesAndUsernameFromUUID(UUID);

			int totalSlayerXP = sbgods.getApiUtil().getPlayerSlayerExp(UUID).getTotalExp();
			usernameSlayerXP.put(thePlayer.getDisplayName(), totalSlayerXP);

		}
		return usernameSlayerXP;
	}

	public int getSlayerProgress() {
		return slayerProgress;
	}

	public int getSkillProgress() {
		return skillProgress;
	}
}
