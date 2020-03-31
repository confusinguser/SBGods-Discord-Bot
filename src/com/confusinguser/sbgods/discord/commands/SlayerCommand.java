package com.confusinguser.sbgods.discord.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import com.confusinguser.sbgods.SBGods;
import com.confusinguser.sbgods.discord.DiscordBot;
import com.confusinguser.sbgods.entities.DiscordServer;
import com.confusinguser.sbgods.entities.SkyblockPlayer;
import com.confusinguser.sbgods.entities.SlayerExp;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;

public class SlayerCommand extends Command implements EventListener {

	public SlayerCommand(SBGods main, DiscordBot discord) {
		this.main = main;
		this.discord = discord;
		this.name = discord.commandPrefix + "slayer";
		this.aliases = new String[] {};
	}

	@Override
	public void onMessageReceived(MessageReceivedEvent e) {
		if (e.getAuthor().isBot() || !isTheCommand(e) || !discord.shouldRun(e)) {
			return;
		}
		
		main.logInfo(e.getAuthor().getName() + " ran command: " + e.getMessage().getContentRaw());
		
		DiscordServer currentDiscordServer = DiscordServer.getDiscordServerFromEvent(e);

		if (currentDiscordServer.getChannelId() != null && !e.getChannel().getId().contentEquals(currentDiscordServer.getChannelId())) {
			e.getChannel().sendMessage("Slayer commands cannot be ran in this channel!").queue();
			return;
		}

		String[] args = e.getMessage().getContentRaw().split(" ");

		if (args.length == 1) {
			e.getChannel().sendMessage("Invalid argument! Valid arguments: `leaderboard`, `player`!").queue();
			return;
		}

		if (args[1].equalsIgnoreCase("leaderboard")) {
			ArrayList<SkyblockPlayer> guildMemberUuids = main.getApiUtil().getGuildMembers(DiscordServer.getDiscordServerFromEvent(e).getHypixelGuild());
			HashMap<String, SlayerExp> usernameSlayerExpHashMap = currentDiscordServer.getHypixelGuild().getSlayerExpHashmap();
			
			if (usernameSlayerExpHashMap.size() == 0) {
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
						e.getChannel().sendMessage("**" + args[2] + "** is not a valid number!").queue();
						return;
					}
				}
			} else {
				topX = 10;
			}

			StringBuilder response = new StringBuilder("**Slayer XP Leaderboard:**\n");
			if (args.length > 2) {
				response.append("\n");
			} else {
				response.append("*Tip: " + this.name + " leaderboard [length / all]*\n\n");
			}

			for (int i = 0; i < topX; i++) {
				Entry<String, SlayerExp> currentEntry = main.getUtil().getHighestKeyValuePairForSlayerExp(usernameSlayerExpHashMap, i);
				response.append("**#" + Math.incrementExact(i) + "** *" + currentEntry.getKey() + ":* " + currentEntry.getValue().getTotalExp() + "\n");
				if (i != topX - 1) {
					response.append("\n");
				}
			}
			response.append("**Average guild slayer exp: " + main.getUtil().getAverageFromSlayerExpArray(usernameSlayerExpHashMap.values().toArray(new SlayerExp[usernameSlayerExpHashMap.size()])) + "**");

			String responseString = response.toString();
			// Split the message every 2000 characters in a nice looking way because of discord limitations
			ArrayList<String> responseList = main.getUtil().processMessageForDiscord(responseString, 2000);

			for (int i = 0; i < responseList.size(); i++) {
				String message = responseList.get(i);
				if (i == 0) {
					e.getChannel().sendMessage(message).queue();
				} else {
					e.getChannel().sendMessage("\u200E" + message).queue();
				}
			}
			return;
		}

		if (args[1].equalsIgnoreCase("player")) {
			String messageId = e.getChannel().sendMessage("...").complete().getId();

			SkyblockPlayer thePlayer;
			if (args.length >= 3) {
				thePlayer = main.getApiUtil().getSkyblockPlayerFromUsername(args[2]);
				if (thePlayer.getSkyblockProfiles().isEmpty()) {
					e.getChannel().editMessageById(messageId, "Player **" + args[2] + "** does not exist!").queue();
					return;
				}

				SlayerExp playerSlayerExp = main.getApiUtil().getPlayerSlayerExp(thePlayer.getUUID());

				EmbedBuilder embedBuilder = new EmbedBuilder().setColor(0x51047d).setTitle(main.getLangUtil().makePossessiveForm(thePlayer.getDisplayName()) + " slayer xp");
				embedBuilder.setDescription(embedBuilder.getDescriptionBuilder()
						.append("Total slayer xp: " + playerSlayerExp.getTotalExp() + "\n\n")
						.append("Zombie: " + playerSlayerExp.getZombie() + "\n")
						.append("Spider: " + playerSlayerExp.getSpider() + "\n")
						.append("Wolf: " + playerSlayerExp.getWolf() + "\n")
						.toString());

				e.getChannel().deleteMessageById(messageId).queue();
				e.getChannel().sendMessage(embedBuilder.build()).queue();
				return;
			} else {
				e.getChannel().editMessageById(messageId, "Invalid usage! Usage: *" + this.name + " player <IGN>*").queue();
				return;
			}
		}

		e.getChannel().sendMessage("Invalid argument! Valid arguments: `leaderboard`, `player`!").queue();
	}
}
