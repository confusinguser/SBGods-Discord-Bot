package com.confusinguser.sbgods.discord.commands;

import com.confusinguser.sbgods.SBGods;
import com.confusinguser.sbgods.discord.DiscordBot;
import com.confusinguser.sbgods.entities.DiscordServer;
import com.confusinguser.sbgods.entities.Player;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;

public class VerifyCommand extends Command implements EventListener {

    public VerifyCommand(SBGods main, DiscordBot discord) {
        this.main = main;
        this.discord = discord;
        this.name = "verify";
        this.aliases = new String[]{"v"};
    }

    @Override
    public void handleCommand(MessageReceivedEvent e, DiscordServer currentDiscordServer, String[] args) {
        if (!e.getChannel().getName().equalsIgnoreCase("verify")) {
            e.getChannel().sendMessage("This command cant be used here.").queue();
            return;
        }

        if (args.length >= 2) {

            // Check if that is actual player ign
            Player player = main.getApiUtil().getPlayerFromUsername(args[1]);
            if (player.getDiscordTag().equalsIgnoreCase(e.getAuthor().getAsTag())) {
                main.logger.fine("Added " + currentDiscordServer.toString() + " verified role to " + e.getAuthor().getAsTag());
                return;
            }

            // Send error message saying to link discord account with mc
            e.getChannel().sendMessage(e.getAuthor().getAsMention() + " You need to link your minecraft account with discord (see the welcome channel) then run this command again.").queue();
            return;
        }

        String mcName = main.getApiUtil().getMcNameFromDisc(e.getAuthor().getAsTag());
        if (mcName.isEmpty()) {
            e.getChannel().sendMessage("There was an error auto-detecting your minecraft ign. Please do -verify <IGN>").queue();
            // Send error saying that auto-detect failed and they need to enter their username
            return;
        }

        // Verify player with mc name mcName
        main.getUtil().verifyPlayer(e.getMember(), mcName, e.getGuild(), e.getChannel());
    }
}
