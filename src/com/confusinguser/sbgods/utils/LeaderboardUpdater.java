package com.confusinguser.sbgods.utils;

import com.confusinguser.sbgods.SBGods;
import com.confusinguser.sbgods.entities.HypixelGuild;
import com.confusinguser.sbgods.entities.Player;
import com.confusinguser.sbgods.entities.leaderboard.BankBalance;
import com.confusinguser.sbgods.entities.leaderboard.LeaderboardValues;
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
        instance = this;
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
                            if ((textChannel = main.getDiscord().getJDA().awaitReady().getTextChannelById("753934993788633170")) != null)
                                textChannel.deleteMessageById(messageId).queue();
                        }
                    }
                    latestEventLbIds = main.getDiscord().eventCommand.sendProgressLbRetIds(main.getDiscord().getJDA().awaitReady().getTextChannelById("753934993788633170"), "skillTotal", "Total Skill Exp Progress\n", false);
                }
            } catch (Throwable t) {
                main.getDiscord().reportFail(t, "Leaderboard Updater");
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
        Map<Player, LeaderboardValues> playerStatMap = new HashMap<>();

        List<Player> guildMembers = main.getApiUtil().getGuildMembers(guild);

        int i = 0;
        guild.setLeaderboardProgress(0);
        for (Player guildMember : guildMembers) {
            Player thePlayer = main.getApiUtil().getPlayerFromUUID(guildMember.getUUID());
            Player.mergePlayerAndGuildMember(thePlayer, guildMember);
            SkillLevels highestSkillLevels = main.getApiUtil().getBestProfileSkillLevels(thePlayer.getUUID());
            SlayerExp totalSlayerExp = main.getApiUtil().getPlayerSlayerExp(thePlayer.getUUID());
            BankBalance totalCoins = main.getApiUtil().getTotalCoinsInPlayer(thePlayer.getUUID());


            playerStatMap.put(thePlayer, new LeaderboardValues(totalSlayerExp, totalCoins, highestSkillLevels));
            guild.setLeaderboardProgress(i++);
        }

        guild.setPlayerStatMap(playerStatMap);
    }

    public List<String> getLatestEventLbIds() {
        return latestEventLbIds;
    }

    public void setLatestEventLbIds(List<String> latestEventLbIds) {
        this.latestEventLbIds = latestEventLbIds;
    }
}