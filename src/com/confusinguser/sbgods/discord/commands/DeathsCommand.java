package com.confusinguser.sbgods.discord.commands;

import com.confusinguser.sbgods.SBGods;
import com.confusinguser.sbgods.discord.DiscordBot;
import com.confusinguser.sbgods.entities.Player;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.HashMap;
import java.util.Map.Entry;

public class DeathsCommand extends Command {

    public DeathsCommand(SBGods main, DiscordBot discord) {
        this.main = main;
        this.discord = discord;
        this.name = "deaths";
        this.usage = this.getName() + " player <IGN>";
        this.aliases = new String[]{"d"};
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent e) {
        if (e.getAuthor().isBot() || isNotTheCommand(e) || discord.shouldNotRun(e)) {
            return;
        }

        main.logger.info(e.getAuthor().getName() + " ran command: " + e.getMessage().getContentRaw());

        String[] args = e.getMessage().getContentRaw().split(" ");

        if (args.length <= 1) {
            e.getChannel().sendMessage("Invalid usage! Usage: `" + this.usage + "`").queue();
            return;
        }

        String messageId = e.getChannel().sendMessage("...").complete().getId();
        e.getChannel().sendTyping().queue();

        if (args[1].equalsIgnoreCase("player")) {
            HashMap<String, Integer> totalDeaths = new HashMap<>();
            Player thePlayer = main.getApiUtil().getPlayerFromUsername(args[2]);

            if (thePlayer.getSkyblockProfiles().isEmpty()) {
                e.getChannel().editMessageById(messageId, "Player **" + args[2] + "** does not exist!").queue();
                return;
            }


            for (String profile : thePlayer.getSkyblockProfiles()) {
                for (Entry<String, Integer> deathType : main.getApiUtil().getProfileDeaths(profile, thePlayer.getUUID()).entrySet()) {
                    if (totalDeaths.containsKey(deathType.getKey())) {
                        totalDeaths.put(deathType.getKey(), totalDeaths.get(deathType.getKey()) + deathType.getValue());
                    } else {
                        totalDeaths.put(deathType.getKey(), deathType.getValue());
                    }
                }
            }

            EmbedBuilder embedBuilder = new EmbedBuilder().setColor(0x2154fc).setTitle(main.getLangUtil().makePossessiveForm(thePlayer.getDisplayName()) + " deaths");

            int topX;
            if (args.length > 3) {
                if (args[3].contentEquals("all")) {
                    topX = totalDeaths.size();
                } else {
                    try {
                        topX = Math.min(totalDeaths.size(), Integer.parseInt(args[3]));
                    } catch (NumberFormatException exception) {
                        e.getChannel().editMessageById(messageId, "**" + args[3] + "** is not a valid number!").queue();
                        return;
                    }
                }
            } else {
                topX = 10;
            }

            for (int i = 0; i < topX; i++) {
                Entry<String, Integer> currentEntry = main.getUtil().getHighestKeyValuePair(totalDeaths, i);
                embedBuilder.appendDescription("**#" + Math.incrementExact(i) + "**\t" + currentEntry.getKey() + ": " + currentEntry.getValue() + '\n');
            }

            e.getChannel().deleteMessageById(messageId).queue();
            e.getChannel().sendMessage(embedBuilder.build()).queue();
            return;
        }
        e.getChannel().editMessageById(messageId, "Invalid usage! Usage: `" + this.usage + "`").queue();
    }
}