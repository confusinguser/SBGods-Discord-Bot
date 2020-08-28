package com.confusinguser.sbgods.discord.commands;

import com.confusinguser.sbgods.SBGods;
import com.confusinguser.sbgods.discord.DiscordBot;
import com.confusinguser.sbgods.entities.DiscordServer;
import com.confusinguser.sbgods.entities.Player;
import com.confusinguser.sbgods.entities.leaderboard.SlayerExp;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class SlayerCommand extends Command {

    public SlayerCommand(SBGods main, DiscordBot discord) {
        this.main = main;
        this.discord = discord;
        this.name = "slayer";
        this.aliases = new String[]{};
    }

    @Override
    public void handleCommand(MessageReceivedEvent e, DiscordServer currentDiscordServer, String[] args) {
        if (currentDiscordServer.getBotChannelId() != null && !e.getChannel().getId().contentEquals(currentDiscordServer.getBotChannelId())) {
            e.getChannel().sendMessage("Slayer commands cannot be ran in this channel!").queue();
            return;
        }

        if (args.length == 1) {
            player(e.getChannel(), main.getApiUtil().getMcNameFromDisc(e.getAuthor().getAsTag()));
            return;
        }

        if (args[1].equalsIgnoreCase("leaderboard") || args[1].equalsIgnoreCase("lb")) {
            List<Player> guildMemberUuids = main.getApiUtil().getGuildMembers(Objects.requireNonNull(DiscordServer.getDiscordServerFromDiscordGuild(e.getGuild())).getHypixelGuild());
            Map<Player, SlayerExp> usernameSlayerExpHashMap = currentDiscordServer.getHypixelGuild().getSlayerExpMap();

            if (usernameSlayerExpHashMap.size() == 0) {
                if (currentDiscordServer.getHypixelGuild().getLeaderboardProgress() == 0) {
                    e.getChannel().sendMessage("Bot is still indexing names, please try again in a few minutes! (Please note that other leaderboards have a higher priority)").queue();
                } else {
                    e.getChannel().sendMessage("Bot is still indexing names, please try again in a few minutes! (" + currentDiscordServer.getHypixelGuild().getLeaderboardProgress() + " / " + currentDiscordServer.getHypixelGuild().getPlayerSize() + ")").queue();
                }
                return;
            }

            int topX;
            if (args.length > 2) {
                if (args[2].equalsIgnoreCase("all")) {
                    topX = usernameSlayerExpHashMap.size();
                } else {
                    try {
                        topX = Math.min(usernameSlayerExpHashMap.size(), Integer.parseInt(args[2]));
                    } catch (NumberFormatException exception) {
                        e.getChannel().sendMessage("**" + args[2] + "** is not a valid number!").queue();
                        return;
                    }
                }
            } else {
                topX = 10;
            }

            List<Entry<Player, SlayerExp>> leaderboardList = usernameSlayerExpHashMap.entrySet().stream()
                    .sorted(Comparator.comparingDouble(entry -> -entry.getValue().getTotalExp()))
                    .collect(Collectors.toList())
                    .subList(0, topX);

            StringBuilder response = new StringBuilder();
            if (args.length >= 4 && args[3].equalsIgnoreCase("spreadsheet")) {
                for (Entry<Player, SlayerExp> currentEntry : leaderboardList) {
                    response.append(currentEntry.getKey().getDisplayName()).append("    ").append(main.getLangUtil().addNotation(main.getSBUtil().toSkillExp(main.getUtil().round(currentEntry.getValue().getTotalExp(), 2)))).append("\n");
                }
            } else {
                int totalSlayer = 0;
                for (Entry<Player, SlayerExp> currentEntry : leaderboardList) {
                    response.append("**#").append(leaderboardList.indexOf(currentEntry) + 1).append("** *").append(currentEntry.getKey().getDisplayName()).append(":* ").append(main.getLangUtil().addNotation(currentEntry.getValue().getTotalExp())).append("\n");
                    totalSlayer += currentEntry.getValue().getTotalExp();
                }
                if (topX == guildMemberUuids.size())
                    response.append("\n**Average guild slayer exp: ");
                else
                    response.append("\n**Average slayer exp top #").append(topX).append(": ");
                response.append(main.getLangUtil().addNotation(Math.round((double) totalSlayer / topX))).append("**");
            }

            String responseString = response.toString();
            // Split the message every 2000 characters in a nice looking way because of discord limitations
            List<String> responseList = main.getUtil().processMessageForDiscord(responseString, 2000);

            boolean spreadsheet = false;
            if (args.length >= 4 && args[3].equalsIgnoreCase("spreadsheet")) {
                spreadsheet = true;
            }
            for (int j = 0; j < responseList.size(); j++) {
                String message = responseList.get(j);
                if (j == 0 && !spreadsheet) {
                    e.getChannel().sendMessage(new EmbedBuilder().setDescription(message).build()).queue();
                } else {
                    if (spreadsheet) {
                        e.getChannel().sendMessage("```arm\n" + message + "```").queue();
                    } else {
                        e.getChannel().sendMessage(new EmbedBuilder().setTitle("Slayer XP Leaderboard").setDescription(message).build()).queue();
                    }
                }
            }
        } else {
            player(e.getChannel(), args[1]);
        }
    }

    public void player(MessageChannel channel, String playerName) {
        Player thePlayer = main.getApiUtil().getPlayerFromUsername(playerName);
        if (thePlayer.getUUID() == null) {
            channel.sendMessage("Player **" + playerName + "** does not exist!").queue();
            return;
        }
        if (thePlayer.getSkyblockProfiles().isEmpty()) {
            channel.sendMessage("Player **" + playerName + "** has never played Skyblock!").queue();
            return;
        }

        SlayerExp playerSlayerExp = main.getApiUtil().getPlayerSlayerExp(thePlayer.getUUID());

        EmbedBuilder embedBuilder = new EmbedBuilder().setColor(0x51047d).setTitle(main.getLangUtil().makePossessiveForm(thePlayer.getDisplayName()) + " slayer xp");
        embedBuilder.setDescription(embedBuilder.getDescriptionBuilder()
                .append("Total slayer xp: **").append(main.getLangUtil().addNotation(playerSlayerExp.getTotalExp())).append("**\n\n")

                .append("Zombie: **").append(main.getLangUtil().addNotation(playerSlayerExp.getZombie())).append("**\n")
                .append("Spider: **").append(main.getLangUtil().addNotation(playerSlayerExp.getSpider())).append("**\n")
                .append("Wolf:   **").append(main.getLangUtil().addNotation(playerSlayerExp.getWolf())).append("**\n")
                .toString());

        channel.sendMessage(embedBuilder.build()).queue();
    }
}