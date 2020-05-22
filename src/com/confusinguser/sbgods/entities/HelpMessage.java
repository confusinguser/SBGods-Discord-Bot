package com.confusinguser.sbgods.entities;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.*;
import java.util.Random;

public enum HelpMessage {

    HELP("Help", "help", "Line1", "Line2");

    private final String title;
    private final String command;
    private final String[] helpLines;

    HelpMessage(String title, String command, String... lines) {
        this.title = title;
        this.command = command;
        this.helpLines = lines;
    }

    public static HelpMessage getHelpFromCommand(String input) {
        for (HelpMessage helpMessage : values()) {
            if (helpMessage.command.equalsIgnoreCase(input)) {
                return helpMessage;
            }
        }
        return null;
    }

    public String getTitle() {
        return title;
    }

    public String getCommand() {
        return command;
    }

    public String[] getHelpLines() {
        return helpLines;
    }

    public MessageEmbed getEmbed() {
        EmbedBuilder embedBuilder = new EmbedBuilder();

        embedBuilder.setTitle(title);
        Random colorRandom = new Random();
        embedBuilder.setColor(new Color(colorRandom.nextFloat(), colorRandom.nextFloat(), colorRandom.nextFloat()));
        embedBuilder.setFooter("Help for the `" + command + "`.");

        for (String helpLine : helpLines) {
            embedBuilder.appendDescription(helpLine);
        }

        return embedBuilder.build();
    }
}