package com.confusinguser.sbgods.discord.commands;

import com.confusinguser.sbgods.SBGods;
import com.confusinguser.sbgods.discord.DiscordBot;
import com.confusinguser.sbgods.entities.AHItem;
import com.confusinguser.sbgods.entities.Player;
import com.confusinguser.sbgods.entities.PlayerAH;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.EventListener;

public class AHCommand extends Command implements EventListener {

    public AHCommand(SBGods main, DiscordBot discord) {
        this.main = main;
        this.discord = discord;
        this.name = "ah";
        this.usage = this.name + " <IGN>";
        this.aliases = new String[]{};
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent e) {
        if (e.getAuthor().isBot() || isNotTheCommand(e) || discord.shouldNotRun(e)) {
            return;
        }

        main.logger.info(e.getAuthor().getName() + " ran command: " + e.getMessage().getContentRaw());

        String[] args = e.getMessage().getContentRaw().split(" ");

        if (args.length <= 1) {
            e.getChannel().sendMessage("Invalid usage! Usage: `" + this.usage + "`").queue();
            return;
        }

        String messageId = e.getChannel().sendMessage("...").complete().getId();
        e.getChannel().sendTyping().queue();

        Player thePlayer = main.getApiUtil().getPlayerFromUsername(args[1]);
        PlayerAH playerAuctions = main.getApiUtil().getPlayerAHFromUsername(thePlayer);

        if (playerAuctions.isError()) {
            e.getChannel().deleteMessageById(messageId).queue();
            e.getChannel().sendMessage(playerAuctions.getError()).queue();
            return;
        }

        e.getChannel().deleteMessageById(messageId).queue();

        if (playerAuctions.getItems().length == 0) {
            e.getChannel().sendMessage(thePlayer.getDisplayName() + " has no active auctions!").queue();
        } else {
            e.getChannel().sendMessage(thePlayer.getDisplayName() + " (and coop)'s Auctions:").queue();
        }

        for (int i = 0; i < playerAuctions.getItems().length; i++) {

            e.getChannel().sendTyping().queue();
            AHItem item = playerAuctions.getItems()[i];

            EmbedBuilder embedBuilder = new EmbedBuilder().setColor(item.getItemTierColor()).setTitle(item.getItemTier() + " | " + item.getItemName() + ":");

            embedBuilder.appendDescription("**" + item.getBids().toString() + "** bids\n");
            embedBuilder.appendDescription("Going for: **" + main.getLangUtil().addNotation(item.getHighestBid() == 0 ? item.getStartingBid() : item.getHighestBid()) + "** (Starting bid: " + main.getLangUtil().addNotation(item.getStartingBid()) + ")\n");
            embedBuilder.appendDescription("Category: **" + item.getCategory() + "**");

            e.getChannel().sendMessage(embedBuilder.build()).queue();
        }
    }
}
