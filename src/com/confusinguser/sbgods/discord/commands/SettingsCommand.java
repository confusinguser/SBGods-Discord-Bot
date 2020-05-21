package com.confusinguser.sbgods.discord.commands;

import com.confusinguser.sbgods.SBGods;
import com.confusinguser.sbgods.discord.DiscordBot;
import com.confusinguser.sbgods.entities.DiscordServer;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Objects;

public class SettingsCommand extends Command {

    public SettingsCommand(SBGods main, DiscordBot discord) {
        this.main = main;
        this.discord = discord;
        this.name = "settings";
        this.aliases = new String[]{};
    }

    @Override
    public void handleCommand(MessageReceivedEvent e, DiscordServer currentDiscordServer, String[] args) {
        if (!Objects.requireNonNull(e.getGuild().getMember(e.getAuthor())).getPermissions().contains(Permission.MANAGE_SERVER) || !e.getAuthor().getId().contentEquals(main.getCreatorId())) {
            e.getChannel().sendMessage("You do not have permission to change the settings!").queue();
            return;
        }

        if (args.length <= 1) {
            e.getChannel().sendMessage("Invalid argument! Valid arguments: `prefix`").queue();
            return;
        }

        if (args[1].contentEquals("prefix")) {
            if (args.length <= 2) {
                e.getChannel().sendMessage("Invalid usage! Usage: " + this.getName() + " prefix <New Prefix>").queue();
                return;
            }

            discord.commandPrefix = args[2];
            main.getJsonApiUtil().updateSettings();
            e.getChannel().sendMessage("The prefix is now `" + args[2] + "`");
        }
    }
}