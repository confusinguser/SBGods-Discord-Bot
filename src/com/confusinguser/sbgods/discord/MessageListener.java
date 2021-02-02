package com.confusinguser.sbgods.discord;

import com.confusinguser.sbgods.SBGods;
import com.confusinguser.sbgods.entities.DiscordServer;
import com.confusinguser.sbgods.utils.Util;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.AbstractMap;
import java.util.concurrent.TimeUnit;

public class MessageListener extends ListenerAdapter {

    final SBGods main;
    final DiscordBot discord;

    public MessageListener(SBGods main, DiscordBot discord) {
        this.main = main;
        this.discord = discord;
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent e) {
        Member member = e.getMember();
        DiscordServer discordServer = DiscordServer.getDiscordServerFromDiscordGuild(e.getGuild());
        if (discord.shouldNotRun(e) || discordServer == null || member == null) return;

        if (e.getChannel().getName().contains("verif") &&
                (!member.hasPermission(Permission.MANAGE_SERVER) || e.getAuthor().isBot())) {
            e.getMessage().delete().queueAfter(30, TimeUnit.SECONDS);

        } else if (e.getChannel().getId().equals(discordServer.getGuildChatChannelId()) && !e.getAuthor().isBot()) {
            e.getChannel().deleteMessageById(e.getMessageId()).queue();

            Character mcColorCode = Util.closestMCColorCode(member.getColor());
            if (mcColorCode == null) mcColorCode = '7';

            String description = "§9Discord > §" + mcColorCode + member.getEffectiveName() + "§f: " + Util.stripColorCodes(main.getDiscord().escapeMarkdown(e.getMessage().getContentRaw()));
            MessageEmbed embed = new EmbedBuilder()
                    .setColor(member.getColor() == null ? 0xaaaaaa : member.getColor().getRGB())
                    .setDescription(Util.stripColorCodes(description))
                    .build();
            e.getChannel().sendMessage(embed).queue();

            String guildId = discordServer.getHypixelGuild().getGuildId();
            if (discordServer != DiscordServer.SBGods && guildId.equals(DiscordServer.SBGods.getHypixelGuild().getGuildId())) guildId = null;
            try {
                main.getRemoteGuildChatManager().queue.put(new AbstractMap.SimpleEntry<>(guildId, description));
            } catch (InterruptedException interruptedException) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
