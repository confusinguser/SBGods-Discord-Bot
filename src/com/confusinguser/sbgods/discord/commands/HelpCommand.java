package com.confusinguser.sbgods.discord.commands;

import com.confusinguser.sbgods.SBGods;
import com.confusinguser.sbgods.discord.DiscordBot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;

import java.awt.*;
import java.util.Random;

public class HelpCommand extends Command implements EventListener {

    public HelpCommand(SBGods main, DiscordBot discord) {
        this.main = main;
        this.discord = discord;
        this.name = "help";
        this.aliases = new String[]{"h"};
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent e) {
        if (e.getAuthor().isBot() || isNotTheCommand(e) || discord.shouldNotRun(e)) {
            return;
        }

        main.logger.info(e.getAuthor().getName() + " ran command: " + e.getMessage().getContentRaw());

        EmbedBuilder embedBuilder = new EmbedBuilder().setTitle("SBGods Discord Bot Help Page");
        Random colorRandom = new Random();
        embedBuilder = embedBuilder.setDescription(embedBuilder.getDescriptionBuilder()
                .append("slayer leaderboard [length / all]: \t**Shows slayer XP leaderboard**\n")
                .append("slayer player <IGN>: \t**Shows a specific player's slayer XP**\n\n")
                .append("skill leaderboard [length / all]:  \t**Shows average skill level leaderboard**\n")
                .append("skill player <IGN>: \t**Shows a specific player's average skill level**\n\n")
                .append("pets <IGN>: \t**Shows a specific player's pets**\n\n")
                .append("player <IGN>: \t**Shows stats of a specific player**\n\n")
                .append("whatguild <IGN>: \t**Shows a specific player's guild**\n\n")
                .append("tax: \t**Check how much tax a player owes**\n")
                .append("tax info <IGN>: \t**Check how much tax a player owes**\n")
                .append("tax owelist: \t**Shows a leaderboard with who owes the most**\n\n")
                .toString())
                .setColor(new Color(colorRandom.nextFloat(), colorRandom.nextFloat(), colorRandom.nextFloat()));

        e.getChannel().sendMessage(embedBuilder.build()).queue();
    }
}