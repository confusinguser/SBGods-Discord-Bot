package com.confusinguser.sbgods.utils;

import com.confusinguser.sbgods.SBGods;
import com.confusinguser.sbgods.entities.DiscordServer;
import com.confusinguser.sbgods.entities.HypixelGuild;
import com.confusinguser.sbgods.entities.Player;
import com.confusinguser.sbgods.entities.leaderboard.LeaderboardValues;
import com.google.gson.JsonObject;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class LeaderboardUpdater {

    public static LeaderboardUpdater instance;
    private final SBGods main;
    private List<String> latestEventLeaderboardIds = new ArrayList<>();

    public LeaderboardUpdater(SBGods main) {
        instance = this;
        this.main = main;
        Multithreading.scheduleAtFixedRate(() -> {
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

        Multithreading.scheduleAtFixedRate(() -> {
            try {
                main.getDiscord().verifyAllCommand.verifyAll(main.getDiscord().getJDA().awaitReady().getTextChannelById("713012939258593290"));
                main.getDiscord().verifyAllCommand.verifyAll(main.getDiscord().getJDA().awaitReady().getTextChannelById("713024923945402431"));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, 1, 24, TimeUnit.HOURS);

        Multithreading.scheduleAtFixedRate(() -> {
            List<JsonObject> messages = main.getApiUtil().getSMPMessageQueue();
//            messages.add(JsonParser.parseString("{\"uuid\":\"dc8c3964-7b29-4e03-ae9e-d13ebd65dd29\",\"player\":\"Soopyboo32\",\"message\":\"i hate debris mining\"}").getAsJsonObject());
            for (JsonObject message : messages) {
                String fullText = "SMP > " + message.get("player").getAsString() + ": " + message.get("message").getAsString();
                main.getRemoteGuildChatManager().handleGuildMessage(HypixelGuild.SBG.getGuildId(), DiscordServer.SBGods, true, Util.getTextWithoutFormattingCodes(fullText));
                main.getRemoteGuildChatManager().queue.offer(new AbstractMap.SimpleEntry<>(HypixelGuild.SBG.getGuildId(), fullText));
            }
        }, 0, 5, TimeUnit.SECONDS);
    }

    private void updateLeaderboardCache(HypixelGuild guild) {
        Map<Player, LeaderboardValues> playerStatMap = new HashMap<>();

        List<Player> guildMembers = main.getApiUtil().getGuildMembers(guild);

        int i = 0;
        guild.setLeaderboardProgress(0);
        for (Player guildMember : guildMembers) {
            Player thePlayer = main.getApiUtil().getPlayerFromUUID(guildMember.getUUID());
            Player.mergePlayerAndGuildMember(thePlayer, guildMember);

            LeaderboardValues leaderboardValues = main.getApiUtil().getBestLeaderboardValues(thePlayer);
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