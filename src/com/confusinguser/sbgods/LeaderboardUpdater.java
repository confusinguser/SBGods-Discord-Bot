package com.confusinguser.sbgods;

import com.confusinguser.sbgods.entities.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class LeaderboardUpdater implements Runnable {

    private static LeaderboardUpdater instance;
    private final SBGods main;

    private boolean alreadyran = false;

    LeaderboardUpdater() {
        this.main = SBGods.getInstance();
        LeaderboardUpdater.instance = this;
    }

    static LeaderboardUpdater getInstance() {
        return instance;
    }

    public void run() {
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(() -> {
            if (System.currentTimeMillis() > 1589014800000L && !alreadyran && System.currentTimeMillis() < 1589018400000L) {
                // 11am 09-05-2020
                alreadyran = true;
                main.getDiscord().skillExpCommand.handleCommand(DiscordServer.SBGods, main.getDiscord().getJDA().getTextChannelById(673619910324387885L), "-skillexp leaderboard all spreadsheet", "Automated Action");
            }
            updateLeaderboardCacheForGuild(HypixelGuild.SBG);
            updateLeaderboardCacheForGuild(HypixelGuild.SBDG);
        }, 0, 9, TimeUnit.MINUTES);
    }

    private void updateLeaderboardCacheForGuild(HypixelGuild guild) {
        guild.setSlayerExpHashMap(getSlayerXPHashMap(guild));
        guild.setAvgSkillLevelHashMap(getAvgSkillLevelHashMap(guild));
    }

    private Map<String, SkillLevels> getAvgSkillLevelHashMap(HypixelGuild guild) {
        Map<String, SkillLevels> usernameSkillLevels = new HashMap<>();
        ArrayList<Player> guildMembers = main.getApiUtil().getGuildMembers(guild);
        guild.setPlayerSize(guildMembers.size());

        for (int i = 0; i < guildMembers.size(); i++) {
            Player thePlayer = main.getApiUtil().getPlayerFromUUID(guildMembers.get(i).getUUID());

            SkillLevels highestSkillLevels = new SkillLevels();
            // Get avg. skill level of the profile that has the highest
            for (String profile : thePlayer.getSkyblockProfiles()) {

                SkillLevels skillLevels = main.getApiUtil().getProfileSkills(profile, thePlayer.getUUID());
                if (highestSkillLevels.getAvgSkillLevel() < skillLevels.getAvgSkillLevel()) {
                    highestSkillLevels = skillLevels;
                }

                if (highestSkillLevels.getAvgSkillLevel() == 0) {
                    skillLevels = main.getApiUtil().getProfileSkillsAlternate(thePlayer.getUUID());

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

    private Map<String, SlayerExp> getSlayerXPHashMap(HypixelGuild guild) {
        Map<String, SlayerExp> usernameSlayerXP = new HashMap<>();
        ArrayList<Player> guildMembers = main.getApiUtil().getGuildMembers(guild);
        guild.setPlayerSize(guildMembers.size());

        for (int i = 0; i < guildMembers.size(); i++) {
            String UUID = guildMembers.get(i).getUUID();
            Player thePlayer = main.getApiUtil().getPlayerFromUUID(UUID);
            usernameSlayerXP.put(thePlayer.getDisplayName(), main.getApiUtil().getPlayerSlayerExp(UUID));
            guild.setSlayerProgress(i + 1);
        }
        return usernameSlayerXP;
    }
}
