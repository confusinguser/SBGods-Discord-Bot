package com.confusinguser.sbgods.discord;

import com.confusinguser.sbgods.SBGods;
import com.confusinguser.sbgods.entities.DiscordServer;
import com.confusinguser.sbgods.entities.Player;
import com.confusinguser.sbgods.entities.leaderboard.SkillLevels;
import com.confusinguser.sbgods.entities.leaderboard.SlayerExp;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ReactionListener extends ListenerAdapter {

    final SBGods main;
    final DiscordBot discord;

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
            e.getChannel().deleteMessageById(e.getMessageId()).queue();
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
            e.getChannel().sendMessage(user.getAsMention() + " you need to verify before applying.").complete().delete().queueAfter(30, TimeUnit.SECONDS);
            e.getChannel().deleteMessageById(messageId).queue();
        }

        Player player = main.getApiUtil().getPlayerFromUsername(playerIGN);

        e.getChannel().editMessageById(messageId, "Loading... (" + main.getLangUtil().getProgressBar(0.25, 20) + ")").queue();

        SlayerExp slayerExp = main.getApiUtil().getPlayerSlayerExp(player.getUUID());

        e.getChannel().editMessageById(messageId, "Loading... (" + main.getLangUtil().getProgressBar(0.5, 20) + ")").queue();

        SkillLevels skillLevels = main.getApiUtil().getBestPlayerSkillLevels(player.getUUID());

        e.getChannel().editMessageById(messageId, "Loading... (" + main.getLangUtil().getProgressBar(0.75, 20) + ")").queue();

        try {
            if (((TextChannel) e.getGuild().getChannels().stream().filter(channel -> channel.getName().contains("accepted-applications"))
                    .collect(Collectors.toList()).get(0))
                    .getHistoryFromBeginning(100)
                    .complete()
                    .getRetrievedHistory()
                    .stream()
                    .filter(message -> !message.getEmbeds().isEmpty())
                    .map(message -> message.getEmbeds().get(0).getTitle())
                    .filter(Objects::nonNull)
                    .anyMatch(title -> title.toLowerCase().contains(player.getDisplayName().toLowerCase()))) {
                e.getChannel().sendMessage(user.getAsMention() + " you already have a pending application.").complete().delete().queueAfter(30, TimeUnit.SECONDS);
                e.getChannel().deleteMessageById(messageId).queue();
            }
            if (skillLevels.isApproximate()) {
                e.getChannel().sendMessage(user.getAsMention() + " you need to turn your skill API on before applying.").complete().delete().queueAfter(30, TimeUnit.SECONDS);
                e.getChannel().deleteMessageById(messageId).queue();
                return;
            }

            boolean meetsSlayer = false;
            boolean meetsSkill = false;

            if (player.getGuildId() != null) {
                if (player.getGuildId().equals(currentDiscordServer.getHypixelGuild().getGuildId())) {
                    e.getChannel().sendMessage(user.getAsMention() + " you are already in the guild.").complete().delete().queueAfter(30, TimeUnit.SECONDS);
                    e.getChannel().deleteMessageById(messageId).queue();
                    return;
                }
            }

            if (slayerExp.getTotalExp() > currentDiscordServer.getHypixelGuild().getSlayerReq()) {
                meetsSlayer = true;
            }
            if (skillLevels.getAvgSkillLevel() > currentDiscordServer.getHypixelGuild().getSkillReq()) {
                meetsSkill = true;
            }
            e.getChannel().editMessageById(messageId, "Loading... (" + main.getLangUtil().getProgressBar(1.0, 20) + ")").queue();

            if (!meetsSkill && !meetsSlayer) {
                e.getChannel().sendMessage(user.getAsMention() + " you dont meet the slayer requirement of " + currentDiscordServer.getHypixelGuild().getSlayerReq() + " slayer exp" + "\nor the skill requirement of " + currentDiscordServer.getHypixelGuild().getSkillReq() + " average skill level").complete().delete().queueAfter(30, TimeUnit.SECONDS);
                e.getChannel().deleteMessageById(messageId).queue();
                return;
            } else if (!meetsSlayer) {
                e.getChannel().sendMessage(user.getAsMention() + " you dont meet the slayer requirement of " + currentDiscordServer.getHypixelGuild().getSlayerReq() + " slayer exp").complete().delete().queueAfter(30, TimeUnit.SECONDS);
                e.getChannel().deleteMessageById(messageId).queue();
                return;
            } else if (!meetsSkill) {
                e.getChannel().sendMessage(user.getAsMention() + " you dont meet the skill requirement of " + currentDiscordServer.getHypixelGuild().getSkillReq() + " average skill level").complete().delete().queueAfter(30, TimeUnit.SECONDS);
                e.getChannel().deleteMessageById(messageId).queue();
                return;
            }
            double playerScore = ((double) slayerExp.getTotalExp() / currentDiscordServer.getHypixelGuild().getSlayerReq() + (skillLevels.getAvgSkillLevel()) / currentDiscordServer.getHypixelGuild().getSkillReq()) / 2;

            EmbedBuilder embedBuilder = new EmbedBuilder()
                    .setTitle(main.getLangUtil().makePossessiveForm(player.getDisplayName()) + " application (Score " + Math.round(playerScore * 100) + ")").setColor(new Color((int) (117 * Math.min(playerScore, 2)) /* Gets "redder" the higher score you have */, 48, 11));

            embedBuilder.setThumbnail("https://visage.surgeplay.com/bust/" + player.getUUID());
            String description = String.format(
                    "Slayer exp: %s\nAvg. skill level: %s\nDiscord: %s",
                    main.getLangUtil().addNotation(slayerExp.getTotalExp()),
                    main.getUtil().round(skillLevels.getAvgSkillLevel(), 2),
                    user.getAsMention());
            embedBuilder.setDescription(description);
            embedBuilder.setTimestamp(new Date().toInstant());
            ((TextChannel) e.getGuild().getChannels().stream().filter(channel -> channel.getName().contains("accepted-applications"))
                    .collect(Collectors.toList()).get(0))
                    .sendMessage(embedBuilder.build())
                    .queue(message -> message.addReaction("☑").queue());

            e.getChannel().sendMessage(user.getAsMention() + " the application was successfully created! It may take up to a day to get invited to the guild").complete().delete().queueAfter(30, TimeUnit.SECONDS);
            e.getChannel().deleteMessageById(messageId).queue();

        } catch (Throwable t) {
            main.getDiscord().reportFail(t, "Reaction Listener");

            e.getChannel().sendMessage(user.getAsMention() + " there was an error somewhere, get in contact with a bot dev to help fix the error.").complete().delete().queueAfter(30, TimeUnit.SECONDS);
            e.getChannel().deleteMessageById(messageId).queue();
        }
    }
}