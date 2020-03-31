package com.confusinguser.sbgods;

import java.util.ArrayList;
import java.util.HashMap;

import com.confusinguser.sbgods.entities.HypixelGuild;
import com.confusinguser.sbgods.entities.SkillLevels;
import com.confusinguser.sbgods.entities.SkyblockPlayer;
import com.confusinguser.sbgods.entities.SlayerExp;

public class LeaderboardUpdater implements Runnable {

	public static LeaderboardUpdater instance;

	SBGods sbgods;

	int slayerProgress;
	int skillProgress;

	public LeaderboardUpdater() {
		this.sbgods = Start.sbgods;
		LeaderboardUpdater.instance = this;
	}

	public void run() {
		while(true) {
			updateLeaderboardCacheForGuild(HypixelGuild.SBG);
			updateLeaderboardCacheForGuild(HypixelGuild.SBDG);
			try {
				Thread.sleep(5 * 60 * 1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void updateLeaderboardCacheForGuild(HypixelGuild guild) {
		guild.setAvgSkillLevelHashMap(getAvgSkillLevelHashMap(guild));
		guild.setSlayerExpHashMap(getSlayerXPHashMap(guild));
	}

	private HashMap<String, SkillLevels> getAvgSkillLevelHashMap(HypixelGuild guild) {
		HashMap<String, SkillLevels> usernameSkillLevels = new HashMap<String, SkillLevels>();
		ArrayList<SkyblockPlayer> guildMembers = sbgods.getApiUtil().getGuildMembers(guild);

		for (int i = 0; i < guildMembers.size(); i++) {
			String UUID = guildMembers.get(i).getUUID();
			SkyblockPlayer thePlayer = sbgods.getApiUtil().getSkyblockPlayerFromUUID(UUID);

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

	private HashMap<String, SlayerExp> getSlayerXPHashMap(HypixelGuild guild) {
		ArrayList<SkyblockPlayer> guildMembers = sbgods.getApiUtil().getGuildMembers(guild);
		HashMap<String, SlayerExp> usernameSlayerXP = new HashMap<String, SlayerExp>();

		for (int i = 0; i < guildMembers.size(); i++) {
			String UUID = guildMembers.get(i).getUUID();
			SkyblockPlayer thePlayer = sbgods.getApiUtil().getSkyblockPlayerFromUUID(UUID);
			usernameSlayerXP.put(thePlayer.getDisplayName(), sbgods.getApiUtil().getPlayerSlayerExp(UUID));
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
