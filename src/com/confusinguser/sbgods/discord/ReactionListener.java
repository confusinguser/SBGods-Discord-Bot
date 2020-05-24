package com.confusinguser.sbgods.discord;

import com.confusinguser.sbgods.SBGods;
import com.confusinguser.sbgods.entities.DiscordServer;
import com.confusinguser.sbgods.entities.Player;
import com.confusinguser.sbgods.entities.SkillLevels;
import com.confusinguser.sbgods.entities.SlayerExp;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ReactionListener extends ListenerAdapter {

    SBGods main;
    DiscordBot discord;

    public ReactionListener(SBGods main, DiscordBot discord) {
        this.main = main;
        this.discord = discord;
    }

    @Override
    public void onMessageReactionAdd(MessageReactionAddEvent e) {
        DiscordServer currentDiscordServer = DiscordServer.getDiscordServerFromDiscordGuild(e.getGuild());
        if ((e.getUser() != null && e.getUser().isBot()) || e.getUser() == null || currentDiscordServer == null) {
            return;
        }

        if (e.getChannel().getName().toLowerCase().contains("accepted-applications") &&
                e.getReaction().getReactionEmote().getEmoji().equalsIgnoreCase("☑")) {
            e.getTextChannel().deleteMessageById(e.getMessageId()).queue();
        }

        if (e.getChannel().getName().toLowerCase().contains("apply") &&
                e.getReaction().getReactionEmote().getEmoji().equalsIgnoreCase("☑")) {
            apply(e, e.getUser(), currentDiscordServer);
        }

    }

    private void apply(MessageReactionAddEvent e, User user, DiscordServer currentDiscordServer) {
        String messageId = e.getChannel().sendMessage("Loading... (" + main.getLangUtil().getProgressBar(0.0, 20) + ")").complete().getId();

        String playerIGN = main.getApiUtil().getMcNameFromDisc(user.getAsTag());

        if (playerIGN == null || playerIGN.equals("")) {
            e.getChannel().sendMessage("You need to verify before applying.").complete().delete().queueAfter(30, TimeUnit.SECONDS);
            e.getChannel().deleteMessageById(messageId).queue();
        }

        Player player = main.getApiUtil().getPlayerFromUsername(playerIGN);

        e.getChannel().editMessageById(messageId, "Loading... (" + main.getLangUtil().getProgressBar(0.25, 20) + ")").queue();

        SlayerExp slayerExp = main.getApiUtil().getPlayerSlayerExp(player.getUUID());

        e.getChannel().editMessageById(messageId, "Loading... (" + main.getLangUtil().getProgressBar(0.5, 20) + ")").queue();

        SkillLevels skillLevels = main.getApiUtil().getBestProfileSkillLevels(player.getUUID());

        e.getChannel().editMessageById(messageId, "Loading... (" + main.getLangUtil().getProgressBar(0.75, 20) + ")").queue();

        if (!((TextChannel) e.getGuild().getChannels().stream().filter(channel -> channel.getName().contains("accepted-applications"))
                .collect(Collectors.toList()).get(0)).getHistoryFromBeginning(100).complete().getRetrievedHistory().stream().filter(message -> message.getEmbeds().get(0).getTitle().contains(player.getDisplayName().toLowerCase() + " application")).collect(Collectors.toList()).isEmpty()) {
            e.getChannel().sendMessage("You already have a pending application.").complete().delete().queueAfter(30, TimeUnit.SECONDS);
            e.getChannel().deleteMessageById(messageId).queue();
        }
        if (skillLevels.isApproximate()) {
            e.getChannel().sendMessage("You need to turn your skill API on before applying.").complete().delete().queueAfter(30, TimeUnit.SECONDS);
            e.getChannel().deleteMessageById(messageId).queue();
            return;
        }

        boolean meetsSlayer = false;
        boolean meetsSkill = false;

        if (player.getGuildId().equals(currentDiscordServer.getHypixelGuild().getGuildId())) {
            e.getChannel().sendMessage("You are already in the guild.").complete().delete().queueAfter(30, TimeUnit.SECONDS);
            e.getChannel().deleteMessageById(messageId).queue();
            return;
        }

        if (slayerExp.getTotalExp() > currentDiscordServer.getHypixelGuild().getSlayerReq()) {
            meetsSlayer = true;
        }
        if (skillLevels.getAvgSkillLevel() > currentDiscordServer.getHypixelGuild().getSkillReq()) {
            meetsSkill = true;
        }

        e.getChannel().editMessageById(messageId, "Loading... (" + main.getLangUtil().getProgressBar(1.0, 20) + ")").queue();

        if (!meetsSkill && !meetsSlayer) {
            e.getChannel().sendMessage("You dont meet the slayer requirement of " + currentDiscordServer.getHypixelGuild().getSlayerReq() + " slayer exp" + "\nor this skill requirement of " + currentDiscordServer.getHypixelGuild().getSkillReq() + " average skill level").complete().delete().queueAfter(30, TimeUnit.SECONDS);
            e.getChannel().deleteMessageById(messageId).queue();
            return;
        } else if (!meetsSlayer) {
            e.getChannel().sendMessage("You dont meet the slayer requirement of " + currentDiscordServer.getHypixelGuild().getSlayerReq() + " slayer exp").complete().delete().queueAfter(30, TimeUnit.SECONDS);
            e.getChannel().deleteMessageById(messageId).queue();
            return;
        } else if (!meetsSkill) {
            e.getChannel().sendMessage("You dont meet the skill requirement of " + currentDiscordServer.getHypixelGuild().getSkillReq() + " average skill level").complete().delete().queueAfter(30, TimeUnit.SECONDS);
            e.getChannel().deleteMessageById(messageId).queue();
            return;
        }
        double playerScore = ((double) slayerExp.getTotalExp() / currentDiscordServer.getHypixelGuild().getSlayerReq() + (skillLevels.getAvgSkillLevel()) / currentDiscordServer.getHypixelGuild().getSkillReq()) / 2;

        EmbedBuilder embedBuilder = new EmbedBuilder().setTitle(main.getLangUtil().makePossessiveForm(player.getDisplayName()) + " application (Score " + Math.round(playerScore * 100) + ")").setColor(new Color((int) (117 * Math.min(playerScore, 2)) /* Gets "redder" the higher score you have */, 48, 11));

        embedBuilder.setThumbnail("https://visage.surgeplay.com/bust/" + player.getUUID());
        embedBuilder.appendDescription("Slayer exp: " + main.getLangUtil().addNotation(slayerExp.getTotalExp()));
        embedBuilder.appendDescription("\nAvg. skill level: " + main.getUtil().round(skillLevels.getAvgSkillLevel(), 2));
        embedBuilder.setTimestamp(new Date().toInstant());
        ((TextChannel) e.getGuild().getChannels().stream().filter(channel -> channel.getName().contains("accepted-applications"))
                .collect(Collectors.toList()).get(0))
                .sendMessage(embedBuilder.build())
                .queue(message -> message.addReaction("☑").queue());

        e.getChannel().sendMessage("The application was successfully created! It may take up to a day to get invited to the guild").complete().delete().queueAfter(30, TimeUnit.SECONDS);
        e.getChannel().deleteMessageById(messageId).queue();
    }
}