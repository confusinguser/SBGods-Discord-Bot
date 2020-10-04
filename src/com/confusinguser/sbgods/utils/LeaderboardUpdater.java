package com.confusinguser.sbgods.utils;

import com.confusinguser.sbgods.SBGods;
import com.confusinguser.sbgods.entities.HypixelGuild;
import com.confusinguser.sbgods.entities.Player;
import com.confusinguser.sbgods.entities.leaderboard.LeaderboardValues;
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
    private List<String> latestEventLeaderboardIds = new ArrayList<>();

    public LeaderboardUpdater(SBGods main) {
        instance = this;
        this.main = main;
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            try {
                for (HypixelGuild hypixelGuild : HypixelGuild.values()) {
                    updateLeaderboardCache(hypixelGuild);
                }
                if (!main.getDiscord().eventCommand.progressTypeToPost.isEmpty()) {
                    String title = main.getDiscord().eventCommand.getLeaderboardTitleForProgressType(main.getDiscord().eventCommand.progressTypeToPost);
                    if (title == null) return;
                    if (latestEventLeaderboardIds.size() != 0) {
                        for (String messageId : latestEventLeaderboardIds) {
                            TextChannel textChannel;
                            if ((textChannel = main.getDiscord().getJDA().awaitReady().getTextChannelById("753934993788633170")) != null)
                                textChannel.deleteMessageById(messageId).queue();
                        }
                    }
                    latestEventLeaderboardIds = main.getDiscord().eventCommand.sendProgressLeaderboard(main.getDiscord().getJDA().awaitReady().getTextChannelById("753934993788633170"), main.getDiscord().eventCommand.progressTypeToPost, title, false);
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

            LeaderboardValues leaderboardValues = main.getApiUtil().getBestLeaderboardValues(thePlayer.getUUID());
            playerStatMap.put(thePlayer, leaderboardValues);
            guild.setLeaderboardProgress(i++);
        }

        guild.setPlayerStatMap(playerStatMap);
    }

    public List<String> getLatestEventLeaderboardIds() {
        return latestEventLeaderboardIds;
    }

    public void setLatestEventLeaderboardIds(List<String> latestEventLeaderboardIds) {
        this.latestEventLeaderboardIds = latestEventLeaderboardIds;
    }
}