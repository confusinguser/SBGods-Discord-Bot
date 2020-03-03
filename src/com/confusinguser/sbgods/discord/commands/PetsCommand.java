package com.confusinguser.sbgods.discord.commands;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Random;

import com.confusinguser.sbgods.SBGods;
import com.confusinguser.sbgods.discord.DiscordBot;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;

public class PetsCommand extends Command implements EventListener {

	public PetsCommand(SBGods main, DiscordBot discord) {
		this.main = main;
		this.discord = discord;
		this.name = discord.commandPrefix + "pets";
		this.usage = this.name + " <IGN>";
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

		ArrayList<String> skyblockProfiles = main.getApiUtil().getSkyblockProfilesAndDisplaynameAndUUIDFromUsername(args[1]);
		if (skyblockProfiles.isEmpty()) {
			e.getChannel().sendMessage("Player **" + args[1] + "** does not exist!").queue();
		}

		HashMap<String, Entry<Integer, Boolean>> totalPets = new HashMap<String, Entry<Integer, Boolean>>();

		for (int i = 2; i < skyblockProfiles.size(); i++) {
			HashMap<String, Entry<Integer, Boolean>> pets = main.getApiUtil().getProfilePets(skyblockProfiles.get(i), skyblockProfiles.get(1)); // Pets in profile
			// Remove from pets if totalPets has higher level of same pet
			totalPets.putAll(main.getSBUtil().keepHighestLevelOfPet(pets, totalPets));
		}

		EmbedBuilder embedBuilder = new EmbedBuilder().setTitle(skyblockProfiles.get(0) + "\'s pets");
		Random colorRandom = new Random();

		StringBuilder descriptionBuilder = embedBuilder.getDescriptionBuilder();

		for (Entry<String, Entry<Integer, Boolean>> pet : totalPets.entrySet()) {
			if (pet.getValue().getValue()) { // IsActive
				descriptionBuilder.append("**" + pet.getKey() + " (" + pet.getValue().getKey() + ")" + "**\n");
			} else {
				descriptionBuilder.append(pet.getKey() + "(" + pet.getValue().getKey() + ")\n");
			}
		}

		embedBuilder = embedBuilder.setDescription(descriptionBuilder.toString());
		embedBuilder.setColor(new Color(colorRandom.nextFloat(), colorRandom.nextFloat(), colorRandom.nextFloat()));

		e.getChannel().sendMessage(embedBuilder.build()).queue();
	}
}