package com.confusinguser.sbgods.discord.commands;

import com.confusinguser.sbgods.SBGods;
import com.confusinguser.sbgods.discord.DiscordBot;
import com.confusinguser.sbgods.entities.SkyblockPlayer;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;

public class WhatguildCommand extends Command implements EventListener{

	public WhatguildCommand(SBGods main, DiscordBot discord) {
		this.main = main;
		this.discord = discord;
		this.name = discord.commandPrefix + "whatguild";
	}

	@Override
	public void onMessageReceived(MessageReceivedEvent e) {
		if (e.getAuthor().isBot() || !e.getMessage().getContentRaw().toLowerCase().startsWith(this.name) || !discord.shouldRun(e)) {
			return;
		}

		main.logInfo(e.getAuthor().getName() + " ran command: " + e.getMessage().getContentRaw());

		String[] args = e.getMessage().getContentRaw().split(" ");

		String messageId = e.getChannel().sendMessage("...").complete().getId();
		if (args.length <= 1) {
			e.getChannel().editMessageById(messageId, "Invalid usage! Usage: " + this.name + " <IGN>").queue();
			return;
		}
		SkyblockPlayer thePlayer = main.getApiUtil().getSkyblockPlayerFromUsername(args[1]);
		if (thePlayer.getSkyblockProfiles().isEmpty()) {
			e.getChannel().editMessageById(messageId, "Player **" + args[1] + "** does not exist").queue();
			return;
		}
		String guildName = main.getApiUtil().getGuildFromUUID(thePlayer.getUUID());

		if (guildName == null) {
			e.getChannel().editMessageById(messageId, "**" + thePlayer.getDisplayName() + "** is not in a guild").queue();
			return;
		}

		e.getChannel().editMessageById(messageId, "**" + thePlayer.getDisplayName() + "** is in **" + guildName + "**").queue();
	}
}