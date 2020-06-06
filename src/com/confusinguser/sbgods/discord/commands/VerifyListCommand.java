package com.confusinguser.sbgods.discord.commands;

import com.confusinguser.sbgods.SBGods;
import com.confusinguser.sbgods.discord.DiscordBot;
import com.confusinguser.sbgods.entities.DiscordServer;
import com.confusinguser.sbgods.entities.Player;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;


public class VerifyListCommand extends Command {
    public VerifyListCommand(SBGods main, DiscordBot discord) {
        this.main = main;
        this.discord = discord;
        this.name = "verifylist";
        this.aliases = new String[]{};
    }

    @Override
    public void handleCommand(MessageReceivedEvent e, DiscordServer currentDiscordServer, String[] args) {
        if(e.getMember() !=null && !e.getMember().hasPermission(Permission.MANAGE_ROLES)) {

            e.getChannel().sendMessage("You don't have permissions to perform that command!").queue();
            return;

        }

        String discordMessage="";


        for(Member member : e.getGuild().getMembers()) {

            String tag=member.getUser().getAsTag();
            String mcName=main.getApiUtil().getMcNameFromDisc(tag);
            Player player=main.getApiUtil().getPlayerFromUsername(mcName);
            String mcGuild=main.getApiUtil().getGuildFromUUID(player.getUUID());
            discordMessage+=tag + " (IGN: " + mcName + ") [Guild: " + mcGuild + "]\n";
        }

        // Split the message every 2000 characters in a nice looking way because of discord limitations
        List<String> responseList = main.getUtil().processMessageForDiscord(discordMessage, 2000);
        for (String message : responseList) {

            e.getChannel().sendMessage(new EmbedBuilder().setDescription(message).build()).queue();
        }
    }
}