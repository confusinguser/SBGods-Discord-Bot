package com.confusinguser.sbgods.discord;

import java.util.ArrayList;
import java.util.Arrays;
import javax.security.auth.login.LoginException;

import com.confusinguser.sbgods.SBGods;
import com.confusinguser.sbgods.discord.commands.Command;
import com.confusinguser.sbgods.discord.commands.HelpCommand;
import com.confusinguser.sbgods.discord.commands.PetsCommand;
import com.confusinguser.sbgods.discord.commands.SbgodsCommand;
import com.confusinguser.sbgods.discord.commands.SkillCommand;
import com.confusinguser.sbgods.discord.commands.SlayerCommand;
import com.confusinguser.sbgods.discord.commands.WhatguildCommand;

import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class DiscordBot {
	public final String commandPrefix = "-";
	public final String leaderboard_channel_id = "673619910324387885";
	public final String sbgods_guild_id = "602137436490956820";

	private SBGods main;
	private JDA jda;

	public SlayerCommand slayerCommand;
	public SkillCommand skillCommand;
	public HelpCommand helpCommand;
	public SbgodsCommand sbgodsCommand;
	public WhatguildCommand whatguildCommand;
	public PetsCommand petsCommand;
	public ArrayList<Command> commands;

	public DiscordBot(SBGods main) throws LoginException {
		String token = "NjY0OTAwNzM0NTk2NDE1NDg4.Xll58w.xV3_wGoB3TN4_ja8tf7a0Q008hA";

		this.main = main;
		slayerCommand = new SlayerCommand(main, this);
		skillCommand = new SkillCommand(main, this);
		helpCommand = new HelpCommand(main, this);
		sbgodsCommand = new SbgodsCommand(main, this);
		whatguildCommand = new WhatguildCommand(main, this);
		petsCommand = new PetsCommand(main, this);

		commands = new ArrayList<Command>(Arrays.asList(slayerCommand, skillCommand, helpCommand, sbgodsCommand, whatguildCommand, petsCommand));

		JDABuilder jdaBuilder = new JDABuilder(AccountType.BOT)
				.setToken(token)
				.setStatus(OnlineStatus.ONLINE)
				.setActivity(Activity.playing("If there are problems, DM ConfusingUser#5712"));

		for (Command command : commands) {
			jdaBuilder.addEventListeners(command);
		}
		jda = jdaBuilder.build();
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
		if (e.getChannel().getId().contentEquals(leaderboard_channel_id) || !e.getGuild().getId().contentEquals(sbgods_guild_id)) {
			return true;
		} else {
			return false;
		}
	}

	public boolean shouldRun(MessageReceivedEvent e) {
		if (main.isTestCopy() && !e.getGuild().getId().contentEquals(sbgods_guild_id)) {
			return true;
		} else if (!main.isTestCopy() && e.getGuild().getId().contentEquals(sbgods_guild_id)) {
			return true;
		} else {
			return false;
		}
	}

	public String escapeMarkdown(String text) {
		String unescaped = text.replaceAll("/\\\\(\\*|_|`|~|\\\\)/g", "$1"); // unescape any "backslashed" character
		String escaped = unescaped.replaceAll("/(\\*|_|`|~|\\\\)/g", "\\$1"); // escape *, _, `, ~, \
		return escaped;

	}
}