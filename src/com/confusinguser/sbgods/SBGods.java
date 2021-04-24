package com.confusinguser.sbgods;

import com.confusinguser.sbgods.discord.DiscordBot;
import com.confusinguser.sbgods.entities.DiscordServer;
import com.confusinguser.sbgods.remoteguildchat.RemoteGuildChatManager;
import com.confusinguser.sbgods.utils.LangUtil;
import com.confusinguser.sbgods.utils.LeaderboardUpdater;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.*;
import java.util.stream.Collectors;

public class SBGods {
    public static final String[] DEVELOPERS = {"244786205873405952", "497210228274757632"};
    /**
     * NOTE: OVERRIDEN IN {@code getActiveServers()} IF NOT IN IDE
     */
    private static DiscordServer[] servers = {DiscordServer.Test};
    private static SBGods instance;
    public final Logger logger = Logger.getLogger(this.getClass().getName());
    private final LeaderboardUpdater leaderboardUpdater;
    private final RemoteGuildChatManager remoteGuildChatManager;
    private String[] keys = null;
    private DiscordBot discordBot;
    private int keyIndex = 0;
    private boolean inIDE = false;

    public SBGods(ServerSocket serverSocket) {
        instance = this;
        try {
            inIDE = !URLDecoder.decode(Start.class.getProtectionDomain().getCodeSource().getLocation().toString().substring(6), StandardCharsets.UTF_8.toString()).endsWith(".jar");
        } catch (UnsupportedEncodingException ignored) {
        }
        ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(new SimpleFormatter() {
            @Override
            public String format(LogRecord record) {
                return String.format("[%s] %s: %s%n", new SimpleDateFormat("MMM dd HH:mm:ss").format(new Date(record.getMillis())), record.getLevel(), record.getMessage());
            }
        });
        logger.addHandler(handler);
        logger.setUseParentHandlers(false);

        if (SBGods.class.getProtectionDomain().getCodeSource().getLocation().toString().endsWith(".jar")) { // If not inside IDE
            handler.setLevel(Level.INFO); // Make handler only send info+ events
            logger.setLevel(Level.INFO); // Only show info+
        } else {
            handler.setLevel(Level.ALL); // Make handler send all events
            logger.setLevel(Level.ALL); // Show all logging events
        }
        this.remoteGuildChatManager = new RemoteGuildChatManager(this);
        remoteGuildChatManager.startListener(serverSocket);
        try {
            this.discordBot = new DiscordBot(this);
        } catch (LoginException e) {
            logger.severe("Failed to login, is the discord token invalid?");
            System.exit(-1);
        }
        this.leaderboardUpdater = new LeaderboardUpdater(this);
    }

    public static SBGods getInstance() {
        return instance;
    }

    public String getNextApiKey() {
        keyIndex++;
        if (keyIndex >= getKeys().length) keyIndex = 0;
        return getKeys()[keyIndex];
    }

    public String getCurrentApiKey() {
        return getKeys()[keyIndex];
    }

    public void removeApiKey(String key) {
        List<String> keysTemp = new ArrayList<>(Arrays.asList(keys)); // Can't remove otherwise
        keysTemp.remove(key);
        keys = keysTemp.toArray(keys);
    }

    public DiscordBot getDiscord() {
        return discordBot;
    }

    public RemoteGuildChatManager getRemoteGuildChatManager() {
        return remoteGuildChatManager;
    }

    public LeaderboardUpdater getLeaderboardUpdater() {
        return leaderboardUpdater;
    }

    public Logger getLogger() {
        return logger;
    }

    public DiscordServer[] getActiveServers() {
        if (!inIDE && !Arrays.asList(servers).contains(DiscordServer.SBGods) && !Arrays.asList(servers).contains(DiscordServer.SBForceful))
            return (servers = new DiscordServer[]{DiscordServer.SBGods, DiscordServer.SBForceful});
        return servers;
    }

    public boolean isDeveloper(String userId) {
        return Arrays.asList(DEVELOPERS).contains(userId);
    }

    public String[] getKeys() {
        if (keys == null) {
            byte[] bytes;
            try (InputStream stream = getClass().getResourceAsStream("/keys.txt")) {
                if (stream != null) {
                    bytes = stream.readAllBytes();
                } else {
                    this.getLogger().warning("Inputstream to get the key returned null");
                    return new String[0];
                }
            } catch (IOException exception) {
                exception.printStackTrace();
                return new String[0];
            }

            String fileContent = new String(bytes);
            List<String> output = Arrays.asList(fileContent.split("\r\n"));
            output = output.stream().filter(s -> s.length() >= 36 && !s.substring(0, 36).contains("//")).map(s -> s.substring(0, 36)).collect(Collectors.toList());
            keys = output.toArray(new String[0]);
            logger.info("Loaded " + keys.length + " api keys!");
        }
        return keys;
    }

    /**
     * Alias for {@code LangUtil.getMessageByKey(String key)}
     */
    public String getMessageByKey(String key) {
        return LangUtil.getMessageByKey(key);
    }
}
