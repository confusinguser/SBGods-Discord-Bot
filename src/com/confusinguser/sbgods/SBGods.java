package com.confusinguser.sbgods;

import com.confusinguser.sbgods.discord.DiscordBot;
import com.confusinguser.sbgods.entities.DiscordServer;
import com.confusinguser.sbgods.utils.*;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.*;
import java.util.stream.Collectors;

public class SBGods {
    public static final String VERSION = "0.9.4";
    public static final String VERSION_DESCRIPTION_MAJOR = ""; // Change this every major release: 0.9.6.3 -> 1.0
    public static final String VERSION_DESCRIPTION_MINOR = "Added live guild chat"; // Change this every minor release: 0.8.11.5 -> 0.8.12
    public static final String VERSION_DESCRIPTION_PATCH = ""; // Change this every patch: 0.8.11.4 -> 0.8.11.5
    public static final String[] DEVELOPERS = {"244786205873405952", "497210228274757632"};
    private static final DiscordServer[] servers = {DiscordServer.SBGods, DiscordServer.SBForceful}; // For release on main servers
    //private static final DiscordServer[] servers = {DiscordServer.Test}; // For testing
    private static SBGods instance;
    public final Logger logger = Logger.getLogger(this.getClass().getName());
    private final ApiUtil apiutil;
    private final Util util;
    private final SBUtil sbUtil;
    private final LangUtil langUtil;
    private final CacheUtil cacheUtil;
    private final LeaderboardUpdater leaderboardUpdater;
    private String[] keys = null;
    private DiscordBot discordBot;
    private int keyIndex = 0;

    public SBGods() {
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

        this.apiutil = new ApiUtil(this);
        this.util = new Util(this);
        this.sbUtil = new SBUtil(this);
        this.langUtil = new LangUtil(this);
        this.cacheUtil = new CacheUtil(this);
        instance = this;
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

    public ApiUtil getApiUtil() {
        return apiutil;
    }

    public Util getUtil() {
        return util;
    }

    public SBUtil getSBUtil() {
        return sbUtil;
    }

    public LangUtil getLangUtil() {
        return langUtil;
    }

    public CacheUtil getCacheUtil() {
        return cacheUtil;
    }

    public DiscordBot getDiscord() {
        return discordBot;
    }

    public LeaderboardUpdater getLeaderboardUpdater() {
        return leaderboardUpdater;
    }

    public Logger getLogger() {
        return logger;
    }

    public DiscordServer[] getActiveServers() {
        return servers;
    }

    public boolean isDeveloper(String userId) {
        return Arrays.asList(DEVELOPERS).contains(userId);
    }

    public String[] getKeys() {
        if (keys == null) {
            byte[] bytes;
            try (InputStream stream = getClass().getResourceAsStream("/resources/keys.txt")) {
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
     * Alias for {@code getLangUtil().getMessageByKey(String key)}
     */
    public String getMessageByKey(String key) {
        return getLangUtil().getMessageByKey(key);
    }
}
