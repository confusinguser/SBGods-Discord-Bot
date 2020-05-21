package com.confusinguser.sbgods.entities;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public enum DiscordServer {
    SBGods("602137436490956820", "673619910324387885", HypixelGuild.SBG), // 5cd01bdf77ce84cf1204cd61
    SBDGods("692302950126846013", "692307247178448967", HypixelGuild.SBDG), // 5e4e6d0d8ea8c9feb3f0e44f
    Test("385431231975653377", "673143091342868481", null);

    private final String serverId;
    private final String botChannelId;
    private final HypixelGuild hypixelGuild;

    DiscordServer(String serverId, String botChannelId, HypixelGuild hypixelGuild) {
        this.serverId = serverId;
        this.botChannelId = botChannelId;
        this.hypixelGuild = hypixelGuild;
    }

    public static DiscordServer getDiscordServerFromEvent(MessageReceivedEvent e) {
        for (DiscordServer server : values()) {
            if (e.getGuild().getId().contentEquals(server.getServerId())) return server;
        }
        return null;
    }

    public static DiscordServer getDiscordServerFromDiscordGuild(Guild guild) {
        for (DiscordServer server : values()) {
            if (guild.getId().contentEquals(server.getServerId())) return server;
        }
        return null;
    }

    public String getServerId() {
        return serverId;
    }

    public String getBotChannelId() {
        return botChannelId;
    }

    public HypixelGuild getHypixelGuild() {
        if (hypixelGuild == null) {
            return SBGods.hypixelGuild;
        }
        return hypixelGuild;
    }
}