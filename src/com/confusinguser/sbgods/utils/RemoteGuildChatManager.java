package com.confusinguser.sbgods.utils;

import com.confusinguser.sbgods.SBGods;
import com.confusinguser.sbgods.entities.DiscordServer;
import com.confusinguser.sbgods.entities.HypixelGuild;
import com.confusinguser.sbgods.entities.HypixelRank;
import com.confusinguser.sbgods.entities.RemoteGuildChat;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class RemoteGuildChatManager {
    private final Map<DiscordServer, RemoteGuildChat> guildChatMap = new HashMap<>();
    private final SBGods main;

    public RemoteGuildChatManager(SBGods main) {
        this.main = main;
    }

    public void startListener(ServerSocket serverSocket) {
        Thread listenerThread = new Thread(() -> {
            while (true) {
                try {
                    Socket socket = serverSocket.accept();
                    Thread socketThread = new Thread(() -> {
                        DataInputStream dataInputStream;
                        String data;
                        InetAddress ipAddr = socket.getInetAddress();
                        JsonObject parsedJson;
                        try {
                            dataInputStream = new DataInputStream(socket.getInputStream());
                            data = DataInputStream.readUTF(dataInputStream);
                            parsedJson = JsonParser.parseString(data).getAsJsonObject();
                        } catch (IOException | JsonParseException ex) {
                            ex.printStackTrace();
                            return;
                        }

                        DiscordServer discordServer = DiscordServer.getDiscordServerFromHypixelGuild(HypixelGuild.getGuildById(main.getApiUtil().getGuildIDFromUUID(parsedJson.get("senderUUID").getAsString())), true);
                        if (discordServer == null) return;
                        String message = parsedJson.get("message").getAsString();

                        main.getRemoteGuildChatManager().handleGuildMessage(
                                discordServer,
                                message,
                                ipAddr);
                    });
                    socketThread.start();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                    return;
                }
            }
        });
        listenerThread.start();
    }

    public void handleGuildMessage(DiscordServer discordServer, String text, InetAddress requestSenderIpAddr) {
        if (discordServer == null) return;
        if (guildChatMap.get(discordServer) == null) {
            RemoteGuildChat remoteGuildChat = new RemoteGuildChat(discordServer);
            guildChatMap.put(discordServer, remoteGuildChat);
        }

        text = text.replace("§r", "");

        String author = main.getUtil().getAuthorFromGuildChatMessage(text);
        String message = main.getUtil().getMessageFromGuildChatMessage(text);
        HypixelRank rank;
        if (RegexUtil.stringMatches("§2Guild > §[0-9a-f]\\w{3,16} (?:§e|)(?:left|joined)\\.", text)) {
            rank = HypixelRank.getHypixelRankFromRankColorCode(text.substring(11, 12).charAt(0)); // §2Guild > §aConfusingUser §eleft.
            guildChatMap.get(discordServer).handleJoinLeaveMessage(
                    main.getUtil().stripColorCodes(text).substring(8)
                            .replace(" §eleft.", "")
                            .replace(" §ejoined.", ""),
                    text.contains("left"), rank, requestSenderIpAddr);
            return;
        }
        rank = HypixelRank.getHypixelRankFromRankName(author.substring(0, author.contains("]") ? author.indexOf("]") + 1 : 0));
        if (RegexUtil.stringMatches("§2Guild > \\w{3,16}§f: @\\w{3,16}, .*", text)) {
            message = "SBGBOT > " + main.getUtil().getMessageFromGuildChatMessage(text).replaceFirst(", ", " -> ");
        }
        guildChatMap.get(discordServer).handleGuildMessage(author, message, rank, requestSenderIpAddr);
    }

}
