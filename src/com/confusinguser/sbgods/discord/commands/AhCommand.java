package com.confusinguser.sbgods.discord.commands;

import com.confusinguser.sbgods.SBGods;
import com.confusinguser.sbgods.discord.DiscordBot;
import com.confusinguser.sbgods.entities.AhItem;
import com.confusinguser.sbgods.entities.DiscordServer;
import com.confusinguser.sbgods.entities.Player;
import com.confusinguser.sbgods.entities.PlayerAH;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class AhCommand extends Command {

    public AhCommand(SBGods main, DiscordBot discord) {
        this.main = main;
        this.discord = discord;
        this.name = "ah";
        this.usage = this.name + " <IGN>";
        this.aliases = new String[]{};
    }

    @Override
    public void handleCommand(MessageReceivedEvent e, DiscordServer currentDiscordServer, String[] args) {
        if (args.length <= 1) {
            e.getChannel().sendMessage("Invalid usage! Usage: `" + this.usage + "`").queue();
            return;
        }

        String messageId = e.getChannel().sendMessage("Loading (" + main.getLangUtil().getProgressBar(0.0, 20) + ")").complete().getId();

        Player thePlayer = main.getApiUtil().getPlayerFromUsername(args[1]);
        e.getChannel().editMessageById(messageId, "Loading (" + main.getLangUtil().getProgressBar(0.5, 20) + ")").queue();
        PlayerAH playerAuctions = main.getApiUtil().getPlayerAHFromUsername(thePlayer);
        e.getChannel().editMessageById(messageId, "Loading (" + main.getLangUtil().getProgressBar(1.0, 20) + ")").queue();

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
            AhItem item = playerAuctions.getItems()[i];

            EmbedBuilder embedBuilder = new EmbedBuilder().setColor(item.getItemTierColor()).setTitle(item.getItemTier() + " | " + item.getItemName() + ":");

            embedBuilder.appendDescription("**" + item.getBids().toString() + "** bids\n");
            embedBuilder.appendDescription("Going for: **" + main.getLangUtil().addNotation(item.getHighestBid() == 0 ? item.getStartingBid() : item.getHighestBid()) + "** (Starting bid: " + main.getLangUtil().addNotation(item.getStartingBid()) + ")\n");
            embedBuilder.appendDescription("Category: **" + item.getCategory() + "**");

            e.getChannel().sendMessage(embedBuilder.build()).queue();
        }
    }
}