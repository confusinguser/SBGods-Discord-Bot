package com.confusinguser.sbgods.discord.commands;

import com.confusinguser.sbgods.SBGods;
import com.confusinguser.sbgods.discord.DiscordBot;
import com.confusinguser.sbgods.entities.DiscordServer;
import com.confusinguser.sbgods.entities.SkillLevels;
import com.confusinguser.sbgods.entities.SkyblockPlayer;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SkillExpCommand extends Command implements EventListener {

    public SkillExpCommand(SBGods main, DiscordBot discord) {
        this.main = main;
        this.discord = discord;
        this.name = "skillxp";
        this.aliases = new String[]{"skillexp"};
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent e) {
        if (e.getAuthor().isBot() || !isTheCommand(e) || !discord.shouldRun(e)) {
            return;
        }
        DiscordServer currentDiscordServer = DiscordServer.getDiscordServerFromEvent(e);
        if (currentDiscordServer == null) {
            return;
        }
        handleCommand(currentDiscordServer, e.getChannel(), e.getMessage().getContentRaw(), e.getAuthor().getName());
    }

    public void handleCommand(DiscordServer currentDiscordServer, MessageChannel channel, String messageRaw, String authorName) {

        if (currentDiscordServer.getChannelId() != null && !channel.getId().contentEquals(currentDiscordServer.getChannelId())) {
            channel.sendMessage("Skill commands cannot be ran in this channel!").queue();
            return;
        }

        main.logger.info(authorName + " ran command: " + messageRaw);

        String[] args = messageRaw.split(" ");

        if (args.length <= 1) {
            channel.sendMessage("Invalid argument! Valid arguments: `leaderboard`, `player`!").queue();
            return;
        }

        boolean spreadsheet = false;
        if (args.length >= 4 && args[3].equalsIgnoreCase("spreadsheet")) {
            spreadsheet = true;
        }

        if (args[1].equalsIgnoreCase("leaderboard") || args[1].equalsIgnoreCase("lb")) {
            ArrayList<SkyblockPlayer> guildMemberUuids = main.getApiUtil().getGuildMembers(currentDiscordServer.getHypixelGuild());
            Map<String, SkillLevels> usernameSkillExpHashMap = currentDiscordServer.getHypixelGuild().getSkillExpHashmap();

            if (usernameSkillExpHashMap.size() == 0) {
                if (currentDiscordServer.getHypixelGuild().getSkillProgress() == 0) {
                    channel.sendMessage("Bot is still indexing names, please try again in a few minutes! (Please note that other leaderboards have a higher priority)").queue();
                } else {
                    channel.sendMessage("Bot is still indexing names, please try again in a few minutes! (" + currentDiscordServer.getHypixelGuild().getSkillProgress() + " / " + currentDiscordServer.getHypixelGuild().getPlayerSize() + ")").queue();
                }
                return;
            }

            int topX;
            if (args.length >= 3) {
                if (args[2].equalsIgnoreCase("all")) {
                    topX = guildMemberUuids.size();
                } else {
                    try {
                        topX = Math.min(guildMemberUuids.size(), Integer.parseInt(args[2]));
                    } catch (NumberFormatException exception) {
                        channel.sendMessage("**" + args[2] + "** is not a valid number!").queue();
                        return;
                    }
                }
            } else {
                topX = 10;
            }

            StringBuilder response = new StringBuilder();

            // print it like a spreadsheet
            if (spreadsheet) {
                for (int i = 0; i < topX; i++) {
                    Map.Entry<String, SkillLevels> currentEntry = main.getUtil().getHighestKeyValuePair(usernameSkillExpHashMap, i, true);
                    if (!currentEntry.getValue().isApproximate()) {
                        response.append(currentEntry.getKey() + "    " + main.getSBUtil().toSkillExp(currentEntry.getValue().getAvgSkillLevel()) + "\n");
                    }
                }
            } else {
                response.append("**Average Skill XP leaderboard:**\n\n");
                int totalAvgSkillExp = 0;
                for (int i = 0; i < topX; i++) {
                    Map.Entry<String, SkillLevels> currentEntry = main.getUtil().getHighestKeyValuePair(usernameSkillExpHashMap, i, true);
                    response.append("**#" + Math.incrementExact(i) + "** *" + currentEntry.getKey() + ":* " + main.getSBUtil().toSkillExp(main.getUtil().round(currentEntry.getValue().getAvgSkillLevel(), 2)));
                    if (currentEntry.getValue().isApproximate()) {
                        response.append(" *(appr.)*");
                    }
                    response.append("\n\n");
                    totalAvgSkillExp += currentEntry.getValue().getAvgSkillLevel();
                }
                if (topX == guildMemberUuids.size())
                    response.append("**Average guild skill xp: ");
                else
                    response.append("**Average skill xp top #").append(topX).append(": ");
                response.append(main.getSBUtil().toSkillExp(main.getUtil().round((double) totalAvgSkillExp / topX, 2))).append("**");
            }

            String responseString = response.toString();
            // Split the message every 2000 characters in a nice looking way because of discord limitations
            List<String> responseList = main.getUtil().processMessageForDiscord(responseString, 2000);
            for (int j = 0; j < responseList.size(); j++) {
                String message = responseList.get(j);
                if (j == 0 && !spreadsheet) {
                    channel.sendMessage(message).queue();
                } else {
                    if (spreadsheet) {
                        channel.sendMessage("```arm\n" + message + "```").queue();
                    } else {
                        channel.sendMessage("\u200E" + message).queue();
                    }
                }
            }
            return;
        }

        if (args[1].equalsIgnoreCase("player")) {
            if (args.length >= 3) {
                SkyblockPlayer thePlayer = main.getApiUtil().getSkyblockPlayerFromUsername(args[2]);

                if (thePlayer.getSkyblockProfiles().isEmpty()) {
                    channel.sendMessage("Player **" + args[2] + "** does not exist!").queue();
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
                StringBuilder descriptionBuilder = embedBuilder.getDescriptionBuilder();

                if (highestSkillLevels.isApproximate()) {
                    descriptionBuilder.append("Approximate average skill xp: ").append(main.getSBUtil().toSkillExp(highestSkillLevels.getAvgSkillLevel())).append("\n\n");
                } else {
                    descriptionBuilder.append("Average skill xp: ").append(main.getSBUtil().toSkillExp(highestSkillLevels.getAvgSkillLevel())).append("\n\n");
                }

                embedBuilder.setDescription(descriptionBuilder
                        .append("Farming: " + main.getSBUtil().toSkillExp(highestSkillLevels.getFarming()) + '\n')
                        .append("Mining: " + main.getSBUtil().toSkillExp(highestSkillLevels.getMining()) + '\n')
                        .append("Combat: " + main.getSBUtil().toSkillExp(highestSkillLevels.getCombat()) + '\n')
                        .append("Foraging: " + main.getSBUtil().toSkillExp(highestSkillLevels.getForaging()) + '\n')
                        .append("Fishing: " + main.getSBUtil().toSkillExp(highestSkillLevels.getFishing()) + '\n')
                        .append("Enchanting: " + main.getSBUtil().toSkillExp(highestSkillLevels.getEnchanting()) + '\n')
                        .append("Alchemy: " + main.getSBUtil().toSkillExp(highestSkillLevels.getAlchemy()) + '\n')
                        .toString());

                StringBuilder footerBuilder = new StringBuilder();
                embedBuilder.setFooter(footerBuilder
                        .append("Carpentry: " + main.getSBUtil().toSkillExp(highestSkillLevels.getCarpentry()) + '\n')
                        .append("Runecrafting: " + main.getSBUtil().toSkillExp(highestSkillLevels.getRunecrafting()))
                        .toString());

                channel.sendMessage(embedBuilder.build()).queue();
                return;

            } else {
                channel.sendMessage("Invalid usage! Usage: *" + getName() + " player <IGN>*").queue();
                return;
            }
        }

        channel.sendMessage("Invalid argument! Valid arguments: `leaderboard`, `player`!").queue();
    }
}