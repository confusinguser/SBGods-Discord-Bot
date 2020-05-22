package com.confusinguser.sbgods.discord;

import com.confusinguser.sbgods.SBGods;
import com.confusinguser.sbgods.discord.commands.*;
import com.confusinguser.sbgods.entities.DiscordServer;
import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.security.auth.login.LoginException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public class DiscordBot {
    public final VerifyAllCommand verifyAllCommand;
    private final SBGods main;
    private final ArrayList<ListenerAdapter> commands;
    private final JDA jda;
    public String commandPrefix = "-";

    public DiscordBot(SBGods main) throws LoginException {
        String token = "NjY0OTAwNzM0NTk2NDE1NDg4.XreLHQ.l2viqcJ-uYvB3rAGcBa3OWhRuf0";

        this.main = main;
        SlayerCommand slayerCommand = new SlayerCommand(main, this);
        SkillCommand skillCommand = new SkillCommand(main, this);
        SkillExpCommand skillExpCommand = new SkillExpCommand(main, this);
        HelpCommand helpCommand = new HelpCommand(main, this);
        SbgodsCommand sbgodsCommand = new SbgodsCommand(main, this);
        WhatguildCommand whatguildCommand = new WhatguildCommand(main, this);
        PetsCommand petsCommand = new PetsCommand(main, this);
        KillsCommand killsCommand = new KillsCommand(main, this);
        DeathsCommand deathsCommand = new DeathsCommand(main, this);
        AhCommand ahCommand = new AhCommand(main, this);
        VerifyCommand verifyCommand = new VerifyCommand(main, this);
        verifyAllCommand = new VerifyAllCommand(main, this);
        TaxCommand taxCommand = new TaxCommand(main, this);
        PlayerCommand playerCommand = new PlayerCommand(main, this);
        CoopBankCommand coopBankCommand = new CoopBankCommand(main, this);

        MessageListener messageListener = new MessageListener(main, this);

        commands = new ArrayList<>(Arrays.asList(slayerCommand, skillCommand, skillExpCommand, helpCommand, sbgodsCommand, whatguildCommand, petsCommand, killsCommand, deathsCommand, ahCommand, verifyCommand, verifyAllCommand, taxCommand, playerCommand, coopBankCommand, messageListener));

        JDABuilder jdaBuilder = new JDABuilder(AccountType.BOT)
                .setToken(token)
                .setStatus(OnlineStatus.ONLINE)
                .setActivity(Activity.playing("Use " + helpCommand.getName() + " to get started"));

        for (ListenerAdapter listener : commands) {
            jdaBuilder.addEventListeners(listener);
        }
        jda = jdaBuilder.build();
        jda.getPresence().setActivity(Activity.playing("Use " + commandPrefix + "help to get started. \nMade by ConfusingUser#5712 & Soopyboo32#3042"));
        main.logger.info("Bot ready to take commands");
    }

    public boolean isValidCommand(String command) {
        for (Command validCommand : commands.stream().filter(listener -> listener instanceof Command).map(listener -> (Command) listener).collect(Collectors.toList())) {
            String validCommandString = validCommand.getName();
            if (command.equalsIgnoreCase(validCommandString)) {
                return true;
            }
        }
        return false;
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
}