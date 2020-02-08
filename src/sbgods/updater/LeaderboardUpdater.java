package sbgods.updater;

import java.util.ArrayList;
import java.util.HashMap;

import sbgods.SBGods;

public class LeaderboardUpdater implements Runnable {

	SBGods main;

	private HashMap<String, Double> usernameAverageSkillLevel = new HashMap<String, Double>();

	public LeaderboardUpdater(SBGods main) {
		this.main = main;
	}


	public void run() {
		while(!Thread.currentThread().isInterrupted()) {
			updateLeaderboardCache();
			System.out.println("Waiting 5 minutes");
			try {
				Thread.sleep(5 * 60 * 1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void updateLeaderboardCache() {
		main.getDiscord().skillCommand.setAvgSkillLevelHashMap(getAvgSkillLevelHashMap());
		main.getDiscord().slayerCommand.setSlayerXPHashMap(getSlayerXPHashMap());
	}


	private HashMap<String, Double> getAvgSkillLevelHashMap() {
		ArrayList<String> guildMemberUuids = main.getApiUtil().getGuildMembers();

		for (int i = 0; i < guildMemberUuids.size(); i++) {
			String UUID = guildMemberUuids.get(i);
			ArrayList<String> profiles = main.getApiUtil().getSkyblockProfilesAndUsernameFromUUID(UUID);

			double highestAverageSkillLevel = 0;
			// Get avg. skill level of the profile that has the highest
			for (int j = 1; j < profiles.size(); j++) {
				String profile = profiles.get(j);
				ArrayList<Double> skillLevels = new ArrayList<Double>();
				for (Integer skill : main.getApiUtil().getProfileSkills(profile, UUID)) {
					skillLevels.add(main.getUtil().toSkillLevel(skill));
				}
				highestAverageSkillLevel = Math.max(highestAverageSkillLevel, main.getUtil().getAverage(skillLevels));
			}
			usernameAverageSkillLevel.put(profiles.get(0), highestAverageSkillLevel);
			System.out.println("Done with UUID (" + i + "/" + guildMemberUuids.size() + ")");
		}
		return usernameAverageSkillLevel;
	}

	private HashMap<String, Integer> getSlayerXPHashMap() {
		ArrayList<String> guildMemberUuids = main.getApiUtil().getGuildMembers();
		HashMap<String, Integer> usernameSlayerXP = new HashMap<String, Integer>();

		for (int i = 0; i < guildMemberUuids.size(); i++) {
			String UUID = guildMemberUuids.get(i);
			ArrayList<String> profiles = main.getApiUtil().getSkyblockProfilesAndUsernameFromUUID(UUID);

			int totalSlayerXP = 0;
			// Get how much slayer xp the profile with the most of it has
			for (int j = 1; j < profiles.size(); j++) {
				String profile = profiles.get(j);
				totalSlayerXP += main.getApiUtil().getProfileSlayerXP(profile, UUID);
			}
			System.out.println("Done with UUID (" + Math.incrementExact(i) + "/" + guildMemberUuids.size() + ")");
			usernameSlayerXP.put(profiles.get(0), totalSlayerXP);

		}
		return usernameSlayerXP;
	}
}
