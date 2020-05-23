package com.confusinguser.sbgods.discord.commands;

import com.confusinguser.sbgods.SBGods;
import com.confusinguser.sbgods.discord.DiscordBot;
import com.confusinguser.sbgods.entities.DiscordServer;
import com.confusinguser.sbgods.entities.Player;
import com.confusinguser.sbgods.entities.SkillLevels;
import com.confusinguser.sbgods.entities.SlayerExp;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.*;
import java.util.Date;
import java.util.stream.Collectors;

public class ApplyCommand extends Command {

    public ApplyCommand(SBGods main, DiscordBot discord) {
        this.main = main;
        this.discord = discord;
        this.name = "apply";
        this.usage = this.getName() + "";
        this.aliases = new String[]{};
    }

    @Override
    public void handleCommand(MessageReceivedEvent e, DiscordServer currentDiscordServer, String[] args) {

        if (!e.getChannel().getName().contains("apply")) {
            e.getChannel().sendMessage("This command cannot be used here.").queue();
            return;
        }

        String messageId = e.getChannel().sendMessage("Loading... (" + main.getLangUtil().getProgressBar(0.0, 20) + ")").complete().getId();

        Player player = main.getApiUtil().getPlayerFromUsername(main.getApiUtil().getMcNameFromDisc(e.getAuthor().getAsTag()));

        e.getChannel().editMessageById(messageId, "Loading... (" + main.getLangUtil().getProgressBar(0.25, 20) + ")").queue();

        SlayerExp slayerExp = main.getApiUtil().getPlayerSlayerExp(player.getUUID());

        e.getChannel().editMessageById(messageId, "Loading... (" + main.getLangUtil().getProgressBar(0.5, 20) + ")").queue();

        SkillLevels skillLevels = main.getApiUtil().getBestProfileSkillLevels(player.getUUID());

        e.getChannel().editMessageById(messageId, "Loading... (" + main.getLangUtil().getProgressBar(0.75, 20) + ")").queue();

        e.getChannel().sendMessage("You already have a pending application.").queue();
        e.getChannel().deleteMessageById(messageId).queue();

        if (skillLevels.isApproximate()) {
            e.getChannel().sendMessage("You need to turn your skill API on before applying.").queue();
            e.getChannel().deleteMessageById(messageId).queue();
            return;
        }

        boolean meetsSlayer = false;
        boolean meetsSkill = false;

        if (slayerExp.getTotalExp() > currentDiscordServer.getHypixelGuild().getSlayerReq()) {
            meetsSlayer = true;
        }
        if (skillLevels.getAvgSkillLevel() > currentDiscordServer.getHypixelGuild().getSkillReq()) {
            meetsSkill = true;
        }

        e.getChannel().editMessageById(messageId, "Loading... (" + main.getLangUtil().getProgressBar(1.0, 20) + ")").queue();

        if (!meetsSkill && !meetsSlayer) {
            e.getChannel().sendMessage("You dont meet the slayer requirement of " + currentDiscordServer.getHypixelGuild().getSlayerReq() + " slayer exp" + "\nor this skill requirement of " + currentDiscordServer.getHypixelGuild().getSkillReq() + " average skill level").queue();
            e.getChannel().deleteMessageById(messageId).queue();
            return;
        } else if (!meetsSlayer) {
            e.getChannel().sendMessage("You dont meet the slayer requirement of " + currentDiscordServer.getHypixelGuild().getSlayerReq() + " slayer exp").queue();
            e.getChannel().deleteMessageById(messageId).queue();
            return;
        } else if (!meetsSkill) {
            e.getChannel().sendMessage("You dont meet the skill requirement of " + currentDiscordServer.getHypixelGuild().getSkillReq() + " average skill level").queue();
            e.getChannel().deleteMessageById(messageId).queue();
            return;
        }

        double playerScore = ((double) slayerExp.getTotalExp() / currentDiscordServer.getHypixelGuild().getSlayerReq() + (double) main.getSBUtil().toSkillExp(skillLevels.getAvgSkillLevel()) / currentDiscordServer.getHypixelGuild().getSkillReq()) / 2;

        EmbedBuilder embedBuilder = new EmbedBuilder().setTitle(main.getLangUtil().makePossessiveForm(player.getDisplayName()) + " application (Score " + Math.round(playerScore * 100) + ")").setColor(new Color((int) (117 * playerScore) /* Gets "redder" the higher score you have */, 48, 11));

        embedBuilder.setThumbnail("https://visage.surgeplay.com/bust/" + player.getUUID());
        embedBuilder.appendDescription("Slayer exp: " + slayerExp.getTotalExp());
        embedBuilder.appendDescription("\nAvg. skill level: " + skillLevels.getAvgSkillLevel());
        embedBuilder.setTimestamp(new Date().toInstant());

        ((TextChannel) e.getGuild().getChannels().stream().filter(channel -> channel.getName().contains("accepted-applications"))
                .collect(Collectors.toList()).get(0))
                .sendMessage(embedBuilder.build())
                .queue(message -> message.addReaction("☑").queue());

        e.getChannel().sendMessage("The application was successfully created! It may take up to a day to get invited to the guild").queue();
        e.getChannel().deleteMessageById(messageId).queue();
    }
}