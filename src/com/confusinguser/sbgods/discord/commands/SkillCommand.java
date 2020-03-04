package com.confusinguser.sbgods.discord.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import com.confusinguser.sbgods.SBGods;
import com.confusinguser.sbgods.discord.DiscordBot;
import com.confusinguser.sbgods.objects.SkillLevels;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;

public class SkillCommand extends Command implements EventListener {

	private HashMap<String, SkillLevels> usernameSkillLevels = new HashMap<String, SkillLevels>();

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

			HashMap<String, SkillLevels> usernameSkillLevelCopy = new HashMap<String, SkillLevels>();
			usernameSkillLevelCopy.putAll(usernameSkillLevels);
			if (usernameSkillLevelCopy.size() == 0) {
				e.getChannel().sendMessage("Bot is still indexing names, please try again in a few minutes!").queue();
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
						e.getChannel().sendMessage("**" + args[2] + "** is not a valid number! Try: `" + this.name + " player " + args[2] + "`").queue();
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
				Entry<String, SkillLevels> currentEntry = main.getUtil().getHighestKeyValuePair(usernameSkillLevelCopy, true);
				usernameSkillLevelCopy.remove(currentEntry.getKey());

				if (currentEntry.getValue().getAvgSkillLevel() == 0) {
					response.append("**#" + Math.incrementExact(i) + "** *" + currentEntry.getKey() + ":* **Skill API off**\n");
				} else {
					response.append("**#" + Math.incrementExact(i) + "** *" + currentEntry.getKey() + ":* " + main.getUtil().round(currentEntry.getValue().getAvgSkillLevel(), 2) + "\n");
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
			if (args.length >= 3) {
				ArrayList<String> profiles = main.getApiUtil().getSkyblockProfilesAndDisplaynameAndUUIDFromUsername(args[2]);

				if (profiles.isEmpty()) {
					e.getChannel().sendMessage("Player **" + args[2] + "** does not exist! Try `" + this.name + " player " + args[2] + "`").queue();
					return;
				}

				SkillLevels highestSkillLevels = new SkillLevels();
				for (int j = 2; j < profiles.size(); j++) {
					String profile = profiles.get(j);
					SkillLevels skillLevels = main.getApiUtil().getProfileSkills(profile, profiles.get(1));

					if (highestSkillLevels.getAvgSkillLevel() < skillLevels.getAvgSkillLevel()) {
						highestSkillLevels = skillLevels;
					}
				}

				if (highestSkillLevels.getAvgSkillLevel() == 0) {
					e.getChannel().sendMessage("Player **" + profiles.get(0) + "** does not have skill API on").queue();
					return;
				}

				EmbedBuilder embedBuilder = new EmbedBuilder().setTitle(main.getLangUtil().makePossessiveForm(profiles.get(0)) + " skill levels");
				embedBuilder.setDescription(embedBuilder.getDescriptionBuilder()
						.append("Average skill level: " + main.getUtil().round(highestSkillLevels.getAvgSkillLevel(), 3) + "\n\n")
						.append("Farming: " + highestSkillLevels.getFarming() + "\n")
						.append("Mining: " + highestSkillLevels.getMining() + "\n")
						.append("Combat: " + highestSkillLevels.getCombat() + "\n")
						.append("Foraging: " + highestSkillLevels.getForaging() + "\n")
						.append("Fishing: " + highestSkillLevels.getFishing() + "\n")
						.append("Enchanting: " + highestSkillLevels.getEnchanting() + "\n")
						.append("Alchemy: " + highestSkillLevels.getAlchemy() + "\n")
						.toString());

				e.getChannel().sendMessage(embedBuilder.build()).queue();
				return;

			} else {
				e.getChannel().sendMessage("Invalid usage! Usage: *" + this.name + " player <IGN>*").queue();
				return;
			}
		}

		e.getChannel().sendMessage("Invalid argument! Valid arguments: `leaderboard`, `player`!").queue();
	}

	public void setAvgSkillLevelHashMap(HashMap<String, SkillLevels> input) {
		usernameSkillLevels = input;
	}
}