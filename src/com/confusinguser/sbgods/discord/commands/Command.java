package com.confusinguser.sbgods.discord.commands;

import com.confusinguser.sbgods.SBGods;
import com.confusinguser.sbgods.discord.DiscordBot;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class Command extends ListenerAdapter {
	protected SBGods main = SBGods.instance;
	protected DiscordBot discord = main.getDiscord();
	public String name;
	public String usage;
	public String[] aliases;

	public boolean isTheCommand(MessageReceivedEvent e) {
		if (e.getMessage().getContentRaw().toLowerCase().split(" ")[0].contentEquals(this.name)) return true;
		for (String alias : aliases) {
			if (e.getMessage().getContentRaw().toLowerCase().split(" ")[0].substring(discord.commandPrefix.length()).contentEquals(alias)) return true; 
		}
		return false;
	}
}
