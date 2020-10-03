package com.confusinguser.sbgods.utils;

import com.confusinguser.sbgods.SBGods;
import com.confusinguser.sbgods.entities.DiscordServer;
import com.confusinguser.sbgods.entities.Player;
import com.confusinguser.sbgods.entities.leaderboard.LeaderboardValue;
import com.confusinguser.sbgods.entities.leaderboard.LeaderboardValues;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
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

    public boolean cannotRunLeaderboardCommandInChannel(MessageReceivedEvent e, @NotNull DiscordServer currentDiscordServer) {
        if (currentDiscordServer.getBotChannelId() != null && !e.getChannel().getId().contentEquals(currentDiscordServer.getBotChannelId())) {
            e.getChannel().sendMessage(main.getMessageByKey("command_cannot_be_used_on_server")).queue();
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
}
