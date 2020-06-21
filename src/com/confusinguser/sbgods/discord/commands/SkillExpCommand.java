package com.confusinguser.sbgods.discord.commands;

import com.confusinguser.sbgods.SBGods;
import com.confusinguser.sbgods.discord.DiscordBot;
import com.confusinguser.sbgods.entities.DiscordServer;
import com.confusinguser.sbgods.entities.Player;
import com.confusinguser.sbgods.entities.SkillLevels;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SkillExpCommand extends Command {

    public SkillExpCommand(SBGods main, DiscordBot discord) {
        this.main = main;
        this.discord = discord;
        this.name = "skillxp";
        this.aliases = new String[]{"skillexp"};
    }

    @Override
    public void handleCommand(MessageReceivedEvent e, DiscordServer currentDiscordServer, String[] args) {
        if (currentDiscordServer.getBotChannelId() != null && !e.getChannel().getId().contentEquals(currentDiscordServer.getBotChannelId())) {
            e.getChannel().sendMessage("Skill commands cannot be ran in this channel!").queue();
            return;
        }

        if (args.length <= 1) {
            e.getChannel().sendMessage("Invalid argument! Valid arguments: `leaderboard`, `player`!").queue();
            return;
        }

        boolean spreadsheet = false;
        if (args.length >= 4 && args[3].equalsIgnoreCase("spreadsheet")) {
            spreadsheet = true;
        }

        if (args[1].equalsIgnoreCase("leaderboard") || args[1].equalsIgnoreCase("lb")) {
            ArrayList<Player> guildMemberUuids = main.getApiUtil().getGuildMembers(currentDiscordServer.getHypixelGuild());
            Map<String, SkillLevels> usernameSkillExpHashMap = currentDiscordServer.getHypixelGuild().getSkillExpMap();

            if (usernameSkillExpHashMap.size() == 0) {
                if (currentDiscordServer.getHypixelGuild().getLeaderboardProgress() == 0) {
                    e.getChannel().sendMessage("Bot is still indexing names, please try again in a few minutes! (Please note that other leaderboards have a higher priority)").queue();
                } else {
                    e.getChannel().sendMessage("Bot is still indexing names, please try again in a few minutes! (" + currentDiscordServer.getHypixelGuild().getLeaderboardProgress() + " / " + currentDiscordServer.getHypixelGuild().getPlayerSize() + ")").queue();
                }
                return;
            }

            int topX;
            if (args.length >= 3) {
                if (args[2].equalsIgnoreCase("all")) {
                    topX = usernameSkillExpHashMap.size();
                } else {
                    try {
                        topX = Math.min(usernameSkillExpHashMap.size(), Integer.parseInt(args[2]));
                    } catch (NumberFormatException exception) {
                        e.getChannel().sendMessage("**" + args[2] + "** is not a valid number!").queue();
                        return;
                    }
                }
            } else {
                topX = 10;
            }

            StringBuilder response = new StringBuilder();

            List<Map.Entry<String, SkillLevels>> leaderboardList = usernameSkillExpHashMap.entrySet().stream()
                    .sorted(Comparator.comparingDouble(entry -> -entry.getValue().getAvgSkillLevel()))
                    .collect(Collectors.toList())
                    .subList(0, topX);

            // print it like a spreadsheet
            if (spreadsheet) {
                for (Map.Entry<String, SkillLevels> currentEntry : leaderboardList) {
                    if (!currentEntry.getValue().isApproximate()) {
                        response.append(currentEntry.getKey()).append("    ").append(main.getSBUtil().toSkillExp(currentEntry.getValue().getAvgSkillLevel())).append("\n");
                    }
                }
            } else {
                response.append("**Average Skill XP leaderboard:**\n\n");
                int totalAvgSkillExp = 0;

                for (Map.Entry<String, SkillLevels> currentEntry : leaderboardList) {
                    response.append("**#").append(leaderboardList.indexOf(currentEntry) + 1).append("** *").append(currentEntry.getKey()).append(":* ").append(main.getLangUtil().addCommas(currentEntry.getValue().getAvgSkillLevel())).append("\n");
                    totalAvgSkillExp += currentEntry.getValue().getAvgSkillLevel();
                }
                if (topX == guildMemberUuids.size())
                    response.append("\n**Average guild slayer exp: ");
                else
                    response.append("\n**Average slayer exp top #").append(topX).append(": ");
                response.append(main.getLangUtil().addNotation(Math.round((double) totalAvgSkillExp / topX))).append("**");
            }

            String responseString = response.toString();
            // Split the message every 2000 characters in a nice looking way because of discord limitations
            List<String> responseList = main.getUtil().processMessageForDiscord(responseString, 2000);
            for (int j = 0; j < responseList.size(); j++) {
                String message = responseList.get(j);
                if (j == 0 && !spreadsheet) {
                    e.getChannel().sendMessage(new EmbedBuilder().setDescription(message).build()).queue();
                } else {
                    if (spreadsheet) {
                        e.getChannel().sendMessage("```arm\n" + message + "```").queue();
                    } else {
                        e.getChannel().sendMessage(new EmbedBuilder().setDescription(message).build()).queue();
                    }
                }
            }
            return;
        }

        if (args[1].equalsIgnoreCase("player")) {
            if (args.length >= 3) {
                Player thePlayer = main.getApiUtil().getPlayerFromUsername(args[2]);

                if (thePlayer.getSkyblockProfiles().isEmpty()) {
                    e.getChannel().sendMessage("Player **" + args[2] + "** does not exist!").queue();
                    return;
                }

                SkillLevels highestSkillLevels = new SkillLevels();
                for (String profile : thePlayer.getSkyblockProfiles()) {
                    SkillLevels skillLevels = main.getApiUtil().getProfileSkills(profile, thePlayer.getUUID());

                    if (highestSkillLevels.getAvgSkillLevel() < skillLevels.getAvgSkillLevel()) {
                        highestSkillLevels = skillLevels;
                    }
                }

                if (highestSkillLevels.getAvgSkillLevel() == 0) {
                    SkillLevels skillLevels = main.getApiUtil().getProfileSkillsAlternate(thePlayer.getUUID());

                    if (highestSkillLevels.getAvgSkillLevel() < skillLevels.getAvgSkillLevel()) {
                        highestSkillLevels = skillLevels;
                    }
                }

                EmbedBuilder embedBuilder = new EmbedBuilder().setColor(0x03731d).setTitle(main.getLangUtil().makePossessiveForm(thePlayer.getDisplayName()) + " skill xp");
                StringBuilder descriptionBuilder = new StringBuilder();

                if (highestSkillLevels.isApproximate()) {
                    descriptionBuilder.append("Approximate average skill xp: ").append(main.getSBUtil().toSkillExp(highestSkillLevels.getAvgSkillLevel())).append("\n\n");
                } else {
                    descriptionBuilder.append("Average skill xp: ").append(main.getSBUtil().toSkillExp(highestSkillLevels.getAvgSkillLevel())).append("\n\n");
                }

                descriptionBuilder
                        .append("Farming: ").append(main.getSBUtil().toSkillExp(highestSkillLevels.getFarming())).append('\n')
                        .append("Mining: ").append(main.getSBUtil().toSkillExp(highestSkillLevels.getMining())).append('\n')
                        .append("Combat: ").append(main.getSBUtil().toSkillExp(highestSkillLevels.getCombat())).append('\n')
                        .append("Foraging: ").append(main.getSBUtil().toSkillExp(highestSkillLevels.getForaging())).append('\n')
                        .append("Fishing: ").append(main.getSBUtil().toSkillExp(highestSkillLevels.getFishing())).append('\n')
                        .append("Enchanting: ").append(main.getSBUtil().toSkillExp(highestSkillLevels.getEnchanting())).append('\n');
                if (!highestSkillLevels.isApproximate())
                    descriptionBuilder.append("Taming: ").append(main.getSBUtil().toSkillExp(highestSkillLevels.getTaming())).append('\n');
                descriptionBuilder.append("Alchemy: ").append(main.getSBUtil().toSkillExp(highestSkillLevels.getAlchemy())).append('\n');


                embedBuilder.setDescription(descriptionBuilder.toString());

                StringBuilder footerBuilder = new StringBuilder();
                embedBuilder.setFooter(footerBuilder
                        .append("Carpentry: ").append(main.getSBUtil().toSkillExp(highestSkillLevels.getCarpentry()))
                        .append(", runecrafting: ").append(main.getSBUtil().toSkillExp(highestSkillLevels.getRunecrafting()))
                        .toString());

                e.getChannel().sendMessage(embedBuilder.build()).queue();

            } else {
                e.getChannel().sendMessage("Invalid usage! Usage: *" + getName() + " player <IGN>*").queue();
            }
            return;
        }

        e.getChannel().sendMessage("Invalid argument! Valid arguments: `leaderboard`, `player`!").queue();
    }
}