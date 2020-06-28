package com.confusinguser.sbgods.utils;

import com.confusinguser.sbgods.SBGods;
import com.confusinguser.sbgods.entities.HypixelGuild;
import com.confusinguser.sbgods.entities.Player;
import com.confusinguser.sbgods.entities.SkillLevels;
import com.confusinguser.sbgods.entities.SlayerExp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
                updateLeaderboardCache(hypixelGuild);
        }, 0, 9, TimeUnit.MINUTES);
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            try {
                main.getDiscord().verifyAllCommand.verifyAll(main.getDiscord().getJDA().awaitReady().getTextChannelById("713012939258593290"));
                main.getDiscord().verifyAllCommand.verifyAll(main.getDiscord().getJDA().awaitReady().getTextChannelById("713024923945402431"));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, 10, 360, TimeUnit.MINUTES); // Every 6h
    }

    private void updateLeaderboardCache(HypixelGuild guild) {
        Map<String, SkillLevels> skillLevelMap = new HashMap<>();
        Map<String, SlayerExp> slayerExpMap = new HashMap<>();
        Map<String, Double> totalCoinsMap = new HashMap<>();

        ArrayList<Player> guildMembers = main.getApiUtil().getGuildMembers(guild);


        int i = 0;

        for (Player guildMember : guildMembers) {
            Player thePlayer = main.getApiUtil().getPlayerFromUUID(guildMember.getUUID());
            SkillLevels highestSkillLevels = main.getApiUtil().getBestProfileSkillLevels(thePlayer.getUUID());
            SlayerExp totalSlayerExp = main.getApiUtil().getPlayerSlayerExp(thePlayer.getUUID());
            double totalCoins = main.getApiUtil().getTotalCoinsInPlayer(thePlayer.getUUID());
            skillLevelMap.put(thePlayer.getDisplayName(), highestSkillLevels == null ? new SkillLevels() : highestSkillLevels);
            slayerExpMap.put(thePlayer.getDisplayName(), totalSlayerExp);
            totalCoinsMap.put(thePlayer.getDisplayName(), totalCoins);
            guild.setLeaderboardProgress(i++);
        }

        guild.setSlayerExpMap(slayerExpMap);
        guild.setAvgSkillLevelMap(skillLevelMap);
        guild.setTotalCoinsMap(totalCoinsMap);
    }

    private void updateLeaderboardCacheFast(HypixelGuild guild) {
        Map<String, SkillLevels> skillLevelMap = new HashMap<>();
        Map<String, SlayerExp> slayerExpMap = new HashMap<>();
        Map<String, Double> totalCoinsMap = new HashMap<>();

        ArrayList<Player> guildMembers = main.getApiUtil().getGuildMembers(guild);


        final int[] i = {0};

        List<Thread> threads = new ArrayList<>();

        for (Player guildMember : guildMembers) {
            Runnable target = () -> {
                Player thePlayer = main.getApiUtil().getPlayerFromUUID(guildMember.getUUID());
                SkillLevels highestSkillLevels = main.getApiUtil().getBestProfileSkillLevels(thePlayer.getUUID());
                SlayerExp totalSlayerExp = main.getApiUtil().getPlayerSlayerExp(thePlayer.getUUID());
                double totalCoins = main.getApiUtil().getTotalCoinsInPlayer(thePlayer.getUUID());
                skillLevelMap.put(thePlayer.getDisplayName(), highestSkillLevels == null ? new SkillLevels() : highestSkillLevels);
                slayerExpMap.put(thePlayer.getDisplayName(), totalSlayerExp);
                totalCoinsMap.put(thePlayer.getDisplayName(), totalCoins);
                guild.setLeaderboardProgress(i[0]++);
            };
            threads.add(new Thread(target));
            threads.get(threads.size() - 1).start();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        guild.setSlayerExpMap(slayerExpMap);
        guild.setAvgSkillLevelMap(skillLevelMap);
        guild.setTotalCoinsMap(totalCoinsMap);
    }
}