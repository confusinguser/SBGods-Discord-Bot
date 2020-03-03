package com.confusinguser.sbgods.discord.commands;

import com.confusinguser.sbgods.SBGods;
import com.confusinguser.sbgods.discord.DiscordBot;

import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class Command extends ListenerAdapter {
	SBGods main;
	DiscordBot discord;
	public String name;
	public String usage;
}
