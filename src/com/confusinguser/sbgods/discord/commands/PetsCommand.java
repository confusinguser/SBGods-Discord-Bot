package com.confusinguser.sbgods.discord.commands;

import com.confusinguser.sbgods.SBGods;
import com.confusinguser.sbgods.discord.DiscordBot;
import com.confusinguser.sbgods.entities.DiscordServer;
import com.confusinguser.sbgods.entities.Pet;
import com.confusinguser.sbgods.entities.Player;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.ArrayList;
import java.util.List;

public class PetsCommand extends Command {

    public PetsCommand(SBGods main, DiscordBot discord) {
        this.main = main;
        this.discord = discord;
        this.name = "pets";
        this.usage = this.getName() + " <IGN>";
        this.aliases = new String[]{};
    }

    @Override
    public void handleCommand(MessageReceivedEvent e, DiscordServer currentDiscordServer, String[] args) {
        if (args.length <= 1) {
            e.getChannel().sendMessage("Invalid usage! Usage: `" + this.usage + "`").queue();
            return;
        }

        String messageId = e.getChannel().sendMessage("...").complete().getId();
        Player thePlayer = main.getApiUtil().getPlayerFromUsername(args[1]);

        if (thePlayer.getUUID() == null) {
            e.getChannel().deleteMessageById(messageId).queue();
            e.getChannel().sendMessage("Player **" + args[1] + "** does not exist!").queue();
        }
        if (thePlayer.getSkyblockProfiles().isEmpty()) {
            e.getChannel().deleteMessageById(messageId).queue();
            e.getChannel().sendMessage("Player **" + args[1] + "** has never played Skyblock!").queue();
            return;
        }

        List<Pet> totalPets = new ArrayList<>();
        for (String profile : thePlayer.getSkyblockProfiles()) {
            List<Pet> pets = main.getApiUtil().getProfilePets(profile, thePlayer.getUUID()); // Pets in profile
            totalPets.addAll(pets);
        }

        EmbedBuilder embedBuilder = new EmbedBuilder().setTitle(main.getLangUtil().makePossessiveForm(thePlayer.getDisplayName()) + " pets");

        StringBuilder descriptionBuilder = embedBuilder.getDescriptionBuilder();

        for (Pet pet : totalPets) {
            if (pet.isActive()) {
                descriptionBuilder.append("**").append(main.getLangUtil().toLowerCaseButFirstLetter(pet.getTier().toString())).append(" ").append(pet.getType()).append(" (").append(pet.getLevel()).append(")").append("**\n");
            } else {
                descriptionBuilder.append(main.getLangUtil().toLowerCaseButFirstLetter(pet.getTier().toString())).append(" ").append(pet.getType()).append(" (").append(pet.getLevel()).append(")\n");
            }
        }

        embedBuilder.setDescription(descriptionBuilder.toString());
        embedBuilder.setColor(0x056bad);

        e.getChannel().deleteMessageById(messageId).queue();
        e.getChannel().sendMessage(embedBuilder.build()).queue();
    }
}