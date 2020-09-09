package com.confusinguser.sbgods.discord.commands;

import com.confusinguser.sbgods.SBGods;
import com.confusinguser.sbgods.discord.DiscordBot;
import com.confusinguser.sbgods.entities.DiscordPerms;
import com.confusinguser.sbgods.entities.DiscordServer;
import com.confusinguser.sbgods.entities.HelpMessage;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class HelpCommand extends Command {

    public HelpCommand(SBGods main, DiscordBot discord) {
        this.main = main;
        this.discord = discord;
        this.name = "help";
        this.aliases = new String[]{"h"};
        this.perm = DiscordPerms.DEFAULT;
    }

    @Override
    public void handleCommand(MessageReceivedEvent e, @NotNull DiscordServer currentDiscordServer, @NotNull Member senderMember, String[] args) {
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
        embedBuilder.addBlankField(false);
        embedBuilder.addField("**Use **`-help [COMMAND]`** to find out more about a command**", "", false);
        StringBuilder description = new StringBuilder();

        for (HelpMessage helpMessage : HelpMessage.values()) {
            if (!helpMessage.getCommand().contains(" ") &&
                    !helpMessage.getCommand().equals("help") &&
                    (!helpMessage.getHelpLines()[helpMessage.getHelpLines().length - 1].equals("Requires to be a bot dev or server admin to use.") ||
                            DiscordPerms.getPerms(senderMember).getPower() >= DiscordPerms.STAFF.getPower())) {
                description.append(helpMessage.getUsage()).append(": \t**").append(helpMessage.getHelpLines()[0]).append("**\n\n");
            }
        }
        embedBuilder.setDescription(description.toString());
        embedBuilder.setColor(0xe3a702);
        embedBuilder.setFooter("Version " + SBGods.VERSION);

        e.getChannel().sendMessage(embedBuilder.build()).queue();
    }
}