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
    public void onMessageReceived(MessageReceivedEvent e) {
        if (e.getAuthor().isBot() || !isTheCommand(e) || !discord.shouldRun(e)) {
            return;
        }

        DiscordServer currentDiscordServer = DiscordServer.getDiscordServerFromEvent(e);
        if (currentDiscordServer == null) {
            return;
        }

        if(!e.getChannel().getName().equalsIgnoreCase("verify")){
            e.getChannel().sendMessage("This command cant be used here.").queue();
            return;
        }

        main.logger.info(e.getAuthor().getName() + " ran command: " + e.getMessage().getContentRaw());
        e.getChannel().sendTyping().queue();

        String[] args = e.getMessage().getContentRaw().split(" ");

        if (args.length >= 2) {

            // Check if that is actual player ign
            Player player = main.getApiUtil().getPlayerFromUsername(args[1]);
            if (player.getDiscordTag().equalsIgnoreCase(e.getAuthor().getAsTag())) {

                String responseId = main.getUtil().verifyPlayer(e.getMember(), player.getDisplayName(), e.getGuild(), e.getChannel());
                main.logger.fine("Added " + currentDiscordServer.toString() + " verified role to " + e.getAuthor().getAsTag());

                try {
                    Thread.sleep(10000);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }

                e.getChannel().deleteMessageById(e.getMessageId());
                e.getChannel().deleteMessageById(responseId);
                return;
            }

            // Send error message saying to link discord account with mc
            e.getChannel().sendMessage(e.getAuthor().getAsMention() + " You need to link your minecraft account with discord (see the welcome channel) then run this command again.").queue();
            return;
        }

        String mcName = main.getApiUtil().getMcNameFromDisc(e.getAuthor().getAsTag().replace("#","*"));
        if (mcName.equals("")) {
            e.getChannel().sendMessage("There was a error auto-detecting your minecraft ign... please do -verify {ign}").queue();
            // Send error saying that auto-detect failed and they need to enter their username
            return;
        }

        // Verify player with mc name mcName
        String responseId = main.getUtil().verifyPlayer(e.getMember(), mcName, e.getGuild(), e.getChannel());
        try {
            Thread.sleep(10000);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }

        e.getChannel().deleteMessageById(e.getMessageId());
        e.getChannel().deleteMessageById(responseId);
    }
}
