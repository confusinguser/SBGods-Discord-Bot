package com.confusinguser.sbgods.utils;

import com.confusinguser.sbgods.SBGods;
import com.confusinguser.sbgods.entities.HypixelGuild;
import com.confusinguser.sbgods.entities.Player;
import com.confusinguser.sbgods.entities.leaderboard.BankBalance;
import com.confusinguser.sbgods.entities.leaderboard.SkillLevels;
import com.confusinguser.sbgods.entities.leaderboard.SlayerExp;
import net.dv8tion.jda.api.entities.TextChannel;

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
                try {
                    updateLeaderboardCache(hypixelGuild);
                } catch (Throwable t) {
                    TextChannel textChannel = main.getDiscord().getJDA().getTextChannelById("713870866051498086");
                    main.logger.severe("Exception when updating leaderboard: \n" + main.getLangUtil().beautifyStackTrace(t.getStackTrace(), t));
                    if (textChannel != null)
                        textChannel.sendMessage("Exception when updating leaderboard: \n" + main.getLangUtil().beautifyStackTrace(t.getStackTrace(), t)).queue();
                }
        }, 0, 15, TimeUnit.MINUTES);
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            try {
                main.getDiscord().verifyAllCommand.verifyAll(main.getDiscord().getJDA().awaitReady().getTextChannelById("713012939258593290"));
                main.getDiscord().verifyAllCommand.verifyAll(main.getDiscord().getJDA().awaitReady().getTextChannelById("713024923945402431"));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, 10, 360, TimeUnit.MINUTES); // Every 6h // Sometimes ppl complain about specific changes not coming into effect immediately
    }

    private void updateLeaderboardCache(HypixelGuild guild) {
        Map<Player, SkillLevels> skillLevelMap = new HashMap<>();
        Map<Player, SlayerExp> slayerExpMap = new HashMap<>();
        Map<Player, BankBalance> totalCoinsMap = new HashMap<>();

        List<Player> guildMembers = main.getApiUtil().getGuildMembers(guild);

        int i = 0;
        guild.setLeaderboardProgress(0);
        for (Player guildMember : guildMembers) {
            Player thePlayer = main.getApiUtil().getPlayerFromUUID(guildMember.getUUID());
            Player.mergePlayerAndGuildMember(thePlayer, guildMember);
            SkillLevels highestSkillLevels = main.getApiUtil().getBestProfileSkillLevels(thePlayer.getUUID());
            SlayerExp totalSlayerExp = main.getApiUtil().getPlayerSlayerExp(thePlayer.getUUID());
            double totalCoins = main.getApiUtil().getTotalCoinsInPlayer(thePlayer.getUUID());
            skillLevelMap.put(thePlayer, highestSkillLevels == null ? new SkillLevels() : highestSkillLevels);
            slayerExpMap.put(thePlayer, totalSlayerExp);
            totalCoinsMap.put(thePlayer, new BankBalance(totalCoins));
            guild.setLeaderboardProgress(i++);
        }

        guild.setSlayerExpMap(slayerExpMap);
        guild.setAvgSkillLevelMap(skillLevelMap);
        guild.setTotalCoinsMap(totalCoinsMap);
    }

    private void updateLeaderboardCacheFast(HypixelGuild guild) {
        Map<Player, SkillLevels> skillLevelMap = new HashMap<>();
        Map<Player, SlayerExp> slayerExpMap = new HashMap<>();
        Map<Player, BankBalance> totalCoinsMap = new HashMap<>();

        List<Player> guildMembers = main.getApiUtil().getGuildMembers(guild);


        final int[] i = {0};

        List<Thread> threads = new ArrayList<>();

        for (Player guildMember : guildMembers) {
            Runnable target = () -> {
                Player thePlayer = main.getApiUtil().getPlayerFromUUID(guildMember.getUUID());
                thePlayer = Player.mergePlayerAndGuildMember(thePlayer, guildMember);
                SkillLevels highestSkillLevels = main.getApiUtil().getBestProfileSkillLevels(thePlayer.getUUID());
                SlayerExp totalSlayerExp = main.getApiUtil().getPlayerSlayerExp(thePlayer.getUUID());
                double totalCoins = main.getApiUtil().getTotalCoinsInPlayer(thePlayer.getUUID());
                skillLevelMap.put(thePlayer, highestSkillLevels == null ? new SkillLevels() : highestSkillLevels);
                slayerExpMap.put(thePlayer, totalSlayerExp);
                totalCoinsMap.put(thePlayer, new BankBalance(totalCoins));
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