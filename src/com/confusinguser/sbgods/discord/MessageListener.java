package com.confusinguser.sbgods.discord;

import com.confusinguser.sbgods.SBGods;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.concurrent.TimeUnit;

public class MessageListener extends ListenerAdapter {

    SBGods main;
    DiscordBot discord;

    public MessageListener(SBGods main, DiscordBot discord) {
        this.main = main;
        this.discord = discord;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent e) {
        if (discord.shouldNotRun(e)) {
            return;
        }
        if (e.getChannel().getName().equalsIgnoreCase("verify") &&
                e.getChannel().getHistoryFromBeginning(1).complete().getRetrievedHistory().get(0).getIdLong() != e.getMessage().getIdLong()) {
            main.getUtil().scheduleCommandAfter(() -> e.getMessage().delete().queue(), 30, TimeUnit.SECONDS);
        }
    }
}
