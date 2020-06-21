package com.confusinguser.sbgods.discord.commands;

import com.confusinguser.sbgods.SBGods;
import com.confusinguser.sbgods.discord.DiscordBot;
import com.confusinguser.sbgods.entities.DiscordServer;
import com.confusinguser.sbgods.entities.Player;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Objects;

public class GuildDiscListCommand extends Command {

    public void guildDiscListCommand(SBGods main, DiscordBot discord) {
        this.main = main;
        this.discord = discord;
        this.name = "-gdisclist";
        this.aliases = new String[]{};
    }

    @Override
    public void handleCommand(MessageReceivedEvent e, DiscordServer currentDiscordServer, String[] args) {
        if(!Objects.requireNonNull(e.getMember()).hasPermission(Permission.MANAGE_ROLES)){
            e.getChannel().sendMessage("You dont have permission to perform this command").queue();
            return;
        }

        String messageId = e.getChannel().sendMessage("Loading... (" + main.getLangUtil().getProgressBar(0.0, 30) + ")").complete().getId();

        for(Player player : main.getApiUtil().getGuildMembers(currentDiscordServer.getHypixelGuild())){

        }
    }

}
