package com.confusinguser.sbgods.discord.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import com.confusinguser.sbgods.SBGods;
import com.confusinguser.sbgods.discord.DiscordBot;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;

public class SkillCommand extends Command implements EventListener {

	private HashMap<String, Double> usernameAverageSkillLevel = new HashMap<String, Double>();

	public SkillCommand(SBGods main, DiscordBot discord) {
		this.main = main;
		this.discord = discord;
		this.name = discord.commandPrefix + "skill";
	}

	@Override
	public void onMessageReceived(MessageReceivedEvent e) {
		if (e.getAuthor().isBot() || !e.getMessage().getContentRaw().toLowerCase().startsWith(this.name) || !discord.shouldRun(e)) {
			return;
		}

		main.logInfo(e.getAuthor().getName() + " ran command: " + e.getMessage().getContentRaw());

		String[] args = e.getMessage().getContentRaw().split(" ");

		if (args.length == 1) {
			e.getChannel().sendMessage("Invalid argument! Valid arguments: `leaderboard`, `player`!").queue();
			return;
		}

		if (args[1].equalsIgnoreCase("leaderboard")) {
			if (!discord.isLeaderboardChannel(e)) {
				if (discord.getJDA().getTextChannelById(discord.leaderboard_channel_id) == null) {
					e.getChannel().sendMessage("Leaderboard commands can only be ran in a channel I don't have access to").queue();
				} else {
					e.getChannel().sendMessage("Leaderboard commands can only be ran in " + discord.getJDA().getTextChannelById(discord.leaderboard_channel_id).getAsMention()).queue();
				}
				return;
			}

			String messageId = e.getChannel().sendMessage("...").complete().getId();
			ArrayList<String> guildMemberUuids = main.getApiUtil().getGuildMembers();

			HashMap<String, Double> usernameAverageSkillLevelCache = new HashMap<String, Double>();
			usernameAverageSkillLevelCache.putAll(usernameAverageSkillLevel);
			if (usernameAverageSkillLevelCache.size() == 0) {
				e.getChannel().editMessageById(messageId, "Bot is still indexing names, please try again in a few minutes!").queue();
				return;
			}

			int topX;
			if (args.length > 2) {
				if (args[2].equalsIgnoreCase("all")) {
					topX = guildMemberUuids.size();
				} else {
					try {
						topX = Math.min(guildMemberUuids.size(), Integer.parseInt(args[2]));
					} catch (NumberFormatException exception) {
						e.getChannel().editMessageById(messageId, "**" + args[2] + "** is not a valid number!").queue();
						return;
					}
				}
			} else {
				topX = 10;
			}

			StringBuilder response = new StringBuilder("**Average skill level Leaderboard:**\n");
			if (args.length > 2) {
				response.append("\n");
			} else {
				response.append("*Tip: " + this.name + " leaderboard [length / all]*\n\n");
			}

			for (int i = 0; i < topX; i++) {
				Entry<String, Double> currentEntry = main.getUtil().getHighestKeyValuePair(usernameAverageSkillLevelCache, true);
				usernameAverageSkillLevelCache.remove(currentEntry.getKey());

				if (currentEntry.getValue() == 0) {
					response.append("**#" + Math.incrementExact(i) + "** *" + currentEntry.getKey() + ":* **Skill API off**\n");
				} else {
					response.append("**#" + Math.incrementExact(i) + "** *" + currentEntry.getKey() + ":* " + main.getUtil().round(currentEntry.getValue(), 2) + "\n");
				}

				if (i != topX - 1) {
					response.append("\n");
				}
			}

			String responseString = response.toString();
			// Split the message every 2000 characters in a nice looking way because of discord limitations
			ArrayList<String> responseList = main.getUtil().processMessageForDiscord(responseString, 2000);

			e.getChannel().deleteMessageById(messageId).queue();
			for (int i = 0; i < responseList.size(); i++) {
				String message = responseList.get(i);
				if (i == 0) {
					e.getChannel().sendMessage(message).complete();
				} else {
					e.getChannel().sendMessage("\u200E" + message).complete();
				}
			}
			return;
		}

		if (args[1].equalsIgnoreCase("player")) {
			String messageId = e.getChannel().sendMessage("...").complete().getId();

			if (args.length >= 3) {
				ArrayList<String> profiles = main.getApiUtil().getSkyblockProfilesAndDisplaynameAndUUIDFromUsername(args[2]);

				if (profiles.isEmpty()) {
					e.getChannel().editMessageById(messageId, "Player **" + args[2] + "** does not exist").queue();
					return;
				}

				double highestAverageSkillLevel = 0;
				for (int j = 2; j < profiles.size(); j++) {
					String profile = profiles.get(j);
					ArrayList<Double> skillLevels = new ArrayList<Double>();
					for (Integer skill : main.getApiUtil().getProfileSkills(profile, profiles.get(1))) {
						skillLevels.add(main.getSBUtil().toSkillLevel(skill));
					}
					highestAverageSkillLevel = Math.max(highestAverageSkillLevel, main.getUtil().getAverage(skillLevels));
				}

				if (highestAverageSkillLevel == 0) {
					e.getChannel().editMessageById(messageId, "Player **" + profiles.get(0) + "** does not have skill API on").queue();
					return;
				}

				e.getChannel().editMessageById(messageId, "Player **" + profiles.get(0) + "** has an average skill level of **" + main.getUtil().round(highestAverageSkillLevel, 3) + "**").queue();
				return;
			} else {
				e.getChannel().editMessageById(messageId, "Invalid usage! Usage: *" + this.name + " player <IGN>*").queue();
				return;
			}
		}

		e.getChannel().sendMessage("Invalid argument! Valid arguments: `leaderboard`, `player`!").queue();
	}

	public void setAvgSkillLevelHashMap(HashMap<String, Double> input) {
		usernameAverageSkillLevel = input;
	}
}