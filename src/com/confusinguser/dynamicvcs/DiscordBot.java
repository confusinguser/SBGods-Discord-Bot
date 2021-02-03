package com.confusinguser.dynamicvcs;

import com.confusinguser.dynamicvcs.entities.DiscordServer;
import com.confusinguser.dynamicvcs.listeners.VCListener;
import com.confusinguser.dynamicvcs.utils.LangUtil;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;

import javax.security.auth.login.LoginException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DiscordBot {
    private final List<ListenerAdapter> listeners;
    private final JDA jda;
    private final DynamicVCs main;
    public String commandPrefix = "-";

    public DiscordBot(DynamicVCs main) throws LoginException {
        String token = "ODA2NTg2MzY1MjY4NTkwNjYy.YBrmFw.2m1E3OmbCBploKMLNI7Z2-nEmN4";

        this.main = main;

        VCListener vcListener = new VCListener();
        listeners = new ArrayList<>(Arrays.asList(
                vcListener
        ));

        List<GatewayIntent> intents = new ArrayList<>();
        intents.add(GatewayIntent.GUILD_VOICE_STATES);
        JDABuilder jdaBuilder = JDABuilder.create(token, intents)
                .setStatus(OnlineStatus.ONLINE)
                .setActivity(Activity.playing("Dynamic Voice Channels! ConfusingUser"));

        for (ListenerAdapter listener : listeners) {
            jdaBuilder.addEventListeners(listener);
        }
        jda = jdaBuilder.build();
        jda.getPresence().setActivity(Activity.playing("Dynamic Voice Channels! ConfusingUser"));
        main.logger.info("Bot ready to take commands on " + Arrays.stream(main.getActiveServers()).map(DiscordServer::toString).collect(Collectors.joining(", ")));
    }

    public void reportFail(Throwable throwable, String place) {
        try {
            String stackTraceView = LangUtil.generateStackTraceView(throwable);
            TextChannel textChannel = main.getDiscord().jda.getTextChannelById("713870866051498086");
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