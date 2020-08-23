package com.confusinguser.sbgods.discord.commands;

import com.confusinguser.sbgods.SBGods;
import com.confusinguser.sbgods.discord.DiscordBot;
import com.confusinguser.sbgods.entities.DiscordServer;
import com.confusinguser.sbgods.entities.Player;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class KillsCommand extends Command {

    public KillsCommand(SBGods main, DiscordBot discord) {
        this.main = main;
        this.discord = discord;
        this.name = "kills";
        this.usage = this.getName() + " player <IGN>";
        this.aliases = new String[]{"k"};
    }

    @Override
    public void handleCommand(MessageReceivedEvent e, DiscordServer currentDiscordServer, String[] args) {
        if (args.length <= 1) {
            e.getChannel().sendMessage("Invalid usage! Usage: `" + this.usage + "`").queue();
            return;
        }

        String messageId = e.getChannel().sendMessage("...").complete().getId();

        if (args[1].equalsIgnoreCase("player")) {
            Map<String, Integer> totalKills = new HashMap<>();
            Player thePlayer = main.getApiUtil().getPlayerFromUsername(args[2]);

            if (thePlayer.getSkyblockProfiles().isEmpty()) {
                e.getChannel().deleteMessageById(messageId).queue();
                e.getChannel().sendMessage("Player **" + args[2] + "** does not exist!").queue();
                return;
            }

            for (String profile : thePlayer.getSkyblockProfiles()) {
                for (Entry<String, Integer> killType : main.getApiUtil().getProfileKills(profile, thePlayer.getUUID()).entrySet()) {
                    if (totalKills.containsKey(killType.getKey())) {
                        totalKills.put(killType.getKey(), totalKills.get(killType.getKey()) + killType.getValue());
                    } else {
                        totalKills.put(killType.getKey(), killType.getValue());
                    }
                }
            }

            EmbedBuilder embedBuilder = new EmbedBuilder().setColor(0xb8300b).setTitle(main.getLangUtil().makePossessiveForm(thePlayer.getDisplayName()) + " kills");

            int totalKillsInt = 0;
            for (Entry<String, Integer> kill : totalKills.entrySet()) {
                totalKillsInt += kill.getValue();
            }
            embedBuilder.appendDescription("Total amount of kills: " + totalKillsInt + "\n\n");

            int topX;
            if (args.length > 3) {
                if (args[3].contentEquals("all")) {
                    topX = totalKills.size();
                } else {
                    try {
                        topX = Math.min(totalKills.size(), Integer.parseInt(args[3]));
                    } catch (NumberFormatException exception) {
                        e.getChannel().editMessageById(messageId, "**" + args[3] + "** is not a valid number!").queue();
                        return;
                    }
                }
            } else {
                topX = 10;
            }

            for (int i = 0; i < topX; i++) {
                Entry<String, Integer> currentEntry = main.getUtil().getHighestKeyValuePair(totalKills, i);
                embedBuilder.appendDescription("**#" + Math.incrementExact(i) + "**\t" + currentEntry.getKey() + ": " + main.getLangUtil().addNotation(currentEntry.getValue()) + '\n');
            }

            e.getChannel().deleteMessageById(messageId).queue();
            e.getChannel().sendMessage(embedBuilder.build()).queue();
            return;
        }
        e.getChannel().deleteMessageById(messageId).queue();
        e.getChannel().sendMessage("Invalid usage! Usage: `" + this.usage + "`").queue();
    }
}