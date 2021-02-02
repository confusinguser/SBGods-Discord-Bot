package com.confusinguser.sbgods.remoteguildchat;

import com.confusinguser.sbgods.SBGods;
import com.confusinguser.sbgods.entities.DiscordServer;
import com.confusinguser.sbgods.entities.HypixelGuild;
import com.confusinguser.sbgods.entities.HypixelRank;
import com.confusinguser.sbgods.utils.Multithreading;
import com.confusinguser.sbgods.utils.RegexUtil;
import com.confusinguser.sbgods.utils.Util;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.MessageChannel;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class RemoteGuildChatManager {
    public final BlockingQueue<Map.Entry<String, String>> queue = new ArrayBlockingQueue<>(1024, true);
    private final Map<String, RemoteGuildChat> guildChatMap = new HashMap<>(); // String is hypixel guild id
    private final SBGods main;
    private final Set<ClientConnection> clientConnections = Collections.synchronizedSet(new HashSet<>());
    private final AtomicReference<Socket> socket = new AtomicReference<>();

    public RemoteGuildChatManager(SBGods main) {
        this.main = main;
    }

    public void startListener(ServerSocket serverSocket) {
        Thread listenerThread = new Thread(() -> {
            while (true) {
                try {
                    socket.set(serverSocket.accept());
                    clientConnections.add(new ClientConnection(socket.get()));
                    Multithreading.runAsync(() -> {
                        listenForSocketMessages(new ClientConnection(socket.get()));
                        clientConnections.remove(new ClientConnection(socket.get()));
                    });
                } catch (IOException ioException) {
                    main.getDiscord().reportFail(ioException, "Listener Thread");
                    return;
                }
            }
        });

        Thread senderThread = new Thread(() -> { // Sends discord messages to all clients of guild
            while (!Thread.currentThread().isInterrupted()) {
                Map.Entry<String, String> message;
                try {
                    message = queue.take();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }

                List<ClientConnection> socketsThatDontWork = new ArrayList<>();
                List<ClientConnection> collect = clientConnections.stream().filter(clientConnection -> message.getKey() == null || (clientConnection.getGuildId() != null && clientConnection.getGuildId().equals(message.getKey()))).collect(Collectors.toList());
                for (int i = 0; i < collect.size(); i++) {
                    ClientConnection clientConnection = collect.get(i);
                    DataOutputStream output;
                    try {
                        output = new DataOutputStream(clientConnection.getSocket().getOutputStream());
                        JsonObject jsonData = new JsonObject();
                        jsonData.addProperty("type", "chat");
                        jsonData.addProperty("message", message.getValue());
                        if (i == socketsThatDontWork.size()) // If it's the first socket that works
                            jsonData.addProperty("send_to_chat", /*"/gc " + */Util.bypassAntiSpam(Util.stripColorCodes(message.getValue())));
                        System.out.println(jsonData.toString());
                        output.writeUTF(jsonData.toString());
                        output.flush();
                    } catch (IOException ex) {
                        socketsThatDontWork.add(clientConnection);
                    }
                }

                for (ClientConnection socket : socketsThatDontWork) {
                    clientConnections.remove(socket);
                }
            }
        });
        listenerThread.start();
        senderThread.start();
    }

    public void listenForSocketMessages(ClientConnection clientConnection) {
        if (clientConnection == null) return;
        DataInputStream dataInputStream;
        try {
            dataInputStream = new DataInputStream(clientConnection.getSocket().getInputStream());
        } catch (IOException e) {
            main.getDiscord().reportFail(e, "DataInputStream Initializer");
            return;
        }
        InetAddress ipAddr = clientConnection.getSocket().getInetAddress();

        while (!Thread.currentThread().isInterrupted()) {
            String data;
            JsonObject parsedJson;
            try {
                byte[] dataBytes = new byte[4096];
                if (dataInputStream.read(dataBytes) < 1) return;
                data = new DataInputStream(new ByteArrayInputStream(dataBytes)).readUTF();
                if (data.isEmpty()) return; // Socket is dead
                parsedJson = JsonParser.parseString(data).getAsJsonObject();
            } catch (IOException ex) { // Stream closed on client side
                try {
                    dataInputStream.close();
                    clientConnection.getSocket().close();
                } catch (IOException e) {
                    main.getDiscord().reportFail(ex, "Socket Data Receiver");
                }
                clientConnections.remove(clientConnection);
                return;
            } catch (JsonParseException | IllegalStateException /* Not a json object */ ex) {
                return;
            }
            try {
                clientConnection.setUuid(parsedJson.get("senderUUID").getAsString());
                clientConnection.setGuildId(main.getApiUtil().getGuildIDFromUUID(clientConnection.getUuid()));
                DiscordServer discordServer = DiscordServer.getDiscordServerFromHypixelGuild(HypixelGuild.getGuildById(clientConnection.getGuildId()), true);
                String message = parsedJson.get("message").getAsString();

                if (discordServer == null) {
                    String guildName = main.getApiUtil().getGuildNameFromId(clientConnection.getGuildId());
                    if (guildName == null) {
                        guildName = "none";
                    }
                    guildName = guildName.replace(' ', '-');
                    boolean found = false;
                    Guild testServer = main.getDiscord().getJDA().getGuildById(DiscordServer.Test.getServerId()); // Discord "guild"
                    if (testServer == null) return;
                    GuildChannel guildChannel = null;

                    for (GuildChannel guildChannelLoop : testServer.getChannels()) {
                        if (guildChannelLoop.getName().equalsIgnoreCase(guildName)) {
                            found = true;
                            guildChannel = guildChannelLoop;
                        }
                    }
                    if (!found) {
                        Category guildChannels = testServer.getCategoryById("790882168086724628");
                        if (guildChannels != null) {
                            guildChannel = guildChannels.createTextChannel(guildName).complete();
                        }
                    }

                    main.getRemoteGuildChatManager().handleGuildMessage(
                            clientConnection.getGuildId(),
                            message,
                            ipAddr,
                            (MessageChannel) guildChannel); // For test server list of guilds
                    continue;
                }
                main.getRemoteGuildChatManager().handleGuildMessage(
                        clientConnection.getGuildId(),
                        discordServer,
                        message,
                        ipAddr);
            } catch (Throwable t) {
                main.getDiscord().reportFail(t, "Remote Guild Chat Manager");
            }
        }
    }

    public void handleGuildMessage(String guildID, DiscordServer discordServer, String text, InetAddress requestSenderIpAddr) {
        MessageChannel channel;
        if ((channel = SBGods.getInstance().getDiscord().getJDA().getTextChannelById(discordServer.getGuildChatChannelId())) != null) {
            handleGuildMessage(guildID, text, requestSenderIpAddr, channel);
        }
    }

    public void handleGuildMessage(String guildID, String text, InetAddress requestSenderIpAddr, MessageChannel channel) {
        if (guildChatMap.get(guildID) == null) {
            RemoteGuildChat remoteGuildChat = new RemoteGuildChat(guildID);
            guildChatMap.put(guildID, remoteGuildChat);
        }

        text = text.replace("§r", "");

        HypixelRank rank;
        if (RegexUtil.stringMatches("§2Guild > §[0-9a-f]\\w{3,16} §e(?:left|joined)\\.", text) || RegexUtil.stringMatches("Guild > \\w{3,16} (?:left|joined)\\.", text)) {
            rank = text.contains("§") ? HypixelRank.getHypixelRankFromRankColorCode(text.substring(11, 12).charAt(0)) : HypixelRank.DEFAULT; // §2Guild > §aConfusingUser §eleft.
            guildChatMap.get(guildID).handleJoinLeaveMessage(
                    Util.stripColorCodes(text).substring(8)
                            .replace(" left.", "")
                            .replace(" joined.", ""),
                    text.contains("left"), rank, requestSenderIpAddr, channel);
            return;
        }

        if (RegexUtil.stringMatches("§2Guild > .*§[a-fA-F0-9](?: |)(\\d{1,2})", text) ||
                RegexUtil.stringMatches("Guild > .* \\(\\d{1,2}\\)", text)) { // Spam filter
            StringJoiner joiner = new StringJoiner(" ");
            String[] fullSplit = text.split(" ");
            String[] split = Arrays.copyOf(fullSplit, fullSplit.length - 1);
            text = String.join(" ", split);
        }

        String author = Util.getAuthorFromGuildChatMessage(text);
        String message = Util.getMessageFromGuildChatMessage(text);
        rank = HypixelRank.getHypixelRankFromRankName(author.substring(0, author.contains("]") ? author.indexOf("]") + 1 : 0));
        boolean showMessageOnly = false;
        boolean guildPrefix = true;
        if (RegexUtil.stringMatches("§2Guild > (?:§[0-9a-f]\\[[\\w§]+\\] |)\\w{3,16}(?: §e\\[\\w*\\]|)§f: @\\w{3,16}, .*", text)
                || RegexUtil.stringMatches("Guild > (?:\\[[\\w\\W]+\\] |)\\w{3,16}(?: \\[\\w*\\]|): @\\w{3,16}, .*", text)) {
            message = "SBGBOT > " + Util.getMessageFromGuildChatMessage(text).replaceFirst(", ", " → ").replace("⭍", "").replace("ࠀ", "");
            showMessageOnly = true;
            guildPrefix = false;
            rank = HypixelRank.VIP; // Green Color!
        }
        if (message.equals(author)) { // Avoid "Guild > stuff: stuff" if invalid message format
            showMessageOnly = true;
        }
        guildChatMap.get(guildID).handleGuildMessage(author, message, rank, requestSenderIpAddr, showMessageOnly, guildPrefix, channel);
    }
}
