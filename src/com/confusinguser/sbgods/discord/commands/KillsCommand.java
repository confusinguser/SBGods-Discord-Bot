package com.confusinguser.sbgods.discord.commands;

import java.util.HashMap;
import java.util.Map.Entry;

import com.confusinguser.sbgods.SBGods;
import com.confusinguser.sbgods.discord.DiscordBot;
import com.confusinguser.sbgods.entities.SkyblockPlayer;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class KillsCommand extends Command {

	public KillsCommand(SBGods main, DiscordBot discord) {
		this.main = main;
		this.discord = discord;
		this.name = discord.commandPrefix + "kills";
		this.usage = this.name + " player <IGN>";
	}

	@Override
	public void onMessageReceived(MessageReceivedEvent e) {
		if (e.getAuthor().isBot() || !e.getMessage().getContentRaw().toLowerCase().startsWith(this.name) || !discord.shouldRun(e)) {
			return;
		}

		main.logInfo(e.getAuthor().getName() + " ran command: " + e.getMessage().getContentRaw());

		String[] args = e.getMessage().getContentRaw().split(" ");

		if (args.length <= 1) {
			e.getChannel().sendMessage("Invalid usage! Usage: `" + this.usage + "`").queue();
			return;
		}

		String messageId = e.getChannel().sendMessage("...").complete().getId();

		if (args[1].equalsIgnoreCase("player")) {
			HashMap<String, Integer> totalKills = new HashMap<String, Integer>();
			SkyblockPlayer thePlayer = main.getApiUtil().getSkyblockPlayerFromUsername(args[2]);

			if (thePlayer.getSkyblockProfiles().isEmpty()) {
				e.getChannel().editMessageById(messageId, "Player **" + args[2] + "** does not exist!").queue();
				return;
			}

			for (String profile : thePlayer.getSkyblockProfiles()) {
				for (Entry<String, Integer> killType : main.getApiUtil().getProfileKills(profile, thePlayer.getUUID()).entrySet()) {
					if (totalKills.containsKey(killType.getKey())) {
						totalKills.put(killType.getKey(), totalKills.get(killType.getKey()) + killType.getValue());
					} else {
						totalKills.put(killType.getKey(), killType.getValue());
					}
				}
			}

			EmbedBuilder embedBuilder = new EmbedBuilder().setColor(0xb8300b).setTitle(main.getLangUtil().makePossessiveForm(thePlayer.getDisplayName()) + " kills");

			int totalKillsInt = 0;
			for (Entry<String, Integer> kill : totalKills.entrySet()) {
				totalKillsInt += kill.getValue();
			}
			embedBuilder.appendDescription("Total amount of deaths: " + totalKillsInt + "\n\n");


			int topX = Math.min(totalKills.size(), 10);
			for (int i = 0; i < topX; i++) {
				Entry<String, Integer> currentEntry = main.getUtil().getHighestKeyValuePair(totalKills, i);
				embedBuilder.appendDescription("**#" + Math.incrementExact(i) + "**\t" + currentEntry.getKey() + ": " + currentEntry.getValue() + "\n");
			}

			e.getChannel().deleteMessageById(messageId).queue();
			e.getChannel().sendMessage(embedBuilder.build()).queue();
			return;
		}
		e.getChannel().editMessageById(messageId, "Invalid usage! Usage: `" + this.usage + "`").queue();
	}
}