package com.confusinguser.sbgods.discord.commands;

import com.confusinguser.sbgods.SBGods;
import com.confusinguser.sbgods.discord.DiscordBot;
import com.confusinguser.sbgods.entities.DiscordServer;
import com.confusinguser.sbgods.entities.Player;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;

public class WhatguildCommand extends Command implements EventListener {

    public WhatguildCommand(SBGods main, DiscordBot discord) {
        this.main = main;
        this.discord = discord;
        this.name = "whatguild";
        this.usage = this.getName() + " <IGN>";
        this.aliases = new String[]{"wg"};
    }

    @Override
    public void handleCommand(MessageReceivedEvent e, DiscordServer currentDiscordserver) {
        String[] args = e.getMessage().getContentRaw().split(" ");

        if (args.length <= 1) {
            e.getChannel().sendMessage("Invalid usage! Usage: " + this.usage).queue();
            return;
        }

        String messageId = e.getChannel().sendMessage("...").complete().getId();

        Player thePlayer = main.getApiUtil().getPlayerFromUsername(args[1]);
        if (thePlayer.getSkyblockProfiles().isEmpty()) {
            e.getChannel().deleteMessageById(messageId).queue();
            e.getChannel().sendMessage("Player **" + args[1] + "** does not exist").queue();
            return;
        }
        String guildName = main.getApiUtil().getGuildFromUUID(thePlayer.getUUID());

        if (guildName == null) {
            e.getChannel().deleteMessageById(messageId).queue();
            e.getChannel().sendMessage("**" + thePlayer.getDisplayName() + "** is not in a guild").queue();
            return;
        }

        e.getChannel().deleteMessageById(messageId).queue();
        e.getChannel().sendMessage("**" + thePlayer.getDisplayName() + "** is in **" + guildName + "**").queue();
    }
}