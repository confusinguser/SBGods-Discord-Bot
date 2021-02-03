package com.confusinguser.sbgods.discord.commands;

import com.confusinguser.sbgods.SBGods;
import com.confusinguser.sbgods.discord.DiscordBot;
import com.confusinguser.sbgods.entities.DiscordServer;
import com.confusinguser.sbgods.entities.Pet;
import com.confusinguser.sbgods.entities.Player;
import com.confusinguser.sbgods.entities.leaderboard.LeaderboardValues;
import com.confusinguser.sbgods.utils.Util;
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
        String messageId = e.getChannel().sendMessage("Loading (" + main.getLangUtil().getProgressBar(0.0, 20) + ")").complete().getId();

        Player player;
        if (args.length == 1) {
            player = main.getApiUtil().getPlayerFromUsername(main.getApiUtil().getMcNameFromDisc(senderMember.getUser().getAsTag()));
            if (player == null || player.getDisplayName() == null) {
                e.getChannel().editMessageById(messageId, "Could not find you mc account, use `-player <IGN>` instead").queue();
                return;
            }
        } else {
            player = main.getApiUtil().getPlayerFromUsername(args[1]);

            if (player.getDisplayName() == null) {
                e.getChannel().editMessageById(messageId, "Invalid player " + args[1]).queue();
                return;
            }
        }

        List<Pet> totalPets = new ArrayList<>();
        String guildName;
        double progress = 0.0;
        LeaderboardValues leaderboardValues = new LeaderboardValues();

        for (int i = 0; i < player.getSkyblockProfiles().size(); i++) {
            String profileId = player.getSkyblockProfiles().get(i);
            progress += 1d / player.getSkyblockProfiles().size();
            e.getChannel().editMessageById(messageId, "Loading (" + main.getLangUtil().getProgressBar(progress, 20) + ")").queue();

            List<Pet> pets = main.getApiUtil().getProfilePets(profileId, player.getUUID()); // Pets in profile
            totalPets.addAll(pets);

            leaderboardValues = main.getApiUtil().getBestLeaderboardValues(player);
        }
        guildName = main.getApiUtil().getGuildFromUUID(player.getUUID());

        EmbedBuilder embedBuilder = new EmbedBuilder().setTitle(player.getDisplayName()).setThumbnail("https://visage.surgeplay.com/bust/" + player.getUUID()).setFooter("SBGods");
        User discordUser = null;
        if (!player.getDiscordTag().equals("")) {
            discordUser = main.getDiscord().getJDA().getUserByTag(player.getDiscordTag());
        }

        if (player.getDiscordTag() != null && discordUser != null)
            embedBuilder.addField("Discord", discordUser.getAsMention(), true);
        embedBuilder.addField("Status", player.isOnline() ? "Online" : "Offline", true);

        embedBuilder.addField("Guild", guildName == null ? "Not in a guild!" : guildName, false);

        embedBuilder.addField("Average skill level", Util.round(leaderboardValues.getSkillLevels().getAvgSkillLevel(), 2) + (leaderboardValues.getSkillLevels().isApproximate() ? " (Approx)" : ""), true);
        embedBuilder.addField("Slayer EXP", main.getLangUtil().addNotation(leaderboardValues.getSlayerExp().getTotalExp()), true);
        embedBuilder.addField("Total money (All coops)", leaderboardValues.getBankBalance().getCoins() == 0 ? "Banking API off" : main.getLangUtil().addNotation(leaderboardValues.getBankBalance().getCoins()), true);

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