package com.confusinguser.sbgods.discord.commands;

import com.confusinguser.sbgods.SBGods;
import com.confusinguser.sbgods.discord.DiscordBot;
import com.confusinguser.sbgods.entities.DiscordServer;
import com.confusinguser.sbgods.entities.Player;
import com.confusinguser.sbgods.utils.ApiUtil;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

public class WhatguildCommand extends Command {

    public WhatguildCommand(SBGods main, DiscordBot discord) {
        this.main = main;
        this.discord = discord;
        this.name = "whatguild";
        this.usage = this.getName() + " <IGN>";
        this.aliases = new String[]{"wg"};
    }

    @Override
    public void handleCommand(MessageReceivedEvent e, @NotNull DiscordServer currentDiscordServer, @NotNull Member senderMember, String[] args) {
        if (args.length <= 1) {
            e.getChannel().sendMessage("Invalid usage! Usage: " + this.usage).queue();
            return;
        }

        String messageId = e.getChannel().sendMessage("...").complete().getId();

        Player thePlayer = ApiUtil.getPlayerFromUsername(args[1]);
        if (thePlayer.getSkyblockProfiles().isEmpty()) {
            e.getChannel().deleteMessageById(messageId).queue();
            e.getChannel().sendMessage("Player **" + args[1] + "** does not exist").queue();
            return;
        }
        String guildName = ApiUtil.getGuildFromUUID(thePlayer.getUUID());

        if (guildName == null) {
            e.getChannel().deleteMessageById(messageId).queue();
            e.getChannel().sendMessage("**" + thePlayer.getDisplayName() + "** is not in a guild").queue();
            return;
        }

        e.getChannel().deleteMessageById(messageId).queue();
        e.getChannel().sendMessage("**" + thePlayer.getDisplayName() + "** is in **" + guildName + "**").queue();
    }
}