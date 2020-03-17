package com.confusinguser.sbgods.discord;

import java.util.ArrayList;
import java.util.Arrays;

import javax.security.auth.login.LoginException;

import com.confusinguser.sbgods.SBGods;
import com.confusinguser.sbgods.discord.commands.Command;
import com.confusinguser.sbgods.discord.commands.DeathsCommand;
import com.confusinguser.sbgods.discord.commands.HelpCommand;
import com.confusinguser.sbgods.discord.commands.KillsCommand;
import com.confusinguser.sbgods.discord.commands.PetsCommand;
import com.confusinguser.sbgods.discord.commands.SbgodsCommand;
import com.confusinguser.sbgods.discord.commands.SettingsCommand;
import com.confusinguser.sbgods.discord.commands.SkillCommand;
import com.confusinguser.sbgods.discord.commands.SlayerCommand;
import com.confusinguser.sbgods.discord.commands.WhatguildCommand;

import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class DiscordBot {
	public String commandPrefix = "-";

	private final String creatorId = "244786205873405952";
	public User creatorUser;
	public String creatorTag;

	private SBGods main;
	private JDA jda;

	public SlayerCommand slayerCommand;
	public SkillCommand skillCommand;
	public HelpCommand helpCommand;
	public SbgodsCommand sbgodsCommand;
	public WhatguildCommand whatguildCommand;
	public PetsCommand petsCommand;
	public KillsCommand killsCommand;
	public DeathsCommand deathsCommand;
	public SettingsCommand settingsCommand;
	public ArrayList<Command> commands;

	public DiscordBot(SBGods main) throws LoginException {
		String token = "NjY0OTAwNzM0NTk2NDE1NDg4.XmAMIw.ZsJeeq4l6uJ5uFajk9x3-g-rDNE";

		this.main = main;
		slayerCommand = new SlayerCommand(main, this);
		skillCommand = new SkillCommand(main, this);
		helpCommand = new HelpCommand(main, this);
		sbgodsCommand = new SbgodsCommand(main, this);
		whatguildCommand = new WhatguildCommand(main, this);
		petsCommand = new PetsCommand(main, this);
		killsCommand = new KillsCommand(main, this);
		deathsCommand = new DeathsCommand(main, this);
		//settingsCommand = new SettingsCommand(main, this);

		commands = new ArrayList<Command>(Arrays.asList(slayerCommand, skillCommand, helpCommand, sbgodsCommand, whatguildCommand, petsCommand, killsCommand, deathsCommand));

		JDABuilder jdaBuilder = new JDABuilder(AccountType.BOT)
				.setToken(token)
				.setStatus(OnlineStatus.ONLINE)
				.setActivity(Activity.playing("Use " + helpCommand.name + " to get started"));

		for (Command command : commands) {
			jdaBuilder.addEventListeners(command);
		}
		jda = jdaBuilder.build();
		creatorUser = jda.getUserById(creatorId);
		creatorTag = "ConfusingUser#5712";//jda.getUserById(creatorId).getAsTag();
		jda.getPresence().setActivity(Activity.playing("Use " + commandPrefix + "help to get started. Made by " + creatorTag));

	}

	public boolean isValidCommand(String command) {
		for (Command validCommand : commands) {
			String validCommandString = validCommand.name;
			if (command.equalsIgnoreCase(validCommandString)) {
				return true;
			}
		}
		return false;
	}

	public JDA getJDA() {
		return jda;
	}

	public boolean isLeaderboardChannel(MessageReceivedEvent e) {
		if (main.getActiveChannel() == null) return true;
		else if (e.getChannel().getId().contentEquals(main.getActiveChannel())) return true;
		else return false;
	}

	public boolean shouldRun(MessageReceivedEvent e) {
		if (main.getActiveServer() == null) return true;
		else if (e.getGuild().getId().contentEquals(main.getActiveServer())) return true;
		else return false;
	}

	public String escapeMarkdown(String text) {
		String unescaped = text.replaceAll("/\\\\(\\*|_|`|~|\\\\)/g", "$1"); // unescape any "backslashed" character
		String escaped = unescaped.replaceAll("/(\\*|_|`|~|\\\\)/g", "\\$1"); // escape *, _, `, ~, \
		return escaped;

	}
}