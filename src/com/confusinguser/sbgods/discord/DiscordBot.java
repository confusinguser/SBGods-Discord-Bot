package com.confusinguser.sbgods.discord;

import com.confusinguser.sbgods.SBGods;
import com.confusinguser.sbgods.discord.commands.*;
import com.confusinguser.sbgods.entities.DiscordServer;
import com.confusinguser.sbgods.utils.LangUtil;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import javax.security.auth.login.LoginException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DiscordBot {
    public final VerifyAllCommand verifyAllCommand;
    public final EventCommand eventCommand;
    private final SBGods main;
    private final JDA jda;
    public String commandPrefix = "-";

    public DiscordBot(SBGods main) throws LoginException {
        String token = "NjY0OTAwNzM0NTk2NDE1NDg4.XhdzEA.wNcSnHx9ds-FD8f4lT_vboA_P1k";

        this.main = main;
        HelpCommand helpCommand = new HelpCommand(main, this);
        SbgodsCommand sbgodsCommand = new SbgodsCommand(main, this);
        WhatguildCommand whatguildCommand = new WhatguildCommand(main, this);
        VerifyCommand verifyCommand = new VerifyCommand(main, this);
        verifyAllCommand = new VerifyAllCommand(main, this);
        eventCommand = new EventCommand(main, this);

        MessageListener messageListener = new MessageListener(main, this);
        ReactionListener reactionListener = new ReactionListener(main, this);

        List<ListenerAdapter> commands = new ArrayList<>(Arrays.asList(
                helpCommand,
                sbgodsCommand,
                whatguildCommand,
                verifyCommand,
                verifyAllCommand,
                eventCommand,
                messageListener,
                reactionListener
        ));

        List<GatewayIntent> intents = new ArrayList<>();
        intents.add(GatewayIntent.GUILD_MESSAGES);
        intents.add(GatewayIntent.GUILD_MESSAGE_TYPING);
        intents.add(GatewayIntent.GUILD_MEMBERS);
        intents.add(GatewayIntent.GUILD_EMOJIS);
        intents.add(GatewayIntent.GUILD_MESSAGE_REACTIONS);
        JDABuilder jdaBuilder = JDABuilder.create(token, intents)
                .disableCache(CacheFlag.ACTIVITY, CacheFlag.EMOTE, CacheFlag.CLIENT_STATUS, CacheFlag.VOICE_STATE)
                .setStatus(OnlineStatus.ONLINE)
                .setActivity(Activity.playing("The SBG Discord Bot" + "\nMade by ConfusingUser#5712 & Soopyboo32#3042"));

        for (ListenerAdapter listener : commands) {
            jdaBuilder.addEventListeners(listener);
        }
        jda = jdaBuilder.build();
        main.logger.info("Bot ready to take commands on " + Arrays.stream(main.getActiveServers()).map(DiscordServer::toString).collect(Collectors.joining(", ")));
    }

    public JDA getJDA() {
        return jda;
    }

    public boolean shouldNotRun(MessageReceivedEvent e) {
        if (main.getActiveServers() == null) return false;
        for (DiscordServer server : main.getActiveServers()) {
            if (e.getGuild().getId().contentEquals(server.getServerId())) return false;
        }
        return true;
    }

    public String escapeMarkdown(String text) {
        String unescaped = text.replaceAll("\\\\([*_`~\\\\])", "$1"); // unescape any "backslashed" character
        return unescaped.replaceAll("([*_`~\\\\])", "\\\\$1"); // escape *, _, `, ~, \
    }

    public void reportFail(Throwable throwable, String place) {
        try {
            String stackTraceView = LangUtil.generateStackTraceView(throwable);
            TextChannel textChannel = main.getDiscord().getJDA().getTextChannelById("713870866051498086");
            main.logger.severe("Exception in " + place.toLowerCase() + ": \n" + stackTraceView);
            if (textChannel != null) {
                for (String message : LangUtil.processMessageForDiscord("Exception in \"" + place + "\": \n" + stackTraceView, 2000)) {
                    textChannel.sendMessage(message).queue();
                }
            }
        } catch (Throwable t) {
            main.logger.severe("EXCEPTION IN FAIL REPORTER!!! THIS HAS TO BE FIXED ASAP!");
            t.printStackTrace();
        }
    }
}