package com.confusinguser.sbgods;

import javax.security.auth.login.LoginException;

import com.confusinguser.sbgods.discord.DiscordBot;
import com.confusinguser.sbgods.utils.ApiUtil;
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

	private String creatorId = "244786205873405952";

	public static final String version = "0.7.3";
	public static final String versionDescription = "The color update! (also `-kills` & `-deaths` are a thing now)";

	public static SBGods instance;

	public SBGods() {
		this.apiutil = new ApiUtil(this);
		this.util = new Util(this);
		this.sbUtil = new SBUtil(this);
		this.langUtil = new LangUtil(this);
		this.jsonApiUtil = new JsonApiUtil(this);
		instance = this;
		try {
			this.discordBot = new DiscordBot(this);
		} catch (LoginException e) {
			e.printStackTrace();
		}
	}

	public String getApikey() {
		return "369a1113-f148-4bc3-b8ee-ff5d258ed107"; // ConfusingUser
		// return "3963906e-ffb6-45b9-b07b-80ca9838eb20"; // TheIllegalOrange
		// return "71c64bb1-f7f1-46a6-8256-2aa832e99e78"; // Cody
	}

	public String getGuildId() {
		return "5cd01bdf77ce84cf1204cd61"; // SBGods
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

	public DiscordBot getDiscord() {
		return discordBot;
	}

	public LeaderboardUpdater getLeaderboardUpdater() {
		return Start.updater;
	}

	public void logInfo(String message) {
		System.out.println(message);
	}

	public String getActiveServer() {
		return "602137436490956820"; // SBGods
		// return "385431231975653377"; // Test server
		// return null // All servers, DO NOT USE
	}

	public String getActiveChannel() {
		return "673619910324387885"; // SBGods #bot-and-leaderboard channel
		// return null; // All channels
	}

	public String getCreatorId() {
		return creatorId;
	}
}
