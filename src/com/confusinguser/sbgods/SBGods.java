package com.confusinguser.sbgods;

import java.util.logging.Logger;

import javax.security.auth.login.LoginException;

import com.confusinguser.sbgods.discord.DiscordBot;
import com.confusinguser.sbgods.entities.DiscordServer;
import com.confusinguser.sbgods.utils.ApiUtil;
import com.confusinguser.sbgods.utils.ApplicationUtil;
import com.confusinguser.sbgods.utils.CacheUtil;
import com.confusinguser.sbgods.utils.JsonApiUtil;
import com.confusinguser.sbgods.utils.LangUtil;
import com.confusinguser.sbgods.utils.SBUtil;
import com.confusinguser.sbgods.utils.Util;

public class SBGods {
	private ApiUtil apiutil;
	private Util util;
	private SBUtil sbUtil;
	private LangUtil langUtil;
	private DiscordBot discordBot;
	private JsonApiUtil jsonApiUtil;
	private ApplicationUtil applicationUtil;
	private CacheUtil cacheUtil;

	private final String creatorId = "244786205873405952";
	private final DiscordServer[] servers = {DiscordServer.Test};
	private final String[] keys = {"369a1113-f148-4bc3-b8ee-ff5d258ed107", "3963906e-ffb6-45b9-b07b-80ca9838eb20", "71c64bb1-f7f1-46a6-8256-2aa832e99e78"};
	private int keyIndex = 0;
	
	
	public static final String VERSION = "0.7.4";
	public static final String VERSION_DESCRIPTION = "The color update! (also `-kills` & `-deaths` are a thing now)\nApplication update coming soon??";
	private static SBGods instance;
	private final Logger logger = Logger.getLogger(this.getClass().getName());
	
	public SBGods() {
		this.apiutil = new ApiUtil(this);
		this.util = new Util(this);
		this.sbUtil = new SBUtil(this);
		this.langUtil = new LangUtil(this);
		this.jsonApiUtil = new JsonApiUtil(this);
		this.applicationUtil = new ApplicationUtil(this);
		this.cacheUtil = new CacheUtil(this);
		instance = this;
		try {
			this.discordBot = new DiscordBot(this);
		} catch (LoginException e) {
			logInfo("Failed to login");
		}
	}

	public String getApikey() {
		keyIndex++;
		if (keyIndex >= keys.length) keyIndex = 0;
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

	public ApplicationUtil getApplicationUtil() {
		return applicationUtil;
	}

	public CacheUtil getCacheUtil() {
		return cacheUtil;
	}

	public DiscordBot getDiscord() {
		return discordBot;
	}

	public LeaderboardUpdater getLeaderboardUpdater() {
		return LeaderboardUpdater.instance;
	}

	public void logInfo(String message) {
		logger.info(message);
	}

	public DiscordServer[] getActiveServers() {
		return servers;
	}

	public String getCreatorId() {
		return creatorId;
	}
	
	public static SBGods getInstance() {
		return instance;
	}
}
