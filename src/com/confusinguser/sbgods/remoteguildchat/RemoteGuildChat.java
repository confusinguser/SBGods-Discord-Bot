package com.confusinguser.sbgods.remoteguildchat;

import com.confusinguser.sbgods.SBGods;
import com.confusinguser.sbgods.entities.HypixelRank;
import com.confusinguser.sbgods.utils.Multithreading;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class RemoteGuildChat {
    private final Map<InetAddress, String> latestMessageByIP = new HashMap<>();
    private final Map<String, String> latestMessageByIGN = new HashMap<>();
    private String latestGuildmessageAuthor = "";
    private String latestGuildmessage = "";
    private final List<String> blockedMessages = new ArrayList<>();

    public void handleGuildMessage(String author, String message, HypixelRank rank, InetAddress requestSenderIpAddr, boolean showMessageOnly, boolean guildPrefix, MessageChannel channel) {
        if (SBGods.getInstance().getDiscord() == null || author.isEmpty() || message.isEmpty()) return;
        boolean shouldSendMessage = !blockedMessages.contains(author + ":" + message) && !latestGuildmessage.equals((guildPrefix ? "Guild > " : "") + author + ": " + message) &&
                !latestGuildmessageAuthor.equals(author);

        if (shouldSendMessage && !latestMessageByIGN.getOrDefault(author, "").equals(message)) {
            Multithreading.runAsync(() -> SBGods.getInstance().getApiUtil().sendGuildMessageToSApi("§2Guild > §r" + author + ": " + message));
            latestMessageByIGN.put(author, message);
            blockedMessages.add(SBGods.getInstance().getUtil().stripColorCodes(author + ":" + message));
            Multithreading.scheduleOnce(() -> blockedMessages.remove(author + ":" + message), 12, TimeUnit.SECONDS);
            sendMessage(author, message, rank, showMessageOnly, guildPrefix, channel);
        }
    }

    public void handleJoinLeaveMessage(String player, boolean leaving, HypixelRank rank, InetAddress requestSenderIpAddr, MessageChannel channel) {
        if (SBGods.getInstance().getDiscord() == null || player.isEmpty()) return;
        String fullMessage = "Guild > " + player + " " + (leaving ? "left" : "joined") + ".";
        boolean shouldSendMessage = !latestGuildmessage.equals(fullMessage) || !latestGuildmessageAuthor.equals(player);

        if (shouldSendMessage) {
            blockedMessages.add(player + " " + (leaving ? "left" : "joined") + ".");
            Multithreading.runAsync(() -> SBGods.getInstance().getApiUtil().sendGuildMessageToSApi(fullMessage));
            Multithreading.scheduleOnce(() -> blockedMessages.remove(player + " " + (leaving ? "left" : "joined") + "."), 12, TimeUnit.SECONDS);
            sendJoinLeaveMessage(player, rank, leaving, channel);
        }
    }

    private void sendMessage(String author, String message, HypixelRank rank, boolean showMessageOnly, boolean guildPrefix, MessageChannel channel) {
        if (channel != null) {
            if (latestGuildmessageAuthor.equals(author) && !showMessageOnly) {
                latestGuildmessage += "\nGuild > " + author + ": " + message;
                try {
                    channel.deleteMessageById(channel.getLatestMessageId()).queue();
                } catch (IllegalStateException ignored) {
                } // If no last message id found
            } else {
                if (showMessageOnly) latestGuildmessage = "Guild > " + message;
                else latestGuildmessage = "Guild > " + author + ": " + message;
            }
            channel.sendMessage(new EmbedBuilder()
                    .setColor(rank.getColor())
                    .setDescription(SBGods.getInstance().getDiscord().escapeMarkdown(latestGuildmessage)).build()).queue();
            latestGuildmessageAuthor = author;
        }
    }

    public void sendJoinLeaveMessage(String player, HypixelRank rank, boolean leaving, MessageChannel channel) {
        String message = String.format("Guild > %s %s.", player, leaving ? "left" : "joined");
        channel.sendMessage(new EmbedBuilder()
                .setColor(rank.getColor())
                .setDescription(SBGods.getInstance().getDiscord().escapeMarkdown(message)).build()).queue();
    }
}
