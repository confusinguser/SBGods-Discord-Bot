package com.confusinguser.sbgods.discord.commands;

import com.confusinguser.sbgods.SBGods;
import com.confusinguser.sbgods.discord.DiscordBot;
import com.confusinguser.sbgods.entities.DiscordServer;
import com.confusinguser.sbgods.entities.Player;
import com.confusinguser.sbgods.entities.SlayerExp;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

public class SlayerCommand extends Command implements EventListener {

    public SlayerCommand(SBGods main, DiscordBot discord) {
        this.main = main;
        this.discord = discord;
        this.name = "slayer";
        this.aliases = new String[]{};
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent e) {
        if (e.getAuthor().isBot() || isNotTheCommand(e) || discord.shouldNotRun(e)) {
            return;
        }

        main.logger.info(e.getAuthor().getName() + " ran command: " + e.getMessage().getContentRaw());

        DiscordServer currentDiscordServer = DiscordServer.getDiscordServerFromEvent(e);
        if (currentDiscordServer == null) {
            return;
        }

        if (currentDiscordServer.getChannelId() != null && !e.getChannel().getId().contentEquals(currentDiscordServer.getChannelId())) {
            e.getChannel().sendMessage("Slayer commands cannot be ran in this channel!").queue();
            return;
        }

        String[] args = e.getMessage().getContentRaw().split(" ");

        if (args.length == 1) {
            e.getChannel().sendMessage("Invalid argument! Valid arguments: `leaderboard`, `player`!").queue();
            return;
        }

        e.getChannel().sendTyping().queue();
        boolean spreadsheet = false;
        if (args.length >= 4 && args[3].equalsIgnoreCase("spreadsheet")) {
            spreadsheet = true;
        }

        if (args[1].equalsIgnoreCase("leaderboard") || args[1].equalsIgnoreCase("lb")) {
            ArrayList<Player> guildMemberUuids = main.getApiUtil().getGuildMembers(Objects.requireNonNull(DiscordServer.getDiscordServerFromEvent(e)).getHypixelGuild());
            Map<String, SlayerExp> usernameSlayerExpHashMap = currentDiscordServer.getHypixelGuild().getSlayerExpMap();

            if (usernameSlayerExpHashMap.size() == 0) {
                if (currentDiscordServer.getHypixelGuild().getSlayerProgress() == 0) {
                    e.getChannel().sendMessage("Bot is still indexing names, please try again in a few minutes! (Please note that other leaderboards have a higher priority)").queue();
                } else {
                    e.getChannel().sendMessage("Bot is still indexing names, please try again in a few minutes! (" + currentDiscordServer.getHypixelGuild().getSlayerProgress() + " / " + currentDiscordServer.getHypixelGuild().getPlayerSize() + ")").queue();
                }
                return;
            }

            int topX;
            if (args.length > 2) {
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

            StringBuilder response = new StringBuilder("**Slayer XP Leaderboard:**\n\n");
            if (args.length >= 4 && args[3].equalsIgnoreCase("spreadsheet")) {
                for (int i = 0; i < topX; i++) {
                    Entry<String, SlayerExp> currentEntry = main.getUtil().getHighestKeyValuePairForSlayerExp(usernameSlayerExpHashMap, i);
                    response.append(currentEntry.getKey() + "    " + main.getLangUtil().addNotation(main.getSBUtil().toSkillExp(main.getUtil().round(currentEntry.getValue().getTotalExp(), 2))) + "\n");
                }
            } else {
                int totalSlayer = 0;
                for (int i = 0; i < topX; i++) {
                    Entry<String, SlayerExp> currentEntry = main.getUtil().getHighestKeyValuePairForSlayerExp(usernameSlayerExpHashMap, i);
                    response.append("**#" + Math.incrementExact(i) + "** *" + currentEntry.getKey() + ":* " + main.getLangUtil().addNotation(currentEntry.getValue().getTotalExp()) + "\n\n");
                    totalSlayer += currentEntry.getValue().getTotalExp();
                }
                if (topX == guildMemberUuids.size())
                    response.append("**Average guild slayer exp: ");
                else
                    response.append("**Average slayer exp top #").append(topX).append(": ");
                response.append(main.getLangUtil().addNotation(Math.round((double) totalSlayer / topX))).append("**");
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
            String messageId = e.getChannel().sendMessage("...").complete().getId();
            e.getChannel().sendTyping().queue();

            Player thePlayer;
            if (args.length >= 3) {
                thePlayer = main.getApiUtil().getPlayerFromUsername(args[2]);
                if (thePlayer.getSkyblockProfiles().isEmpty()) {
                    e.getChannel().editMessageById(messageId, "Player **" + args[2] + "** does not exist!").queue();
                    return;
                }

                SlayerExp playerSlayerExp = main.getApiUtil().getPlayerSlayerExp(thePlayer.getUUID());

                EmbedBuilder embedBuilder = new EmbedBuilder().setColor(0x51047d).setTitle(main.getLangUtil().makePossessiveForm(thePlayer.getDisplayName()) + " slayer xp");
                embedBuilder.setDescription(embedBuilder.getDescriptionBuilder()
                        .append("Total slayer xp: " + main.getLangUtil().addNotation(playerSlayerExp.getTotalExp()) + "\n\n")
                        .append("Zombie: " + main.getLangUtil().addNotation(playerSlayerExp.getZombie()) + '\n')
                        .append("Spider: " + main.getLangUtil().addNotation(playerSlayerExp.getSpider()) + '\n')
                        .append("Wolf: " + main.getLangUtil().addNotation(playerSlayerExp.getWolf()) + '\n')
                        .toString());

                e.getChannel().deleteMessageById(messageId).queue();
                e.getChannel().sendMessage(embedBuilder.build()).queue();
            } else {
                e.getChannel().editMessageById(messageId, "Invalid usage! Usage: *" + this.getName() + " player <IGN>*").queue();
            }
            return;
        }

        e.getChannel().sendMessage("Invalid argument! Valid arguments: `leaderboard`, `player`!").queue();
    }
}
