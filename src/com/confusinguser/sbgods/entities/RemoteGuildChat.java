package com.confusinguser.sbgods.entities;

import com.confusinguser.sbgods.SBGods;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

public class RemoteGuildChat {
    private final Map<InetAddress, String> latestMessageByIP = new HashMap<>();
    private final Map<String, String> latestMessageByIGN = new HashMap<>();
    private final DiscordServer discordServer;
    private String latestGuildmessageAuthor = "";
    private String latestGuildmessage = "";

    public RemoteGuildChat(DiscordServer discordServer) {
        this.discordServer = discordServer;
    }

    public void handleGuildMessage(String author, String message, HypixelRank rank, InetAddress requestSenderIpAddr) {
        if (SBGods.getInstance().getDiscord() == null || author.isEmpty() || message.isEmpty()) return;
        boolean shouldSendMessage = latestGuildmessage.equals("Guild > " + author + ": " + message) && latestGuildmessageAuthor.equals(author) &&
                !latestMessageByIP.getOrDefault(requestSenderIpAddr, "").equals(author + ":" + message);

        if (shouldSendMessage && !latestMessageByIGN.getOrDefault(author, "").equals(message)) {
            latestMessageByIGN.put(author, message);
            latestMessageByIP.put(requestSenderIpAddr, author + ":" + message);
            sendMessage(author, message, rank);
        }
    }

    private void sendMessage(String author, String message, HypixelRank rank) {
        MessageChannel channel;
        if ((channel = SBGods.getInstance().getDiscord().getJDA().getTextChannelById(discordServer.getGuildChatChannelId())) != null) {
            if (latestGuildmessageAuthor.equals(author)) {
                latestGuildmessage += "\n" + "Guild > " + author + ": " + message;
                try {
                    channel.deleteMessageById(channel.getLatestMessageId()).queue();
                } catch (IllegalStateException ignored) {
                } // If no last message id found
            } else {
                latestGuildmessage = "Guild > " + author + ": " + message;
            }
            channel.sendMessage(new EmbedBuilder()
                    .setColor(rank.getColor())
                    .setDescription(SBGods.getInstance().getDiscord().escapeMarkdown(latestGuildmessage)).build()).queue();
            latestGuildmessageAuthor = author;
        }
    }

    public void handleJoinLeaveMessage(String player, boolean leaving, HypixelRank rank, InetAddress requestSenderIpAddr) {
        if (SBGods.getInstance().getDiscord() == null || player.isEmpty()) return;
        boolean shouldSendMessage = latestGuildmessage.equals("Guild > " + player + " " + (leaving ? "left" : "joined") + ".") && latestGuildmessageAuthor.equals(player) &&
                !latestMessageByIP.getOrDefault(requestSenderIpAddr, "").equals(player + " " + (leaving ? "left" : "joined") + ".");

        if (shouldSendMessage && !latestMessageByIGN.getOrDefault(player, "").equals((leaving ? "left" : "joined") + ".")) {
            latestMessageByIGN.put(player, (leaving ? "left" : "joined") + ".");
            latestMessageByIP.put(requestSenderIpAddr, player + " " + (leaving ? "left" : "joined") + ".");
            sendJoinLeaveMessage(player, rank, leaving);
        }
    }

    public void sendJoinLeaveMessage(String player, HypixelRank rank, boolean leaving) {
        MessageChannel channel;
        if ((channel = SBGods.getInstance().getDiscord().getJDA().getTextChannelById(discordServer.getGuildChatChannelId())) != null) {
            String message = String.format("Guild > %s %s.", player, leaving ? "left" : "joined");
            channel.sendMessage(new EmbedBuilder()
                    .setColor(rank.getColor())
                    .setDescription(SBGods.getInstance().getDiscord().escapeMarkdown(message)).build()).queue();
        }
    }
}
