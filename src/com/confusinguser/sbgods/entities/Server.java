package com.confusinguser.sbgods.entities;

public enum Server {
	SBGods("602137436490956820", "673619910324387885"),
	Test("385431231975653377", null);

	String serverId;
	String channelId;


	private Server(String serverId, String channelId) {
		this.serverId = serverId;
		this.channelId = channelId;
	}

	public String getServerId() {
		return serverId;
	}


	public String getChannelId() {
		return channelId;
	}
}