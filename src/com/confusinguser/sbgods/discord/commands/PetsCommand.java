package com.confusinguser.sbgods.discord.commands;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Random;

import com.confusinguser.sbgods.SBGods;
import com.confusinguser.sbgods.discord.DiscordBot;
import com.confusinguser.sbgods.entities.Pet;
import com.confusinguser.sbgods.entities.SkyblockPlayer;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;

public class PetsCommand extends Command implements EventListener {

	public PetsCommand(SBGods main, DiscordBot discord) {
		this.main = main;
		this.discord = discord;
		this.name = discord.commandPrefix + "pets";
		this.usage = this.name + " <IGN>";
		this.aliases = new String[] {};
	}
	@Override
	public void onMessageReceived(MessageReceivedEvent e) {
		if (e.getAuthor().isBot() || !isTheCommand(e) || !discord.shouldRun(e)) {
			return;
		}

		main.logInfo(e.getAuthor().getName() + " ran command: " + e.getMessage().getContentRaw());

		String[] args = e.getMessage().getContentRaw().split(" ");

		if (args.length <= 1) {
			e.getChannel().sendMessage("Invalid usage! Usage: `" + this.usage + "`").queue();
			return;
		}

		String messageId = e.getChannel().sendMessage("...").complete().getId();

		SkyblockPlayer thePlayer = main.getApiUtil().getSkyblockPlayerFromUsername(args[1]);
		if (thePlayer.getSkyblockProfiles().isEmpty()) {
			e.getChannel().editMessageById(messageId, "Player **" + args[1] + "** does not exist!").queue();
			return;
		}

		ArrayList<Pet> totalPets = new ArrayList<Pet>();
		for (String profile : thePlayer.getSkyblockProfiles()) {
			ArrayList<Pet> pets = main.getApiUtil().getProfilePets(profile, thePlayer.getUUID()); // Pets in profile
			totalPets.addAll(pets);
		}

		EmbedBuilder embedBuilder = new EmbedBuilder().setTitle(main.getLangUtil().makePossessiveForm(thePlayer.getDisplayName()) + " pets");
		Random colorRandom = new Random();

		StringBuilder descriptionBuilder = embedBuilder.getDescriptionBuilder();

		for (Pet pet : totalPets) {
			if (pet.isActive()) {
				descriptionBuilder.append("**" + main.getLangUtil().toLowerCaseButFirstLetter(pet.getTier().toString()) + " " + pet.getType() + " (" + pet.getLevel() + ")" + "**\n");
			} else {
				descriptionBuilder.append(main.getLangUtil().toLowerCaseButFirstLetter(pet.getTier().toString())+ " " + pet.getType() + " (" + pet.getLevel() + ")\n");
			}
		}

		embedBuilder = embedBuilder.setDescription(descriptionBuilder.toString());
		embedBuilder.setColor(new Color(colorRandom.nextFloat(), colorRandom.nextFloat(), colorRandom.nextFloat()));

		e.getChannel().deleteMessageById(messageId).queue();
		e.getChannel().sendMessage(embedBuilder.build()).queue();
	}
}