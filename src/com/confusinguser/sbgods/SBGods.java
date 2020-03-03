package com.confusinguser.sbgods;

import javax.security.auth.login.LoginException;

import com.confusinguser.sbgods.discord.DiscordBot;
import com.confusinguser.sbgods.updater.LeaderboardUpdater;
import com.confusinguser.sbgods.utils.ApiUtil;
import com.confusinguser.sbgods.utils.SBUtil;
import com.confusinguser.sbgods.utils.Util;

public class SBGods {

	String[] guildMemberUuids;
	private ApiUtil apiutil;
	private Util util;
	private DiscordBot discordBot;
	private SBUtil sbUtil;
	public static final String version = "0.7";
	public static final String versionDescription = "The version where `-pets` was added";

	public SBGods() {
		this.apiutil = new ApiUtil(this);
		this.util = new Util(this);
		this.sbUtil = new SBUtil(this);
		try {
			this.discordBot = new DiscordBot(this);
		} catch (LoginException e) {
			e.printStackTrace();
		}
	}

	public String getApikey() {
		return "93109329-3db6-462b-955b-9310b58d5c1e";
	}

	public String getGuildId() {
		return "5cd01bdf77ce84cf1204cd61";
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

	public DiscordBot getDiscord() {
		return discordBot;
	}

	public LeaderboardUpdater getLeaderboardUpdater() {
		return Start.updater;
	}

	public boolean isTestCopy() {
		return true;
	}

	public void logInfo(String message) {
		System.out.println(message);
	}
}
