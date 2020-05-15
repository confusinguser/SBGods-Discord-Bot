package com.confusinguser.sbgods.discord.commands;

import com.confusinguser.sbgods.SBGods;
import com.confusinguser.sbgods.discord.DiscordBot;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class Command extends ListenerAdapter {
    SBGods main = SBGods.getInstance();
    DiscordBot discord = main.getDiscord();
    String name;
    String usage;
    String[] aliases;

    boolean isTheCommand(MessageReceivedEvent e) {
        if (e.getMessage().getContentRaw().toLowerCase().split(" ")[0].contentEquals(discord.commandPrefix + this.getName()))
            return true;
        for (String alias : aliases) {
            if (e.getMessage().getContentRaw().toLowerCase().split(" ")[0].contentEquals(discord.commandPrefix + alias))
                return true;
        }
        return false;
    }

    public String getName() {
        return name;
    }
}
