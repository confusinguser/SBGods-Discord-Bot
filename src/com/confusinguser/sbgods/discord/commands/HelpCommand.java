package com.confusinguser.sbgods.discord.commands;

import com.confusinguser.sbgods.SBGods;
import com.confusinguser.sbgods.discord.DiscordBot;
import com.confusinguser.sbgods.entities.DiscordServer;
import com.confusinguser.sbgods.entities.HelpMessage;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;

import java.awt.*;
import java.util.Arrays;
import java.util.Random;

public class HelpCommand extends Command implements EventListener {

    public HelpCommand(SBGods main, DiscordBot discord) {
        this.main = main;
        this.discord = discord;
        this.name = "help";
        this.aliases = new String[]{"h"};
    }

    @Override
    public void handleCommand(MessageReceivedEvent e, DiscordServer currentDiscordServer, String[] args) {
        if (args.length >= 2) {
            HelpMessage message = HelpMessage.getHelpFromCommand(String.join(" ", Arrays.copyOfRange(args, 1, args.length)));
            if (message != null) {
                e.getChannel().sendMessage(message.getEmbed()).queue();
            } else {
                e.getChannel().sendMessage("There is no help menu for that command!").queue();
            }
            return;
        }

        EmbedBuilder embedBuilder = new EmbedBuilder().setTitle("SBGods Discord Bot Help Page");
        Random colorRandom = new Random();
        embedBuilder = embedBuilder.setDescription(embedBuilder.getDescriptionBuilder()
                .append("verify <IGN>: \t**Verify yourself**\n")
                .append("slayer leaderboard [length / all]: \t**Shows slayer XP leaderboard**\n")
                .append("slayer player <IGN>: \t**Shows a specific player's slayer XP**\n\n")
                .append("skill leaderboard [length / all]:  \t**Shows average skill level leaderboard**\n")
                .append("skill player <IGN>: \t**Shows a specific player's average skill level**\n\n")
                .append("player <IGN>: \t**Shows the player's stats and position on the leaderboards**\n\n")
                .append("pets <IGN>: \t**Shows a specific player's pets**\n\n")
                .append("player <IGN>: \t**Shows stats of a specific player**\n\n")
                .append("whatguild <IGN>: \t**Shows a specific player's guild**\n\n")
                .append("tax: \t**Check how much tax a player owes**\n")
                .append("tax info <IGN>: \t**Check how much tax a player owes**\n")
                .append("tax owelist: \t**Shows a leaderboard with who owes the most**\n\n")
                .append("ah <IGN>: \t**Shows the player's auctions**\n\n")
                .append("help <COMMAND>: \t**Shows the help for a specific command**\n\n")
                .toString())
                .setColor(new Color(colorRandom.nextFloat(), colorRandom.nextFloat(), colorRandom.nextFloat()))
                .setFooter("Version " + SBGods.VERSION);

        e.getChannel().sendMessage(embedBuilder.build()).queue();
    }
}