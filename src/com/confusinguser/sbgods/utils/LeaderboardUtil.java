package com.confusinguser.sbgods.utils;

import com.confusinguser.sbgods.SBGods;
import com.confusinguser.sbgods.entities.DiscordServer;
import com.confusinguser.sbgods.entities.Player;
import com.confusinguser.sbgods.entities.leaderboard.LeaderboardValue;
import com.confusinguser.sbgods.entities.leaderboard.LeaderboardValues;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class LeaderboardUtil {
    private final SBGods main;

    public LeaderboardUtil(SBGods main) {
        this.main = main;
    }

    public List<? extends Map.Entry<Player, ? extends LeaderboardValue>> sortLeaderboard(Map<Player, ? extends LeaderboardValue> unsorted, int topX) {
        return unsorted.entrySet().stream()
                .sorted(Comparator.comparingDouble(entry -> -entry.getValue().getValue()))
                .collect(Collectors.toList())
                .subList(0, topX);
    }

    public boolean cannotRunLeaderboardCommandInChannel(MessageChannel channel, @NotNull DiscordServer currentDiscordServer) {
        if (currentDiscordServer.getBotChannelId() != null && !channel.getId().contentEquals(currentDiscordServer.getBotChannelId())) {
            channel.sendMessage(main.getMessageByKey("command_cannot_be_used_in_channel")).queue();
            return true;
        }
        return false;
    }

    public int calculateTopXFromArgs(String[] args, int guildSize) {
        int topX = 10;
        if (args.length >= 3) {
            if (args[2].equalsIgnoreCase("all")) {
                topX = guildSize;
            } else {
                try {
                    topX = Math.min(guildSize, Integer.parseInt(args[2]));
                } catch (NumberFormatException exception) {
                    return -1;
                }
            }
        }
        return topX;
    }

    public Map<Player, ? extends LeaderboardValue> convertPlayerStatMap(Map<Player, LeaderboardValues> playerStatMap, Function<Map.Entry<Player, LeaderboardValues>, ? extends LeaderboardValue> conversationFunction) {
        return playerStatMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, conversationFunction));
    }

    public void sendLeaderboard(List<String> data, String title, MessageChannel channel, boolean spreadsheet) {
        for (int j = 0; j < data.size(); j++) {
            String message = data.get(j);
            if (j != 0 && !spreadsheet) {
                channel.sendMessage(new EmbedBuilder().setDescription(message).build()).queue();
            } else {
                if (spreadsheet) {
                    channel.sendMessage("```arm\n" + message + "```").queue();
                } else {
                    channel.sendMessage(new EmbedBuilder().setTitle(title).setDescription(message).build()).queue();
                }
            }
        }
    }
}
