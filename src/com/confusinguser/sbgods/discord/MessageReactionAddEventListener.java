package com.confusinguser.sbgods.discord;

import com.confusinguser.sbgods.SBGods;
import com.confusinguser.sbgods.entities.DiscordServer;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.concurrent.TimeUnit;

public class MessageReactionAddEventListener extends ListenerAdapter {

    SBGods main;
    DiscordBot discord;

    public MessageReactionAddEventListener(SBGods main, DiscordBot discord) {
        this.main = main;
        this.discord = discord;
    }

    @Override
    public void onMessageReactionAdd(MessageReactionAddEvent e) {
        if(!e.getChannelType().equals(ChannelType.TEXT)){
            return;
        }
        if(!e.getTextChannel().getParent().getName().toLowerCase().contains("applications")){

            if(!e.getTextChannel().getName().toLowerCase().contains("accepted-applications")){
                return;
            }
            if(e.getUser().isBot()){
                return;
            }

            if(e.getReaction().getReactionEmote().getEmoji().equalsIgnoreCase("☑")) {
                e.getTextChannel().deleteMessageById(e.getMessageId()).queue();
            }
            return;
        }
        if(e.getUser().isBot()){
            return;
        }
        if(!e.getMember().hasPermission(Permission.MESSAGE_MANAGE)){
            return;
        }

        if(e.getReaction().getReactionEmote().getEmoji().equalsIgnoreCase("\uD83D\uDC4D")){
            String messageAccept = e.getTextChannel().sendMessage(new EmbedBuilder().setTitle("APPLICATION ACCEPTED!").appendDescription("Deleting channel in 1m\n" + main.getLangUtil().getProgressBar(0.0,15)).build()).complete().getId();

            Message message = e.getGuild().getTextChannelsByName("accepted-applications", true).get(0).sendMessage(new EmbedBuilder().setTitle(e.getChannel().getName().replace("s-application", "")).appendDescription("Score: " + e.getChannel().retrievePinnedMessages().complete().get(0).getEmbeds().get(0).getTitle().replace("Guild Application (Score ", "").replace(")", "")).build()).complete();
            //Sends the embed into the accepted-applications with the info
            //have fun reading

            message.addReaction("☑").queue();

            e.getTextChannel().retrievePinnedMessages().complete().get(0).delete().queue();

            e.getTextChannel().editMessageById(messageAccept, new EmbedBuilder().setTitle("APPLICATION ACCEPTED!").appendDescription("Deleting channel in 1m\n" + main.getLangUtil().getProgressBar(0.1,15)).build()).queueAfter(6,TimeUnit.SECONDS);
            e.getTextChannel().editMessageById(messageAccept, new EmbedBuilder().setTitle("APPLICATION ACCEPTED!").appendDescription("Deleting channel in 1m\n" + main.getLangUtil().getProgressBar(0.2,15)).build()).queueAfter(12,TimeUnit.SECONDS);
            e.getTextChannel().editMessageById(messageAccept, new EmbedBuilder().setTitle("APPLICATION ACCEPTED!").appendDescription("Deleting channel in 1m\n" + main.getLangUtil().getProgressBar(0.3,15)).build()).queueAfter(18,TimeUnit.SECONDS);
            e.getTextChannel().editMessageById(messageAccept, new EmbedBuilder().setTitle("APPLICATION ACCEPTED!").appendDescription("Deleting channel in 1m\n" + main.getLangUtil().getProgressBar(0.4,15)).build()).queueAfter(24,TimeUnit.SECONDS);
            e.getTextChannel().editMessageById(messageAccept, new EmbedBuilder().setTitle("APPLICATION ACCEPTED!").appendDescription("Deleting channel in 1m\n" + main.getLangUtil().getProgressBar(0.5,15)).build()).queueAfter(30,TimeUnit.SECONDS);
            e.getTextChannel().editMessageById(messageAccept, new EmbedBuilder().setTitle("APPLICATION ACCEPTED!").appendDescription("Deleting channel in 1m\n" + main.getLangUtil().getProgressBar(0.6,15)).build()).queueAfter(36,TimeUnit.SECONDS);
            e.getTextChannel().editMessageById(messageAccept, new EmbedBuilder().setTitle("APPLICATION ACCEPTED!").appendDescription("Deleting channel in 1m\n" + main.getLangUtil().getProgressBar(0.7,15)).build()).queueAfter(42,TimeUnit.SECONDS);
            e.getTextChannel().editMessageById(messageAccept, new EmbedBuilder().setTitle("APPLICATION ACCEPTED!").appendDescription("Deleting channel in 1m\n" + main.getLangUtil().getProgressBar(0.8,15)).build()).queueAfter(48,TimeUnit.SECONDS);
            e.getTextChannel().editMessageById(messageAccept, new EmbedBuilder().setTitle("APPLICATION ACCEPTED!").appendDescription("Deleting channel in 1m\n" + main.getLangUtil().getProgressBar(0.9,15)).build()).queueAfter(54,TimeUnit.SECONDS);
            e.getTextChannel().editMessageById(messageAccept, new EmbedBuilder().setTitle("APPLICATION ACCEPTED!").appendDescription("Deleting channel in 1m\n" + main.getLangUtil().getProgressBar(1.0,15)).build()).queueAfter(60,TimeUnit.SECONDS);
            e.getTextChannel().delete().queueAfter(1,TimeUnit.MINUTES);
            return;
        }
        if(e.getReaction().getReactionEmote().getEmoji().equalsIgnoreCase("\uD83D\uDC4E")){
            String messageAccept = e.getTextChannel().sendMessage(new EmbedBuilder().setTitle("APPLICATION DENIED!").appendDescription("Deleting channel in 1m\n" + main.getLangUtil().getProgressBar(0.0,15)).build()).complete().getId();
            e.getTextChannel().editMessageById(messageAccept, new EmbedBuilder().setTitle("APPLICATION DENIED!").appendDescription("Deleting channel in 1m\n" + main.getLangUtil().getProgressBar(0.1,15)).build()).queueAfter(6,TimeUnit.SECONDS);
            e.getTextChannel().editMessageById(messageAccept, new EmbedBuilder().setTitle("APPLICATION DENIED!").appendDescription("Deleting channel in 1m\n" + main.getLangUtil().getProgressBar(0.2,15)).build()).queueAfter(12,TimeUnit.SECONDS);
            e.getTextChannel().editMessageById(messageAccept, new EmbedBuilder().setTitle("APPLICATION DENIED!").appendDescription("Deleting channel in 1m\n" + main.getLangUtil().getProgressBar(0.3,15)).build()).queueAfter(18,TimeUnit.SECONDS);
            e.getTextChannel().editMessageById(messageAccept, new EmbedBuilder().setTitle("APPLICATION DENIED!").appendDescription("Deleting channel in 1m\n" + main.getLangUtil().getProgressBar(0.4,15)).build()).queueAfter(24,TimeUnit.SECONDS);
            e.getTextChannel().editMessageById(messageAccept, new EmbedBuilder().setTitle("APPLICATION DENIED!").appendDescription("Deleting channel in 1m\n" + main.getLangUtil().getProgressBar(0.5,15)).build()).queueAfter(30,TimeUnit.SECONDS);
            e.getTextChannel().editMessageById(messageAccept, new EmbedBuilder().setTitle("APPLICATION DENIED!").appendDescription("Deleting channel in 1m\n" + main.getLangUtil().getProgressBar(0.6,15)).build()).queueAfter(36,TimeUnit.SECONDS);
            e.getTextChannel().editMessageById(messageAccept, new EmbedBuilder().setTitle("APPLICATION DENIED!").appendDescription("Deleting channel in 1m\n" + main.getLangUtil().getProgressBar(0.7,15)).build()).queueAfter(42,TimeUnit.SECONDS);
            e.getTextChannel().editMessageById(messageAccept, new EmbedBuilder().setTitle("APPLICATION DENIED!").appendDescription("Deleting channel in 1m\n" + main.getLangUtil().getProgressBar(0.8,15)).build()).queueAfter(48,TimeUnit.SECONDS);
            e.getTextChannel().editMessageById(messageAccept, new EmbedBuilder().setTitle("APPLICATION DENIED!").appendDescription("Deleting channel in 1m\n" + main.getLangUtil().getProgressBar(0.9,15)).build()).queueAfter(54,TimeUnit.SECONDS);
            e.getTextChannel().editMessageById(messageAccept, new EmbedBuilder().setTitle("APPLICATION DENIED!").appendDescription("Deleting channel in 1m\n" + main.getLangUtil().getProgressBar(1.0,15)).build()).queueAfter(60,TimeUnit.SECONDS);
            e.getTextChannel().retrievePinnedMessages().complete().get(0).delete().queue();
            e.getTextChannel().delete().queueAfter(1,TimeUnit.MINUTES);
            return;
        }
    }
}
