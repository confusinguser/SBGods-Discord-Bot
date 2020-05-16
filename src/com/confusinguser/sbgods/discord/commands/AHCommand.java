package com.confusinguser.sbgods.discord.commands;

import com.confusinguser.sbgods.SBGods;
import com.confusinguser.sbgods.discord.DiscordBot;
import com.confusinguser.sbgods.entities.AHItem;
import com.confusinguser.sbgods.entities.PlayerAH;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.EventListener;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class AHCommand extends Command implements EventListener {

    public AHCommand(SBGods main, DiscordBot discord) {
        this.main = main;
        this.discord = discord;
        this.name = "ah";
        this.usage = this.name + "  <IGN>";
        this.aliases = new String[] {};
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent e) {
        if (e.getAuthor().isBot() || !isTheCommand(e) || !discord.shouldRun(e)) {
            return;
        }

        main.logger.info(e.getAuthor().getName() + " ran command: " + e.getMessage().getContentRaw());

        String[] args = e.getMessage().getContentRaw().split(" ");

        if (args.length <= 1) {
            e.getChannel().sendMessage("Invalid usage! Usage: `" + this.usage + "`").queue();
            return;
        }

        String messageId = e.getChannel().sendMessage("Loading.").complete().getId();

        PlayerAH playerAuctions = main.getApiUtil().getPlayerAHFromUsername(args[1], messageId, e);

        if (playerAuctions.getIsError()) {
            e.getChannel().editMessageById(messageId, playerAuctions.getError()).queue();
            return;
        }

        e.getChannel().deleteMessageById(messageId).queue();

        //main.logger.info("Loaded player auctions");

        for (int i=0;i<playerAuctions.length;i++) {
            AHItem item = playerAuctions.getItems()[i];

            EmbedBuilder embedBuilder = new EmbedBuilder().setColor(0xb8300b).setTitle(item.getItemTier() + " | " + item.getItemName() + ":");

            embedBuilder.appendDescription("**" + item.getBids().toString() + "** bids\n");
            embedBuilder.appendDescription("Going for: **" + item.getHighestBid() + "** (Starting bid: " + item.getStartingBid() + ")\n");
            embedBuilder.appendDescription("Category: **" + item.getCategory() + "**\n\n");

            e.getChannel().sendMessage(embedBuilder.build()).queue();

            //e.getChannel().sendMessage(item.getItemTier() + " | " + item.getItemName()).queue();

            //main.logger.info("loop");
        }
        //main.logger.info("sending messages... (" + playerAuctions.length + ")");

        if(playerAuctions.length == 0){
            e.getChannel().sendMessage(args[1] + " has no active auctions!").queue();
        }

    }
}
