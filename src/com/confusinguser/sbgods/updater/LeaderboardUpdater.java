package com.confusinguser.sbgods.updater;

import java.util.ArrayList;
import java.util.HashMap;

import com.confusinguser.sbgods.SBGods;
import com.confusinguser.sbgods.Start;

public class LeaderboardUpdater implements Runnable {

	SBGods sbgods;

	private HashMap<String, Double> usernameAverageSkillLevel = new HashMap<String, Double>();

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


	private HashMap<String, Double> getAvgSkillLevelHashMap() {
		ArrayList<String> guildMemberUuids = sbgods.getApiUtil().getGuildMembers();

		for (int i = 0; i < guildMemberUuids.size(); i++) {
			String UUID = guildMemberUuids.get(i);
			ArrayList<String> profiles = sbgods.getApiUtil().getSkyblockProfilesAndUsernameFromUUID(UUID);

			double highestAverageSkillLevel = 0;
			// Get avg. skill level of the profile that has the highest
			for (int j = 1; j < profiles.size(); j++) {
				String profile = profiles.get(j);
				ArrayList<Double> skillLevels = new ArrayList<Double>();
				for (Integer skill : sbgods.getApiUtil().getProfileSkills(profile, UUID)) {
					skillLevels.add(sbgods.getSBUtil().toSkillLevel(skill));
				}
				highestAverageSkillLevel = Math.max(highestAverageSkillLevel, sbgods.getUtil().getAverage(skillLevels));
			}
			usernameAverageSkillLevel.put(profiles.get(0), highestAverageSkillLevel);
		}
		return usernameAverageSkillLevel;
	}

	private HashMap<String, Integer> getSlayerXPHashMap() {
		ArrayList<String> guildMemberUuids = sbgods.getApiUtil().getGuildMembers();
		HashMap<String, Integer> usernameSlayerXP = new HashMap<String, Integer>();

		for (int i = 0; i < guildMemberUuids.size(); i++) {
			String UUID = guildMemberUuids.get(i);
			ArrayList<String> profiles = sbgods.getApiUtil().getSkyblockProfilesAndUsernameFromUUID(UUID);

			int totalSlayerXP = 0;
			// Get how much slayer xp the profile with the most of it has
			for (int j = 1; j < profiles.size(); j++) {
				String profile = profiles.get(j);
				totalSlayerXP += sbgods.getApiUtil().getProfileSlayerXP(profile, UUID);
			}
			usernameSlayerXP.put(profiles.get(0), totalSlayerXP);

		}
		return usernameSlayerXP;
	}
}
