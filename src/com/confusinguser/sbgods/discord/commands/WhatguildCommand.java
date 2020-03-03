package com.confusinguser.sbgods.discord.commands;

import java.util.ArrayList;

import com.confusinguser.sbgods.SBGods;
import com.confusinguser.sbgods.discord.DiscordBot;

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
		ArrayList<String> playerInfo = main.getApiUtil().getSkyblockProfilesAndDisplaynameAndUUIDFromUsername(args[1]);
		if (playerInfo.isEmpty()) {
			e.getChannel().editMessageById(messageId, "Player **" + args[1] + "** does not exist").queue();
			return;
		}
		String guildName = main.getApiUtil().getGuildFromUUID(playerInfo.get(1));

		if (guildName == null) {
			e.getChannel().editMessageById(messageId, "**" + playerInfo.get(0) + "** is not in a guild").queue();
			return;
		}

		e.getChannel().editMessageById(messageId, "**" + playerInfo.get(0) + "** is in **" + guildName + "**").queue();
	}
}