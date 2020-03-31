package com.confusinguser.sbgods.entities;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public enum DiscordServer {
	SBGods("602137436490956820", "673619910324387885", HypixelGuild.SBG), // 5cd01bdf77ce84cf1204cd61
	SBDGods("692302950126846013", "692307247178448967", HypixelGuild.SBDG), // 5e4e6d0d8ea8c9feb3f0e44f
	Test("385431231975653377", null, null);

	private String serverId;
	private String channelId;
	private HypixelGuild hypixelGuild;

	private DiscordServer(String serverId, String channelId, HypixelGuild hypixelGuild) {
		this.serverId = serverId;
		this.channelId = channelId;
		this.hypixelGuild = hypixelGuild;
	}

	public String getServerId() {
		return serverId;
	}

	public String getChannelId() {
		return channelId;
	}
	
	public HypixelGuild getHypixelGuild() {
		if (hypixelGuild == null) {
			return SBGods.hypixelGuild;
		}
		return hypixelGuild;
	}
	
	public static DiscordServer getDiscordServerFromEvent(MessageReceivedEvent e) {
		for (DiscordServer server : values()) {
			if (e.getGuild().getId().contentEquals(server.getServerId())) return server;
		}
		return null;
	}
}