package com.confusinguser.sbgods.discord;

import com.confusinguser.sbgods.SBGods;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.concurrent.TimeUnit;

public class MessageListener extends ListenerAdapter {

    final SBGods main;
    final DiscordBot discord;

    public MessageListener(SBGods main, DiscordBot discord) {
        this.main = main;
        this.discord = discord;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent e) {
        if (discord.shouldNotRun(e)) {
            return;
        }
        if (e.getChannel().getName().contains("verif") &&
                e.getChannel().getHistoryFromBeginning(1).complete().getRetrievedHistory().get(0).getIdLong() != e.getMessage().getIdLong()) {
            e.getMessage().delete().queueAfter(30, TimeUnit.SECONDS);
        } else if (e.getMessage().getMentions(Message.MentionType.USER).stream().anyMatch((mention) -> discord.getJDA().getSelfUser().getId().equals(mention.getId()))) {
            e.getChannel().sendMessage("Yeah I'm here did someone say my name?");
        }
    }
}
