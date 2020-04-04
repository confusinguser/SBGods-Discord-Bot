package com.confusinguser.sbgods.discord.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import com.confusinguser.sbgods.SBGods;
import com.confusinguser.sbgods.discord.DiscordBot;
import com.confusinguser.sbgods.entities.DiscordServer;
import com.confusinguser.sbgods.entities.SkillLevels;
import com.confusinguser.sbgods.entities.SkyblockPlayer;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;

public class SkillCommand extends Command implements EventListener {

	public SkillCommand(SBGods main, DiscordBot discord) {
		this.main = main;
		this.discord = discord;
		this.name = "skill";
		this.aliases = new String[] {"skills"};
	}

	@Override
	public void onMessageReceived(MessageReceivedEvent e) {
		if (e.getAuthor().isBot() || !isTheCommand(e) || !discord.shouldRun(e)) {
			return;
		}

		DiscordServer currentDiscordServer = DiscordServer.getDiscordServerFromEvent(e);

		if (currentDiscordServer.getChannelId() != null && !e.getChannel().getId().contentEquals(currentDiscordServer.getChannelId())) {
			e.getChannel().sendMessage("Skill commands cannot be ran in this channel!").queue();
			return;
		}

		main.logInfo(e.getAuthor().getName() + " ran command: " + e.getMessage().getContentRaw());

		String[] args = e.getMessage().getContentRaw().split(" ");

		if (args.length <= 1) {
			e.getChannel().sendMessage("Invalid argument! Valid arguments: `leaderboard`, `player`!").queue();
			return;
		}

		if (args[1].equalsIgnoreCase("leaderboard")) {
			ArrayList<SkyblockPlayer> guildMemberUuids = main.getApiUtil().getGuildMembers(DiscordServer.getDiscordServerFromEvent(e).getHypixelGuild());
			HashMap<String, SkillLevels> usernameSkillExpHashMap = currentDiscordServer.getHypixelGuild().getSkillExpHashmap();

			if (usernameSkillExpHashMap.size() == 0) {
				if (currentDiscordServer.getHypixelGuild().getSkillProgress() == 0) {
					e.getChannel().sendMessage("Bot is still indexing names, please try again in a few minutes! (Please note that other leaderboards have a higher priority)").queue();
				} else {
					e.getChannel().sendMessage("Bot is still indexing names, please try again in a few minutes! (" + currentDiscordServer.getHypixelGuild().getSkillProgress() + " / " + currentDiscordServer.getHypixelGuild().getPlayerSize() + ")").queue();
				}
				return;
			}

			int topX;
			if (args.length >= 2) {
				if (args[2].equalsIgnoreCase("all")) {
					topX = guildMemberUuids.size();
				} else {
					try {
						topX = Math.min(guildMemberUuids.size(), Integer.parseInt(args[2]));
					} catch (NumberFormatException exception) {
						e.getChannel().sendMessage("**" + args[2] + "** is not a valid number!").queue();
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
				response.append("*Tip: " + this.getName() + " leaderboard [length / all]*\n\n");
			}

			for (int i = 0; i < topX; i++) {
				Entry<String, SkillLevels> currentEntry = main.getUtil().getHighestKeyValuePair(usernameSkillExpHashMap, i, true);
				response.append("**#" + Math.incrementExact(i) + "** *" + currentEntry.getKey() + ":* " + main.getUtil().round(currentEntry.getValue().getAvgSkillLevel(), 2));
				if (currentEntry.getValue().isApproximate()) {
					response.append(" *(appr.)*");
				}
				response.append("\n\n");
			}
			response.append("**Average guild skill level: " + main.getUtil().round(main.getUtil().getAverageFromSkillLevelArray(usernameSkillExpHashMap.values().toArray(new SkillLevels[usernameSkillExpHashMap.size()])), 2) + "**");

			String responseString = response.toString();
			// Split the message every 2000 characters in a nice looking way because of discord limitations
			ArrayList<String> responseList = main.getUtil().processMessageForDiscord(responseString, 2000);
			for (int j = 0; j < responseList.size(); j++) {
				String message = responseList.get(j);
				if (j == 0) {
					e.getChannel().sendMessage(message).queue();
				} else {
					e.getChannel().sendMessage("\u200E" + message).queue();
				}
			}
			return;
		}

		if (args[1].equalsIgnoreCase("player")) {
			if (args.length >= 3) {
				SkyblockPlayer thePlayer = main.getApiUtil().getSkyblockPlayerFromUsername(args[2]);

				if (thePlayer.getSkyblockProfiles().isEmpty()) {
					e.getChannel().sendMessage("Player **" + args[2] + "** does not exist!").queue();
					return;
				}

				SkillLevels highestSkillLevels = new SkillLevels();
				for (String profile : thePlayer.getSkyblockProfiles()) {
					SkillLevels skillLevels = main.getApiUtil().getProfileSkills(profile, thePlayer.getUUID());

					if (highestSkillLevels.getAvgSkillLevel() < skillLevels.getAvgSkillLevel()) {
						highestSkillLevels = skillLevels;
					}
				}

				if (highestSkillLevels.getAvgSkillLevel() == 0) {
					SkillLevels skillLevels = main.getApiUtil().getProfileSkillsAlternate(thePlayer.getUUID());

					if (highestSkillLevels.getAvgSkillLevel() < skillLevels.getAvgSkillLevel()) {
						highestSkillLevels = skillLevels;
					}
				}

				EmbedBuilder embedBuilder = new EmbedBuilder().setColor(0x03731d).setTitle(main.getLangUtil().makePossessiveForm(thePlayer.getDisplayName()) + " skill levels");
				StringBuilder descriptionBuilder = embedBuilder.getDescriptionBuilder();

				if (highestSkillLevels.isApproximate()) {
					descriptionBuilder.append("Approximate average skill level: " + main.getUtil().round(highestSkillLevels.getAvgSkillLevel(), 3) + "\n\n");
				} else {
					descriptionBuilder.append("Average skill level: " + main.getUtil().round(highestSkillLevels.getAvgSkillLevel(), 3) + "\n\n");
				}

				embedBuilder.setDescription(descriptionBuilder
						.append("Farming: " + highestSkillLevels.getFarming() + "\n")
						.append("Mining: " + highestSkillLevels.getMining() + "\n")
						.append("Combat: " + highestSkillLevels.getCombat() + "\n")
						.append("Foraging: " + highestSkillLevels.getForaging() + "\n")
						.append("Fishing: " + highestSkillLevels.getFishing() + "\n")
						.append("Enchanting: " + highestSkillLevels.getEnchanting() + "\n")
						.append("Alchemy: " + highestSkillLevels.getAlchemy() + "\n")
						.toString());
				
				StringBuilder footerBuilder = new StringBuilder();
				embedBuilder.setFooter(footerBuilder
						.append("Carpentry: " + highestSkillLevels.getCarpentry() + "\n")
						.append("Runecrafting: " + highestSkillLevels.getRunecrafting())
						.toString());

				e.getChannel().sendMessage(embedBuilder.build()).queue();
				return;

			} else {
				e.getChannel().sendMessage("Invalid usage! Usage: *" + this.getName() + " player <IGN>*").queue();
				return;
			}
		}

		e.getChannel().sendMessage("Invalid argument! Valid arguments: `leaderboard`, `player`!").queue();
	}
}