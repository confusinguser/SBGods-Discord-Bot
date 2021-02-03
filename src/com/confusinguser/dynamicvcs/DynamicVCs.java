package com.confusinguser.dynamicvcs;

import com.confusinguser.dynamicvcs.entities.DiscordServer;

import javax.security.auth.login.LoginException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.*;

public class DynamicVCs { // Temporarily in here until i can get it to separate jar and work on soopy server pc
    /**
     * NOTE: (not) OVERRIDEN IN {@code getActiveServers()} IF NOT IN IDE
     */
    private static final DiscordServer[] servers = {DiscordServer.Test}; // For testing
    private static DynamicVCs instance;
    public final Logger logger = Logger.getLogger(this.getClass().getName());
    private final boolean inIDE;
    private DiscordBot discordBot;

    public DynamicVCs() {
        instance = this;
        inIDE = !DynamicVCs.class.getProtectionDomain().getCodeSource().getLocation().toString().endsWith(".jar");
        ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(new SimpleFormatter() {
            @Override
            public String format(LogRecord record) {
                return String.format("[%s] %s: %s%n", new SimpleDateFormat("MMM dd HH:mm:ss").format(new Date(record.getMillis())), record.getLevel(), record.getMessage());
            }
        });
        logger.addHandler(handler);
        logger.setUseParentHandlers(false);

        if (!inIDE) {
            handler.setLevel(Level.INFO); // Make handler only send info+ events
            logger.setLevel(Level.INFO); // Only show info+
        } else {
            handler.setLevel(Level.ALL); // Make handler send all events
            logger.setLevel(Level.ALL); // Show all logging events
        }

        try {
            this.discordBot = new DiscordBot(this);
        } catch (LoginException e) {
            logger.severe("Failed to login, is the discord token invalid?");
            System.exit(-1);
        }
    }

    public static DynamicVCs getInstance() {
        return instance;
    }

    public DiscordBot getDiscord() {
        return discordBot;
    }

    public Logger getLogger() {
        return logger;
    }

    public DiscordServer[] getActiveServers() {
//        if (!inIDE && !Arrays.asList(servers).contains(DiscordServer.SBGods))
//            return (servers = new DiscordServer[]{DiscordServer.SBGods});
        return servers;
    }
}
