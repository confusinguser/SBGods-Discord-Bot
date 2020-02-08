package sbgods;

import javax.security.auth.login.LoginException;

import sbgods.discord.DiscordBot;
import sbgods.utils.ApiUtil;
import sbgods.utils.Util;

public class SBGods {

	String[] guildMemberUuids;
	private ApiUtil apiutil;
	private Util util;
	private DiscordBot discordBot;

	public SBGods() {
		this.apiutil = new ApiUtil(this);
		this.util = new Util(this);
		try {
			this.discordBot = new DiscordBot(this);
		} catch (LoginException e) {
			e.printStackTrace();
		}
	}

	public void init() {
		getApiUtil();
	}

	public String getApikey() {
		return "1040878f-c8b0-4098-a89c-06b7619a77f4";
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

	public DiscordBot getDiscord() {
		return discordBot;
	}
}
