package com.confusinguser.sbgods.discord.commands;

import com.confusinguser.sbgods.SBGods;
import com.confusinguser.sbgods.discord.DiscordBot;
import com.confusinguser.sbgods.entities.DiscordServer;
import com.confusinguser.sbgods.entities.Pet;
import com.confusinguser.sbgods.entities.Player;
import com.confusinguser.sbgods.entities.leaderboard.SkillLevels;
import com.confusinguser.sbgods.entities.leaderboard.SlayerExp;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class PlayerCommand extends Command {

    public PlayerCommand(SBGods main, DiscordBot discord) {
        this.main = main;
        this.discord = discord;
        this.name = "player";
        this.usage = this.name + " <IGN>";
        this.aliases = new String[]{"user"};
    }

    @Override
    public void handleCommand(MessageReceivedEvent e, @NotNull DiscordServer currentDiscordServer, @NotNull Member senderMember, String[] args) {
        if (args.length == 1) {



        }

        String messageId = e.getChannel().sendMessage("Loading (" + main.getLangUtil().getProgressBar(0.0, 20) + ")").complete().getId();

        Player player = main.getApiUtil().getPlayerFromUsername(args[1]);

        if (player.getDisplayName() == null) {
            e.getChannel().editMessageById(messageId, "Invalid player " + args[1]).queue();
            return;
        }

        SkillLevels skillLevels = new SkillLevels();
        List<Pet> totalPets = new ArrayList<>();
        SlayerExp slayerExp = new SlayerExp();
        String guildName;
        double totalMoney = 0.0;
        double progress = 0.0;

        for (int i = 0; i < player.getSkyblockProfiles().size(); i++) {
            String profileId = player.getSkyblockProfiles().get(i);
            progress += 1d / player.getSkyblockProfiles().size();
            e.getChannel().editMessageById(messageId, "Loading (" + main.getLangUtil().getProgressBar(progress, 20) + ")").queue();

            List<Pet> pets = main.getApiUtil().getProfilePets(profileId, player.getUUID()); // Pets in profile
            totalPets.addAll(pets);

            skillLevels = main.getApiUtil().getBestProfileSkillLevels(player.getUUID());
            totalMoney += main.getApiUtil().getTotalCoinsInProfile(profileId);
            slayerExp = SlayerExp.addExps(slayerExp, main.getApiUtil().getProfileSlayerExp(profileId, player.getUUID()));
        }
        guildName = main.getApiUtil().getGuildFromUUID(player.getUUID());

        EmbedBuilder embedBuilder = new EmbedBuilder().setTitle(player.getDisplayName()).setColor(main.getUtil().getColorFromRankString()).setThumbnail("https://visage.surgeplay.com/bust/" + player.getUUID()).setFooter("SBGods");
        User discordUser = null;
        if (!player.getDiscordTag().equals("")) {
            discordUser = main.getDiscord().getJDA().getUserByTag(player.getDiscordTag());
        }

        if (player.getDiscordTag() != null && discordUser != null)
            embedBuilder.addField("Discord", discordUser.getAsMention(), true);
        embedBuilder.addField("Status", player.isOnline() ? "Online" : "Offline", true);

        embedBuilder.addField("Guild", guildName == null ? "Not in a guild!" : guildName, false);

        embedBuilder.addField("Average skill level", main.getUtil().round(skillLevels.getAvgSkillLevel(), 2) + (skillLevels.isApproximate() ? " (Approx)" : ""), true);
        embedBuilder.addField("Slayer EXP", main.getLangUtil().addNotation(slayerExp.getTotalExp()), true);
        embedBuilder.addField("Total money (All coops)", totalMoney == 0 ? "Banking API off" : main.getLangUtil().addNotation(totalMoney), true);

        embedBuilder.addField("Skill LB Position", player.getSkillPos(true) == -1 ? "Not in guild" : player.getSkillPos(true) == -2 ? "Bot is loading..." : "#" + (player.getSkillPos(true) + 1), true);
        embedBuilder.addField("Slayer LB Position", player.getSlayerPos(true) == -1 ? "Not in guild" : player.getSkillPos(true) == -2 ? "Bot is loading..." : "#" + (player.getSlayerPos(true) + 1), true);

        StringBuilder petStr = new StringBuilder();
        for (Pet pet : totalPets) {
            if (pet.isActive()) {
                petStr.append("\n").append(main.getLangUtil().toLowerCaseButFirstLetter(pet.getTier().toString())).append(" ").append(pet.getType()).append(" (").append(pet.getLevel()).append(")");
            }
        }
        embedBuilder.addField("Active pets (one per profile)", petStr.toString(), false);

        MessageEmbed messageEmbed = embedBuilder.build();

        e.getChannel().deleteMessageById(messageId).queue();
        e.getChannel().sendMessage(messageEmbed).queue();
    }
}