package com.confusinguser.sbgods.discord.commands;

import com.confusinguser.sbgods.SBGods;
import com.confusinguser.sbgods.discord.DiscordBot;
import com.confusinguser.sbgods.entities.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.ArrayList;
import java.util.EventListener;

public class PlayerCommand extends Command implements EventListener {

    public PlayerCommand(SBGods main, DiscordBot discord) {
        this.main = main;
        this.discord = discord;
        this.name = "player";
        this.usage = this.name + " <IGN>";
        this.aliases = new String[]{"user"};
    }

    @Override
    public void handleCommand(MessageReceivedEvent e, DiscordServer currentDiscordServer, String[] args) {
        if (args.length <= 1) {
            e.getChannel().sendMessage("Invalid usage! Usage: `" + this.usage + "`").queue();
            return;
        }

        String messageId = e.getChannel().sendMessage("Loading (" + main.getLangUtil().getProgressBar(0.0, 20) + ")").complete().getId();

        Player player = main.getApiUtil().getPlayerFromUsername(args[1]);

        if (player.getDisplayName() == null) {
            e.getChannel().editMessageById(messageId, "Invalid player " + args[1]).queue();
            return;
        }

        SkillLevels skillLevels = new SkillLevels();
        ArrayList<Pet> totalPets = new ArrayList<>();
        SlayerExp slayerExp = new SlayerExp();
        double totalMoney = 0.0;
        double progress = 0.0;
        int progressLength = 20;
        double perProfileProgress = 1 / ((double) player.getSkyblockProfiles().size());

        for (int i = 0; i < player.getSkyblockProfiles().size(); i++) {
            String profileId = player.getSkyblockProfiles().get(i);
            progress += perProfileProgress / 4;
            e.getChannel().editMessageById(messageId, "Loading (" + main.getLangUtil().getProgressBar(progress, progressLength) + ")").queue();

            ArrayList<Pet> pets = main.getApiUtil().getProfilePets(profileId, player.getUUID()); // Pets in profile
            totalPets.addAll(pets);

            progress += perProfileProgress / 4;
            e.getChannel().editMessageById(messageId, "Loading (" + main.getLangUtil().getProgressBar(progress, progressLength) + ")").queue();

            skillLevels = main.getApiUtil().getBestProfileSkillLevels(player.getUUID());

            progress += perProfileProgress / 4;
            e.getChannel().editMessageById(messageId, "Loading (" + main.getLangUtil().getProgressBar(progress, progressLength) + ")").queue();

            totalMoney += main.getApiUtil().getTotalMoneyInProfile(profileId);

            progress += perProfileProgress / 4;
            e.getChannel().editMessageById(messageId, "Loading (" + main.getLangUtil().getProgressBar(progress, progressLength) + ")").queue();

            slayerExp = SlayerExp.addExps(slayerExp, main.getApiUtil().getProfileSlayerExp(profileId, player.getUUID()));
        }

        EmbedBuilder embedBuilder = new EmbedBuilder().setTitle(player.getDisplayName()).setColor(0xb8300b).setThumbnail("https://visage.surgeplay.com/bust/" + player.getUUID()).setFooter("SBGods");
        User discordUser = main.getDiscord().getJDA().getUserByTag(player.getDiscordTag());

        if (player.getDiscordTag() != null && discordUser != null)
            embedBuilder.addField("Discord", discordUser.getAsMention(), true);
        embedBuilder.addField("Status", player.isOnline() ? "Online" : "Offline", true);

        embedBuilder.addField("Guild", main.getApiUtil().getGuildFromUUID(player.getUUID()), false);

        embedBuilder.addField("Average skill level", main.getUtil().round(skillLevels.getAvgSkillLevel(), 2) + (skillLevels.isApproximate() ? " (Approx)" : ""), true);
        embedBuilder.addField("Slayer EXP", main.getLangUtil().addNotation(slayerExp.getTotalExp()), true);
        embedBuilder.addField("Total money (All coops)", totalMoney == 0 ? "Banking API off" : main.getLangUtil().addNotation(totalMoney), true);

        embedBuilder.addField("Skill LB Position", player.getSkillPos() == -1 ? "Not in Guild" : player.getSkillPos() == -2 ? "Bot is loading..." : "#" + (player.getSkillPos() + 1), true);
        embedBuilder.addField("Slayer LB Position", player.getSlayerPos() == -1 ? "Not in Guild" : player.getSkillPos() == -2 ? "Bot is loading..." : "#" + (player.getSlayerPos() + 1), true);

        StringBuilder petStr = new StringBuilder();
        for (Pet pet : totalPets) {
            if (pet.isActive()) {
                petStr.append("\n").append(main.getLangUtil().toLowerCaseButFirstLetter(pet.getTier().toString())).append(" ").append(pet.getType()).append(" (").append(pet.getLevel()).append(")");
            }
        }
        embedBuilder.addField("Active pets (One per profile)", petStr.toString(), false);

        e.getChannel().deleteMessageById(messageId).queue();
        e.getChannel().sendMessage(embedBuilder.build()).queue();
    }
}