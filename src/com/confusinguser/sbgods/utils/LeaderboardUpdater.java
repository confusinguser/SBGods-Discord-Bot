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

    public static LeaderboardUpdater instance;
    private final SBGods main;
    private List<String> latestEventLbIds = new ArrayList<>();

    public LeaderboardUpdater(SBGods main) {
        this.main = main;
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            try {
                for (HypixelGuild hypixelGuild : HypixelGuild.values()) {
                    updateLeaderboardCache(hypixelGuild);
                }
                if (main.getDiscord().eventCommand.postLeaderboard) {
                    if (latestEventLbIds.size() != 0) {
                        for (String messageId : latestEventLbIds) {
                            TextChannel textChannel;
                            if ((textChannel = main.getDiscord().getJDA().awaitReady().getTextChannelById("747881093444796527")) != null)
                                textChannel.deleteMessageById(messageId).queue();
                        }
                    }
                    latestEventLbIds = main.getDiscord().eventCommand.sendProgressLbRetIds(main.getDiscord().getJDA().awaitReady().getTextChannelById("747881093444796527"), "slayerTotal", "Total Slayer Exp Progress\n", false);
                }
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
        }, 10, 180, TimeUnit.MINUTES); // Every 3h
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

    public List<String> getLatestEventLbIds() {
        return latestEventLbIds;
    }

    public void setLatestEventLbIds(List<String> latestEventLbIds) {
        this.latestEventLbIds = latestEventLbIds;
    }
}