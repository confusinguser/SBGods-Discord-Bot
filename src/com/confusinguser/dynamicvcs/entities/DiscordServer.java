package com.confusinguser.dynamicvcs.entities;

public enum DiscordServer {
    SBGods("602137436490956820"),
    Test("385431231975653377");

    private final String serverId;

    DiscordServer(String serverId) {
        this.serverId = serverId;
    }

    public String getServerId() {
        return serverId;
    }
}