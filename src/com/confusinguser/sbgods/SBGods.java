package com.confusinguser.sbgods;

import com.confusinguser.sbgods.discord.DiscordBot;
import com.confusinguser.sbgods.entities.DiscordServer;
import com.confusinguser.sbgods.utils.*;

import javax.security.auth.login.LoginException;
import java.util.logging.Logger;

public class SBGods {
    public static final String VERSION = "0.7.5";
    public static final String VERSION_DESCRIPTION = "Fixes and Patches";
    private static final String CREATOR_ID = "244786205873405952";
    private static final DiscordServer[] servers = {DiscordServer.SBGods, DiscordServer.SBDGods};
    private static SBGods instance;
    public final String[] keys = {"bc90572a-1547-41a5-8f28-d7664916a28d", "3963906e-ffb6-45b9-b07b-80ca9838eb20"};
    public final Logger logger = Logger.getLogger(this.getClass().getName());
    //private ConfigValues configValues;
    private final ApiUtil apiutil;
    private final Util util;
    private final SBUtil sbUtil;
    private final LangUtil langUtil;
    private final JsonApiUtil jsonApiUtil;
    private final CacheUtil cacheUtil;
    private DiscordBot discordBot;
    private int keyIndex = 0;

    public SBGods() {
        this.apiutil = new ApiUtil(this);
        this.util = new Util(this);
        this.sbUtil = new SBUtil(this);
        this.langUtil = new LangUtil(this);
        this.jsonApiUtil = new JsonApiUtil(this);
        this.cacheUtil = new CacheUtil(this);
        //this.configValues = new ConfigValues(this);
        instance = this;
        try {
            this.discordBot = new DiscordBot(this);
        } catch (LoginException e) {
            logger.severe("Failed to login, is the discord token invalid?");
        }
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
	
	/*public ConfigValues getConfigValues() {
		return configValues;
	}*/

    public CacheUtil getCacheUtil() {
        return cacheUtil;
    }

    public DiscordBot getDiscord() {
        return discordBot;
    }

    public LeaderboardUpdater getLeaderboardUpdater() {
        return LeaderboardUpdater.getInstance();
    }

    public Logger getLogger() {
        return logger;
    }

    public DiscordServer[] getActiveServers() {
        return servers;
    }

    public String getCreatorId() {
        return CREATOR_ID;
    }
}
