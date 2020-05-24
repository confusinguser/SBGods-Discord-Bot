package com.confusinguser.sbgods.discord.commands;

import com.confusinguser.sbgods.SBGods;
import com.confusinguser.sbgods.discord.DiscordBot;
import com.confusinguser.sbgods.entities.DiscordServer;
import com.confusinguser.sbgods.entities.Player;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class VerifyCommand extends Command {

    public VerifyCommand(SBGods main, DiscordBot discord) {
        this.main = main;
        this.discord = discord;
        this.name = "verify";
        this.aliases = new String[]{"v"};
    }

    @Override
    public void handleCommand(MessageReceivedEvent e, DiscordServer currentDiscordServer, String[] args) {
        if (!e.getChannel().getName().toLowerCase().contains("verify") && !e.getChannel().getName().toLowerCase().contains("bot")) {
            e.getChannel().sendMessage("This command cannot be used in this channel").queue();
            return;
        }
        if (e.getMember() == null) return;

        if (args.length >= 2) {
            // Check if that is actual player ign
            Player player = main.getApiUtil().getPlayerFromUsername(args[1]);
            if (player.getDiscordTag().equalsIgnoreCase(e.getAuthor().getAsTag())) {
                int response = main.getUtil().verifyPlayer(e.getMember(), player.getDisplayName(), e.getGuild(), e.getChannel());
                if (response == 0) {
                    e.getChannel().sendMessage("You are already verified or you need to link your minecraft account with discord (see the welcome channel)").queue(); // TODO make this 2 different messages
                    return;
                } else if (response == 2) {
                    e.getChannel().sendMessage("Bot is still loading the leaderboards! Try again in a few minutes").queue();
                    return;
                } else {
                    main.logger.fine("Added " + currentDiscordServer.toString() + " verified role to " + e.getAuthor().getAsTag());
                    return;
                }
            }

            // Send error message saying to link discord account with mc
            e.getChannel().sendMessage(e.getAuthor().getAsMention() + " You need to link your minecraft account with discord (see the welcome channel) then run this command again.").queue();
            return;
        }

        String mcName = main.getApiUtil().getMcNameFromDisc(e.getAuthor().getAsTag());
        if (mcName.isEmpty()) {
            e.getChannel().sendMessage("There was an error auto-detecting your minecraft ign. Please do -verify <IGN>").queue();
            return;
        }

        // Verify player with mc name mcName
        main.getUtil().verifyPlayer(e.getMember(), mcName, e.getGuild(), e.getChannel());
    }
}
