package com.confusinguser.sbgods.utils;

import com.confusinguser.sbgods.SBGods;
import com.confusinguser.sbgods.entities.HypixelGuild;
import com.confusinguser.sbgods.entities.Player;
import com.confusinguser.sbgods.entities.SkillLevels;
import com.confusinguser.sbgods.entities.SlayerExp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class LeaderboardUpdater {

    private static LeaderboardUpdater instance;
    private final SBGods main;

    public LeaderboardUpdater(SBGods main) {
        this.main = main;
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            for (HypixelGuild hypixelGuild : HypixelGuild.values())
                updateLeaderboardCacheForGuild(hypixelGuild);
        }, 0, 9, TimeUnit.MINUTES);
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            try {
                main.getDiscord().verifyAllCommand.verifyAll(main.getDiscord().getJDA().awaitReady().getTextChannelById("713012939258593290"));
                main.getDiscord().verifyAllCommand.verifyAll(main.getDiscord().getJDA().awaitReady().getTextChannelById("713024923945402431"));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, 10, 720, TimeUnit.MINUTES); // Every 12h
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
