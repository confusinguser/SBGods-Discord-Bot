package com.confusinguser.sbgods.discord.commands;

import com.confusinguser.sbgods.SBGods;
import com.confusinguser.sbgods.discord.DiscordBot;
import com.confusinguser.sbgods.entities.DiscordPerms;
import com.confusinguser.sbgods.entities.DiscordServer;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public abstract class Command extends ListenerAdapter {
    SBGods main = SBGods.getInstance();
    DiscordBot discord = main.getDiscord();
    String name;
    String usage;
    String[] aliases;
    DiscordPerms perm = DiscordPerms.DEFAULT;

    @Override
    public void onMessageReceived(MessageReceivedEvent e) {
        if (e.getAuthor().isBot() || isNotTheCommand(e) || discord.shouldNotRun(e)) {
            return;
        }

        main.logger.info(e.getAuthor().getName() + " on " + e.getGuild().getName() + " ran command: " + e.getMessage().getContentRaw());

        DiscordServer discordServer = DiscordServer.getDiscordServerFromDiscordGuild(e.getGuild());
        Member member = e.getMember();
        if (discordServer == null) return; // Only allowed servers may use commands
        if (member == null) return;

        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.execute(() -> {
            main.getUtil().setTyping(true, e.getChannel());
            try {
                handleCommand(e, discordServer, member, e.getMessage().getContentRaw().split(" "));
            } catch (Throwable t) {
                main.getDiscord().reportFail(t, "Command Handler");
            } finally {
                main.getUtil().setTyping(false, e.getChannel());
            }
        });
        executorService.shutdown();
    }

    boolean isNotTheCommand(MessageReceivedEvent e) {
        if (e.getMessage().getContentRaw().toLowerCase().split(" ")[0].contentEquals(discord.commandPrefix + this.getName()))
            return false;
        for (String alias : aliases) {
            if (e.getMessage().getContentRaw().toLowerCase().split(" ")[0].contentEquals(discord.commandPrefix + alias))
                return false;
        }
        return true;
    }

    public String getName() {
        return name;
    }

    public abstract void handleCommand(MessageReceivedEvent e, @NotNull DiscordServer currentDiscordServer, @NotNull Member senderMember, String[] args) throws Throwable;
}
