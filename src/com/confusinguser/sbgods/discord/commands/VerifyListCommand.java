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
        if (e.getMember() != null && !e.getMember().hasPermission(Permission.MANAGE_ROLES)) {

            e.getChannel().sendMessage("You don't have permissions to perform that command!").queue();
            return;

        }

        StringBuilder discordMessage = new StringBuilder();
        String messageId = e.getChannel().sendMessage("Loading (" + main.getLangUtil().getProgressBar(0.0, 20) + ")").complete().getId();

        List<Member> discordMembers = e.getGuild().getMembers();
        for (int i = 0; i < discordMembers.size(); i++) {
            Member member = discordMembers.get(i);
            if (i % 5 == 0) {
                e.getChannel().editMessageById(messageId, "Loading (" + main.getLangUtil().getProgressBar((double) i / e.getGuild().getMembers().size(), 20) + ")").queue();
            }
            String tag = member.getUser().getAsTag();
            String mcName = main.getApiUtil().getMcNameFromDisc(tag);
            if (mcName == null || mcName.equals("")) {
                discordMessage.append(tag).append("    (IGN: Not Verified)    [Guild: Unknown Guild]\n");
                continue;
            }
            Player player = main.getApiUtil().getPlayerFromUsername(mcName);

            String mcGuild = main.getApiUtil().getGuildFromUUID(player.getUUID());
            if (mcGuild == null) {
                discordMessage.append(tag).append("    (IGN: ").append(mcName).append(")    [Guild: Unknown Guild]\n");
                continue;
            }

            discordMessage.append(tag).append("    (IGN: ").append(mcName).append(")    [Guild: ").append(mcGuild.equals("") ? "No Guild" : mcGuild).append("]\n");
            i++;
        }
        e.getChannel().deleteMessageById(messageId).queue();

        // Split the message every 2000 characters in a nice looking way because of discord limitations
        List<String> responseList = main.getUtil().processMessageForDiscord(discordMessage.toString(), 2000);
        for (String message : responseList) {

            e.getChannel().sendMessage(new EmbedBuilder().setDescription(message).build()).queue();
        }
    }
}