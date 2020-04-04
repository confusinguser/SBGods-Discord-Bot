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
		guild.setSlayerExpHashMap(getSlayerXPHashMap(guild));
		guild.setAvgSkillLevelHashMap(getAvgSkillLevelHashMap(guild));
	}

	private HashMap<String, SkillLevels> getAvgSkillLevelHashMap(HypixelGuild guild) {
		HashMap<String, SkillLevels> usernameSkillLevels = new HashMap<String, SkillLevels>();
		ArrayList<SkyblockPlayer> guildMembers = sbgods.getApiUtil().getGuildMembers(guild);
		guild.setPlayerSize(guildMembers.size());

		for (int i = 0; i < guildMembers.size(); i++) {
			SkyblockPlayer thePlayer = sbgods.getApiUtil().getSkyblockPlayerFromUUID(guildMembers.get(i).getUUID());

			SkillLevels highestSkillLevels = new SkillLevels();
			// Get avg. skill level of the profile that has the highest
			for (String profile : thePlayer.getSkyblockProfiles()) {

				SkillLevels skillLevels = sbgods.getApiUtil().getProfileSkills(profile, thePlayer.getUUID());
				if (highestSkillLevels.getAvgSkillLevel() < skillLevels.getAvgSkillLevel()) {
					highestSkillLevels = skillLevels;
				}

				if (highestSkillLevels.getAvgSkillLevel() == 0) {
					skillLevels = sbgods.getApiUtil().getProfileSkillsAlternate(thePlayer.getUUID());

					if (highestSkillLevels.getAvgSkillLevel() < skillLevels.getAvgSkillLevel()) {
						highestSkillLevels = skillLevels;
					}
				}
			}
			usernameSkillLevels.put(thePlayer.getDisplayName(), highestSkillLevels);
			guild.setSkillProgress(i + 1);
		}
		return usernameSkillLevels;
	}

	private HashMap<String, SlayerExp> getSlayerXPHashMap(HypixelGuild guild) {
		HashMap<String, SlayerExp> usernameSlayerXP = new HashMap<String, SlayerExp>();
		ArrayList<SkyblockPlayer> guildMembers = sbgods.getApiUtil().getGuildMembers(guild);
		guild.setPlayerSize(guildMembers.size());
		
		for (int i = 0; i < guildMembers.size(); i++) {
			String UUID = guildMembers.get(i).getUUID();
			SkyblockPlayer thePlayer = sbgods.getApiUtil().getSkyblockPlayerFromUUID(UUID);
			usernameSlayerXP.put(thePlayer.getDisplayName(), sbgods.getApiUtil().getPlayerSlayerExp(UUID));
			guild.setSlayerProgress(i + 1);
		}
		return usernameSlayerXP;
	}
}
