package com.confusinguser.sbgods;

import com.confusinguser.sbgods.discord.DiscordBot;
import com.confusinguser.sbgods.entities.DiscordServer;
import com.confusinguser.sbgods.utils.*;

import javax.security.auth.login.LoginException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.logging.*;

public class SBGods {
    public static final String VERSION = "0.9.2.0";
    public static final String VERSION_DESCRIPTION_MAJOR = ""; // Change this every major release: 0.9.6.3 -> 1.0
    public static final String VERSION_DESCRIPTION_MINOR = "Added event capability"; // Change this every minor release: 0.8.11.5 -> 0.8.12
    public static final String VERSION_DESCRIPTION_PATCH = "Fixed bugs and performance issues"; // Change this every patch: 0.8.11.4 -> 0.8.11.5
    public static final String[] DEVELOPERS = {"244786205873405952", "497210228274757632"};
    private static final DiscordServer[] servers = {DiscordServer.SBGods, DiscordServer.SBDGods}; // For release on main servers
    //private static final DiscordServer[] servers = {DiscordServer.Test}; // For testing
    private static SBGods instance;
    public final String[] keys = {"bc90572a-1547-41a5-8f28-d7664916a28d", "3963906e-ffb6-45b9-b07b-80ca9838eb20",//ConfusingUser's keys
            "a269efda-d93f-4521-a7a1-b793166d9ca3", "7673c96b-95b0-429d-81b6-031d9e249c75", "fbb3bea4-0f61-4b5f-a656-502403a3e7c5", "13d28584-99db-4473-a068-467498fcaa8b"};//Soopy's keys
    public final Logger logger = Logger.getLogger(this.getClass().getName());
    private final ApiUtil apiutil;
    private final Util util;
    private final SBUtil sbUtil;
    private final LangUtil langUtil;
    private final JsonApiUtil jsonApiUtil;
    private final CacheUtil cacheUtil;
    private final LeaderboardUpdater leaderboardUpdater;
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
        this.jsonApiUtil = new JsonApiUtil(this);
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
        if (keyIndex >= keys.length) keyIndex = 0;
        return keys[keyIndex];
    }

    public String getCurrentApiKey() {
        return keys[keyIndex];
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

    public JsonApiUtil getJsonApiUtil() {
        return jsonApiUtil;
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
}
