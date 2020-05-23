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
import java.sql.Timestamp;
import java.time.temporal.TemporalAccessor;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;

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

        if(!e.getChannel().getName().contains("apply")){
            e.getChannel().sendMessage("This command cannot be used here.").queue();
            return;
        }

        String messageId = e.getChannel().sendMessage("Loading... (" + main.getLangUtil().getProgressBar(0.0,20) + ")").complete().getId();

        Player player = main.getApiUtil().getPlayerFromUsername(main.getApiUtil().getMcNameFromDisc(e.getAuthor().getAsTag()));

        e.getChannel().editMessageById(messageId,"Loading... (" + main.getLangUtil().getProgressBar(0.25,20) + ")").queue();

        SlayerExp slayerExp = main.getApiUtil().getPlayerSlayerExp(player.getUUID());

        e.getChannel().editMessageById(messageId,"Loading... (" + main.getLangUtil().getProgressBar(0.5,20) + ")").queue();

        SkillLevels skillLevels = main.getApiUtil().getBestProfileSkillLevels(player.getUUID());

        e.getChannel().editMessageById(messageId,"Loading... (" + main.getLangUtil().getProgressBar(0.75,20) + ")").queue();

        if(e.getGuild().getCategoriesByName("applications",true).contains(e.getGuild().getTextChannelsByName(player.getDisplayName() + "s-application", true))){
            e.getChannel().sendMessage("You already have a pending application.").queue();
            e.getChannel().deleteMessageById(messageId).queue();
            return;
        }

        if(skillLevels.isApproximate()){
            e.getChannel().sendMessage("You need to turn your api on before doing this command.").queue();
            e.getChannel().deleteMessageById(messageId).queue();
            return;
        }

        boolean meetsSlayer = true;
        boolean meetsSkill = true;

        if(slayerExp.getTotalExp() > currentDiscordServer.getHypixelGuild().getSlayerReq()){
            meetsSlayer = true;
        }
        if(skillLevels.getAvgSkillLevel() > currentDiscordServer.getHypixelGuild().getSkillReq()){
            meetsSkill = true;
        }

        e.getChannel().editMessageById(messageId,"Loading... (" + main.getLangUtil().getProgressBar(1.0,20) + ")").queue();

        if(!meetsSkill && !meetsSlayer){
            e.getChannel().sendMessage("You dont meet the slayer requirement of " + currentDiscordServer.getHypixelGuild().getSlayerReq() + "\nOr this skill requirement of " + currentDiscordServer.getHypixelGuild().getSkillReq()).queue();
            e.getChannel().deleteMessageById(messageId).queue();
            return;
        }
        if(!meetsSlayer){
            e.getChannel().sendMessage("You dont meet the slayer requirement of " + currentDiscordServer.getHypixelGuild().getSlayerReq()).queue();
            e.getChannel().deleteMessageById(messageId).queue();
            return;
        }
        if(!meetsSkill){
            e.getChannel().sendMessage("You dont meet the skill requirement of " + currentDiscordServer.getHypixelGuild().getSkillReq()).queue();
            e.getChannel().deleteMessageById(messageId).queue();
            return;
        }

        double playerScore = ((slayerExp.getTotalExp()/currentDiscordServer.getHypixelGuild().getSlayerReq())+(skillLevels.getAvgSkillLevel()/currentDiscordServer.getHypixelGuild().getSkillReq()))-2;

        TextChannel textChannel = e.getGuild().getTextChannelById(e.getGuild().createTextChannel(player.getDisplayName() + "s-application").setParent(e.getGuild().getCategoriesByName("applications",true).get(0)).setSlowmode(5).setTopic(player.getDisplayName() + "'s application").setPosition(100000-((int) (playerScore*1000))).complete().getId());

        EmbedBuilder embedBuilder = new EmbedBuilder().setTitle("Guild Application").setColor(0xb8300b);

        embedBuilder.setThumbnail("https://visage.surgeplay.com/bust/" + player.getUUID());
        embedBuilder.appendDescription("Your application is under review...");
        embedBuilder.appendDescription("\nAny news about the application's status will be posted in this channel");
        embedBuilder.appendDescription("\n\nYou can also talk to staff about the application in this channel");
        embedBuilder.setTimestamp(new Date().toInstant());

        textChannel.sendMessage(embedBuilder.build()).complete().pin().queue();

        e.getChannel().sendMessage("Application successfully created!").queue();
        e.getChannel().deleteMessageById(messageId).queue();
        return;
    }
}