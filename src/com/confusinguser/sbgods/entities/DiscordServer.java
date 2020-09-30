package com.confusinguser.sbgods.entities;

import net.dv8tion.jda.api.entities.Guild;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public enum DiscordServer {
    SBGods("602137436490956820", "673619910324387885", "745532614659801138", HypixelGuild.SBG), // 5cd01bdf77ce84cf1204cd61
    SBForceful("692302950126846013", "695722880754909214", "747021170397937754", HypixelGuild.SBF), // 5e4e6d0d8ea8c9feb3f0e44f
    Test("385431231975653377", "673143091342868481", "745632663456579664", null);

    private final String serverId;
    private final String botChannelId;
    private final String guildChatChannelId;
    private final HypixelGuild hypixelGuild;

    DiscordServer(String serverId, String botChannelId, String guildChatChannelId, HypixelGuild hypixelGuild) {
        this.serverId = serverId;
        this.botChannelId = botChannelId;
        this.guildChatChannelId = guildChatChannelId;
        this.hypixelGuild = hypixelGuild;
    }

    public static DiscordServer getDiscordServerFromDiscordGuild(Guild guild) {
        for (DiscordServer server : values()) {
            if (guild.getId().contentEquals(server.getServerId())) return server;
        }
        return null;
    }

    public static DiscordServer getDiscordServerFromHypixelGuild(HypixelGuild hypixelGuild) {
        return getDiscordServerFromHypixelGuild(hypixelGuild, false);
    }

    public static DiscordServer getDiscordServerFromHypixelGuild(HypixelGuild hypixelGuild, boolean activeOnly) {
        for (DiscordServer discordServer : values()) {
            if (discordServer.hypixelGuild == null && (!activeOnly || Arrays.asList(com.confusinguser.sbgods.SBGods.getInstance().getActiveServers()).contains(discordServer)))
                return discordServer;
            if (discordServer.getHypixelGuild() == hypixelGuild && (!activeOnly || Arrays.asList(com.confusinguser.sbgods.SBGods.getInstance().getActiveServers()).contains(discordServer)))
                return discordServer;
        }
        return null;
    }

    public String getServerId() {
        return serverId;
    }

    public String getBotChannelId() {
        return botChannelId;
    }

    public String getGuildChatChannelId() {
        return guildChatChannelId;
    }

    @NotNull
    public HypixelGuild getHypixelGuild() {
        if (hypixelGuild == null) {
            return SBGods.hypixelGuild;
        }
        return hypixelGuild;
    }
}