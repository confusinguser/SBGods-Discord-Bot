package com.confusinguser.sbgods.discord.commands;

import com.confusinguser.sbgods.SBGods;
import com.confusinguser.sbgods.discord.DiscordBot;
import com.confusinguser.sbgods.entities.DiscordServer;
import com.confusinguser.sbgods.entities.Player;
import com.confusinguser.sbgods.entities.SkillLevels;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class SkillCommand extends Command implements EventListener {

    public SkillCommand(SBGods main, DiscordBot discord) {
        this.main = main;
        this.discord = discord;
        this.name = "skill";
        this.aliases = new String[]{"skills"};
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent e) {
        if (e.getAuthor().isBot() || isNotTheCommand(e) || discord.shouldNotRun(e)) {
            return;
        }

        DiscordServer currentDiscordServer = DiscordServer.getDiscordServerFromEvent(e);
        if (currentDiscordServer == null) {
            return;
        }

        if (currentDiscordServer.getChannelId() != null && !e.getChannel().getId().contentEquals(currentDiscordServer.getChannelId())) {
            e.getChannel().sendMessage("Skill commands cannot be ran in this channel!").queue();
            return;
        }

        main.logger.info(e.getAuthor().getName() + " ran command: " + e.getMessage().getContentRaw());

        String[] args = e.getMessage().getContentRaw().split(" ");

        if (args.length <= 1) {
            e.getChannel().sendMessage("Invalid argument! Valid arguments: `leaderboard`, `player`!").queue();
            return;
        }

        boolean spreadsheet = false;
        if (args.length >= 4 && args[3].equalsIgnoreCase("spreadsheet")) {
            spreadsheet = true;
        }

        e.getChannel().sendTyping().queue();

        if (args[1].equalsIgnoreCase("leaderboard") || args[1].equalsIgnoreCase("lb")) {
            ArrayList<Player> guildMemberUuids = main.getApiUtil().getGuildMembers(currentDiscordServer.getHypixelGuild());
            Map<String, SkillLevels> usernameSkillExpHashMap = currentDiscordServer.getHypixelGuild().getSkillExpHashmap();

            if (usernameSkillExpHashMap.size() == 0) {
                if (currentDiscordServer.getHypixelGuild().getSkillProgress() == 0) {
                    e.getChannel().sendMessage("Bot is still indexing names, please try again in a few minutes! (Please note that other leaderboards have a higher priority)").queue();
                } else {
                    e.getChannel().sendMessage("Bot is still indexing names, please try again in a few minutes! (" + currentDiscordServer.getHypixelGuild().getSkillProgress() + " / " + currentDiscordServer.getHypixelGuild().getPlayerSize() + ")").queue();
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
                        e.getChannel().sendMessage("**" + args[2] + "** is not a valid number!").queue();
                        return;
                    }
                }
            } else {
                topX = 10;
            }

            StringBuilder response = new StringBuilder("**Average Skill Level Leaderboard:**\n\n");
            if (args.length >= 4 && args[3].equalsIgnoreCase("spreadsheet")) {
                for (int i = 0; i < topX; i++) {
                    Entry<String, SkillLevels> currentEntry = main.getUtil().getHighestKeyValuePair(usernameSkillExpHashMap, i, true);
                    if (!currentEntry.getValue().isApproximate()) {
                        response.append(currentEntry.getKey() + "    " + main.getUtil().round(currentEntry.getValue().getAvgSkillLevel(), 2) + "\n");
                    }
                }
            } else {
                int totalAvgSkillLvl = 0;
                for (int i = 0; i < topX; i++) {
                    Entry<String, SkillLevels> currentEntry = main.getUtil().getHighestKeyValuePair(usernameSkillExpHashMap, i, true);
                    response.append("**#" + Math.incrementExact(i) + "** *" + currentEntry.getKey() + ":* " + main.getUtil().round(currentEntry.getValue().getAvgSkillLevel(), 2));
                    if (currentEntry.getValue().isApproximate()) {
                        response.append(" *(appr.)*");
                    }
                    response.append("\n\n");
                    totalAvgSkillLvl += currentEntry.getValue().getAvgSkillLevel();
                }
                if (topX == guildMemberUuids.size())
                    response.append("**Average guild skill level: ");
                else
                    response.append("**Average skill level top #").append(topX).append(": ");
                response.append(main.getUtil().round((double) totalAvgSkillLvl / topX, 2)).append("**");
            }

            String responseString = response.toString();
            // Split the message every 2000 characters in a nice looking way because of discord limitations
            List<String> responseList = main.getUtil().processMessageForDiscord(responseString, 2000);
            for (int j = 0; j < responseList.size(); j++) {
                String message = responseList.get(j);
                if (j == 0 && !spreadsheet) {
                    e.getChannel().sendMessage(message).queue();
                } else {
                    if (spreadsheet) {
                        e.getChannel().sendMessage("```arm\n" + message + "```").queue();
                    } else {
                        e.getChannel().sendMessage("\u200E" + message).queue();
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

                EmbedBuilder embedBuilder = new EmbedBuilder().setColor(0x03731d).setTitle(main.getLangUtil().makePossessiveForm(thePlayer.getDisplayName()) + " skill levels");
                StringBuilder descriptionBuilder = embedBuilder.getDescriptionBuilder();

                if (highestSkillLevels.isApproximate()) {
                    descriptionBuilder.append("Approximate average skill level: " + main.getUtil().round(highestSkillLevels.getAvgSkillLevel(), 3) + "\n\n");
                } else {
                    descriptionBuilder.append("Average skill level: " + main.getUtil().round(highestSkillLevels.getAvgSkillLevel(), 3) + "\n\n");
                }

                descriptionBuilder
                        .append("Farming: " + highestSkillLevels.getFarming() + '\n')
                        .append("Mining: " + highestSkillLevels.getMining() + '\n')
                        .append("Combat: " + highestSkillLevels.getCombat() + '\n')
                        .append("Foraging: " + highestSkillLevels.getForaging() + '\n')
                        .append("Fishing: " + highestSkillLevels.getFishing() + '\n')
                        .append("Enchanting: " + highestSkillLevels.getEnchanting() + '\n');
                if (highestSkillLevels.isApproximate())
                    descriptionBuilder.append("Taming: " + highestSkillLevels.getTaming() + '\n');
                descriptionBuilder.append("Alchemy: " + highestSkillLevels.getAlchemy() + '\n');

                embedBuilder.setDescription(descriptionBuilder.toString());

                StringBuilder footerBuilder = new StringBuilder();
                embedBuilder.setFooter(footerBuilder
                        .append("Carpentry: " + highestSkillLevels.getCarpentry() + '\n')
                        .append("Runecrafting: " + highestSkillLevels.getRunecrafting())
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