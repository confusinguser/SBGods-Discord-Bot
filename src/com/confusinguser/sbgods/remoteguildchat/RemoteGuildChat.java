package com.confusinguser.sbgods.remoteguildchat;

import com.confusinguser.sbgods.SBGods;
import com.confusinguser.sbgods.entities.HypixelRank;
import com.confusinguser.sbgods.utils.ApiUtil;
import com.confusinguser.sbgods.utils.LangUtil;
import com.confusinguser.sbgods.utils.Multithreading;
import com.confusinguser.sbgods.utils.Util;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class RemoteGuildChat {
    private String latestGuildmessageAuthor = "";
    private String latestGuildmessage = "";
    private final List<String> blockedMessages = new ArrayList<>();
    private final String guildID;

    public RemoteGuildChat(String guildID) {
        this.guildID = guildID;
    }


    public void handleGuildMessage(String author, String message, HypixelRank rank, boolean showMessageOnly, boolean guildPrefix, boolean messageFromSMP, MessageChannel channel) {
        if (SBGods.getInstance().getDiscord() == null || author.isEmpty() || message.isEmpty()) return;
        boolean shouldSendMessage = !blockedMessages.contains(author + ":" + message);

        if (shouldSendMessage) {
            blockedMessages.add(Util.stripColorCodes(author + ":" + message));
            Multithreading.scheduleOnce(() -> blockedMessages.remove(author + ":" + message), 12, TimeUnit.SECONDS);
            sendMessage(author, message, rank, showMessageOnly, guildPrefix, messageFromSMP, channel);
        }
    }

    public void handleJoinLeaveMessage(String player, boolean leaving, HypixelRank rank, MessageChannel channel) {
        if (SBGods.getInstance().getDiscord() == null || player.isEmpty()) return;
        String fullMessage = "§2Guild > §r" + player + " " + (leaving ? "left" : "joined") + ".";
        boolean shouldSendMessage = !blockedMessages.contains(fullMessage);

        if (shouldSendMessage) {
            blockedMessages.add(fullMessage);
//            Multithreading.runAsync(() -> ApiUtil.sendGuildMessageToSApi("§2" + fullMessage));
            Multithreading.scheduleOnce(() -> blockedMessages.remove(fullMessage), 8, TimeUnit.SECONDS);
            sendJoinLeaveMessage(player, rank, leaving, channel);
        }
    }

    private void sendMessage(String author, String message, HypixelRank rank, boolean showMessageOnly, boolean guildPrefix, boolean messageFromSMP, MessageChannel channel) {
        if (channel == null) return;
        if (latestGuildmessageAuthor.equals(author) && !showMessageOnly) {
            latestGuildmessage += "\n" + (guildPrefix ? "Guild > " : "") + author + ": " + message;
            try {
                channel.deleteMessageById(channel.getLatestMessageId()).complete();
            } catch (IllegalStateException ignored) {
            } // If no last message id found
        } else {
            if (showMessageOnly) latestGuildmessage = (guildPrefix ? "Guild > " : "") + message;
            else latestGuildmessage = (guildPrefix ? "Guild > " : "") + author + ": " + message;
        }
        if (guildID.equals("5fea32eb8ea8c9724b8e3f3c") && !messageFromSMP) {
            String gRank = LangUtil.getAuthorGuildRank(author);
            char colorCode = rank.getColorCode();
            String nameAndRank = LangUtil.getAuthorNameAndRank(author);
            String msgToSend = (guildPrefix ? "§2Guild > " : "") +
                    (showMessageOnly ? "" : "§" + colorCode + nameAndRank + (gRank.isEmpty() ? "" : " §7") + gRank + "§f:") + message;
            Multithreading.runAsync(() -> ApiUtil.sendGuildMessageToSApi(msgToSend));
        }
        channel.sendMessage(new EmbedBuilder()
                .setColor(rank.getColor())
                .setDescription(SBGods.getInstance().getDiscord().escapeMarkdown(latestGuildmessage)).build()).queue();
        latestGuildmessageAuthor = author;
    }

    public void sendJoinLeaveMessage(String player, HypixelRank rank, boolean leaving, MessageChannel channel) {
        String message = String.format("Guild > %s %s.", player, leaving ? "left" : "joined");
        channel.sendMessage(new EmbedBuilder()
                .setColor(rank.getColor())
                .setDescription(SBGods.getInstance().getDiscord().escapeMarkdown(message)).build()).complete();
    }
}
