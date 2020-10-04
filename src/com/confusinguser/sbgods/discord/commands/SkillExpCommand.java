package com.confusinguser.sbgods.discord.commands;

import com.confusinguser.sbgods.SBGods;
import com.confusinguser.sbgods.discord.DiscordBot;
import com.confusinguser.sbgods.entities.DiscordServer;
import com.confusinguser.sbgods.entities.Player;
import com.confusinguser.sbgods.entities.leaderboard.SkillLevels;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
public class SkillExpCommand extends Command {

    public SkillExpCommand(SBGods main, DiscordBot discord) {
        this.main = main;
        this.discord = discord;
        this.name = "skillxp";
        this.aliases = new String[]{"skillexp"};
    }

    @Override
    public void handleCommand(MessageReceivedEvent e, @NotNull DiscordServer currentDiscordServer, @NotNull Member senderMember, String[] args) {
        if (main.getLeaderboardUtil().cannotRunLeaderboardCommandInChannel(e.getChannel(), currentDiscordServer))
            return;

        if (args.length == 1) {
            player(e.getChannel(), main.getApiUtil().getMcNameFromDisc(e.getAuthor().getAsTag()));
            return;
        }

        boolean spreadsheet = false;
        if (args.length >= 4 && args[3].equalsIgnoreCase("spreadsheet")) {
            spreadsheet = true;
        }

        if (args[1].equalsIgnoreCase("leaderboard") || args[1].equalsIgnoreCase("lb")) {
            List<Player> guildMemberUuids = main.getApiUtil().getGuildMembers(currentDiscordServer.getHypixelGuild());
            Map<Player, SkillLevels> playerStatMap = (Map<Player, SkillLevels>) main.getLeaderboardUtil().convertPlayerStatMap(currentDiscordServer.getHypixelGuild().getPlayerStatMap(), entry -> entry.getValue().getSkillLevels());

            if (playerStatMap.size() == 0) {
                if (currentDiscordServer.getHypixelGuild().getLeaderboardProgress() == 0) {
                    e.getChannel().sendMessage(main.getMessageByKey("bot_is_still_indexing_names")).queue();
                } else {
                    e.getChannel().sendMessage(String.format(main.getMessageByKey("bot_is_still_indexing_names_progress"), currentDiscordServer.getHypixelGuild().getLeaderboardProgress(), currentDiscordServer.getHypixelGuild().getPlayerSize())).queue();
                }
                return;
            }

            int topX = main.getLeaderboardUtil().calculateTopXFromArgs(args, playerStatMap.size());
            if (topX < 0) {
                e.getChannel().sendMessage("**" + args[2] + "** is not a valid number!").queue();
                return;
            }

            StringBuilder response = new StringBuilder();

            List<Map.Entry<Player, SkillLevels>> leaderboardList = (List<Map.Entry<Player, SkillLevels>>) main.getLeaderboardUtil().sortLeaderboard(playerStatMap, topX);

            // print it like a spreadsheet
            if (spreadsheet) {
                for (Map.Entry<Player, SkillLevels> currentEntry : leaderboardList) {
                    if (!currentEntry.getValue().isApproximate()) {
                        response.append(currentEntry.getKey().getDisplayName()).append("    ").append(main.getSBUtil().toSkillExp(currentEntry.getValue().getAvgSkillLevel())).append("\n");
                    }
                }
            } else {
                int totalAvgSkillExp = 0;

                for (Map.Entry<Player, SkillLevels> currentEntry : leaderboardList) {
                    response.append("**#").append(leaderboardList.indexOf(currentEntry) + 1).append("** *").append(currentEntry.getKey().getDisplayName()).append(":* ").append(main.getLangUtil().addCommas(main.getSBUtil().toSkillExp(currentEntry.getValue().getAvgSkillLevel()))).append("\n");
                    totalAvgSkillExp += currentEntry.getValue().getAvgSkillLevel();
                }
                if (topX == guildMemberUuids.size())
                    response.append("\n**Average guild skill exp: ");
                else
                    response.append("\n**Average skill exp top #").append(topX).append(": ");
                response.append(main.getLangUtil().addNotation(Math.round((double) totalAvgSkillExp / topX))).append("**");
            }

            String responseString = response.toString();
            // Split the message every 2000 characters in a nice looking way because of discord limitations
            List<String> responseList = main.getLangUtil().processMessageForDiscord(responseString, 2000);
            for (int j = 0; j < responseList.size(); j++) {
                String message = responseList.get(j);
                if (j != 0 && !spreadsheet) {
                    e.getChannel().sendMessage(new EmbedBuilder().setDescription(message).build()).queue();
                } else {
                    if (spreadsheet) {
                        e.getChannel().sendMessage("```arm\n" + message + "```").queue();
                    } else {
                        e.getChannel().sendMessage(new EmbedBuilder().setTitle("Average Skill XP Leaderboard").setDescription(message).build()).queue();
                    }
                }
            }
        } else {
            player(e.getChannel(), args[1]);
        }
    }

    public void player(MessageChannel channel, String playerName) {
        Player thePlayer = main.getApiUtil().getPlayerFromUsername(playerName);

        if (thePlayer.getSkyblockProfiles().isEmpty()) {
            channel.sendMessage("Player **" + playerName + "** does not exist!").queue();
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
                .append("Enchanting: ").append(main.getSBUtil().toSkillExp(highestSkillLevels.getEnchanting())).append('\n')
                .append("Taming: ").append(main.getSBUtil().toSkillExp(highestSkillLevels.getTaming())).append('\n')
                .append("Alchemy: ").append(main.getSBUtil().toSkillExp(highestSkillLevels.getAlchemy())).append('\n');


        embedBuilder.setDescription(descriptionBuilder.toString());

        StringBuilder footerBuilder = new StringBuilder();
        embedBuilder.setFooter(footerBuilder
                .append("Carpentry: ").append(main.getSBUtil().toSkillExp(highestSkillLevels.getCarpentry()))
                .append(", runecrafting: ").append(main.getSBUtil().toSkillExp(highestSkillLevels.getRunecrafting()))
                .toString());

        channel.sendMessage(embedBuilder.build()).queue();

        channel.sendMessage("Invalid usage! Usage: *" + getName() + " player <IGN>*").queue();
    }
}