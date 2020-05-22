package com.confusinguser.sbgods.discord.commands;

import com.confusinguser.sbgods.SBGods;
import com.confusinguser.sbgods.discord.DiscordBot;
import com.confusinguser.sbgods.entities.DiscordServer;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public abstract class Command extends ListenerAdapter {
    SBGods main = SBGods.getInstance();
    DiscordBot discord = main.getDiscord();
    String name;
    String usage;
    String[] aliases;

    @Override
    public void onMessageReceived(MessageReceivedEvent e) {
        if (e.getAuthor().isBot() || isNotTheCommand(e) || discord.shouldNotRun(e)) {
            return;
        }

        main.logger.info(e.getAuthor().getName() + " ran command: " + e.getMessage().getContentRaw());

        DiscordServer discordServer = DiscordServer.getDiscordServerFromEvent(e);
        if (discordServer == null) {
            return;
        } // Only allowed servers may use commands

        main.getUtil().setTyping(true, e.getChannel());
        try {
            ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
            executorService.execute(() ->
                    handleCommand(e, discordServer, e.getMessage().getContentRaw().split(" ")));
            executorService.shutdown();
        } catch (Throwable t) {
            main.logger.severe("Exception when handling command '" + e.getMessage().getContentRaw() + "': \n" + main.getLangUtil().beautifyStackTrace(t.getStackTrace(), t));
        }
        main.getUtil().setTyping(false, e.getChannel());
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

    public abstract void handleCommand(MessageReceivedEvent e, DiscordServer currentDiscordServer, String[] args);
}
