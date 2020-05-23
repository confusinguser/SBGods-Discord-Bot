package com.confusinguser.sbgods.discord;

import com.confusinguser.sbgods.SBGods;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ReactionListener extends ListenerAdapter {

    SBGods main;
    DiscordBot discord;

    public ReactionListener(SBGods main, DiscordBot discord) {
        this.main = main;
        this.discord = discord;
    }

    @Override
    public void onMessageReactionAdd(MessageReactionAddEvent e) {
        if (!e.getChannel().getName().toLowerCase().contains("accepted-applications")) {
            return;
        }

        if (e.getChannel().getName().toLowerCase().contains("accepted-applications") &&
                e.getReaction().getReactionEmote().getEmoji().equalsIgnoreCase("☑")) {
            e.getTextChannel().deleteMessageById(e.getMessageId()).queue();
        }
    }
}